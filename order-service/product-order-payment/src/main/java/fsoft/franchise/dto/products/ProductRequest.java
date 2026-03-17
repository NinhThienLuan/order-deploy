package fsoft.franchise.dto.products;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ProductRequest {

    @NotBlank(message = "Product name is required")
    @Size(max = 50, message = "Product name must be 50 characters or fewer")
    private String name;

    @Size(max = 500, message = "Product description must be 500 characters or fewer")
    private String description;

    private java.util.UUID categoryId;

    private fsoft.franchise.enums.ProductType type;

    private boolean active;
}
