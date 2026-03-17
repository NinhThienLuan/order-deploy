package fsoft.franchise.dto.orders;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record CreateOrderRequest(
                @NotNull(message = "Store ID is required") UUID storeId,
                @NotEmpty(message = "Order items must not be empty") @Valid List<OrderItemRequest> items,
                @NotNull(message = "Delivery Address is required") String deliveryAddress,
                String recipientName,
                String recipientPhone,
                String note) {
        public record OrderItemRequest(
                        @NotNull(message = "Product ID is required") UUID productId,
                        UUID variantId,
                        @NotNull(message = "Quantity is required") @Min(value = 1, message = "Quantity must be at least 1") Integer quantity) {
        }
}