package fsoft.franchise.dto.orders;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record CreatePosOrderRequest(
        @NotEmpty(message = "Order items must not be empty")
        @Valid List<CreateOrderRequest.OrderItemRequest> items,
        @NotNull(message = "Store ID is required")
        UUID storeId,
        UUID customerId,   // optional
        String recipientName,
        String recipientPhone,
        String note) {
}
