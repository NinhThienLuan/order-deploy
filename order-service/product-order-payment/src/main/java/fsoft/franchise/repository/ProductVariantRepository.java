package fsoft.franchise.repository;

import fsoft.franchise.entity.ProductVariantEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProductVariantRepository extends JpaRepository<ProductVariantEntity, UUID> {

    List<ProductVariantEntity> findAllByProductId(UUID productId);
}
