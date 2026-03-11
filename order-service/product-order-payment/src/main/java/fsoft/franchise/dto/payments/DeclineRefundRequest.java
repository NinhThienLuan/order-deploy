package fsoft.franchise.dto.payments;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DeclineRefundRequest(
        @NotBlank(message = "Decline reason is required")
        @Size(max = 256, message = "Decline reason must not exceed 256 characters")
        String declineReason
) {
}

