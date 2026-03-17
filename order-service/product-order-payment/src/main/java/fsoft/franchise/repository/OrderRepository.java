package fsoft.franchise.repository;

import fsoft.franchise.entity.OrderEntity;
import fsoft.franchise.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<OrderEntity, UUID> {

        @Query("SELECT o FROM OrderEntity o WHERE " +
                        "(:status IS NULL OR o.status = :status) AND " +
                        "(:storeId IS NULL OR o.storeId = :storeId) AND " +
                        "(:fromDate IS NULL OR o.orderTime >= :fromDate) AND " +
                        "(:toDate IS NULL OR o.orderTime <= :toDate)")
        Page<OrderEntity> findOrderHistory(
                        @Param("status") OrderStatus status,
                        @Param("storeId") UUID storeId,
                        @Param("fromDate") LocalDateTime fromDate,
                        @Param("toDate") LocalDateTime toDate,
                        Pageable pageable);

        @Query("SELECT o FROM OrderEntity o WHERE o.id = :id")
        Optional<OrderEntity> findByIdWithCustomer(@Param("id") UUID id);

        @Query("SELECT o FROM OrderEntity o " +
                        "LEFT JOIN FETCH o.orderItems oi " +
                        "LEFT JOIN FETCH oi.productVariant " +
                        "WHERE o.id = :id")
        Optional<OrderEntity> findByIdWithItems(@Param("id") UUID id);

        @Query("SELECT o FROM OrderEntity o " +
                        "LEFT JOIN FETCH o.payments " +
                        "WHERE o.id = :id")
        Optional<OrderEntity> findByIdWithPayments(@Param("id") UUID id);

        Page<OrderEntity> findByCustomerIdOrderByOrderTimeDesc(UUID customerId, Pageable pageable);

        @Query("SELECT COUNT(o) FROM OrderEntity o WHERE o.storeId = :storeId AND o.status = 'PREPARING'")
        int countPreparingByStoreId(@Param("storeId") UUID storeId);

        @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM OrderEntity o WHERE " +
                        "(:status IS NULL OR o.status = :status) AND " +
                        "(:storeId IS NULL OR o.storeId = :storeId) AND " +
                        "(:fromDate IS NULL OR o.orderTime >= :fromDate) AND " +
                        "(:toDate IS NULL OR o.orderTime <= :toDate)")
        BigDecimal sumTotalAmountByFilter(
                        @Param("status") OrderStatus status,
                        @Param("storeId") UUID storeId,
                        @Param("fromDate") LocalDateTime fromDate,
                        @Param("toDate") LocalDateTime toDate);
}
