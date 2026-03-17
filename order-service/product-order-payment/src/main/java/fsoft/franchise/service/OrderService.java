package fsoft.franchise.service;

import fsoft.franchise.dto.orders.*;
// import fsoft.franchise.model.dto.*;
import org.springframework.stereotype.Service;

import fsoft.franchise.enums.OrderStatus;
import java.util.List;
import java.util.UUID;
import java.time.LocalDate;
import java.util.Optional;

@Service
public interface OrderService {

    public OrderCancelResponse cancelOrder(UUID orderId, UUID userId, String role);

    public OrderDetailResponse getOrderDetail(String orderId, UUID currentUserId, String role);

    /**
     * Get order status. Customer chỉ xem được order của mình; ADMIN và
     * MANAGER xem được mọi order.
     */
    OrderStatusResponse getStatus(UUID orderId, UUID userId, String role);

    /**
     * Order History: pagination + filter. Chỉ ADMIN và MANAGER được
     * gọi.
     */
    OrderHistoryPage getOrderHistory(int page, int size,
            Optional<String> status,
            Optional<UUID> storeId,
            Optional<LocalDate> fromDate,
            Optional<LocalDate> toDate,
            String role);

    /**
     * Get order history for the authenticated customer (their own orders only).
     */
    OrderHistoryPage getMyOrders(UUID customerId, int page, int size);

    /** Returns all possible order statuses. */
    List<OrderStatus> getOrderStatuses();

    /** Returns all relevant enums for order creation/management. */
    OrderEnumsResponse getOrderEnums();

    CreateOrderResponse createOrder(CreateOrderRequest request, UUID customerId);


    EstimateResponse estimatePreparationTime(UUID storeId, int itemCount);

    FlagOrderResponse flagOrder(UUID orderId, FlagOrderRequest request, UUID currentUserId, String role,
            Long currentUserStoreId);

    CreatePosOrderResponse createPosOrder(CreatePosOrderRequest request, UUID staffId);
    UpdatePosOrderResponse updatePosOrder(UUID orderId, UpdatePosOrderRequest request, UUID staffId);
}
