package fsoft.franchise.repository;

import fsoft.franchise.entity.ProductVariantIngredientEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ProductVariantIngredientRepository extends JpaRepository<ProductVariantIngredientEntity, UUID> {

    void deleteAllByVariantId(UUID variantId);
}
