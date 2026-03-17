package fsoft.franchise.dto.integration;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Maps the result field from GET /api/v1/franchise-stores/{id}
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record FranchiseStoreResponse(
        UUID id,
        UUID requestId,
        UUID managerId,
        String storeName,
        String address,
        String status,
        LocalDate openDate,
        LocalDateTime createdAt
) {}
