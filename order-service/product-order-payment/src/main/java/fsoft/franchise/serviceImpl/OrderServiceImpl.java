package fsoft.franchise.serviceImpl;

import fsoft.franchise.client.FranchiseStoreClient;
import fsoft.franchise.client.InventoryClient;
import fsoft.franchise.common.exception.ApiException;
import fsoft.franchise.dto.integration.FranchiseStoreResponse;
import fsoft.franchise.dto.integration.StoreInventoryResponse;
import fsoft.franchise.dto.orders.*;
import fsoft.franchise.dto.payments.PaymentResponse;
import fsoft.franchise.exception.CommonErrorCode;
import fsoft.franchise.exception.OrderErrorCode;
import fsoft.franchise.exception.PaymentErrorCode;
import fsoft.franchise.dto.payments.PaymentRequest;
import fsoft.franchise.entity.*;
import fsoft.franchise.enums.OrderStatus;
import fsoft.franchise.enums.PaymentStatus;
import fsoft.franchise.enums.PaymentType;
import fsoft.franchise.enums.MomoRequestType;
import fsoft.franchise.enums.PaymentMethod;
import fsoft.franchise.repository.OrderRepository;
import fsoft.franchise.repository.PaymentRepository;
import fsoft.franchise.repository.ProductRepository;
import fsoft.franchise.service.MoMoPaymentService;
import fsoft.franchise.service.OrderService;
import fsoft.franchise.service.OrderTrackingService;
import fsoft.franchise.service.PaymentMethodService;
import fsoft.franchise.service.VNPayService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
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
        private final MoMoPaymentService moMoPaymentService;
        private final PaymentMethodService paymentMethodService;
        private final VNPayService vnPayService;
        private final OrderTrackingService orderTrackingService;
        private final FranchiseStoreClient franchiseStoreClient;
        private final InventoryClient inventoryClient;

        @Override
        public OrderCancelResponse cancelOrder(UUID orderId, UUID userId, String role) {
                // validate order id
                OrderEntity order = orderRepository.findById(orderId)
                                .orElseThrow(() -> new ApiException(OrderErrorCode.ORDER_NOT_FOUND));
                boolean canCancle = "ADMIN".equalsIgnoreCase(role)
                        || "MANAGER".equalsIgnoreCase(role)
                        || "POS".equalsIgnoreCase(role);
                // validate owner (uid in jwt)
                if (!canCancle && !order.getCustomerId().equals(userId))
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
                Page<OrderEntity> slice = orderRepository.findByCustomerIdOrderByOrderTimeDesc(customerId, pageable);
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

        // =====================================================================
        // PRIVATE HELPER: Validate store (active) + inventory stock
        // =====================================================================

        /**
         * Validates:
         * 1. The franchise store exists and is ACTIVE.
         * 2. The store has sufficient ingredient stock to fulfill the order items.
         *
         * Algorithm:
         * - For each order item, load the selected (or first active) ProductVariant.
         * - From the variant's ProductVariantIngredientEntity list, accumulate the
         *   total ingredient quantities needed (quantity_per_unit × order_quantity).
         * - Fetch the store's current inventory from the Inventory service.
         * - Compare required vs available for each ingredient. Throw INSUFFICIENT_STOCK
         *   if any ingredient falls short.
         */
        private void validateStoreAndStock(UUID storeId,
                        List<CreateOrderRequest.OrderItemRequest> items,
                        List<ProductEntity> resolvedProducts) {

                // --- 1. Validate store ---
                FranchiseStoreResponse store;
                try {
                        var resp = franchiseStoreClient.getStoreById(storeId);
                        store = resp.result();
                } catch (feign.FeignException.NotFound e) {
                        throw new ApiException(OrderErrorCode.STORE_NOT_FOUND,
                                        "Store not found: " + storeId);
                } catch (Exception e) {
                        log.error("Failed to call FranchiseStore service for storeId={}: {}", storeId, e.getMessage());
                        throw new ApiException(OrderErrorCode.STORE_NOT_FOUND,
                                        "Unable to reach FranchiseStore service");
                }

                if (store == null || !"ACTIVE".equalsIgnoreCase(store.status())) {
                        String currentStatus = store != null ? store.status() : "unknown";
                        throw new ApiException(OrderErrorCode.STORE_NOT_ACTIVE,
                                        "Store " + storeId + " is not active (status: " + currentStatus + ")");
                }

                // --- 2. Aggregate required ingredient quantities from order items ---
                // Map: ingredientId -> total quantity required
                Map<UUID, BigDecimal> requiredIngredients = new HashMap<>();

                for (int i = 0; i < items.size(); i++) {
                        CreateOrderRequest.OrderItemRequest itemReq = items.get(i);
                        ProductEntity product = resolvedProducts.get(i);

                        // Resolve variant (same logic as createOrder)
                        ProductVariantEntity variant = null;
                        if (itemReq.variantId() != null) {
                                variant = product.getVariants().stream()
                                                .filter(v -> v.getId().equals(itemReq.variantId()))
                                                .findFirst().orElse(null);
                        } else {
                                variant = product.getVariants() == null ? null
                                                : product.getVariants().stream()
                                                                .filter(v -> Boolean.TRUE.equals(v.getActive()))
                                                                .findFirst().orElse(null);
                        }

                        if (variant == null || variant.getIngredients() == null) {
                                continue; // no ingredient mapping — skip
                        }

                        BigDecimal orderQty = BigDecimal.valueOf(itemReq.quantity());
                        for (ProductVariantIngredientEntity pvi : variant.getIngredients()) {
                                if (pvi.getIngredient() == null || pvi.getQuantity() == null) continue;
                                UUID ingredientId = pvi.getIngredient().getId();
                                BigDecimal needed = pvi.getQuantity().multiply(orderQty);
                                requiredIngredients.merge(ingredientId, needed, BigDecimal::add);
                        }
                }

                if (requiredIngredients.isEmpty()) {
                        log.debug("No ingredient mapping found for storeId={}, skipping inventory check", storeId);
                        return;
                }

                // --- 3. Fetch store inventory from Inventory service ---
                List<StoreInventoryResponse> inventoryList;
                try {
                        var invResp = inventoryClient.getStoreInventory(storeId);
                        inventoryList = invResp.result();
                } catch (Exception e) {
                        log.error("Failed to call Inventory service for storeId={}: {}", storeId, e.getMessage());
                        throw new ApiException(OrderErrorCode.INSUFFICIENT_STOCK,
                                        "Unable to reach Inventory service to verify stock");
                }

                // Build a map: ingredientId -> available quantity in inventory
                Map<UUID, BigDecimal> availableStock = new HashMap<>();
                if (inventoryList != null) {
                        for (StoreInventoryResponse inv : inventoryList) {
                                availableStock.put(inv.ingredientId(),
                                                inv.quantity() != null ? inv.quantity() : BigDecimal.ZERO);
                        }
                }

                // --- 4. Compare required vs available ---
                for (Map.Entry<UUID, BigDecimal> entry : requiredIngredients.entrySet()) {
                        UUID ingredientId = entry.getKey();
                        BigDecimal required = entry.getValue();
                        BigDecimal available = availableStock.getOrDefault(ingredientId, BigDecimal.ZERO);
                        if (available.compareTo(required) < 0) {
                                throw new ApiException(OrderErrorCode.INSUFFICIENT_STOCK,
                                                "Insufficient stock for ingredient " + ingredientId
                                                                + ": required=" + required
                                                                + ", available=" + available);
                        }
                }
        }

        @Override
        @Transactional
        public CreateOrderResponse createOrder(CreateOrderRequest request, UUID customerId) {

                // 2. Tạo order
                OrderEntity order = OrderEntity.builder()
                                .customerId(customerId)
                                .status(OrderStatus.PENDING)
                                .orderTime(LocalDateTime.now())
                                .deliveryAddress(request.deliveryAddress())
                                .note(request.note())
                                .deliveryAddress(request.deliveryAddress())
                                .build();

                // 3. Tạo order items, tính tiền, trừ tồn kho
                List<OrderItemEntity> orderItems = new ArrayList<>();
                List<CreateOrderResponse.OrderItemResponse> itemResponses = new ArrayList<>();
                BigDecimal totalAmount = BigDecimal.ZERO;

                // Pre-load all products for stock validation
                List<ProductEntity> resolvedProducts = new ArrayList<>();
                for (CreateOrderRequest.OrderItemRequest itemReq : request.items()) {
                        ProductEntity product = productRepository.findById(itemReq.productId())
                                        .orElseThrow(() -> new ApiException(OrderErrorCode.ORDER_NOT_FOUND));
                        if (!Boolean.TRUE.equals(product.getActive())) {
                                throw new ApiException(OrderErrorCode.PRODUCT_OUT_OF_STOCK);
                        }
                        resolvedProducts.add(product);
                }

                // Validate store + inventory BEFORE creating anything
                validateStoreAndStock(request.storeId(), request.items(), resolvedProducts);

                // Set storeId on the order
                order.setStoreId(request.storeId());

                for (int idx = 0; idx < request.items().size(); idx++) {
                        CreateOrderRequest.OrderItemRequest itemReq = request.items().get(idx);
                        ProductEntity product = resolvedProducts.get(idx);

                        // Kiểm tra tồn kho (active is now Boolean) — already checked above, kept for clarity
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
                                itemResponses,
                                savedOrder.getNote(),
                                savedOrder.getDeliveryAddress());
        }


        private static final int BASE_TIME_MINUTES = 3;
        private static final int AVG_TIME_PER_ORDER = 2;
        private static final int AVG_TIME_PER_ITEM = 1;

        @Override
        public EstimateResponse estimatePreparationTime(UUID storeId, int itemCount) {

                if (itemCount <= 0) {
                        throw new ApiException(CommonErrorCode.BAD_REQUEST, "itemCount must be > 0");
                }
                int activeOrderCount = orderRepository.countPreparingByStoreId(storeId);
                int estimatedMinutes = BASE_TIME_MINUTES + (activeOrderCount * AVG_TIME_PER_ORDER)
                                + (itemCount * AVG_TIME_PER_ITEM);
                return EstimateResponse.builder()
                                .storeId(storeId)
                                .estimatedMinutes(estimatedMinutes)
                                .activeOrderCount(activeOrderCount)
                                .itemCount(itemCount)
                                .calculatedAt(LocalDateTime.now())
                                .build();
        }

        @Override
        @Transactional
        public FlagOrderResponse flagOrder(UUID orderId, FlagOrderRequest request, UUID currentUserId, String role,
                        Long currentUserStoreId) {
                OrderEntity order = orderRepository.findById(orderId)
                                .orElseThrow(() -> new ApiException(OrderErrorCode.ORDER_NOT_FOUND));
                boolean isAdmin = "ADMIN".equalsIgnoreCase(role);
                if (!isAdmin && !order.getStoreId().equals(currentUserStoreId)) {
                        throw new ApiException(CommonErrorCode.FORBIDDEN,
                                        "You do not have permission to flag this order");
                }
                if (order.getStatus() != OrderStatus.PAID && order.getStatus() != OrderStatus.PREPARING) {
                        throw new ApiException(OrderErrorCode.INVALID_ORDER_STATUS,
                                        "Chỉ đơn PAID hoặc PREPARING mới được flag");
                }
                order.setIsFlagged(true);
                order.setFlagReason(request.getReason());
                order.setFlaggedBy(currentUserId);
                order.setFlaggedAt(LocalDateTime.now());
                OrderEntity saved = orderRepository.save(order);
                return FlagOrderResponse.builder()
                                .orderId(saved.getId())
                                .orderNumber(saved.getOrderNumber())
                                .isFlagged(saved.getIsFlagged())
                                .flagReason(saved.getFlagReason())
                                .flaggedBy(saved.getFlaggedBy())
                                .flaggedAt(saved.getFlaggedAt())
                                .currentStatus(saved.getStatus().name())
                                .build();
        }

        @Override
        @Transactional(readOnly = true)
        public OrderStatusResponse getStatus(UUID orderId, UUID userId, String role) {
                OrderEntity order = orderRepository.findByIdWithCustomer(orderId)
                                .orElseThrow(() -> new ApiException(OrderErrorCode.ORDER_NOT_FOUND));
                // ADMIN and MANAGER được xem mọi order; CUSTOMER chỉ xem order
                // của mình
                boolean canViewAnyOrder = "ADMIN".equalsIgnoreCase(role)
                                || "MANAGER".equalsIgnoreCase(role)
                                || "POS".equalsIgnoreCase(role);
                if (!canViewAnyOrder && !order.getCustomerId().equals(userId))
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
                        Optional<UUID> storeId,
                        Optional<LocalDate> fromDate,
                        Optional<LocalDate> toDate,
                        String role) {
                // Chỉ ADMIN và MANAGER được dùng API lịch sử đơn hàng; CUSTOMER
                // → 403
                if (!"ADMIN".equalsIgnoreCase(role) && !"MANAGER".equalsIgnoreCase(role)) {
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
                                storeId.orElse(null),
                                fromDateTime,
                                toDateTime,
                                pageable);
                BigDecimal filterTotalAmount = orderRepository.sumTotalAmountByFilter(
                                statusEnum,
                                storeId.orElse(null),
                                fromDateTime,
                                toDateTime);
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
                                .totalAmount(filterTotalAmount != null ? filterTotalAmount : BigDecimal.ZERO)
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
                // ADMIN and MANAGER can view any order
                boolean isManager = "ADMIN".equalsIgnoreCase(role)
                                || "MANAGER".equalsIgnoreCase(role)
                                || "POS".equalsIgnoreCase(role);
                if (!isManager && !order.getCustomerId().equals(currentUserId))
                        throw new ApiException(OrderErrorCode.ORDER_NOT_OWNED);

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

                // 7. Build response
                return OrderDetailResponse.builder()
                                .orderId(order.getId())
                                .orderNumber(order.getOrderNumber())
                                .status(order.getStatus())
                                .orderType(order.getOrderType())
                                .orderTime(order.getOrderTime())
                                .note(order.getNote())
                                .deliveryAddress(order.getDeliveryAddress())
                                .items(itemInfos)
                                .pricing(OrderDetailResponse.PricingInfo.builder()
                                                .subtotal(subtotal)
                                                .discount(discount)
                                                .totalAmount(totalAmount)
                                                .build())
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
                                .variantId(item.getProductVariant() != null ? item.getProductVariant().getId() : null)
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

        @Override
        @Transactional
        public CreatePosOrderResponse createPosOrder(CreatePosOrderRequest request, UUID staffId) {
                // 1. Tạo order với type POS
                OrderEntity order = OrderEntity.builder()
                                .storeId(request.storeId())
                                .customerId(request.customerId())
                                .status(OrderStatus.PENDING)
                                .orderTime(LocalDateTime.now())
                                .orderType("POS")
                                .note(request.note())
                                // .staffId(staffId) // Có thể thêm field này vào OrderEntity nếu cần tracking
                                .build();

                // 2. Tạo order items, tính tiền, trừ tồn kho (logic tương tự createOrder)
                List<OrderItemEntity> orderItems = new ArrayList<>();
                List<CreatePosOrderResponse.OrderItemResponse> itemResponses = new ArrayList<>();
                BigDecimal totalAmount = BigDecimal.ZERO;

                // Pre-load all products for stock validation
                List<ProductEntity> resolvedProducts = new ArrayList<>();
                for (CreateOrderRequest.OrderItemRequest itemReq : request.items()) {
                        ProductEntity product = productRepository.findById(itemReq.productId())
                                        .orElseThrow(() -> new ApiException(OrderErrorCode.ORDER_NOT_FOUND));
                        if (!Boolean.TRUE.equals(product.getActive())) {
                                throw new ApiException(OrderErrorCode.PRODUCT_OUT_OF_STOCK);
                        }
                        resolvedProducts.add(product);
                }

                // Validate store + inventory BEFORE creating anything
                validateStoreAndStock(request.storeId(), request.items(), resolvedProducts);

                for (int idx = 0; idx < request.items().size(); idx++) {
                        CreateOrderRequest.OrderItemRequest itemReq = request.items().get(idx);
                        ProductEntity product = resolvedProducts.get(idx);

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

                        itemResponses.add(new CreatePosOrderResponse.OrderItemResponse(
                                        product.getId(),
                                        product.getName(),
                                        variant != null ? variant.getSizeName() : null,
                                        variant != null ? variant.getId() : null,
                                        itemReq.quantity(),
                                        unitPrice,
                                        subtotal));
                }

                order.setOrderItems(orderItems);
                order.setTotalAmount(totalAmount);

                // 3. Lưu order
                OrderEntity savedOrder = orderRepository.save(order);

                // 4. Tracking
                orderTrackingService.sendTrackingUpdate(
                                savedOrder.getId(), null, OrderStatus.PENDING, staffId);

                return new CreatePosOrderResponse(
                                savedOrder.getId(),
                                savedOrder.getStatus(),
                                savedOrder.getOrderTime(),
                                totalAmount,
                                itemResponses,
                                savedOrder.getNote()); //lưu tạm cho đê
        }

    @Override
    @Transactional
    public UpdatePosOrderResponse updatePosOrder(UUID orderId,
                                                 UpdatePosOrderRequest request,
                                                 UUID staffId) {

        // 1. Lay order — 404 neu khong co
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ApiException(OrderErrorCode.ORDER_NOT_FOUND));

        // 2. Chi update duoc khi dang PENDING
        //    PREPARING tro di = bep da bat dau lam -> khong cho sua
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new ApiException(OrderErrorCode.ORDER_ALREADY_PAID,
                    "Only PENDING orders can be modified. Current status: " + order.getStatus());
        }

        // 3. Update note neu co
        if (request.note() != null) {
            order.setNote(request.note());
        }

        // 4. Rebuild items neu client gui items moi
        //    An toan hon la diff vi orphanRemoval = true se tu xoa item cu khoi DB
        List<UpdatePosOrderResponse.OrderItemResponse> itemResponses = new ArrayList<>();
        if (request.items() != null && !request.items().isEmpty()) {

            // Xoa items cu — orphanRemoval tren @OneToMany se DELETE khoi DB
            order.getOrderItems().clear();

            List<OrderItemEntity> newItems = new ArrayList<>();
            BigDecimal totalAmount = BigDecimal.ZERO;

            for (UpdatePosOrderRequest.OrderItemRequest itemReq : request.items()) {
                ProductEntity product = productRepository.findById(itemReq.productId())
                        .orElseThrow(() -> new ApiException(OrderErrorCode.ORDER_NOT_FOUND));

                if (!Boolean.TRUE.equals(product.getActive())) {
                    throw new ApiException(OrderErrorCode.PRODUCT_OUT_OF_STOCK);
                }

                // Logic chon variant — giu nguyen y tu createPosOrder
                ProductVariantEntity variant = null;
                if (itemReq.variantId() != null) {
                    variant = product.getVariants().stream()
                            .filter(v -> v.getId().equals(itemReq.variantId()))
                            .findFirst()
                            .orElseThrow(() -> new ApiException(OrderErrorCode.ORDER_NOT_FOUND,
                                    "Variant not found: " + product.getName()));

                    if (!Boolean.TRUE.equals(variant.getActive())) {
                        throw new ApiException(OrderErrorCode.PRODUCT_OUT_OF_STOCK,
                                "Variant is inactive");
                    }
                } else {
                    variant = product.getVariants() == null ? null
                            : product.getVariants().stream()
                            .filter(v -> Boolean.TRUE.equals(v.getActive()))
                            .findFirst()
                            .orElse(null);
                }

                BigDecimal unitPrice = variant != null && variant.getPrice() != null
                        ? variant.getPrice() : BigDecimal.ZERO;
                BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(itemReq.quantity()));
                totalAmount = totalAmount.add(subtotal);

                OrderItemEntity item = new OrderItemEntity();
                item.setOrder(order);
                item.setProductVariant(variant);
                item.setQuantity(itemReq.quantity());
                item.setUnitPrice(unitPrice);
                newItems.add(item);

                itemResponses.add(new UpdatePosOrderResponse.OrderItemResponse(
                        product.getId(), product.getName(), variant != null ? variant.getSizeName() : null,
                        itemReq.quantity(), unitPrice, subtotal));
            }

            order.getOrderItems().addAll(newItems);
            order.setTotalAmount(totalAmount);
        }

        // 5. Luu — @Transactional + orphanRemoval xu ly delete/insert tu dong
        OrderEntity saved = orderRepository.save(order);

        // 6. Tracking
        orderTrackingService.sendTrackingUpdate(
                saved.getId(), OrderStatus.PENDING, OrderStatus.PENDING, staffId);

        return new UpdatePosOrderResponse(
                saved.getId(),
                saved.getStatus(),
                saved.getOrderTime(),
                saved.getTotalAmount(),
                itemResponses,
                saved.getNote(),
                saved.getUpdatedAt());
    }
}
