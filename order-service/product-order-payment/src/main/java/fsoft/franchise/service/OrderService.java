package fsoft.franchise.service;

import fsoft.franchise.dto.orders.*;
import fsoft.franchise.dto.payments.PaymentRequest;
import fsoft.franchise.dto.payments.PaymentResponse;
// import fsoft.franchise.model.dto.*;
import org.springframework.stereotype.Service;

import fsoft.franchise.enums.OrderStatus;
import java.util.List;
import java.util.UUID;
import java.time.LocalDate;
import java.util.Optional;

@Service
public interface OrderService {

    public OrderCancelResponse cancelOrder(UUID orderId, UUID userId);

    public OrderDetailResponse getOrderDetail(String orderId, UUID currentUserId, String role);

    /**
     * Get order status. Customer chỉ xem được order của mình; FRANCHISE_ADMIN và
     * STORE_MANAGER xem được mọi order.
     */
    OrderStatusResponse getStatus(UUID orderId, UUID userId, String role);

    /**
     * Order History: pagination + filter. Chỉ FRANCHISE_ADMIN và STORE_MANAGER được gọi.
     */
    OrderHistoryPage getOrderHistory(int page, int size,
            Optional<String> status,
            Optional<Long> branchId,
            Optional<LocalDate> fromDate,
            Optional<LocalDate> toDate,
            String role);

    /**
     * Get order history for the authenticated customer (their own orders only).
     */
    OrderHistoryPage getMyOrders(UUID customerId, int page, int size);

    /** Returns all possible order statuses. */
    List<OrderStatus> getOrderStatuses();

    CreateOrderResponse createOrder(CreateOrderRequest request, UUID customerId);

    PaymentResponse processPayment(UUID orderId, PaymentRequest request, UUID customerId, String ipAddress);

    PaymentResponse confirmPayment(UUID orderId, UUID paymentId, boolean success, UUID customerId);
}
