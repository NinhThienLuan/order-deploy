package fsoft.franchise.dto.orders;


import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Builder;
import java.util.List;
import java.util.UUID; // Cần thiết vì hệ thống dùng UUID
@Builder
public record UpdatePosOrderRequest(
        @Valid
        @NotEmpty(message = "Order items must not be empty")
        List<OrderItemRequest> items,
        @Size(max = 500, message = "Note must not exceed 500 characters")
        String note
) {
    @Builder
    public record OrderItemRequest(
            @NotNull(message = "productId is required")
            UUID productId, // Sửa từ Long sang UUID
            UUID variantId, // Sửa từ Long sang UUID
            @NotNull(message = "quantity is required")
            @Min(value = 1, message = "quantity must be at least 1")
            Integer quantity
    ) {}
}
