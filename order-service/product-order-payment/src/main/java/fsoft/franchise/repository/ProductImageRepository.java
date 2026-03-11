package fsoft.franchise.repository;

import fsoft.franchise.entity.ProductImageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductImageRepository extends JpaRepository<ProductImageEntity, UUID> {

    List<ProductImageEntity> findAllByProductId(UUID productId);

    Optional<ProductImageEntity> findByProductIdAndIsPrimaryTrue(UUID productId);
}
