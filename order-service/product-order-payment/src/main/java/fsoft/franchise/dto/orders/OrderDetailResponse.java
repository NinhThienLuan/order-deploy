package fsoft.franchise.dto.orders;

import fsoft.franchise.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDetailResponse {
    private UUID orderId;
    private UUID storeId;
    private String orderNumber;
    private OrderStatus status;
    private String orderType;
    private LocalDateTime orderTime;
    private UUID customerId;
    private List<OrderItemInfo> items;
    private PricingInfo pricing;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String note;
    private String deliveryAddress;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemInfo {
        private String productId;
        private String productName;
        private int quantity;
        private String variantName;
        private UUID variantId;
        private BigDecimal unitPrice;
        private BigDecimal subtotal;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PricingInfo {
        private BigDecimal subtotal;
        private BigDecimal discount;
        private BigDecimal totalAmount;
    }
}
