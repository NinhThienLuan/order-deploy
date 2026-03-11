package fsoft.franchise.dto.products;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.UUID;

/**
 * Request DTO for creating or updating a category.
 */
@Data
public class CategoryRequest {

    @NotBlank(message = "Category name must not be blank")
    @Size(max = 120, message = "Category name must be 120 characters or fewer")
    private String name;

    @Size(max = 500, message = "Description must be 500 characters or fewer")
    private String description;

    private UUID parentId;
}
