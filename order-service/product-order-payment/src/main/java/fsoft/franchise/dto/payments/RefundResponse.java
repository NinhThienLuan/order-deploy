package fsoft.franchise.dto.payments;

import fsoft.franchise.enums.RefundStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record RefundResponse(
        UUID refundId,
        UUID orderId,
        String orderNumber,
        BigDecimal amount,
        String reason,
        RefundStatus status,
        String declineReason,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
