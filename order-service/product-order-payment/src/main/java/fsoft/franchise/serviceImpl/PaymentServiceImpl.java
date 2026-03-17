package fsoft.franchise.serviceImpl;

import fsoft.franchise.common.exception.ApiException;
import fsoft.franchise.dto.payments.*;
import fsoft.franchise.exception.CommonErrorCode;
import fsoft.franchise.exception.OrderErrorCode;
import fsoft.franchise.exception.PaymentErrorCode;
import fsoft.franchise.entity.OrderEntity;
import fsoft.franchise.entity.PaymentEntity;
import fsoft.franchise.entity.TransactionEntity;
import fsoft.franchise.enums.*;
import fsoft.franchise.repository.OrderRepository;
import fsoft.franchise.repository.PaymentRepository;
import fsoft.franchise.repository.TransactionRepository;
import fsoft.franchise.service.MoMoPaymentService;
import fsoft.franchise.service.PaymentService;
import fsoft.franchise.service.PaymentMethodService;
import fsoft.franchise.service.VNPayService;
import fsoft.franchise.service.OrderTrackingService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

        private static final int MAX_PAGE_SIZE = 50;

        private final PaymentRepository paymentRepository;
        private final TransactionRepository transactionRepository;
        private final VNPayService vnPayService;
        private final MoMoPaymentService moMoPaymentService;
        private final OrderRepository orderRepository;
        // Push real-time tracking khi VNPay callback thanh toán thành công
        private final OrderTrackingService orderTrackingService;
        private final PaymentMethodService paymentMethodService;

        public PaymentListResponse getPayments(PaymentFilterRequest filter, UUID currentUserId, String role) {

                // 1. Clamp page size, 1-based page for API
                int size = Math.min(Math.max(1, filter.getSize()), MAX_PAGE_SIZE);
                int pageIndex = filter.getPage() < 1 ? 0 : filter.getPage() - 1;
                Pageable pageable = PageRequest.of(
                                pageIndex, size,
                                Sort.by(Sort.Direction.DESC, "createdAt"));

                // 2. Resolve date range
                Instant fromDate = filter.getFromDate();
                Instant toDate = filter.getToDate();
                if (fromDate != null && toDate == null) {
                        toDate = Instant.now();
                }
                if (toDate != null && fromDate == null) {
                        fromDate = Instant.now().minus(30, ChronoUnit.DAYS);
                }

                // 3. Parse optional UUID filters
                UUID storeIdFilter = parseUuid(filter.getStoreId());
                UUID orderIdFilter = parseUuid(filter.getOrderId());
                UUID customerIdFilter = parseUuid(filter.getCustomerId());

                // 4. Parse optional status filter (PaymentStatus enum)
                PaymentStatus statusFilter = parsePaymentStatus(filter.getStatus());

                // 5. Branch logic by role (ADMIN / MANAGER use admin filters)
                Page<PaymentEntity> page;
                BigDecimal totalAmount;
                boolean isAdmin = "ADMIN".equalsIgnoreCase(role) || "MANAGER".equalsIgnoreCase(role);

                LocalDateTime fromLocalDate = fromDate != null
                                ? LocalDateTime.ofInstant(fromDate, ZoneId.systemDefault())
                                : null;
                LocalDateTime toLocalDate = toDate != null ? LocalDateTime.ofInstant(toDate, ZoneId.systemDefault())
                                : null;

                if (isAdmin) {
                        page = paymentRepository.findByAdminFilters(
                                        storeIdFilter,
                                        orderIdFilter, customerIdFilter,
                                        statusFilter, fromLocalDate, toLocalDate, pageable);
                        totalAmount = paymentRepository.sumAmountByAdminFilters(
                                        storeIdFilter,
                                        orderIdFilter, customerIdFilter,
                                        statusFilter, fromLocalDate, toLocalDate);
                } else {
                        page = paymentRepository.findByCustomerFilters(
                                        storeIdFilter,
                                        currentUserId, orderIdFilter, statusFilter,
                                        fromLocalDate, toLocalDate, pageable);
                        totalAmount = paymentRepository.sumAmountByCustomerFilters(
                                        storeIdFilter,
                                        currentUserId, orderIdFilter, statusFilter,
                                        fromLocalDate, toLocalDate);
                }

                // 6. Map entity to DTO — resolve transaction details from latest transaction
                List<PaymentListResponse.PaymentRecord> records = page.getContent().stream()
                                .map(p -> buildPaymentRecord(p, isAdmin))
                                .collect(Collectors.toList());

                return PaymentListResponse.builder()
                                .data(records)
                                .totalAmount(totalAmount != null ? totalAmount : BigDecimal.ZERO)
                                .pagination(PaymentListResponse.PaginationInfo.builder()
                                                .currentPage(page.getNumber() + 1)
                                                .totalPages(page.getTotalPages())
                                                .totalElements(page.getTotalElements())
                                                .pageSize(page.getSize())
                                                .build())
                                .build();
        }

        // ── Thanh toán đơn hàng (moved from OrderServiceImpl) ─────────────────────

        @Override
        @Transactional
        public PaymentResponse processPayment(UUID orderId, PaymentRequest request, UUID customerId, String ipAddress) {
                // 1. Tìm order
                OrderEntity order = orderRepository.findById(orderId)
                                .orElseThrow(() -> new ApiException(OrderErrorCode.ORDER_NOT_FOUND));
                // 2. Validate owner
                if (!order.getCustomerId().equals(customerId)) {
                        throw new ApiException(OrderErrorCode.ORDER_NOT_OWNED);
                }

                // 3. Kiểm tra trạng thái đơn hàng
                if (order.getStatus() == OrderStatus.PAID) {
                        throw new ApiException(OrderErrorCode.ORDER_ALREADY_PAID);
                }
                if (order.getStatus() != OrderStatus.PENDING && order.getStatus() != OrderStatus.PROCESSING) {
                        throw new ApiException(OrderErrorCode.INVALID_ORDER_STATUS);
                }

                // 4. Tính tổng tiền đơn hàng
                BigDecimal orderTotal = order.getOrderItems().stream()
                                .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                // 5. Validate số tiền thanh toán
                if (request.amount().compareTo(orderTotal) != 0) {
                        throw new ApiException(PaymentErrorCode.PAYMENT_AMOUNT_MISMATCH);
                }

                // 6. Idempotency: nếu đã có payment PENDING cho order này → tái sử dụng
                PaymentEntity payment = paymentRepository
                                .findFirstByOrder_IdAndStatus(orderId, PaymentStatus.PENDING)
                                .orElse(null);

                // Bug #6: Nếu user đổi sang payment method khác → FAIL payment cũ, tạo mới
                if (payment != null && payment.getPaymentMethod() != request.paymentMethod()) {
                        payment.setStatus(PaymentStatus.FAILED);
                        payment.setErrorMessage("Replaced by new payment method: " + request.paymentMethod());
                        paymentRepository.save(payment);
                        payment = null;
                }

                // Bug #6 (same-method): nếu đã có PENDING payment cùng method VÀ đã có URL
                // → trả URL cũ, KHÔNG tạo URL mới (tránh nhiều URL hợp lệ trên gateway)
                if (payment != null && payment.getPaymentUrl() != null
                                && !payment.getPaymentUrl().isBlank()) {
                        return new PaymentResponse(
                                        payment.getId(),
                                        order.getId(),
                                        payment.getPaymentMethod().toString(),
                                        payment.getAmountPaid(),
                                        payment.getStatus().toString(),
                                        order.getStatus(),
                                        payment.getPaymentUrl(),
                                        payment.getExpiredAt(),
                                        LocalDateTime.now());
                }

                if (payment == null) {
                        payment = new PaymentEntity();
                        payment.setOrder(order);
                        payment.setPaymentMethod(request.paymentMethod());
                        payment.setAmountPaid(request.amount());
                        payment.setStatus(PaymentStatus.PENDING);
                        payment.setTransactionId(UUID.randomUUID().toString());
                        paymentRepository.save(payment);
                }

                // Bug #1 fix: truyền amount gốc (VNĐ). VNPayServiceImpl tự ×100 nội bộ.
                // MoMo nhận VNĐ nguyên gốc (không cần ×100).
                long amountRaw = request.amount().longValue();

                // 7a. VNPAY → generate VNPay payment URL
                if (request.paymentMethod() == PaymentMethod.VNPAY) {
                        String payUrl = vnPayService.createPaymentUrl(
                                        orderId.toString(),
                                        amountRaw,
                                        payment.getTransactionId(),
                                        ipAddress != null ? ipAddress : "127.0.0.1");

                        order.setStatus(OrderStatus.PROCESSING);
                        payment.setPaymentUrl(payUrl);
                        orderRepository.save(order);
                        paymentRepository.save(payment);

                        return new PaymentResponse(
                                        payment.getId(),
                                        order.getId(),
                                        payment.getPaymentMethod().toString(),
                                        payment.getAmountPaid(),
                                        payment.getStatus().toString(),
                                        order.getStatus(),
                                        payUrl,
                                        LocalDateTime.now().plusMinutes(15),
                                        LocalDateTime.now());
                }

                // 7b. MOMO → validate request type rồi gọi MoMo API
                if (request.paymentMethod() == PaymentMethod.MOMO) {
                        MomoRequestType requestType = request.resolvedMomoRequestType();
                        if (!paymentMethodService.isMomoRequestTypeEnabled(requestType.getMomoCode())) {
                                throw new ApiException(PaymentErrorCode.INVALID_PAYMENT_METHOD,
                                                "MoMo request type '" + requestType.getMomoCode()
                                                                + "' is not enabled in the current environment");
                        }
                        String payUrl = moMoPaymentService.createPaymentLink(
                                        orderId,
                                        amountRaw,
                                        "Thanh toan don hang #" + orderId,
                                        requestType.getMomoCode());

                        order.setStatus(OrderStatus.PROCESSING);
                        payment.setPaymentUrl(payUrl);
                        orderRepository.save(order);
                        paymentRepository.save(payment);

                        return new PaymentResponse(
                                        payment.getId(),
                                        order.getId(),
                                        payment.getPaymentMethod().toString(),
                                        payment.getAmountPaid(),
                                        payment.getStatus().toString(),
                                        order.getStatus(),
                                        payUrl,
                                        LocalDateTime.now().plusMinutes(15),
                                        LocalDateTime.now());
                }

                // 8. Các phương thức khác (CASH, WALLET...) → Paid ngay
                order.setStatus(OrderStatus.PAID);
                payment.setStatus(PaymentStatus.PAID);
                orderRepository.save(order);
                paymentRepository.save(payment);

                // Push real-time notification
                orderTrackingService.sendTrackingUpdate(orderId, OrderStatus.PENDING, OrderStatus.PAID, customerId);

                return new PaymentResponse(
                                payment.getId(),
                                order.getId(),
                                payment.getPaymentMethod().toString(),
                                payment.getAmountPaid(),
                                payment.getStatus().toString(),
                                order.getStatus(),
                                null,
                                null,
                                LocalDateTime.now());
        }

        // ── Method 1.5: Xác nhận thanh toán CASH ──────────────────────────────────

//        @Override
//        @Transactional
//        public PaymentResponse confirmCashPayment(UUID paymentId, UUID staffId) {
//
//                PaymentEntity payment = paymentRepository.findById(paymentId)
//                                .orElseThrow(() -> new ApiException(PaymentErrorCode.PAYMENT_NOT_FOUND,
//                                                "Payment not found"));
//
//                if (payment.getPaymentMethod() != PaymentMethod.CASH) {
//                        throw new ApiException(PaymentErrorCode.INVALID_PAYMENT_METHOD,
//                                        "Can only confirm CASH payments");
//                }
//
//                if (payment.getStatus() == PaymentStatus.PAID) {
//                        throw new ApiException(PaymentErrorCode.PAYMENT_ALREADY_PAID,
//                                        "Payment is already marked as PAID");
//                }
//
//                payment.setStatus(PaymentStatus.PAID);
//                payment.setPaymentDate(LocalDateTime.now());
//                paymentRepository.save(payment);
//
//                if (payment.getOrder() != null) {
//                        OrderEntity order = payment.getOrder();
//                        if (order.getStatus() != OrderStatus.PAID
//                                        && order.getStatus() != OrderStatus.CANCELED
//                                        && order.getStatus() != OrderStatus.REFUNDED) {
//                                OrderStatus previousStatus = order.getStatus();
//                                order.setStatus(OrderStatus.PAID);
//                                orderRepository.save(order);
//                                orderTrackingService.sendTrackingUpdate(order.getId(), previousStatus,
//                                                OrderStatus.PAID, null);
//                        }
//                }
//
//                // Create Transaction record for cash
//                TransactionEntity txn = TransactionEntity.builder()
//                                .payment(payment)
//                                .vnpTxnRef("CASH-" + paymentId.toString().substring(0, 8))
//                                .vnpTransactionNo("CASH-" + Instant.now().toEpochMilli())
//                                .amount(payment.getAmountPaid())
//                                .type(TransactionType.PAYMENT)
//                                .build();
//                transactionRepository.save(txn);
//
//                return new PaymentResponse(
//                                payment.getId(),
//                                payment.getOrder() != null ? payment.getOrder().getId() : null,
//                                payment.getPaymentMethod().toString(),
//                                payment.getAmountPaid(),
//                                payment.getStatus().toString(),
//                                payment.getOrder() != null ? payment.getOrder().getStatus() : null,
//                                payment.getPaymentUrl(),
//                                payment.getExpiredAt(),
//                                payment.getPaymentDate());
//        }


        @Override
        @Transactional
        public PaymentResponse createInboundPayment(CreateInboundPaymentRequest request, String ipAddress) {
                String inboundTxnRef = request.orderId();

                // 1) Logic check existing
                if (inboundTxnRef != null) {
                        Optional<PaymentEntity> existing = paymentRepository.findByTransactionId(inboundTxnRef);
                        if (existing.isPresent()) {
                                PaymentEntity p = existing.get();
                                return new PaymentResponse(p.getId(), UUID.fromString(p.getTransactionId()),
                                                p.getPaymentMethod().toString(), p.getAmountPaid(), p.getStatus().toString(),
                                                null, p.getPaymentUrl(), p.getExpiredAt(), p.getPaymentDate());
                        }
                }

                // 2) Generate payment URL based on method
                String paymentUrl = null;
                if (request.paymentMethod() == PaymentMethod.MOMO) {
                        paymentUrl = moMoPaymentService.createPaymentLink(
                                        UUID.fromString(request.orderId()),
                                        request.amount().longValue(),
                                        "Payment for order " + request.orderId(),
                                        request.resolvedMomoRequestType());
                } else if (request.paymentMethod() == PaymentMethod.VNPAY) {
                        // VNPay
                        paymentUrl = vnPayService.createPaymentUrl(
                                        request.orderId(),
                                        request.amount().longValue(),
                                        inboundTxnRef,
                                        ipAddress);
                }
                // If CASH, paymentUrl remains null

                // 3) Persist payment
                PaymentEntity payment = PaymentEntity.builder()
                                .order(null)
                                .transactionId(inboundTxnRef)
                                .paymentMethod(request.paymentMethod())
                                .paymentType(PaymentType.INBOUND)
                                .amountPaid(request.amount())
                                .status(PaymentStatus.PENDING)
                                .paymentUrl(paymentUrl)
                                .expiredAt(LocalDateTime.now().plus(15, ChronoUnit.MINUTES))
                                .paymentDate(LocalDateTime.now())
                                .build();
                paymentRepository.save(payment);

                return new PaymentResponse(
                                payment.getId(),
                                UUID.fromString(payment.getTransactionId()), // orderId cua INBOUND
                                payment.getPaymentMethod().toString(),
                                payment.getAmountPaid(),
                                payment.getStatus().toString(),
                                null, // no orderStatus for INBOUND
                                paymentUrl,
                                payment.getExpiredAt(),
                                payment.getPaymentDate());
        }

        // ── Method 2: Xử lý webhook từ VNPay ─────────────────────────────────────

        @Override
        @Transactional
        public WebHookResponse processWebhook(Map<String, String> vnpayParams) {

                String vnpTxnRef = vnpayParams.get("vnp_TxnRef");
                String responseCode = vnpayParams.get("vnp_ResponseCode");
                String vnpTxnNo = vnpayParams.get("vnp_TransactionNo");
                String bankCode = vnpayParams.get("vnp_BankCode");
                String amountStr = vnpayParams.get("vnp_Amount"); // VNPay gửi * 100

                boolean isSuccess = "00".equals(responseCode);

                // 1. Idempotent: nếu transaction với vnpTxnRef đã tồn tại thì không xử lý lại
                Optional<TransactionEntity> existingTxnOpt = transactionRepository.findByVnpTxnRef(vnpTxnRef);
                if (existingTxnOpt.isPresent()) {
                        TransactionEntity existingTxn = existingTxnOpt.get();
                        PaymentEntity existingPayment = existingTxn.getPayment();
                        return WebHookResponse.builder()
                                        .paymentId(existingPayment.getId())
                                        .transactionId(existingTxn.getVnpTxnRef())
                                        .orderId(existingPayment.getOrder() != null ? existingPayment.getOrder().getId()
                                                        : null)
                                        .status(existingPayment.getStatus().toString())
                                        .processedAt(LocalDateTime.now())
                                        .build();
                }

                // 2. Resolve payment by merchant transaction reference with pessimistic lock
                // Bug #4: @Lock(PESSIMISTIC_WRITE) prevents concurrent IPN from double-processing
                PaymentEntity payment = paymentRepository.findByTransactionIdForUpdate(vnpTxnRef)
                                .orElseThrow(() -> new ApiException(PaymentErrorCode.PAYMENT_NOT_FOUND,
                                                "Payment not found for transaction ref: " + vnpTxnRef));

                // 3. Store gateway response on payment regardless of outcome (preserves failure
                // codes)
                payment.setVnpResponseCode(responseCode);
                payment.setVnpBankCode(bankCode);

                // Bug #6 fix: Guard — nếu payment đã FAILED (bị cancel do user đổi method)
                // hoặc đã PAID (IPN retry) → không xử lý thêm
                if (payment.getStatus() == PaymentStatus.FAILED) {
                        log.warn("VNPay IPN for FAILED payment txnRef={} — payment was cancelled. "
                                        + "Gateway still charged. Manual refund required.", vnpTxnRef);
                        return WebHookResponse.builder()
                                        .paymentId(payment.getId())
                                        .transactionId(vnpTxnRef)
                                        .orderId(payment.getOrder() != null ? payment.getOrder().getId() : null)
                                        .status("CANCELLED")
                                        .processedAt(LocalDateTime.now())
                                        .build();
                }
                if (payment.getStatus() == PaymentStatus.PAID) {
                        log.info("VNPay IPN duplicate for already-PAID payment txnRef={}", vnpTxnRef);
                        return WebHookResponse.builder()
                                        .paymentId(payment.getId())
                                        .transactionId(vnpTxnRef)
                                        .orderId(payment.getOrder() != null ? payment.getOrder().getId() : null)
                                        .status("ALREADY_PAID")
                                        .processedAt(LocalDateTime.now())
                                        .build();
                }

                // 4. Cập nhật PaymentEntity + Order tuỳ theo responseCode
                if (isSuccess) {
                        // Bug #2 fix: Validate amount khớp → chống hacker trả ít hơn
                        if (amountStr != null) {
                                BigDecimal gatewayAmount = new BigDecimal(amountStr).divide(BigDecimal.valueOf(100));
                                if (gatewayAmount.compareTo(payment.getAmountPaid()) != 0) {
                                        log.error("VNPAY AMOUNT MISMATCH: gateway={} vs db={} for txnRef={}",
                                                        gatewayAmount, payment.getAmountPaid(), vnpTxnRef);
                                        payment.setStatus(PaymentStatus.FAILED);
                                        payment.setErrorMessage("Amount mismatch: expected " + payment.getAmountPaid()
                                                        + " but gateway returned " + gatewayAmount);
                                        paymentRepository.save(payment);
                                        return WebHookResponse.builder()
                                                        .paymentId(payment.getId())
                                                        .transactionId(vnpTxnRef)
                                                        .orderId(payment.getOrder() != null
                                                                        ? payment.getOrder().getId()
                                                                        : null)
                                                        .status("AMOUNT_MISMATCH")
                                                        .processedAt(LocalDateTime.now())
                                                        .build();
                                }
                        }

                        payment.setStatus(PaymentStatus.PAID);
                        // VNPay gửi amount * 100, convert về đơn vị gốc nếu cần
                        if (amountStr != null) {
                                payment.setAmountPaid(
                                                new BigDecimal(amountStr).divide(BigDecimal.valueOf(100)));
                        }
                        payment.setPaymentDate(LocalDateTime.now());
                        paymentRepository.save(payment);

                        // 5. Update Order status — guard: do NOT override CANCELED/REFUNDED (Bug #5:
                        // late IPN
                        // after cancel/refund)
                        if (payment.getOrder() != null) {
                                OrderEntity order = payment.getOrder();
                                if (order.getStatus() != OrderStatus.PAID
                                                && order.getStatus() != OrderStatus.CANCELED
                                                && order.getStatus() != OrderStatus.REFUNDED) {
                                        OrderStatus previousStatus = order.getStatus();
                                        order.setStatus(OrderStatus.PAID);
                                        orderRepository.save(order);
                                        orderTrackingService.sendTrackingUpdate(order.getId(), previousStatus,
                                                        OrderStatus.PAID, null);
                                }
                        }

                        // 6. Create TransactionEntity only on success (immutable ledger entry)
                        TransactionEntity txn = TransactionEntity.builder()
                                        .payment(payment)
                                        .vnpTxnRef(vnpTxnRef)
                                        .vnpTransactionNo(vnpTxnNo)
                                        .amount(amountStr != null
                                                        ? new BigDecimal(amountStr).divide(BigDecimal.valueOf(100))
                                                        : null)
                                        .type(TransactionType.PAYMENT)
                                        .build();
                        transactionRepository.save(txn);

                        return WebHookResponse.builder()
                                        .paymentId(payment.getId())
                                        .transactionId(vnpTxnRef)
                                        .orderId(payment.getOrder() != null ? payment.getOrder().getId() : null)
                                        .status(payment.getStatus().toString())
                                        .processedAt(LocalDateTime.now())
                                        .build();
                } else {
                        // Thanh toán thất bại / bị hủy: cập nhật Payment, KHÔNG tạo Transaction
                        payment.setStatus(PaymentStatus.FAILED);
                        paymentRepository.save(payment);

                        // Nếu order đang PROCESSING thì đưa về PENDING để user có thể chọn PTTT khác
                        if (payment.getOrder() != null) {
                                OrderEntity order = payment.getOrder();
                                if (order.getStatus() == OrderStatus.PROCESSING) {
                                        order.setStatus(OrderStatus.PENDING);
                                        orderRepository.save(order);
                                }
                        }

                        return WebHookResponse.builder()
                                        .paymentId(payment.getId())
                                        .transactionId(null)
                                        .orderId(payment.getOrder() != null ? payment.getOrder().getId() : null)
                                        .status(payment.getStatus().toString())
                                        .processedAt(LocalDateTime.now())
                                        .build();
                }
        }

        private PaymentListResponse.PaymentRecord buildPaymentRecord(PaymentEntity p,
                        boolean isAdmin) {
                // Resolve latest SUCCESS transaction for money amounts
                TransactionEntity latestTxn = resolveLatestTransaction(p.getId());

                BigDecimal amountPaid = latestTxn != null ? latestTxn.getAmount() : p.getAmountPaid();

                return PaymentListResponse.PaymentRecord.builder()
                                .storeId(p.getOrder() != null ? p.getOrder().getStoreId() : null)
                                .orderId(p.getOrder() != null ? p.getOrder().getId().toString() : null)
                                .orderNumber(p.getOrder() != null ? p.getOrder().getOrderNumber() : null)
                                .customerName(resolveCustomerName(p))
                                .paymentMethod(p.getPaymentMethod().toString())
                                .amountPaid(amountPaid)
                                .paymentDate(p.getCreatedAt().atZone(ZoneId.systemDefault()).toInstant())
                                .status(p.getStatus().toString())
                                // Error info from gateway response code stored on payment (admin only)
                                .errorMessage(isAdmin && p.getVnpResponseCode() != null
                                                ? p.getVnpResponseCode()
                                                : null)
                                .build();
        }

        @Override
        public PaymentStatusResponse getPaymentStatus(UUID orderId, UUID currentUserId, String role) {
                List<PaymentEntity> payments = paymentRepository.findByOrder_IdOrderByPaymentDateDesc(orderId);
                if (payments == null || payments.isEmpty()) {
                        throw new ApiException(PaymentErrorCode.PAYMENT_NOT_FOUND, "Payment not found");
                }
                PaymentEntity latest = payments.get(0);
                boolean canViewAll = "ADMIN".equalsIgnoreCase(role)
                        || "MANAGER".equalsIgnoreCase(role)
                        || "POS".equalsIgnoreCase(role);
                if (!canViewAll) {
                        if (latest.getOrder() == null || !latest.getOrder().getCustomerId().equals(currentUserId)) {
                                throw new ApiException(PaymentErrorCode.PAYMENT_ACCESS_DENIED);
                        }
                }
                return mapToPaymentStatusResponse(latest);
        }

        private PaymentStatusResponse mapToPaymentStatusResponse(PaymentEntity p) {
                UUID orderId = p.getOrder() != null ? p.getOrder().getId() : null;
                // VNPay transaction details come from TransactionEntity
                TransactionEntity txn = resolveLatestTransaction(p.getId());
                // Gateway response codes now live on PaymentEntity; txn refs come from
                // TransactionEntity
                PaymentStatusResponse.TransactionInfo transaction = PaymentStatusResponse.TransactionInfo.builder()
                                .vnpTxnRef(txn != null ? txn.getVnpTxnRef() : null)
                                .vnpTransactionNo(txn != null ? txn.getVnpTransactionNo() : null)
                                .vnpResponseCode(p.getVnpResponseCode())
                                .vnpBankCode(p.getVnpBankCode())
                                .build();
                return PaymentStatusResponse.builder()
                                .orderId(orderId)
                                .paymentId(p.getId())
                                .paymentMethod(p.getPaymentMethod().toString())
                                .status(p.getStatus().toString())
                                .amountPaid(txn != null ? txn.getAmount() : null)
                                .transaction(transaction)
                                .build();
        }

        @Override
        public AdminTransactionListResponse getAdminTransactions(int page, int size,
                        Optional<String> status, Optional<String> paymentMethod,
                        Optional<LocalDate> fromDate, Optional<LocalDate> toDate) {
                if (page < 1 || size < 1 || size > MAX_PAGE_SIZE) {
                        throw new ApiException(CommonErrorCode.BAD_REQUEST, "Invalid pagination parameters");
                }
                if (fromDate.isPresent() && toDate.isPresent() && fromDate.get().isAfter(toDate.get())) {
                        throw new ApiException(CommonErrorCode.VALIDATION_FAILED, "Invalid date range");
                }
                PaymentStatus statusEnum = parsePaymentStatus(status.orElse(null));
                PaymentMethod paymentMethodEnum = parsePaymentMethod(paymentMethod.orElse(null));
                Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));

                LocalDateTime fromInstant = fromDate
                                .map(d -> d.atTime(LocalTime.MIN).atOffset(ZoneOffset.UTC).toLocalDateTime())
                                .orElse(null);
                LocalDateTime toInstant = toDate
                                .map(d -> d.atTime(LocalTime.MAX).atOffset(ZoneOffset.UTC).toLocalDateTime())
                                .orElse(null);
                Page<PaymentEntity> resultPage = paymentRepository.findByAdminTransactionFilters(
                                statusEnum, paymentMethodEnum, fromInstant, toInstant, pageable);
                List<AdminTransactionListResponse.TransactionItem> content = resultPage.getContent().stream()
                                .map(this::toAdminTransactionItem)
                                .collect(Collectors.toList());
                return AdminTransactionListResponse.builder()
                                .content(content)
                                .page(page)
                                .size(size)
                                .totalElements(resultPage.getTotalElements())
                                .totalPages(resultPage.getTotalPages())
                                .build();
        }

        private AdminTransactionListResponse.TransactionItem toAdminTransactionItem(PaymentEntity p) {
                UUID orderId = p.getOrder() != null ? p.getOrder().getId() : null;
                UUID customerId = p.getOrder() != null && p.getOrder().getCustomerId() != null
                                ? p.getOrder().getCustomerId()
                                : null;
                TransactionEntity txn = resolveLatestTransaction(p.getId());
                return AdminTransactionListResponse.TransactionItem.builder()
                                .transactionId(txn != null ? txn.getId() : null)
                                .orderId(orderId)
                                .customerId(customerId)
                                .paymentId(p.getId())
                                .paymentMethod(p.getPaymentMethod().toString())
                                .paymentStatus(p.getStatus().toString())
                                .amountPaid(txn != null ? txn.getAmount() : null)
                                .vnpTxnRef(txn != null ? txn.getVnpTxnRef() : null)
                                .vnpTransactionNo(txn != null ? txn.getVnpTransactionNo() : null)
                                .vnpResponseCode(p.getVnpResponseCode())
                                .vnpBankCode(p.getVnpBankCode())
                                .createdDate(p.getCreatedAt().atZone(ZoneId.systemDefault()).toInstant())
                                .build();
        }

        // ── Helpers ──────────────────────────────────────────────────────────────

        /**
         * Parse status string to PaymentStatus; null/blank → null; invalid → null
         * (filter ignored).
         */
        private PaymentStatus parsePaymentStatus(String value) {
                if (value == null || value.isBlank())
                        return null;
                try {
                        return PaymentStatus.valueOf(value.trim().toUpperCase());
                } catch (IllegalArgumentException e) {
                        return null;
                }
        }

        /**
         * Parse UUID string; null/blank → null; invalid → null.
         */
        private UUID parseUuid(String value) {
                if (value == null || value.isBlank())
                        return null;
                try {
                        return UUID.fromString(value.trim());
                } catch (IllegalArgumentException e) {
                        return null;
                }
        }

        /**
         * Parse payment method string to PaymentMethod; null/blank → null; invalid →
         * null.
         */
        private PaymentMethod parsePaymentMethod(String value) {
                if (value == null || value.isBlank())
                        return null;
                try {
                        return PaymentMethod.valueOf(value.trim().toUpperCase());
                } catch (IllegalArgumentException e) {
                        return null;
                }
        }

        /** Returns the most recent transaction for a payment (any status). */
        private TransactionEntity resolveLatestTransaction(UUID paymentId) {
                List<TransactionEntity> txns = transactionRepository.findByPayment_IdOrderByCreatedAtDesc(paymentId);
                return txns != null && !txns.isEmpty() ? txns.get(0) : null;
        }

        private String resolveCustomerName(PaymentEntity p) {
                if (p.getOrder() == null || p.getOrder().getCustomerId() == null)
                        return null;
                return "Customer"; // You can replace this with actual customer name logic if you have access to
                                   // customer profiles
        }
}
