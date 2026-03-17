package fsoft.franchise.dto.orders;

import fsoft.franchise.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record CreatePosOrderResponse(
        UUID orderId,
        OrderStatus status,
        LocalDateTime orderTime,
        BigDecimal totalAmount,
        List<OrderItemResponse> items,
        String note) {
    public record OrderItemResponse(
            UUID productId,
            String productName,
            String variantName,
            UUID variantId,
            Integer quantity,
            BigDecimal unitPrice,
            BigDecimal subtotal) {
    }
}
