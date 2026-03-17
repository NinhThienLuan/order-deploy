package fsoft.franchise.dto.products;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VariantIngredientResponse {
    private UUID ingredientId;
    private String name;
    private BigDecimal quantity;
    private String unit;
}
