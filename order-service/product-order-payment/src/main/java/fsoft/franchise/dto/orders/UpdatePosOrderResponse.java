package fsoft.franchise.dto.orders;

import fsoft.franchise.enums.OrderStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
public record UpdatePosOrderResponse(
        UUID orderId,
        OrderStatus status,
        LocalDateTime orderTime,
        BigDecimal totalAmount,
        List<OrderItemResponse> items,
        String note,
        LocalDateTime updatedAt
) {
        public record OrderItemResponse(
            UUID productId,
            String productName,
            String variantName,
            Integer quantity,
            BigDecimal unitPrice,
            BigDecimal subtotal
    ) {
    }
}
