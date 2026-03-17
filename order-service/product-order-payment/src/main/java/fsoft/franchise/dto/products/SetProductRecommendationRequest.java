package fsoft.franchise.dto.products;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SetProductRecommendationRequest {
    @NotNull(message = "isRecommended is required")
    private Boolean isRecommended;
}

