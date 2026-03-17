package fsoft.franchise.controller;

import fsoft.franchise.common.response.ApiResponse;
import fsoft.franchise.security.JwtService;
import fsoft.franchise.dto.payments.OrderRefundResponse;
import fsoft.franchise.dto.payments.UpdateOrderStatusRequest;
import fsoft.franchise.dto.payments.UpdateOrderStatusResponse;
import fsoft.franchise.dto.payments.OrderTrackingTimelineDTO;
import fsoft.franchise.service.OrderTrackingService;
import fsoft.franchise.service.RefundService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.UUID;

/**
 * Controller cho order tracking — 2 API:
 *
 * <ol>
 * <li><b>GET /v1/orders/{id}/tracking</b> — Khách hàng xem timeline tracking
 * (giống hình tracking DHL/Shopee)</li>
 * <li><b>PUT /v1/orders/{order_id}/status</b> —
 * FRANCHISE_ADMIN/STORE_MANAGER/POS cập nhật trạng
 * thái (CUSTOMER không dùng được) → tự động push real-time qua WebSocket</li>
 * <li><b>POST /v1/orders/{order_id}/refund</b> — FRANCHISE_ADMIN/STORE_MANAGER
 * thực hiện hoàn tiền đơn hàng</li>
 * </ol>
 *
 * <pre>
 *  Phía NHÂN VIÊN:                       Phía KHÁCH HÀNG:
 *  PATCH {"status":"PREPARING"}           GET /tracking → timeline
 *       │                                      │
 *       ▼                                      ▼
 *  Update DB + lưu history        ✅ Ordered    — 10:00
 *       │                         ✅ Paid       — 10:05
 *       │── push WebSocket ──▶    ⏳ Preparing  — 10:15 ← NOW
 *                                 ○  Ready
 *                                 ○  Completed
 * </pre>
 */
@RestController
@RequestMapping("/v1/orders")
@RequiredArgsConstructor
@Tag(name = "order-tracking-controller", description = "Order tracking timeline and manual status/refund actions. Permission varies by endpoint (USER/ADMIN/MANAGER as documented per API).")
public class OrderTrackingController {

        private final OrderTrackingService orderTrackingService;
        private final RefundService refundService;
        private final JwtService jwtService;

        // ======================== CUSTOMER API ========================

        /**
         * GET /v1/orders/{id}/tracking — Timeline tracking cho khách hàng.
         *
         * <p>
         * Trả về danh sách các bước + bước nào ✅ completed, ⏳ current, ○ chưa tới.
         * </p>
         * <p>
         * Customer chỉ xem được đơn của mình; Admin/Store Manager xem được mọi đơn.
         * </p>
         */
        @GetMapping("/{id}/tracking")
        @Operation(summary = "Get order tracking", description = "Get order tracking timeline. Permission: USER, ADMIN, MANAGER (USER can access own orders only).")
        @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'MANAGER', 'POS')")
        public ResponseEntity<ApiResponse<OrderTrackingTimelineDTO>> getTrackingTimeline(
                        HttpServletRequest request,
                        @PathVariable("id") UUID id) {

                String token = jwtService.getTokenFromRequest(request);
                UUID userId = UUID.fromString(jwtService.getUid(token));
                String role = jwtService.getPrimaryRole(token);

                OrderTrackingTimelineDTO timeline = orderTrackingService.getTrackingTimeline(id, userId, role);

                return ResponseEntity.ok(
                                ApiResponse.<OrderTrackingTimelineDTO>builder()
                                                .code(200)
                                                .message("Get order tracking timeline successfully")
                                                .result(timeline)
                                                .timestamp(Instant.now())
                                                .path(request.getRequestURI())
                                                .build());
        }

        // ======================== ADMIN API ========================

        /**
         * PUT /v1/orders/{order_id}/status — FRANCHISE_ADMIN, STORE_MANAGER hoặc POS
         * cập nhật trạng thái đơn hàng.
         * CUSTOMER không được dùng API này.
         *
         * <p>
         * Khi gọi API này → server sẽ:
         * </p>
         * <ol>
         * <li>Validate transition theo OrderStatus (VD: PAID → PREPARING ✓, PAID →
         * COMPLETED ✗)</li>
         * <li>Update DB</li>
         * <li>Lưu vào bảng history (để timeline có data)</li>
         * <li>Push WebSocket message → khách hàng thấy ngay</li>
         * </ol>
         */
        @PutMapping("/{order_id}/status")
        @Operation(summary = "Update order status", description = "Update order status and push realtime tracking update. Permission: ADMIN, MANAGER.")
        @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'POS')")
        public ResponseEntity<ApiResponse<UpdateOrderStatusResponse>> updateOrderStatus(
                        HttpServletRequest request,
                        @PathVariable("order_id") UUID orderId,
                        @Valid @RequestBody UpdateOrderStatusRequest body) {

                String token = jwtService.getTokenFromRequest(request);
                UUID updatedBy = UUID.fromString(jwtService.getUid(token));

                UpdateOrderStatusResponse result = orderTrackingService.updateOrderStatus(orderId, body.status(),
                                updatedBy);

                return ResponseEntity.ok(
                                ApiResponse.<UpdateOrderStatusResponse>builder()
                                                .code(200)
                                                .message("Order status updated successfully")
                                                .result(result)
                                                .timestamp(Instant.now())
                                                .path(request.getRequestURI())
                                                .build());
        }

        /**
         * POST /v1/orders/{order_id}/refund — FRANCHISE_ADMIN hoặc STORE_MANAGER thực
         * hiện hoàn tiền đơn hàng.
         * Đơn phải ở trạng thái PAID, COMPLETED hoặc READY; không được đã refund trước
         * đó.
         */
        @PostMapping("/{order_id}/refund")
        @Operation(summary = "Refund order", description = "Trigger order refund flow for eligible orders. Permission: ADMIN, MANAGER.")
        @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
        public ResponseEntity<ApiResponse<OrderRefundResponse>> refundOrder(
                        HttpServletRequest request,
                        @PathVariable("order_id") UUID orderId) {

                String token = jwtService.getTokenFromRequest(request);
                UUID performedBy = UUID.fromString(jwtService.getUid(token));

                OrderRefundResponse result = refundService.processOrderRefund(orderId, performedBy);

                return ResponseEntity.ok(
                                ApiResponse.<OrderRefundResponse>builder()
                                                .code(200)
                                                .message("Order refunded successfully")
                                                .result(result)
                                                .timestamp(Instant.now())
                                                .path(request.getRequestURI())
                                                .build());
        }
}
