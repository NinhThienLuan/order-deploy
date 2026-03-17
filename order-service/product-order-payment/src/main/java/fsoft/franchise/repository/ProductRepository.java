package fsoft.franchise.repository;

import fsoft.franchise.entity.ProductEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<ProductEntity, UUID>,
                JpaSpecificationExecutor<ProductEntity> {

        Optional<ProductEntity> findByIdAndDeleteAtIsNull(UUID id);

        Page<ProductEntity> findByDeleteAtIsNull(Pageable pageable);

        Page<ProductEntity> findByCategoryIdAndDeleteAtIsNull(UUID categoryId, Pageable pageable);

        Page<ProductEntity> findByActiveTrueAndDeleteAtIsNull(Pageable pageable);

        Page<ProductEntity> findByActiveFalseAndDeleteAtIsNull(Pageable pageable);

        List<ProductEntity> findByIsRecommendedTrueAndActiveTrueAndDeleteAtIsNull();

        boolean existsByNameIgnoreCaseAndDeleteAtIsNull(String name);

        boolean existsByNameIgnoreCaseAndIdNotAndDeleteAtIsNull(String name, java.util.UUID id);
}
