package fsoft.franchise.dto.products;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class VariantIngredientRequest {

    @NotNull(message = "Ingredient ID is required")
    private UUID ingredientId;

    @NotNull(message = "Quantity is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Quantity must be greater than 0")
    private BigDecimal quantity;

    @NotNull(message = "Unit is required")
    private String unit;
}
