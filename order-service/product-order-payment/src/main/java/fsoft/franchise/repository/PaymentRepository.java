package fsoft.franchise.repository;

import fsoft.franchise.entity.PaymentEntity;
import fsoft.franchise.enums.PaymentMethod;
import fsoft.franchise.enums.PaymentStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<PaymentEntity, UUID> {

    /** Find payments by order id, newest first (for payment status by order). */
    List<PaymentEntity> findByOrder_IdOrderByPaymentDateDesc(UUID orderId);

    /**
     * Idempotency: find an existing payment for this order with the given status.
     */
    Optional<PaymentEntity> findFirstByOrder_IdAndStatus(UUID orderId, PaymentStatus status);

    boolean existsByOrder_IdAndStatus(UUID uuid, PaymentStatus paymentStatus);

    /**
     * Resolve payment by gateway reference / merchant transaction id.
     * Used by VNPay/MoMo webhooks to link callbacks back to the original payment.
     */
    Optional<PaymentEntity> findByTransactionId(String transactionId);

    /**
     * Bug #4: Pessimistic lock for webhook handlers — prevents concurrent IPN from
     * creating duplicate transactions.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM PaymentEntity p WHERE p.transactionId = :txnRef")
    Optional<PaymentEntity> findByTransactionIdForUpdate(@Param("txnRef") String txnRef);

    PaymentEntity findByOrder_IdAndStatus(UUID orderId, PaymentStatus status);

    @Query("""
                SELECT p FROM PaymentEntity p
                JOIN p.order o
                WHERE (cast(:storeId as uuid) IS NULL OR o.storeId = :storeId)
                AND (cast(:orderId as uuid) IS NULL OR o.id = :orderId)
                AND (cast(:customerId as uuid) IS NULL OR o.customerId = :customerId)
                AND (:status IS NULL OR p.status = :status)
                AND (cast(:fromDate as timestamp) IS NULL OR p.createdAt >= :fromDate)
                AND (cast(:toDate as timestamp) IS NULL OR p.createdAt <= :toDate)
            """)
    Page<PaymentEntity> findByAdminFilters(
            @Param("storeId") UUID storeId,
            @Param("orderId") UUID orderId,
            @Param("customerId") UUID customerId,
            @Param("status") PaymentStatus status,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate,
            Pageable pageable);

    @Query("""
                SELECT SUM(p.amountPaid) FROM PaymentEntity p
                JOIN p.order o
                WHERE (cast(:storeId as uuid) IS NULL OR o.storeId = :storeId)
                AND (cast(:orderId as uuid) IS NULL OR o.id = :orderId)
                AND (cast(:customerId as uuid) IS NULL OR o.customerId = :customerId)
                AND (:status IS NULL OR p.status = :status)
                AND (cast(:fromDate as timestamp) IS NULL OR p.createdAt >= :fromDate)
                AND (cast(:toDate as timestamp) IS NULL OR p.createdAt <= :toDate)
            """)
    BigDecimal sumAmountByAdminFilters(
            @Param("storeId") UUID storeId,
            @Param("orderId") UUID orderId,
            @Param("customerId") UUID customerId,
            @Param("status") PaymentStatus status,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate);

    @Query("""
                SELECT p FROM PaymentEntity p
                JOIN p.order o
                WHERE o.customerId = :customerId
                AND (cast(:storeId as uuid) IS NULL OR o.storeId = :storeId)
                AND (cast(:orderId as uuid) IS NULL OR o.id = :orderId)
                AND (:status IS NULL OR p.status = :status)
                AND (cast(:fromDate as timestamp) IS NULL OR p.createdAt >= :fromDate)
                AND (cast(:toDate as timestamp) IS NULL OR p.createdAt <= :toDate)
            """)
    Page<PaymentEntity> findByCustomerFilters(
            @Param("storeId") UUID storeId,
            @Param("customerId") UUID customerId,
            @Param("orderId") UUID orderId,
            @Param("status") PaymentStatus status,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate,
            Pageable pageable);

    @Query("""
                SELECT SUM(p.amountPaid) FROM PaymentEntity p
                JOIN p.order o
                WHERE o.customerId = :customerId
                AND (cast(:storeId as uuid) IS NULL OR o.storeId = :storeId)
                AND (cast(:orderId as uuid) IS NULL OR o.id = :orderId)
                AND (:status IS NULL OR p.status = :status)
                AND (cast(:fromDate as timestamp) IS NULL OR p.createdAt >= :fromDate)
                AND (cast(:toDate as timestamp) IS NULL OR p.createdAt <= :toDate)
            """)
    BigDecimal sumAmountByCustomerFilters(
            @Param("storeId") UUID storeId,
            @Param("customerId") UUID customerId,
            @Param("orderId") UUID orderId,
            @Param("status") PaymentStatus status,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate);

    @Query("""
            SELECT p FROM PaymentEntity p
            JOIN p.order o
            WHERE (:status IS NULL OR p.status = :status)
            AND (:paymentMethod IS NULL OR p.paymentMethod = :paymentMethod)
            AND (:fromDate IS NULL OR p.paymentDate >= :fromDate)
            AND (:toDate IS NULL OR p.paymentDate <= :toDate)
            ORDER BY p.paymentDate DESC
            """)
    Page<PaymentEntity> findByAdminTransactionFilters(
            @Param("status") PaymentStatus status,
            @Param("paymentMethod") PaymentMethod paymentMethod,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate,
            Pageable pageable);

}
