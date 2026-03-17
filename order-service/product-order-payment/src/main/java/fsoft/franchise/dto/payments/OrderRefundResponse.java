package fsoft.franchise.dto.payments;

import com.fasterxml.jackson.annotation.JsonFormat;
import fsoft.franchise.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response cho API POST /v1/orders/{order_id}/refund.
 * Theo tài liệu: orderId, status REFUNDED, refundAmount, refundTime.
 */
public record OrderRefundResponse(
        UUID orderId,
        OrderStatus status,
        BigDecimal refundAmount,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime refundTime) {
}
