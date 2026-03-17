package fsoft.franchise.controller;

import fsoft.franchise.common.response.ApiResponse;
import fsoft.franchise.dto.orders.*;
import fsoft.franchise.common.exception.ApiException;
import fsoft.franchise.exception.CommonErrorCode;
import fsoft.franchise.security.JwtService;
import fsoft.franchise.dto.payments.RefundRequest;
import fsoft.franchise.dto.payments.RefundResponse;
import fsoft.franchise.service.OrderService;
import fsoft.franchise.service.RefundService;
import fsoft.franchise.client.FranchiseClient;
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
@Tag(name = "Orders", description = "Order creation, status, history, payment processing, and refund requests. Permission varies by endpoint (USER/ADMIN/MANAGER as documented per API).")
public class OrderController {

        private final OrderService orderService;
        private final RefundService refundService;
        private final JwtService jwtService;
        private final FranchiseClient franchiseClient;

        /**
         * POST /v1/orders — Tạo đơn hàng mới
         */
        @PostMapping
        @Operation(summary = "Create order", description = "Create a new order for the authenticated user. Permission: USER.")
        public ResponseEntity<ApiResponse<CreateOrderResponse>> createOrder(
                        HttpServletRequest request,
                        @Valid @RequestBody CreateOrderRequest body) {

                String token = jwtService.getTokenFromRequest(request);
                UUID customerId = jwtService.getUserId(token);
                if (customerId == null) {
                        throw new ApiException(CommonErrorCode.UNAUTHORIZED);
                }

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
         * ── API 1: LÊN ĐƠN POS ──
         */
        @PostMapping("/pos")
        @Operation(summary = "Create POS order", description = "Create an order via POS interface")
        @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'POS')")
        public ResponseEntity<ApiResponse<CreatePosOrderResponse>> createPosOrder(
                        HttpServletRequest request,
                        @Valid @RequestBody CreatePosOrderRequest body) {

                String token = jwtService.getTokenFromRequest(request);
                UUID userId = UUID.fromString(jwtService.getUid(token));

                CreatePosOrderResponse result = orderService.createPosOrder(body, userId);
                return ResponseEntity.status(HttpStatus.CREATED)
                                .body(ApiResponse.<CreatePosOrderResponse>builder()
                                                .code(201)
                                                .message(CommonErrorCode.CREATED.getMessage())
                                                .result(result)
                                                .timestamp(Instant.now())
                                                .path(request.getRequestURI())
                                                .build());
        }

    @PatchMapping("/pos/{id}")
    @Operation(summary = "Update POS order", description = "Update an order via POS interface")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'POS')")
    public ResponseEntity<ApiResponse<UpdatePosOrderResponse>> updatePosOrder(
            @PathVariable UUID id,
            HttpServletRequest httpRequest,
            @RequestBody @Valid UpdatePosOrderRequest body) {

        String token = jwtService.getTokenFromRequest(httpRequest);
        UUID userId = UUID.fromString(jwtService.getUid(token));

        UpdatePosOrderResponse result = orderService.updatePosOrder(id, body, userId);

        return ResponseEntity.ok(
                ApiResponse.<UpdatePosOrderResponse>builder()
                        .code(200)
                        .message(CommonErrorCode.SUCCESS.getMessage())
                        .result(result)
                        .timestamp(Instant.now())
                        .path(httpRequest.getRequestURI())
                        .build());
    }

        /**
         * ── API 2: ESTIMATE TIME ──
         */
        @GetMapping("/estimate")
        @Operation(summary = "Estimate Preparation Time", description = "Gets estimated time based on queue")
        public ResponseEntity<ApiResponse<EstimateResponse>> estimatePreparationTime(
                        HttpServletRequest request,
                        @RequestParam UUID storeId,
                        @RequestParam int itemCount) {

                EstimateResponse data = orderService.estimatePreparationTime(storeId, itemCount);
                return ResponseEntity.ok(ApiResponse.<EstimateResponse>builder()
                                .code(200)
                                .message(CommonErrorCode.SUCCESS.getMessage())
                                .result(data)
                                .timestamp(Instant.now())
                                .path(request.getRequestURI())
                                .build());
        }

        /**
         * ── API 3: FLAG ORDER ──
         */
        @PatchMapping("/{id}/flag")
        @Operation(summary = "Flag an order", description = "Flags an active order that needs attention")
        @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
        public ResponseEntity<ApiResponse<FlagOrderResponse>> flagOrder(
                        HttpServletRequest request,
                        @PathVariable("id") UUID id,
                        @Valid @RequestBody FlagOrderRequest body,
                        // Dùng tạm custom header / Lấy storeId dựa trên profile tài khoản thực tế của
                        // bạn
                        @RequestHeader("X-Store-Id") Long currentUserStoreId) {

                String token = jwtService.getTokenFromRequest(request);
                UUID currentUserId = UUID.fromString(jwtService.getUid(token));
                String role = jwtService.getPrimaryRole(token);

                FlagOrderResponse data = orderService.flagOrder(id, body, currentUserId, role, currentUserStoreId);
                return ResponseEntity.ok(ApiResponse.<FlagOrderResponse>builder()
                                .code(200)
                                .message("Order flagged successfully")
                                .result(data)
                                .timestamp(Instant.now())
                                .path(request.getRequestURI())
                                .build());
        }



        /**
         * GET /v1/orders/statuses — Returns all possible order status values.
         */
        @GetMapping("/statuses")
        @Operation(summary = "Get order statuses", description = "Returns all possible order status enum values. Permission: Public (no JWT required).")
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
        @Operation(summary = "Test endpoint", description = "Test endpoint to verify authentication and fetch franchise stores data from franchise service")
        public ResponseEntity<ApiResponse<?>> test(HttpServletRequest request) {
                String token = jwtService.getTokenFromRequest(request);
                UUID userId = jwtService.getUserId(token);
                if (userId == null) {
                        throw new ApiException(CommonErrorCode.UNAUTHORIZED);
                }
                
                // Call FranchiseClient to fetch franchise stores
                ApiResponse<?> franchiseData = franchiseClient.getAllStores();
                
                return ResponseEntity.ok(ApiResponse.builder()
                                .code(200)
                                .message("Test: Franchise stores fetched successfully")
                                .result(franchiseData.getResult())
                                .timestamp(Instant.now())
                                .path(request.getRequestURI())
                                .build());
        }

        @PatchMapping("/{id}/cancel")
        @Operation(summary = "Cancel order", description = "Cancel a pending order. Only the owning customer or an admin may cancel. Permission: Authenticated user with ownership/admin checks.")
        public ResponseEntity<ApiResponse<OrderCancelResponse>> cancelOrder(HttpServletRequest request,
                        @PathVariable("id") UUID id) {
                String token = jwtService.getTokenFromRequest(request);
                UUID userId = UUID.fromString(jwtService.getUid(token));
                String role = jwtService.getPrimaryRole(token);

                OrderCancelResponse OrderCancelResponse = orderService.cancelOrder(id, userId, role);
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
         * GET /v1/orders/{id}/status — Customer chỉ xem được order của mình;
         * ADMIN và MANAGER xem được mọi order.
         */
        @GetMapping("/{id}/status")
        @Operation(summary = "Get order status", description = "Returns the current status of an order. Users may only see their own orders. Permission: USER, ADMIN, MANAGER.")
        @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'MANAGER', 'POS')")
        public ResponseEntity<ApiResponse<OrderStatusResponse>> getOrderStatus(
                        HttpServletRequest request,
                        @PathVariable("id") UUID id) {
                String token = jwtService.getTokenFromRequest(request);
                UUID userId = jwtService.getUserId(token);
                if (userId == null) {
                        throw new ApiException(CommonErrorCode.UNAUTHORIZED);
                }
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
         * GET /v1/orders/me — Order history for the authenticated customer (their own
         * orders only).
         */
        @GetMapping("/me")
        @Operation(summary = "Get my orders", description = "Paginated order history for the authenticated user. Permission: Any authenticated user.")
        @PreAuthorize("isAuthenticated()")
        public ResponseEntity<ApiResponse<OrderHistoryPage>> getMyOrders(
                        HttpServletRequest request,
                        @RequestParam(name = "page", defaultValue = "1") int page,
                        @RequestParam(name = "size", defaultValue = "10") int size) {
                String token = jwtService.getTokenFromRequest(request);
                UUID customerId = jwtService.getUserId(token);
                if (customerId == null) {
                        throw new ApiException(CommonErrorCode.UNAUTHORIZED);
                }
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
         * GET /v1/orders — Lịch sử đơn hàng. Chỉ ADMIN và MANAGER được
         * gọi.
         */
        @GetMapping
        @Operation(summary = "Get all orders", description = "Paginated, filterable order history for admins. ADMIN and MANAGER only.")
        public ResponseEntity<ApiResponse<OrderHistoryPage>> getOrderHistory(
                        HttpServletRequest request,
                        @RequestParam(name = "page", defaultValue = "1") int page,
                        @RequestParam(name = "size", defaultValue = "10") int size,
                        @RequestParam(required = false) String status,
                        @RequestParam(required = false) UUID storeId,
                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
                String token = jwtService.getTokenFromRequest(request);
                String role = jwtService.getPrimaryRole(token);
                OrderHistoryPage result = orderService.getOrderHistory(
                                page, size,
                                Optional.ofNullable(status).filter(s -> s != null && !s.isBlank()),
                                Optional.ofNullable(storeId),
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
        @Operation(summary = "Get order detail", description = "Full order detail including items, payment, and status. Users may only view their own orders. Permission: USER, MANAGER, ADMIN.")
        @PreAuthorize("hasAnyRole('USER', 'MANAGER', 'ADMIN', 'POS')")
        public ResponseEntity<ApiResponse<OrderDetailResponse>> getOrderDetail(
                        @PathVariable("id") String id,
                        HttpServletRequest request) {

                String token = jwtService.getTokenFromRequest(request);
                UUID userId = jwtService.getUserId(token);
                if (userId == null) {
                        throw new ApiException(CommonErrorCode.UNAUTHORIZED);
                }
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
        @Operation(summary = "Create refund request", description = "Submit a refund request for a paid order. Permission: Authenticated customer/owner flow.")
        public ResponseEntity<ApiResponse<RefundResponse>> createRefund(
                        HttpServletRequest request,
                        @Valid @RequestBody RefundRequest requestDTO) {
                String token = jwtService.getTokenFromRequest(request);
                UUID customerId = jwtService.getUserId(token);
                if (customerId == null) {
                        throw new ApiException(CommonErrorCode.UNAUTHORIZED);
                }

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
