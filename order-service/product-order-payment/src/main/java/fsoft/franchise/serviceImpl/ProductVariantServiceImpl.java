package fsoft.franchise.serviceImpl;

import fsoft.franchise.common.exception.ApiException;
import fsoft.franchise.exception.ProductErrorCode;
import fsoft.franchise.dto.products.ProductVariantRequest;
import fsoft.franchise.dto.products.VariantIngredientRequest;
import fsoft.franchise.dto.products.ProductVariantResponse;
import fsoft.franchise.dto.products.VariantIngredientResponse;
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
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

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

        // Surgical update of ingredients to avoid unique constraint violations
        List<VariantIngredientRequest> newReqs = request.getIngredients() != null ? request.getIngredients() : new ArrayList<>();
        
        // 1. Map existing ones by ingredientId
        Map<UUID, ProductVariantIngredientEntity> existingMap = variant.getIngredients() == null 
            ? Map.of() 
            : variant.getIngredients().stream()
                .collect(Collectors.toMap(i -> i.getIngredient().getId(), i -> i));

        // 2. Identify new items to add and existing to update
        List<ProductVariantIngredientEntity> toKeep = new ArrayList<>();
        for (VariantIngredientRequest req : newReqs) {
            ProductVariantIngredientEntity existing = existingMap.get(req.getIngredientId());
            if (existing != null) {
                // Update existing record
                existing.setQuantity(req.getQuantity());
                existing.setUnit(req.getUnit());
                toKeep.add(existing);
            } else {
                // Add new record
                IngredientEntity ingredient = ingredientEntityRepository.findById(req.getIngredientId())
                        .orElseThrow(() -> new ApiException(ProductErrorCode.INGREDIENT_NOT_FOUND));
                toKeep.add(ProductVariantIngredientEntity.builder()
                        .variant(variant)
                        .ingredient(ingredient)
                        .quantity(req.getQuantity())
                        .unit(req.getUnit())
                        .build());
            }
        }

        // 3. Update the collection (Hibernate orphanRemoval will handle the rest)
        if (variant.getIngredients() == null) {
            variant.setIngredients(new ArrayList<>());
        }
        variant.getIngredients().clear();
        variant.getIngredients().addAll(toKeep);

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
                .ingredients(v.getIngredients() == null ? new ArrayList<VariantIngredientResponse>() : v.getIngredients().stream()
                        .map(i -> VariantIngredientResponse.builder()
                                .ingredientId(i.getIngredient().getId())
                                .name(i.getIngredient().getName())
                                .quantity(i.getQuantity())
                                .unit(i.getUnit())
                                .build())
                        .toList())
                .build();
    }
}
