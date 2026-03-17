package fsoft.franchise.dto.products;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * A single size variant of a product.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductVariantResponse {
    private UUID id;
    private String sizeName;
    private BigDecimal price;
    private Boolean active;
    private java.util.List<VariantIngredientResponse> ingredients;
}
