package fsoft.franchise.serviceImpl;

import fsoft.franchise.common.exception.ApiException;
import fsoft.franchise.exception.ProductErrorCode;
import fsoft.franchise.dto.products.ProductRequest;
import fsoft.franchise.dto.products.ProductDetailResponse;
import fsoft.franchise.dto.products.ProductSummaryResponse;
import fsoft.franchise.dto.products.ProductVariantResponse;
import fsoft.franchise.entity.CategoryEntity;
import fsoft.franchise.entity.ProductEntity;
import fsoft.franchise.entity.ProductImageEntity;
import fsoft.franchise.entity.ProductVariantEntity;
import fsoft.franchise.repository.CategoryRepository;
import fsoft.franchise.repository.ProductRepository;
import fsoft.franchise.service.ProductService;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/**
 * Product queries (read + admin write) backed by JPA Specifications for dynamic
 * filtering.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductServiceImpl implements ProductService {

        private final ProductRepository productRepository;
        private final CategoryRepository categoryRepository;

        // ── List ────────────────────────────────────────────────────────────────

        @Override
        public Page<ProductSummaryResponse> getProducts(int page, int size,
                        UUID categoryId, String search, String type) {
                Specification<ProductEntity> spec = buildSpec(categoryId, search, type);
                PageRequest pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
                return productRepository.findAll(spec, pageable)
                                .map(this::toSummary);
        }

        // ── Detail ───────────────────────────────────────────────────────────────

        @Override
        public ProductDetailResponse getProductById(UUID id) {
                ProductEntity p = productRepository.findById(id)
                                .orElseThrow(() -> new ApiException(ProductErrorCode.PRODUCT_NOT_FOUND));
                return toDetail(p);
        }

        // ── Admin write ───────────────────────────────────────────────────────────

        @Override
        @Transactional
        public ProductDetailResponse createProduct(ProductRequest request) {
                CategoryEntity category = findActiveCategoryById(request.getCategoryId());
                ProductEntity product = ProductEntity.builder()
                                .name(request.getName())
                                .description(request.getDescription())
                                .type(request.getType())
                                .active(request.isActive())
                                .category(category)
                                .build();
                return toDetail(productRepository.save(product));
        }

        @Override
        @Transactional
        public ProductDetailResponse updateProduct(UUID id, ProductRequest request) {
                ProductEntity product = getNonDeletedProductOrThrow(id);
                product.setName(request.getName());
                product.setDescription(request.getDescription());
                product.setType(request.getType());
                product.setActive(request.isActive());
                if (request.getCategoryId() != null) {
                        product.setCategory(findActiveCategoryById(request.getCategoryId()));
                }
                return toDetail(productRepository.save(product));
        }

        @Override
        @Transactional
        public void deleteProduct(UUID id) {
                ProductEntity product = getNonDeletedProductOrThrow(id);
                product.setDeleteAt(LocalDateTime.now());
                productRepository.save(product);
        }

        @Override
        @Transactional
        public ProductDetailResponse toggleActive(UUID id) {
                ProductEntity product = getNonDeletedProductOrThrow(id);
                product.setActive(!Boolean.TRUE.equals(product.getActive()));
                return toDetail(productRepository.save(product));
        }

        @Override
        @Transactional(readOnly = true)
        public ProductEntity getActiveProductOrThrow(UUID id) {
                return productRepository.findByIdAndDeleteAtIsNull(id)
                                .orElseThrow(() -> new ApiException(ProductErrorCode.PRODUCT_NOT_FOUND));
        }

        // ── Private helpers ───────────────────────────────────────────────────────

        private ProductEntity getNonDeletedProductOrThrow(UUID id) {
                return productRepository.findByIdAndDeleteAtIsNull(id)
                                .orElseThrow(() -> new ApiException(ProductErrorCode.PRODUCT_NOT_FOUND));
        }

        private CategoryEntity findActiveCategoryById(UUID categoryId) {
                if (categoryId == null)
                        return null;
                CategoryEntity category = categoryRepository.findById(categoryId)
                                .orElseThrow(() -> new ApiException(ProductErrorCode.CATEGORY_NOT_FOUND));
                if (!Boolean.TRUE.equals(category.getActive())) {
                        throw new ApiException(ProductErrorCode.CATEGORY_INACTIVE);
                }
                return category;
        }

        // ── Spec builder ─────────────────────────────────────────────────────────

        private Specification<ProductEntity> buildSpec(UUID categoryId, String search, String type) {
                return (root, query, cb) -> {
                        List<Predicate> predicates = new ArrayList<>();

                        predicates.add(cb.isTrue(root.get("active")));
                        predicates.add(cb.isNull(root.get("deleteAt")));

                        if (categoryId != null) {
                                predicates.add(cb.equal(root.get("category").get("id"), categoryId));
                        }
                        if (search != null && !search.isBlank()) {
                                String like = "%" + search.toLowerCase() + "%";
                                predicates.add(cb.or(
                                                cb.like(cb.lower(root.get("name")), like),
                                                cb.like(cb.lower(root.get("description")), like)));
                        }
                        if (type != null && !type.isBlank()) {
                                predicates.add(cb.equal(cb.upper(root.get("type")), type.toUpperCase()));
                        }
                        return cb.and(predicates.toArray(new Predicate[0]));
                };
        }

        // ── Mappers ──────────────────────────────────────────────────────────────

        private ProductSummaryResponse toSummary(ProductEntity p) {
                String primaryImg = p.getImages() == null ? null
                                : p.getImages().stream()
                                                .filter(i -> Boolean.TRUE.equals(i.getIsPrimary()))
                                                .map(ProductImageEntity::getImageUrl)
                                                .findFirst()
                                                .orElse(p.getImages().isEmpty() ? null
                                                                : p.getImages().get(0).getImageUrl());

                BigDecimal basePrice = p.getVariants() == null ? null
                                : p.getVariants().stream()
                                                .filter(v -> Boolean.TRUE.equals(v.getActive())
                                                                && v.getDeletedAt() == null)
                                                .map(ProductVariantEntity::getPrice)
                                                .min(Comparator.naturalOrder())
                                                .orElse(null);

                return ProductSummaryResponse.builder()
                                .id(p.getId())
                                .name(p.getName())
                                .description(p.getDescription())
                                .type(p.getType())
                                .categoryId(p.getCategory() != null ? p.getCategory().getId() : null)
                                .categoryName(p.getCategory() != null ? p.getCategory().getName() : null)
                                .primaryImageUrl(primaryImg)
                                .basePrice(basePrice)
                                .active(p.getActive())
                                .build();
        }

        private ProductDetailResponse toDetail(ProductEntity p) {
                List<ProductVariantResponse> variants = p.getVariants() == null ? List.of()
                                : p.getVariants().stream()
                                                .filter(v -> Boolean.TRUE.equals(v.getActive())
                                                                && v.getDeletedAt() == null)
                                                .sorted(Comparator.comparing(ProductVariantEntity::getPrice))
                                                .map(v -> ProductVariantResponse.builder()
                                                                .id(v.getId())
                                                                .sizeName(v.getSizeName())
                                                                .price(v.getPrice())
                                                                .active(v.getActive())
                                                                .build())
                                                .toList();

                List<ProductDetailResponse.ImageInfo> images = p.getImages() == null ? List.of()
                                : p.getImages().stream()
                                                .sorted(Comparator.comparing(
                                                                i -> Boolean.FALSE.equals(i.getIsPrimary())))
                                                .map(i -> ProductDetailResponse.ImageInfo.builder()
                                                                .imageUrl(i.getImageUrl())
                                                                .isPrimary(i.getIsPrimary())
                                                                .build())
                                                .toList();

                return ProductDetailResponse.builder()
                                .id(p.getId())
                                .name(p.getName())
                                .description(p.getDescription())
                                .type(p.getType())
                                .categoryId(p.getCategory() != null ? p.getCategory().getId() : null)
                                .categoryName(p.getCategory() != null ? p.getCategory().getName() : null)
                                .active(p.getActive())
                                .variants(variants)
                                .images(images)
                                .build();
        }
}
