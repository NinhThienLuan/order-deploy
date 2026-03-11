package fsoft.franchise.service;

import fsoft.franchise.dto.products.ProductVariantRequest;
import fsoft.franchise.dto.products.ProductVariantResponse;

import java.util.List;
import java.util.UUID;

public interface ProductVariantService {

    List<ProductVariantResponse> getVariantsByProductId(UUID productId);

    ProductVariantResponse createVariant(UUID productId, ProductVariantRequest request);

    ProductVariantResponse updateVariant(UUID productId, UUID variantId, ProductVariantRequest request);

    void deleteVariant(UUID productId, UUID variantId);
}
