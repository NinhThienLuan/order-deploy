package fsoft.franchise.dto.payments;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

public record RefundRequest(
        @NotNull(message = "Order ID is required")
        UUID orderId,

        @NotNull(message = "Refund amount is required")
        @Positive(message = "Refund amount must be positive")
        BigDecimal amount,

        @NotBlank(message = "Reason is required")
        @Size(max = 256, message = "Reason must not exceed 256 characters")
        String reason
) {
}

