package fsoft.franchise.dto.orders;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class EstimateResponse {
    private UUID storeId; // Type Long matches database
    private int estimatedMinutes;
    private int activeOrderCount;
    private int itemCount;
    private LocalDateTime calculatedAt;
}
