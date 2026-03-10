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

        public PaymentListResponse getPayments(PaymentFilterRequest filter,
                        UUID currentUserId, String role) {

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
                UUID orderIdFilter = null;
                if (filter.getOrderId() != null && !filter.getOrderId().isBlank()) {
                        try {
                                orderIdFilter = UUID.fromString(filter.getOrderId().trim());
                        } catch (IllegalArgumentException ignored) {
                        }
                }

                // 4. Parse optional status filter (PaymentStatus enum)
                PaymentStatus statusFilter = parsePaymentStatus(filter.getStatus());

                // 5. Branch logic by role (FRANCHISE_ADMIN / STORE_MANAGER use admin filters)
                Page<PaymentEntity> page;
                boolean isAdmin = "FRANCHISE_ADMIN".equalsIgnoreCase(role) || "STORE_MANAGER".equalsIgnoreCase(role);
                if (isAdmin) {
                        page = paymentRepository.findByAdminFilters(
                                        orderIdFilter, filter.getCustomerId(), filter.getEmail(),
                                        statusFilter, fromDate, toDate, pageable);
                } else {
                        page = paymentRepository.findByCustomerFilters(
                                        currentUserId, orderIdFilter, statusFilter,
                                        fromDate, toDate, pageable);
                }

                // 6. Map entity to DTO — resolve transaction details from latest transaction
                List<PaymentListResponse.PaymentRecord> records = page.getContent().stream()
                                .map(p -> buildPaymentRecord(p, isAdmin))
                                .collect(Collectors.toList());

                return PaymentListResponse.builder()
                                .data(records)
                                .pagination(PaymentListResponse.PaginationInfo.builder()
                                                .currentPage(page.getNumber())
                                                .totalPages(page.getTotalPages())
                                                .totalElements(page.getTotalElements())
                                                .pageSize(page.getSize())
                                                .build())
                                .build();
        }

        @Override
        @Transactional
        public PaymentResponse createPayment(CreatePaymentRequest request, String ipAddress) {

                // 1. Validate order tồn tại
                OrderEntity order = orderRepository.findById(request.orderId())
                                .orElseThrow(() -> new ApiException(OrderErrorCode.ORDER_NOT_FOUND, "Order not found"));

                // 1.1. Nếu order chưa thanh toán, chuyển trạng thái sang PROCESSING để thể hiện
                // đang trong quá trình thanh toán online
                if (order.getStatus() != OrderStatus.PAID && order.getStatus() != OrderStatus.PROCESSING) {
                        order.setStatus(OrderStatus.PROCESSING);
                        orderRepository.save(order);
                }

                // 2. Check duplicate Pending payment for this order
                PaymentEntity response = paymentRepository
                                .findByOrder_IdAndStatus(request.orderId(), PaymentStatus.PENDING);
                if (response != null) {
                        return new PaymentResponse(
                                        response.getId(), // paymentId
                                        order.getId(), // orderId
                                        response.getPaymentMethod().toString(), // paymentMethod
                                        response.getAmountPaid(), // amountPaid (stored in vnd)
                                        PaymentStatus.PENDING.toString(), // paymentStatus
                                        OrderStatus.PROCESSING, // orderStatus
                                        response.getPaymentUrl(), // paymentUrl
                                        LocalDateTime.now().plusMinutes(15), // expiredAt
                                        LocalDateTime.now());
                }

                // 3. Generate payment URL based on payment method
                String paymentUrl;
                if (request.paymentMethod() == PaymentMethod.MOMO) {
                        paymentUrl = moMoPaymentService.createPaymentLink(
                                        order.getId(),
                                        request.amount().longValue(),
                                        "Payment for order " + order.getId(),
                                        request.resolvedMomoRequestType());
                } else {
                        // Default: VNPay
                        paymentUrl = vnPayService.createPaymentUrl(
                                        order.getId().toString(),
                                        request.amount().longValue(),
                                        request.transactionId(),
                                        ipAddress);
                }

                // 4. Tạo PaymentEntity (Transaction sẽ chỉ được tạo sau khi gateway callback
                // thành công)
                PaymentEntity payment = PaymentEntity.builder()
                                .order(order)
                                .paymentMethod(request.paymentMethod())
                                .amountPaid(request.amount())
                                .status(PaymentStatus.PENDING)
                                .transactionId(request.transactionId())
                                .paymentUrl(paymentUrl)
                                .expiredAt(LocalDateTime.now().plus(15, ChronoUnit.MINUTES))
                                .build();
                paymentRepository.save(payment);

                return new PaymentResponse(
                                payment.getId(), // paymentId
                                order.getId(), // orderId
                                request.paymentMethod().toString(), // paymentMethod
                                request.amount(), // amountPaid (stored in vnd)
                                PaymentStatus.PENDING.toString(), // paymentStatus
                                OrderStatus.PROCESSING, // orderStatus
                                paymentUrl, // paymentUrl
                                LocalDateTime.now().plusMinutes(15), // expiredAt
                                LocalDateTime.now());
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

                // 2. Resolve payment by merchant transaction reference (transactionId sent when
                // creating payment)
                PaymentEntity payment = paymentRepository.findByTransactionId(vnpTxnRef)
                                .orElseThrow(() -> new ApiException(PaymentErrorCode.PAYMENT_NOT_FOUND,
                                                "Payment not found for transaction ref: " + vnpTxnRef));

                // 3. Store gateway response on payment regardless of outcome (preserves failure
                // codes)
                payment.setVnpResponseCode(responseCode);
                payment.setVnpBankCode(bankCode);

                // 4. Cập nhật PaymentEntity + Order tuỳ theo responseCode
                if (isSuccess) {
                        payment.setStatus(PaymentStatus.PAID);
                        // VNPay gửi amount * 100, convert về đơn vị gốc nếu cần
                        if (amountStr != null) {
                                payment.setAmountPaid(
                                                new BigDecimal(amountStr).divide(BigDecimal.valueOf(100)));
                        }
                        payment.setPaymentDate(LocalDateTime.now());
                        paymentRepository.save(payment);

                        // 5. Update Order status — guard: do NOT override CANCELED (Bug #5: late IPN
                        // after cancel)
                        if (payment.getOrder() != null) {
                                OrderEntity order = payment.getOrder();
                                if (order.getStatus() != OrderStatus.PAID
                                                && order.getStatus() != OrderStatus.CANCELED) {
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

        private PaymentListResponse.PaymentRecord buildPaymentRecord(PaymentEntity p, boolean isAdmin) {
                // Resolve latest SUCCESS transaction for money amounts
                TransactionEntity latestTxn = resolveLatestTransaction(p.getId());

                return PaymentListResponse.PaymentRecord.builder()
                                .orderId(p.getOrder() != null ? p.getOrder().getId().toString() : null)
                                .orderNumber(p.getOrder() != null ? p.getOrder().getOrderNumber() : null)
                                .customerName(resolveCustomerName(p))
                                .paymentMethod(p.getPaymentMethod().toString())
                                .amountPaid(latestTxn != null ? latestTxn.getAmount() : null)
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
                boolean canViewAll = "FRANCHISE_ADMIN".equalsIgnoreCase(role) || "STORE_MANAGER".equalsIgnoreCase(role);
                if (!canViewAll) {
                        if (latest.getOrder() == null || latest.getOrder().getCustomer() == null
                                        || !latest.getOrder().getCustomer().getId().equals(currentUserId)) {
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
                Instant fromInstant = fromDate.map(d -> d.atTime(LocalTime.MIN).atOffset(ZoneOffset.UTC).toInstant())
                                .orElse(null);
                Instant toInstant = toDate.map(d -> d.atTime(LocalTime.MAX).atOffset(ZoneOffset.UTC).toInstant())
                                .orElse(null);
                Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
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
                UUID customerId = p.getOrder() != null && p.getOrder().getCustomer() != null
                                ? p.getOrder().getCustomer().getId()
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
                if (p.getOrder() == null || p.getOrder().getCustomer() == null)
                        return null;
                var customer = p.getOrder().getCustomer();
                if (customer.getProfile() != null) {
                        return customer.getProfile().getFirstName() + " " + customer.getProfile().getLastName();
                }
                return customer.getEmail();
        }
}
