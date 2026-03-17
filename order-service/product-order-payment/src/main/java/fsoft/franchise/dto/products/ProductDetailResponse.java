package fsoft.franchise.dto.products;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

/**
 * Full product detail including all variants and images.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDetailResponse {
    private UUID id;
    private String name;
    private String description;
    private fsoft.franchise.enums.ProductType type;
    private UUID categoryId;
    private String categoryName;
    private Boolean active;
    private Boolean isRecommended;
    private List<ProductVariantResponse> variants;
    private List<ImageInfo> images;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ImageInfo {
        private java.util.UUID id;
        private String imageUrl;
        private Boolean isPrimary;
    }
}
