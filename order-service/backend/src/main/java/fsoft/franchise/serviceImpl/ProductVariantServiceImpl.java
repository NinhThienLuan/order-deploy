package fsoft.franchise.serviceImpl;

import fsoft.franchise.common.exception.ApiException;
import fsoft.franchise.exception.ProductErrorCode;
import fsoft.franchise.dto.products.ProductVariantRequest;
import fsoft.franchise.dto.products.VariantIngredientRequest;
import fsoft.franchise.dto.products.ProductVariantResponse;
import fsoft.franchise.entity.IngredientEntity;
import fsoft.franchise.entity.ProductEntity;
import fsoft.franchise.entity.ProductVariantEntity;
import fsoft.franchise.entity.ProductVariantIngredientEntity;
import fsoft.franchise.repository.IngredientRepository;
import fsoft.franchise.repository.ProductVariantIngredientRepository;
import fsoft.franchise.repository.ProductVariantRepository;
import fsoft.franchise.service.ProductService;
import fsoft.franchise.service.ProductVariantService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductVariantServiceImpl implements ProductVariantService {

    private final ProductVariantRepository variantRepository;
    private final ProductVariantIngredientRepository ingredientRepository;
    private final ProductService productService;
    private final IngredientRepository ingredientEntityRepository;

    @Override
    public List<ProductVariantResponse> getVariantsByProductId(UUID productId) {
        productService.getActiveProductOrThrow(productId);
        return variantRepository.findAllByProductId(productId).stream()
                .filter(v -> v.getDeletedAt() == null)
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public ProductVariantResponse createVariant(UUID productId, ProductVariantRequest request) {
        ProductEntity product = productService.getActiveProductOrThrow(productId);

        ProductVariantEntity variant = ProductVariantEntity.builder()
                .product(product)
                .sizeName(request.getSizeName())
                .price(request.getPrice())
                .active(request.isActive())
                .build();

        ProductVariantEntity saved = variantRepository.save(variant);

        // Save ingredients
        List<ProductVariantIngredientEntity> ingredients = buildIngredients(request.getIngredients(), saved);
        if (!ingredients.isEmpty()) {
            ingredientRepository.saveAll(ingredients);
            saved.setIngredients(ingredients);
        }

        return toResponse(saved);
    }

    @Override
    @Transactional
    public ProductVariantResponse updateVariant(UUID productId, UUID variantId, ProductVariantRequest request) {
        productService.getActiveProductOrThrow(productId);
        ProductVariantEntity variant = findActiveVariantOrThrow(productId, variantId);

        variant.setSizeName(request.getSizeName());
        variant.setPrice(request.getPrice());
        variant.setActive(request.isActive());

        // Replace ingredients: delete old, insert new
        ingredientRepository.deleteAllByVariantId(variantId);
        variantRepository.saveAndFlush(variant);

        List<ProductVariantIngredientEntity> ingredients = buildIngredients(request.getIngredients(), variant);
        if (!ingredients.isEmpty()) {
            ingredientRepository.saveAll(ingredients);
        }
        variant.setIngredients(ingredients);

        return toResponse(variantRepository.save(variant));
    }

    @Override
    @Transactional
    public void deleteVariant(UUID productId, UUID variantId) {
        productService.getActiveProductOrThrow(productId);
        ProductVariantEntity variant = findActiveVariantOrThrow(productId, variantId);
        variant.setDeletedAt(LocalDateTime.now());
        variantRepository.save(variant);
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private ProductVariantEntity findActiveVariantOrThrow(UUID productId, UUID variantId) {
        return variantRepository.findById(variantId)
                .filter(v -> v.getProduct().getId().equals(productId))
                .filter(v -> v.getDeletedAt() == null)
                .orElseThrow(() -> new ApiException(ProductErrorCode.VARIANT_NOT_FOUND));
    }

    private List<ProductVariantIngredientEntity> buildIngredients(
            List<VariantIngredientRequest> reqs, ProductVariantEntity variant) {
        List<ProductVariantIngredientEntity> list = new ArrayList<>();
        if (reqs == null)
            return list;
        for (VariantIngredientRequest req : reqs) {
            IngredientEntity ingredient = ingredientEntityRepository.findById(req.getIngredientId())
                    .orElseThrow(() -> new ApiException(ProductErrorCode.INGREDIENT_NOT_FOUND));
            list.add(ProductVariantIngredientEntity.builder()
                    .variant(variant)
                    .ingredient(ingredient)
                    .quantity(req.getQuantity())
                    .unit(req.getUnit())
                    .build());
        }
        return list;
    }

    private ProductVariantResponse toResponse(ProductVariantEntity v) {
        return ProductVariantResponse.builder()
                .id(v.getId())
                .sizeName(v.getSizeName())
                .price(v.getPrice())
                .active(v.getActive())
                .build();
    }
}
