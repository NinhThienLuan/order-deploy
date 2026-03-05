package fsoft.franchise.controller;

import fsoft.franchise.common.response.ApiResponse;
import fsoft.franchise.exception.CommonErrorCode;
import fsoft.franchise.security.JwtService;
import fsoft.franchise.dto.orders.CreateOrderRequest;
import fsoft.franchise.dto.orders.CreateOrderResponse;
import fsoft.franchise.dto.orders.OrderCancelResponse;
import fsoft.franchise.dto.orders.OrderDetailResponse;
import fsoft.franchise.dto.orders.OrderHistoryPage;
import fsoft.franchise.dto.orders.OrderStatusResponse;
import fsoft.franchise.dto.payments.PaymentRequest;
import fsoft.franchise.dto.payments.PaymentResponse;
import fsoft.franchise.dto.payments.RefundRequest;
import fsoft.franchise.dto.payments.RefundResponse;
import fsoft.franchise.service.OrderService;
import fsoft.franchise.service.RefundService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import fsoft.franchise.enums.OrderStatus;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


@RestController
@RequestMapping("/api/v1/orders")
@Validated
@RequiredArgsConstructor
@Tag(name = "Orders", description = "Order creation, status, history, payment processing, and refund requests")
public class OrderController {

    private final OrderService orderService;
    private final RefundService refundService;
    private final JwtService jwtService;

    /**
     * POST /v1/orders — Tạo đơn hàng mới
     */
    @PostMapping
    @Operation(summary = "Create order", description = "Create a new order for the authenticated customer.")
    public ResponseEntity<ApiResponse<CreateOrderResponse>> createOrder(
            HttpServletRequest request,
            @Valid @RequestBody CreateOrderRequest body) {

        String token = jwtService.getTokenFromRequest(request);
        UUID customerId = UUID.fromString(jwtService.getUid(token));

        CreateOrderResponse result = orderService.createOrder(body, customerId);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<CreateOrderResponse>builder()
                        .code(201)
                        .message(CommonErrorCode.CREATED.getMessage())
                        .result(result)
                        .timestamp(Instant.now())
                        .path(request.getRequestURI())
                        .build());
    }

    /**
     * POST /v1/orders/{id}/payments/confirm — Gateway callback: confirm or fail a pending payment
     */
    @PostMapping("/{id}/payments/confirm")
    @Operation(summary = "Confirm payment", description = "Gateway callback to confirm or fail a pending payment for an order.")
    public ResponseEntity<ApiResponse<PaymentResponse>> confirmPayment(
            HttpServletRequest request,
            @PathVariable UUID id,
            @RequestParam UUID paymentId,
            @RequestParam boolean success) {

        String token = jwtService.getTokenFromRequest(request);
        UUID customerId = UUID.fromString(jwtService.getUid(token));

        PaymentResponse result = orderService.confirmPayment(id, paymentId, success, customerId);

        return ResponseEntity.ok()
                .body(ApiResponse.<PaymentResponse>builder()
                        .code(200)
                        .message(success ? "Payment confirmed successfully" : "Payment failed")
                        .result(result)
                        .timestamp(Instant.now())
                        .path(request.getRequestURI())
                        .build());
    }

    /**
     * POST /v1/orders/{id}/payments — Thanh toán đơn hàng
     */
    @PostMapping("/{id}/payments")
    @Operation(summary = "Process payment", description = "Initiate payment for an order. Returns a payment URL for gateway-based methods.")
    public ResponseEntity<ApiResponse<PaymentResponse>> processPayment(
            HttpServletRequest request,
            @PathVariable UUID id,
            @Valid @RequestBody PaymentRequest body) {

        String token = jwtService.getTokenFromRequest(request);
        UUID customerId = UUID.fromString(jwtService.getUid(token));
        String ipAddress = request.getRemoteAddr();

        PaymentResponse result = orderService.processPayment(id, body, customerId, ipAddress);

        return ResponseEntity.ok()
                .body(ApiResponse.<PaymentResponse>builder()
                        .code(200)
                        .message(CommonErrorCode.SUCCESS.getMessage())
                        .result(result)
                        .timestamp(Instant.now())
                        .path(request.getRequestURI())
                        .build());
    }

    /**
     * GET /v1/orders/statuses — Returns all possible order status values.
     */
    @GetMapping("/statuses")
    @Operation(summary = "Get order statuses", description = "Returns all possible order status enum values.")
    public ResponseEntity<ApiResponse<List<OrderStatus>>> getOrderStatuses(HttpServletRequest request) {
        List<OrderStatus> statuses = orderService.getOrderStatuses();
        return ResponseEntity.ok(ApiResponse.<List<OrderStatus>>builder()
                .code(200)
                .message("Get order statuses successfully")
                .result(statuses)
                .timestamp(Instant.now())
                .path(request.getRequestURI())
                .build());
    }

    @GetMapping("/test")
    @Operation(summary = "Token test", description = "Returns the authenticated user's UUID — for debugging only.")
    public String test(HttpServletRequest request) {
        String token = jwtService.getTokenFromRequest(request);
        UUID userId = UUID.fromString(jwtService.getUid(token));
        return userId.toString();
    }
    @PatchMapping("/{id}/cancel")
    @Operation(summary = "Cancel order", description = "Cancel a pending order. Only the owning customer or an admin may cancel.")
    public ResponseEntity<ApiResponse<OrderCancelResponse>> cancelOrder(HttpServletRequest request, @PathVariable("id") UUID id) {
        String token = jwtService.getTokenFromRequest(request);
        UUID userId = UUID.fromString(jwtService.getUid(token));

        OrderCancelResponse OrderCancelResponse = orderService.cancelOrder(id, userId);
        return ResponseEntity.ok()
                .body(ApiResponse.<OrderCancelResponse>builder()
                        .code(200)
                        .message(CommonErrorCode.SUCCESS.getMessage())
                        .result(OrderCancelResponse)
                        .timestamp(Instant.now())
                        .path(request.getRequestURI())
                        .build());
    }

    /**
     * GET /v1/orders/{id}/status — Customer chỉ xem được order của mình; FRANCHISE_ADMIN và STORE_MANAGER xem được mọi order.
     */
    @GetMapping("/{id}/status")
    @Operation(summary = "Get order status", description = "Returns the current status of an order. Customers may only see their own orders.")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'FRANCHISE_ADMIN', 'STORE_MANAGER')")
    public ResponseEntity<ApiResponse<OrderStatusResponse>> getOrderStatus(
            HttpServletRequest request,
            @PathVariable("id") UUID id) {
        String token = jwtService.getTokenFromRequest(request);
        UUID userId = UUID.fromString(jwtService.getUid(token));
        String role = jwtService.getPrimaryRole(token);
        OrderStatusResponse result = orderService.getStatus(id, userId, role);
        return ResponseEntity.ok(ApiResponse.<OrderStatusResponse>builder()
                .code(200)
                .message("Get order status successfully")
                .result(result)
                .timestamp(Instant.now())
                .path(request.getRequestURI())
                .build());
    }

    /**
     * GET /v1/orders/me — Order history for the authenticated customer (their own orders only).
     */
    @GetMapping("/me")
    @Operation(summary = "Get my orders", description = "Paginated order history for the authenticated customer.")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<OrderHistoryPage>> getMyOrders(
            HttpServletRequest request,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        String token = jwtService.getTokenFromRequest(request);
        UUID customerId = UUID.fromString(jwtService.getUid(token));
        OrderHistoryPage result = orderService.getMyOrders(customerId, page, size);
        return ResponseEntity.ok(ApiResponse.<OrderHistoryPage>builder()
                .code(200)
                .message("Get my orders successfully")
                .result(result)
                .timestamp(Instant.now())
                .path(request.getRequestURI())
                .build());
    }

    /**
     * GET /v1/orders — Lịch sử đơn hàng. Chỉ FRANCHISE_ADMIN và STORE_MANAGER được gọi.
     */
    @GetMapping
    @Operation(summary = "Get all orders", description = "Paginated, filterable order history for admins. FRANCHISE_ADMIN and STORE_MANAGER only.")
    @PreAuthorize("hasAnyRole('FRANCHISE_ADMIN', 'STORE_MANAGER')")
    public ResponseEntity<ApiResponse<OrderHistoryPage>> getOrderHistory(
            HttpServletRequest request,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long branchId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        String token = jwtService.getTokenFromRequest(request);
        String role = jwtService.getPrimaryRole(token);
        OrderHistoryPage result = orderService.getOrderHistory(
                page, size,
                Optional.ofNullable(status).filter(s -> s != null && !s.isBlank()),
                Optional.ofNullable(branchId),
                Optional.ofNullable(fromDate),
                Optional.ofNullable(toDate),
                role);
        return ResponseEntity.ok(ApiResponse.<OrderHistoryPage>builder()
                .code(200)
                .message("Get order list successfully")
                .result(result)
                .timestamp(Instant.now())
                .path(request.getRequestURI())
                .build());
    }
    @GetMapping("/{id}")
    @Operation(summary = "Get order detail", description = "Full order detail including items, payment, and status. Customers may only view their own orders.")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'STORE_MANAGER', 'FRANCHISE_ADMIN')")
    public ResponseEntity<ApiResponse<OrderDetailResponse>> getOrderDetail(
            @PathVariable String id,
            HttpServletRequest request) {

                String token = jwtService.getTokenFromRequest(request);
                UUID userId = UUID.fromString(jwtService.getUid(token));
                String role = jwtService.getPrimaryRole(token);

                OrderDetailResponse data = orderService.getOrderDetail(id, userId, role);
                return ResponseEntity.ok(
                                ApiResponse.<OrderDetailResponse>builder()
                                                .code(1000)
                                                .message("Get order detail successfully")
                                                .result(data)
                                                .build());
        }

    /**
     * POST /v1/orders/refund — Create refund request
     */
    @PostMapping("/refund")
    @Operation(summary = "Create refund request", description = "Submit a refund request for a paid order.")
    public ResponseEntity<ApiResponse<RefundResponse>> createRefund(
            HttpServletRequest request,
            @Valid @RequestBody RefundRequest requestDTO) {
        String token = jwtService.getTokenFromRequest(request);
        UUID customerId = UUID.fromString(jwtService.getUid(token));

        RefundResponse result = refundService.createRefundRequest(requestDTO, customerId);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<RefundResponse>builder()
                        .code(201)
                        .message(CommonErrorCode.CREATED.getMessage())
                        .result(result)
                        .timestamp(Instant.now())
                        .path(request.getRequestURI())
                        .build());
    }

}
