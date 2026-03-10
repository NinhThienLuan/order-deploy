package fsoft.franchise.serviceImpl;

import fsoft.franchise.common.exception.ApiException;
import fsoft.franchise.dto.orders.*;
import fsoft.franchise.dto.payments.PaymentResponse;
import fsoft.franchise.exception.CommonErrorCode;
import fsoft.franchise.exception.OrderErrorCode;
import fsoft.franchise.exception.PaymentErrorCode;
import fsoft.franchise.dto.payments.PaymentRequest;
import fsoft.franchise.entity.*;
import fsoft.franchise.enums.OrderStatus;
import fsoft.franchise.enums.PaymentStatus;
import fsoft.franchise.enums.MomoRequestType;
import fsoft.franchise.entity.external.AccountEntity;
import fsoft.franchise.enums.PaymentMethod;
import fsoft.franchise.repository.AccountRepository;
import fsoft.franchise.repository.OrderRepository;
import fsoft.franchise.repository.PaymentRepository;
import fsoft.franchise.repository.ProductRepository;
import fsoft.franchise.service.MoMoPaymentService;
import fsoft.franchise.service.OrderService;
import fsoft.franchise.service.OrderTrackingService;
import fsoft.franchise.service.PaymentMethodService;
import fsoft.franchise.service.VNPayService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

        private static final int MAX_PAGE_SIZE = 100;

        @Override
        public List<OrderStatus> getOrderStatuses() {
                return List.of(OrderStatus.values());
        }

        @Value("${app.payment.return-url:http://localhost:5173/payment/result}")
        private String paymentReturnUrl;

        private final OrderRepository orderRepository;
        private final ProductRepository productRepository;
        private final PaymentRepository paymentRepository;
        private final AccountRepository AccountRepository;
        private final MoMoPaymentService moMoPaymentService;
        private final PaymentMethodService paymentMethodService;
        private final VNPayService vnPayService;

        private final OrderTrackingService orderTrackingService;

        @Override
        public OrderCancelResponse cancelOrder(UUID orderId, UUID userId) {
                // validate order id
                OrderEntity order = orderRepository.findById(orderId)
                                .orElseThrow(() -> new ApiException(OrderErrorCode.ORDER_NOT_FOUND));

                // validate owner (uid in jwt)
                if (!order.getCustomer().getId().equals(userId))
                        throw new ApiException(OrderErrorCode.ORDER_NOT_OWNED);

                if (order.getStatus() == OrderStatus.PAID) {
                        throw new ApiException(OrderErrorCode.INVALID_ORDER_STATUS,
                                        "Order đã thanh toán, vui lòng sử dụng chức năng Refund");
                }

                if (order.getStatus() != OrderStatus.PENDING) {
                        throw new ApiException(OrderErrorCode.INVALID_ORDER_STATUS,
                                        "Cannot cancel order in current state: " + order.getStatus());
                }

                OrderStatus previousStatus = order.getStatus();
                order.setStatus(OrderStatus.CANCELED);
                orderRepository.save(order);

                // Push real-time notification: đơn bị hủy
                orderTrackingService.sendTrackingUpdate(orderId, previousStatus, OrderStatus.CANCELED, userId);

                LocalDateTime cancelTime = LocalDateTime.now();
                return new OrderCancelResponse(order.getId(), cancelTime);
        }

        @Override
        @Transactional(readOnly = true)
        public OrderHistoryPage getMyOrders(UUID customerId, int page, int size) {
                if (page < 1 || size < 1 || size > MAX_PAGE_SIZE) {
                        throw new ApiException(CommonErrorCode.BAD_REQUEST, "Invalid pagination parameters");
                }
                Pageable pageable = PageRequest.of(page - 1, size);
                Page<OrderEntity> slice = orderRepository.findByCustomer_IdOrderByOrderTimeDesc(customerId, pageable);
                List<OrderHistoryItem> content = slice.getContent().stream()
                                .map(o -> {
                                        BigDecimal amount = o.getTotalAmount();
                                        if (amount == null && o.getOrderItems() != null) {
                                                amount = o.getOrderItems().stream()
                                                                .map(item -> item.getUnitPrice()
                                                                                .multiply(BigDecimal.valueOf(
                                                                                                item.getQuantity())))
                                                                .reduce(BigDecimal.ZERO, BigDecimal::add);
                                        }
                                        return OrderHistoryItem.builder()
                                                        .id(o.getId())
                                                        .totalAmount(amount)
                                                        .status(o.getStatus())
                                                        .createdAt(o.getOrderTime())
                                                        .build();
                                })
                                .collect(Collectors.toList());
                return OrderHistoryPage.builder()
                                .content(content)
                                .page(page)
                                .size(size)
                                .totalElements(slice.getTotalElements())
                                .totalPages(slice.getTotalPages())
                                .build();
        }

        @Override
        @Transactional
        public CreateOrderResponse createOrder(CreateOrderRequest request, UUID customerId) {
                // 1. Tìm customer
                AccountEntity customer = AccountRepository.findById(customerId)
                                .orElseThrow(() -> new ApiException(OrderErrorCode.ORDER_NOT_FOUND));

                // 2. Tạo order
                OrderEntity order = OrderEntity.builder()
                                .customer(customer)
                                .status(OrderStatus.PENDING)
                                .orderTime(LocalDateTime.now())
                                .deliveryAddress(request.deliveryAddress())
                                .build();

                // 3. Tạo order items, tính tiền, trừ tồn kho
                List<OrderItemEntity> orderItems = new ArrayList<>();
                List<CreateOrderResponse.OrderItemResponse> itemResponses = new ArrayList<>();
                BigDecimal totalAmount = BigDecimal.ZERO;

                for (CreateOrderRequest.OrderItemRequest itemReq : request.items()) {
                        ProductEntity product = productRepository.findById(itemReq.productId())
                                        .orElseThrow(() -> new ApiException(OrderErrorCode.ORDER_NOT_FOUND));

                        // Kiểm tra tồn kho (active is now Boolean)
                        if (!Boolean.TRUE.equals(product.getActive())) {
                                throw new ApiException(OrderErrorCode.PRODUCT_OUT_OF_STOCK);
                        }

                        // NOTE: price was moved from ProductEntity to ProductVariantEntity (ERD
                        // refactor).
                        // If variantId is provided, use it; otherwise fallback to first active variant
                        ProductVariantEntity variant = null;
                        if (itemReq.variantId() != null) {
                                variant = product.getVariants().stream()
                                                .filter(v -> v.getId().equals(itemReq.variantId()))
                                                .findFirst()
                                                .orElseThrow(() -> new ApiException(OrderErrorCode.ORDER_NOT_FOUND,
                                                                "Variant not found for product: " + product.getName()));

                                if (!Boolean.TRUE.equals(variant.getActive())) {
                                        throw new ApiException(OrderErrorCode.PRODUCT_OUT_OF_STOCK,
                                                        "Requested variant is inactive");
                                }
                        } else {
                                variant = product.getVariants() == null ? null
                                                : product.getVariants().stream()
                                                                .filter(v -> Boolean.TRUE.equals(v.getActive()))
                                                                .findFirst()
                                                                .orElse(null);
                        }

                        BigDecimal unitPrice = variant != null && variant.getPrice() != null
                                        ? variant.getPrice()
                                        : BigDecimal.ZERO;
                        BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(itemReq.quantity()));
                        totalAmount = totalAmount.add(subtotal);

                        OrderItemEntity orderItem = new OrderItemEntity();
                        orderItem.setOrder(order);
                        orderItem.setProductVariant(variant);
                        orderItem.setQuantity(itemReq.quantity());
                        orderItem.setUnitPrice(unitPrice);
                        orderItems.add(orderItem);

                        itemResponses.add(new CreateOrderResponse.OrderItemResponse(
                                        product.getId(),
                                        product.getName(),
                                        itemReq.quantity(),
                                        unitPrice,
                                        subtotal));
                }

                order.setOrderItems(orderItems);
                order.setTotalAmount(totalAmount);

                // 4. Lưu order (cascade sẽ lưu orderItems)
                OrderEntity savedOrder = orderRepository.save(order);

                // 5. Ghi record đầu tiên vào history timeline (PENDING)
                orderTrackingService.sendTrackingUpdate(
                                savedOrder.getId(), null, OrderStatus.PENDING, customerId);

                return new CreateOrderResponse(
                                savedOrder.getId(),
                                savedOrder.getStatus(),
                                savedOrder.getOrderTime(),
                                totalAmount,
                                itemResponses);
        }

        @Override
        @Transactional
        public PaymentResponse processPayment(UUID orderId, PaymentRequest request, UUID customerId, String ipAddress) {
                // 1. Tìm order
                OrderEntity order = orderRepository.findById(orderId)
                                .orElseThrow(() -> new ApiException(OrderErrorCode.ORDER_NOT_FOUND));

                // 2. Validate owner
                if (!order.getCustomer().getId().equals(customerId)) {
                        throw new ApiException(OrderErrorCode.ORDER_NOT_OWNED);
                }

                // 3. Kiểm tra trạng thái đơn hàng
                if (order.getStatus() == OrderStatus.PAID) {
                        throw new ApiException(OrderErrorCode.ORDER_ALREADY_PAID);
                }
                if (order.getStatus() != OrderStatus.PENDING && order.getStatus() != OrderStatus.PROCESSING) {
                        throw new ApiException(OrderErrorCode.INVALID_ORDER_STATUS);
                }

                // 4. Validate payment method
                // Spring Boot tự throw lỗi nếu k match enum

                // 5. Tính tổng tiền đơn hàng
                BigDecimal orderTotal = order.getOrderItems().stream()
                                .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                // 6. Validate số tiền thanh toán
                if (request.amount().compareTo(orderTotal) != 0) {
                        throw new ApiException(PaymentErrorCode.PAYMENT_AMOUNT_MISMATCH);
                }

                // 7. Idempotency: nếu đã có payment PENDING cho order này → tái sử dụng, không
                // tạo mới
                PaymentEntity payment = paymentRepository
                                .findFirstByOrder_IdAndStatus(orderId, PaymentStatus.PENDING)
                                .orElse(null);

                if (payment == null) {
                        payment = new PaymentEntity();
                        payment.setOrder(order);
                        payment.setPaymentMethod(request.paymentMethod());
                        payment.setAmountPaid(request.amount());
                        payment.setStatus(PaymentStatus.PENDING);
                        payment.setTransactionId(UUID.randomUUID().toString());
                        paymentRepository.save(payment);
                }

            // Giá trị đơn hàng đã là VNĐ. VNPay & MoMo yêu cầu truyền lên số nguyên nhân với 100.
            long amountVnd = request.amount().multiply(java.math.BigDecimal.valueOf(100)).longValue();

            // 8a. VNPAY → generate VNPay payment URL
                if (request.paymentMethod() == PaymentMethod.VNPAY) {
                        String payUrl = vnPayService.createPaymentUrl(
                                        orderId.toString(),
                                        amountVnd,
                                        payment.getTransactionId(),
                                        ipAddress != null ? ipAddress : "127.0.0.1");

                        // Update order status and save payment URL
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

                // 8b. MOMO → validate request type rồi gọi MoMo API tạo payment link
                if (request.paymentMethod() == PaymentMethod.MOMO) {
                        MomoRequestType requestType = request.resolvedMomoRequestType();
                        if (!paymentMethodService.isMomoRequestTypeEnabled(requestType.getMomoCode())) {
                                throw new ApiException(PaymentErrorCode.INVALID_PAYMENT_METHOD,
                                                "MoMo request type '" + requestType.getMomoCode()
                                                                + "' is not enabled in the current environment");
                        }
                        String payUrl = moMoPaymentService.createPaymentLink(
                                        orderId,
                                        amountVnd,
                                        "Thanh toan don hang #" + orderId,
                                        requestType.getMomoCode());

                        // Update order status and save payment URL
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

                // 9. Các phương thức khác (CASH, WALLET...) → cập nhật trạng thái đơn thành
                // Paid ngay
                order.setStatus(OrderStatus.PAID);
                payment.setStatus(PaymentStatus.PAID);
                orderRepository.save(order);
                paymentRepository.save(payment);

                // Push real-time notification: thanh toán thành công
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

        @Override
        @Transactional
        public PaymentResponse confirmPayment(UUID orderId, UUID paymentId, boolean success, UUID customerId) {
                OrderEntity order = orderRepository.findById(orderId)
                                .orElseThrow(() -> new ApiException(OrderErrorCode.ORDER_NOT_FOUND));

                if (!order.getCustomer().getId().equals(customerId))
                        throw new ApiException(OrderErrorCode.ORDER_NOT_OWNED);

                PaymentEntity payment = paymentRepository.findByOrder_IdOrderByPaymentDateDesc(orderId)
                                .stream()
                                .filter(p -> p.getId().equals(paymentId) && p.getStatus() == PaymentStatus.PENDING)
                                .findFirst()
                                .orElseThrow(() -> new ApiException(PaymentErrorCode.PAYMENT_NOT_FOUND));

                OrderStatus previousStatus = order.getStatus();
                if (success) {
                        payment.setStatus(PaymentStatus.PAID);
                        payment.setPaymentDate(LocalDateTime.now());
                        payment.setPaymentUrl(null); // Clear URL once paid
                        order.setStatus(OrderStatus.PAID);
                } else {
                        payment.setStatus(PaymentStatus.FAILED);
                        payment.setErrorMessage("Payment cancelled or failed");
                        payment.setPaymentUrl(null); // Clear URL so they can retry
                        // Order reverts to PENDING — customer can retry from order detail
                        order.setStatus(OrderStatus.PENDING);
                }

                paymentRepository.save(payment);
                orderRepository.save(order);

                // Push real-time notification: payment result
                if (success) {
                        orderTrackingService.sendTrackingUpdate(orderId, previousStatus, OrderStatus.PAID, customerId);
                }

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

        @Override
        @Transactional(readOnly = true)
        public OrderStatusResponse getStatus(UUID orderId, UUID userId, String role) {
                OrderEntity order = orderRepository.findByIdWithCustomer(orderId)
                                .orElseThrow(() -> new ApiException(OrderErrorCode.ORDER_NOT_FOUND));
                // FRANCHISE_ADMIN và STORE_MANAGER được xem mọi order; CUSTOMER chỉ xem order
                // của mình
                boolean canViewAnyOrder = "FRANCHISE_ADMIN".equalsIgnoreCase(role)
                                || "STORE_MANAGER".equalsIgnoreCase(role);
                if (!canViewAnyOrder && !order.getCustomer().getId().equals(userId))
                        throw new ApiException(OrderErrorCode.ORDER_NOT_OWNED);
                return OrderStatusResponse.builder()
                                .id(order.getId())
                                .status(order.getStatus())
                                .lastUpdated(order.getOrderTime())
                                .build();
        }

        @Override
        @Transactional(readOnly = true)
        public OrderHistoryPage getOrderHistory(int page, int size,
                        Optional<String> status,
                        Optional<Long> branchId,
                        Optional<LocalDate> fromDate,
                        Optional<LocalDate> toDate,
                        String role) {
                // Chỉ FRANCHISE_ADMIN và STORE_MANAGER được dùng API lịch sử đơn hàng; CUSTOMER
                // → 403
                if (!"FRANCHISE_ADMIN".equalsIgnoreCase(role) && !"STORE_MANAGER".equalsIgnoreCase(role)) {
                        throw new ApiException(CommonErrorCode.FORBIDDEN);
                }
                if (page < 1 || size < 1 || size > MAX_PAGE_SIZE) {
                        throw new ApiException(CommonErrorCode.BAD_REQUEST, "Invalid pagination parameters");
                }
                if (fromDate.isPresent() && toDate.isPresent() && fromDate.get().isAfter(toDate.get())) {
                        throw new ApiException(CommonErrorCode.BAD_REQUEST, "Invalid date range");
                }
                OrderStatus statusEnum = null;
                if (status.isPresent() && status.get() != null && !status.get().isBlank()) {
                        try {
                                statusEnum = OrderStatus.valueOf(status.get().trim().toUpperCase());
                        } catch (IllegalArgumentException e) {
                                throw new ApiException(OrderErrorCode.INVALID_ORDER_STATUS, "Invalid order status");
                        }
                }
                LocalDateTime fromDateTime = fromDate.map(d -> LocalDateTime.of(d, LocalTime.MIN)).orElse(null);
                LocalDateTime toDateTime = toDate.map(d -> LocalDateTime.of(d, LocalTime.MAX)).orElse(null);

                Pageable pageable = PageRequest.of(page - 1, size);
                Page<OrderEntity> slice = orderRepository.findOrderHistory(
                                statusEnum,
                                branchId.orElse(null),
                                fromDateTime,
                                toDateTime,
                                pageable);
                List<OrderHistoryItem> content = slice.getContent().stream()
                                .map(o -> {
                                        OrderStatus orderStatus = null;
                                        if (o.getStatus() != null) {
                                                try {
                                                        orderStatus = o.getStatus();
                                                } catch (IllegalArgumentException ignored) {
                                                        // Trong DB có status không nằm trong enum (vd: "Pending") →
                                                        // hiển thị null
                                                }
                                        }
                                        BigDecimal amount = o.getTotalAmount();
                                        if (amount == null && o.getOrderItems() != null) {
                                                amount = o.getOrderItems().stream()
                                                                .map(item -> item.getUnitPrice()
                                                                                .multiply(BigDecimal.valueOf(
                                                                                                item.getQuantity())))
                                                                .reduce(BigDecimal.ZERO, BigDecimal::add);
                                        }
                                        return OrderHistoryItem.builder()
                                                        .id(o.getId())
                                                        .totalAmount(amount)
                                                        .status(orderStatus)
                                                        .createdAt(o.getOrderTime())
                                                        .build();
                                })
                                .collect(Collectors.toList());

                return OrderHistoryPage.builder()
                                .content(content)
                                .page(page)
                                .size(size)
                                .totalElements(slice.getTotalElements())
                                .totalPages(slice.getTotalPages())
                                .build();
        }

        @Override
        public OrderDetailResponse getOrderDetail(String orderId, UUID currentUserId, String role) {

                // 1. Fetch order with all details (Using two separate queries to avoid
                // MultipleBagFetchException)
                UUID orderUUID;
                try {
                        orderUUID = UUID.fromString(orderId);
                } catch (IllegalArgumentException e) {
                        throw new ApiException(CommonErrorCode.RESOURCE_NOT_FOUND,
                                        "Order not found. Please check the order number.");
                }
                OrderEntity order = orderRepository.findByIdWithItems(orderUUID)
                                .orElseThrow(() -> new ApiException(
                                                CommonErrorCode.RESOURCE_NOT_FOUND,
                                                "Order not found. Please check the order number."));

                // Fetch payments separately
                OrderEntity orderWithPayments = orderRepository.findByIdWithPayments(orderUUID)
                                .orElse(order);
                order.setPayments(orderWithPayments.getPayments());

                // 2. Ownership check: CUSTOMER can only view their own orders;
                // FRANCHISE_ADMIN and STORE_MANAGER can view any order
                boolean isManager = "FRANCHISE_ADMIN".equalsIgnoreCase(role)
                                || "STORE_MANAGER".equalsIgnoreCase(role);
                if (!isManager && !order.getCustomer().getId().equals(currentUserId))
                        throw new ApiException(OrderErrorCode.ORDER_NOT_OWNED);

                // 3. Build CustomerInfo
                OrderDetailResponse.CustomerInfo customerInfo = OrderDetailResponse.CustomerInfo.builder()
                                .customerId(String.valueOf(order.getCustomer().getId()))
                                .customerName(order.getCustomer().getProfile() != null
                                                ? order.getCustomer().getProfile().getFirstName() + " "
                                                                + order.getCustomer().getProfile().getLastName()
                                                : "")
                                .contactNumber(order.getCustomer().getPhoneNumber())
                                .deliveryAddress("POS".equalsIgnoreCase(order.getOrderType())
                                                ? null
                                                : order.getDeliveryAddress())
                                .build();

                // 4. Build OrderItems
                List<OrderDetailResponse.OrderItemInfo> itemInfos = order.getOrderItems().stream()
                                .map(this::mapOrderItem)
                                .collect(Collectors.toList());

                // 5. Calculate pricing
                BigDecimal subtotal = itemInfos.stream()
                                .map(OrderDetailResponse.OrderItemInfo::getSubtotal)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);
                BigDecimal discount = BigDecimal.ZERO;
                BigDecimal totalAmount = subtotal.subtract(discount);

                // 6. Build PaymentInfo
                PaymentEntity payment = order.getPayments() != null && !order.getPayments().isEmpty()
                                ? order.getPayments().stream().findFirst().orElse(null)
                                : null;

                OrderDetailResponse.PaymentInfo paymentInfo;
                if (payment != null) {
                        paymentInfo = OrderDetailResponse.PaymentInfo.builder()
                                        .paymentMethod(payment.getPaymentMethod().toString())
                                        .amountPaid(payment.getAmountPaid())
                                        .paymentStatus(mapPaymentStatus(payment.getStatus().toString()))
                                        .paymentDate("Pending".equalsIgnoreCase(payment.getStatus().toString())
                                                        ? null
                                                        : payment.getPaymentDate())
                                        .paymentUrl(payment.getPaymentUrl())
                                        .build();
                } else {
                        paymentInfo = OrderDetailResponse.PaymentInfo.builder()
                                        .paymentStatus("Pending")
                                        .amountPaid(BigDecimal.ZERO)
                                        .build();
                }

                // 7. Build response
                return OrderDetailResponse.builder()
                                .orderId(order.getId())
                                .orderNumber(order.getOrderNumber())
                                .status(order.getStatus())
                                .orderType(order.getOrderType())
                                .orderTime(order.getOrderTime())
                                .customer(customerInfo)
                                .items(itemInfos)
                                .pricing(OrderDetailResponse.PricingInfo.builder()
                                                .subtotal(subtotal)
                                                .discount(discount)
                                                .totalAmount(totalAmount)
                                                .build())
                                .payment(paymentInfo)
                                .createdAt(order.getCreatedAt())
                                .updatedAt(order.getUpdatedAt())
                                .build();
        }

        private OrderDetailResponse.OrderItemInfo mapOrderItem(OrderItemEntity item) {
                BigDecimal subtotal = item.getUnitPrice()
                                .multiply(BigDecimal.valueOf(item.getQuantity()));

                // Product info resolved via the variant relationship
                ProductEntity product = item.getProductVariant() != null
                                ? item.getProductVariant().getProduct()
                                : null;

                return OrderDetailResponse.OrderItemInfo.builder()
                                .productId(product != null ? String.valueOf(product.getId()) : null)
                                .productName(product != null ? product.getName() : "Unknown")
                                .variantName(item.getProductVariant() != null ? item.getProductVariant().getSizeName()
                                                : null)
                                .quantity(item.getQuantity())
                                .unitPrice(item.getUnitPrice())
                                .subtotal(subtotal)
                                .build();
        }

        private String mapPaymentStatus(String dbStatus) {
                return switch (dbStatus) {
                        case "PAID" -> "Paid";
                        case "FAILED" -> "Failed";
                        case "REFUNDED" -> "Refunded";
                        default -> "Pending";
                };
        }
}
