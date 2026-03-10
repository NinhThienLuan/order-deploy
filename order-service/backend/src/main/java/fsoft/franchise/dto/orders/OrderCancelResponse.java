package fsoft.franchise.dto.orders;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;
import java.util.UUID;

public record OrderCancelResponse(
        @NotBlank UUID orderId,
        @NotBlank LocalDateTime canceledAt
) {
}
