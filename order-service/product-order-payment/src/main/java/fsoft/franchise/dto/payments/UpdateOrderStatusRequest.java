package fsoft.franchise.dto.payments;

import fsoft.franchise.enums.OrderStatus;
import jakarta.validation.constraints.NotNull;

/**
 * Request body cho API cập nhật trạng thái đơn hàng (Admin/Store Manager).
 *
 * <p>
 * Ví dụ request:
 * </p>
 * 
 * <pre>
 * PUT /v1/orders/{order_id}/status
 * {
 *   "status": "PREPARING"
 * }
 * </pre>
 *
 * <p>
 * Luồng hợp lệ: PAID → PREPARING → READY → COMPLETED
 * </p>
 */
public record UpdateOrderStatusRequest(
        @NotNull(message = "Status is required") OrderStatus status) {
}
