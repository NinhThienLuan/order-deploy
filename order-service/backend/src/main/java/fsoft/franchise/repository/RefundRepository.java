package fsoft.franchise.repository;

import fsoft.franchise.entity.RefundEntity;
import fsoft.franchise.enums.RefundStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RefundRepository extends JpaRepository<RefundEntity, UUID> {
    boolean existsByOrderId(UUID orderId);

    List<RefundEntity> findByStatus(RefundStatus status);
}
