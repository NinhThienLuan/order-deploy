package fsoft.franchise.dto.payments;

import com.fasterxml.jackson.annotation.JsonFormat;
import fsoft.franchise.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO gửi qua WebSocket khi trạng thái đơn hàng thay đổi.
 *
 * <p>
 * Client subscribe "/topic/orders/{orderId}" sẽ nhận message dạng JSON:
 * </p>
 * 
 * <pre>
 * {
 *   "orderId": "550e8400-e29b-41d4-a716-446655440000",
 *   "previousStatus": "PAID",
 *   "currentStatus": "PREPARING",
 *   "message": "Đơn hàng đang được chuẩn bị",
 *   "updatedAt": "2026-03-04T14:30:00",
 *   "updatedBy": "admin-uuid"
 * }
 * </pre>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderTrackingDTO {

    /** ID đơn hàng */
    private UUID orderId;

    /** Trạng thái trước khi thay đổi */
    private OrderStatus previousStatus;

    /** Trạng thái mới */
    private OrderStatus currentStatus;

    /** Message mô tả (VD: "Đơn hàng đang được chuẩn bị") */
    private String message;

    /** Thời điểm cập nhật */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    /** ID người cập nhật (admin/store manager) */
    private UUID updatedBy;
}
