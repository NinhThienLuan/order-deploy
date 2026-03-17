package fsoft.franchise.service;

import fsoft.franchise.dto.products.ProductRequest;
import fsoft.franchise.dto.products.ProductDetailResponse;
import fsoft.franchise.dto.products.ProductSummaryResponse;
import fsoft.franchise.entity.ProductEntity;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.UUID;

/**
 * Public contract for product queries (read-only) + admin write operations.
 */
public interface ProductService {

    /**
     * Paginated product list with optional filters.
     */
    Page<ProductSummaryResponse> getProducts(int page, int size, UUID categoryId, String search, fsoft.franchise.enums.ProductType type, Boolean active);

    /**
     * Full product detail including variants and images.
     */
    ProductDetailResponse getProductById(UUID id);

    List<ProductSummaryResponse> getRecommended();

    // ── Admin write operations ───────────────────────────────────────────────

    ProductDetailResponse createProduct(ProductRequest request);

    ProductDetailResponse updateProduct(UUID id, ProductRequest request);

    void deleteProduct(UUID id);

    ProductDetailResponse toggleActive(UUID id);

    ProductDetailResponse setRecommended(UUID id, boolean isRecommended);

    /**
     * Internal helper used by variant/image services. Returns a non-deleted
     * ProductEntity or throws.
     */
    ProductEntity getActiveProductOrThrow(UUID id);
}
