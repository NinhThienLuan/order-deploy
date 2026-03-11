package fsoft.franchise.dto.products;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Lightweight product card shown in list / grid views.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductSummaryResponse {
    private UUID id;
    private String name;
    private String description;
    private String type;
    private UUID categoryId;
    private String categoryName;
    /** URL of the primary image (null if none). */
    private String primaryImageUrl;
    /** Lowest active-variant price; null if no active variants exist. */
    private BigDecimal basePrice;
    private Boolean active;
}
