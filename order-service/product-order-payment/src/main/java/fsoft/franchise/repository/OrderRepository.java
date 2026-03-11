package fsoft.franchise.repository;

import fsoft.franchise.entity.OrderEntity;
import fsoft.franchise.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<OrderEntity, UUID> {

        @Query("SELECT o FROM OrderEntity o WHERE " +
                        "(:status IS NULL OR o.status = :status) AND " +
                        "(:branchId IS NULL OR o.storeId = :branchId) AND " +
                        "(:fromDate IS NULL OR o.orderTime >= :fromDate) AND " +
                        "(:toDate IS NULL OR o.orderTime <= :toDate)")
        Page<OrderEntity> findOrderHistory(
                        @Param("status") OrderStatus status,
                        @Param("branchId") Long branchId,
                        @Param("fromDate") LocalDateTime fromDate,
                        @Param("toDate") LocalDateTime toDate,
                        Pageable pageable);

        @Query("SELECT o FROM OrderEntity o LEFT JOIN FETCH o.customer WHERE o.id = :id")
        Optional<OrderEntity> findByIdWithCustomer(@Param("id") UUID id);

        @Query("SELECT o FROM OrderEntity o " +
                        "LEFT JOIN FETCH o.orderItems oi " +
                        "LEFT JOIN FETCH oi.productVariant " +
                        "LEFT JOIN FETCH o.customer " +
                        "WHERE o.id = :id")
        Optional<OrderEntity> findByIdWithItems(@Param("id") UUID id);

        @Query("SELECT o FROM OrderEntity o " +
                        "LEFT JOIN FETCH o.payments " +
                        "WHERE o.id = :id")
        Optional<OrderEntity> findByIdWithPayments(@Param("id") UUID id);

        Page<OrderEntity> findByCustomer_IdOrderByOrderTimeDesc(UUID customerId, Pageable pageable);
}
