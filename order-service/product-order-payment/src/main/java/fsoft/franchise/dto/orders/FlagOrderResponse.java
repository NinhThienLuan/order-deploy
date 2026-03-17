package fsoft.franchise.dto.orders;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class FlagOrderResponse {
    private UUID orderId;
    private String orderNumber;
    private Boolean isFlagged;
    private String flagReason;
    private UUID flaggedBy;
    private LocalDateTime flaggedAt;
    private String currentStatus;
}
