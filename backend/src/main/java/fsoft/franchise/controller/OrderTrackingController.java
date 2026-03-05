package fsoft.franchise.controller;

import fsoft.franchise.common.response.ApiResponse;
import fsoft.franchise.security.JwtService;
import fsoft.franchise.dto.payments.UpdateOrderStatusRequest;
import fsoft.franchise.dto.payments.OrderTrackingTimelineDTO;
import fsoft.franchise.service.OrderTrackingService;
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
 * <li><b>PATCH /v1/orders/{id}/status</b> — Admin/Store Manager cập nhật trạng
 * thái
 * → tự động push real-time qua WebSocket</li>
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
public class OrderTrackingController {

    private final OrderTrackingService orderTrackingService;
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
    @PreAuthorize("hasAnyRole('CUSTOMER', 'FRANCHISE_ADMIN', 'STORE_MANAGER')")
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
     * PATCH /v1/orders/{id}/status — Admin cập nhật trạng thái đơn hàng.
     *
     * <p>
     * Khi gọi API này → server sẽ:
     * </p>
     * <ol>
     * <li>Validate transition (VD: PAID → PREPARING ✓, PAID → COMPLETED ✗)</li>
     * <li>Update DB</li>
     * <li>Lưu vào bảng history (để timeline có data)</li>
     * <li>Push WebSocket message → khách hàng thấy ngay</li>
     * </ol>
     */
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('FRANCHISE_ADMIN', 'STORE_MANAGER')")
    public ResponseEntity<ApiResponse<String>> updateOrderStatus(
            HttpServletRequest request,
            @PathVariable("id") UUID id,
            @Valid @RequestBody UpdateOrderStatusRequest body) {

        String token = jwtService.getTokenFromRequest(request);
        UUID updatedBy = UUID.fromString(jwtService.getUid(token));

        orderTrackingService.updateOrderStatus(id, body.status(), updatedBy);

        return ResponseEntity.ok(
                ApiResponse.<String>builder()
                        .code(200)
                        .message("Order status updated successfully")
                        .result("Order " + id + " status changed to " + body.status())
                        .timestamp(Instant.now())
                        .path(request.getRequestURI())
                        .build());
    }
}
