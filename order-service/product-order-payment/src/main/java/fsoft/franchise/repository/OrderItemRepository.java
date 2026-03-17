package fsoft.franchise.repository;

import fsoft.franchise.entity.OrderItemEntity;
import fsoft.franchise.entity.ProductEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItemEntity, UUID> {

	@Query("""
			SELECT oi.productVariant.product
			FROM OrderItemEntity oi
			WHERE oi.productVariant.deletedAt IS NULL
			  AND oi.productVariant.active = true
			  AND oi.productVariant.product.deleteAt IS NULL
			  AND oi.productVariant.product.active = true
			GROUP BY oi.productVariant.product
			ORDER BY SUM(oi.quantity) DESC
			""")
	List<ProductEntity> findTopOrderedProducts(Pageable pageable);
}

