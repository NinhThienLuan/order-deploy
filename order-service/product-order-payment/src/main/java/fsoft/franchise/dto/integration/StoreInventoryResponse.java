package fsoft.franchise.dto.integration;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Maps each item in the result list from GET /api/v1/store-inventories/store/{storeId}
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record StoreInventoryResponse(
        UUID id,
        UUID storeId,
        UUID ingredientId,
        BigDecimal quantity,
        BigDecimal minThreshold,
        LocalDateTime lastUpdatedAt
) {}
