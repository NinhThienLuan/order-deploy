package fsoft.franchise.dto.payments;

import com.fasterxml.jackson.annotation.JsonFormat;
import fsoft.franchise.enums.OrderStatus;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response body cho API PUT /v1/orders/{order_id}/status.
 *
 * <p>
 * Theo tài liệu: FRANCHISE_ADMIN, STORE_MANAGER cập nhật trạng thái đơn hàng.
 * </p>
 */
public record UpdateOrderStatusResponse(
        UUID orderId,
        OrderStatus status,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime lastUpdated) {
}
