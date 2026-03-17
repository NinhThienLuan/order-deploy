package fsoft.franchise.dto.products;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ProductVariantRequest {

    @NotBlank(message = "Size name is required")
    @Size(max = 50, message = "Size name must be 50 characters or fewer")
    private String sizeName;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    private BigDecimal price;

    private boolean active;

    @Valid
    private List<VariantIngredientRequest> ingredients;
}
