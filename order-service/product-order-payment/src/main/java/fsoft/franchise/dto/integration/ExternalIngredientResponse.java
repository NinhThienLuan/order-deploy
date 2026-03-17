package fsoft.franchise.dto.integration;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.UUID;

/**
 * Maps each item in the ingredient master list from external service.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ExternalIngredientResponse(
        UUID id,
        String name,
        String description
) {}
