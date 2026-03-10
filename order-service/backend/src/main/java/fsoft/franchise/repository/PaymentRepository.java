package fsoft.franchise.repository;

import fsoft.franchise.entity.PaymentEntity;
import fsoft.franchise.enums.PaymentMethod;
import fsoft.franchise.enums.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
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

        PaymentEntity findByOrder_IdAndStatus(UUID orderId, PaymentStatus status);
        @Query("""
                            SELECT p FROM PaymentEntity p
                            JOIN p.order o
                            JOIN o.customer c
                            WHERE (:orderId IS NULL OR o.id = :orderId)
                            AND (:customerId IS NULL OR c.id = :customerId)
                            AND (:email IS NULL OR LOWER(c.email) = LOWER(:email))
                            AND (:status IS NULL OR p.status = :status)
                            AND (:fromDate IS NULL OR p.paymentDate >= :fromDate)
                            AND (:toDate IS NULL OR p.paymentDate <= :toDate)
                        """)
        Page<PaymentEntity> findByAdminFilters(
                        @Param("orderId") UUID orderId,
                        @Param("customerId") UUID customerId,
                        @Param("email") String email,
                        @Param("status") PaymentStatus status,
                        @Param("fromDate") Instant fromDate,
                        @Param("toDate") Instant toDate,
                        Pageable pageable);

        @Query("""
                            SELECT p FROM PaymentEntity p
                            JOIN p.order o
                            LEFT JOIN o.customer c
                            WHERE c.id = :customerId
                            AND (:orderId IS NULL OR o.id = :orderId)
                            AND (:status IS NULL OR p.status = :status)
                            AND (:fromDate IS NULL OR p.paymentDate >= :fromDate)
                            AND (:toDate IS NULL OR p.paymentDate <= :toDate)
                        """)
        Page<PaymentEntity> findByCustomerFilters(
                        @Param("customerId") UUID customerId,
                        @Param("orderId") UUID orderId,
                        @Param("status") PaymentStatus status,
                        @Param("fromDate") Instant fromDate,
                        @Param("toDate") Instant toDate,
                        Pageable pageable);

        @Query("""
                        SELECT p FROM PaymentEntity p
                        JOIN p.order o
                        LEFT JOIN o.customer c
                        WHERE (:status IS NULL OR p.status = :status)
                        AND (:paymentMethod IS NULL OR p.paymentMethod = :paymentMethod)
                        AND (:fromDate IS NULL OR p.paymentDate >= :fromDate)
                        AND (:toDate IS NULL OR p.paymentDate <= :toDate)
                        ORDER BY p.paymentDate DESC
                        """)
        Page<PaymentEntity> findByAdminTransactionFilters(
                        @Param("status") PaymentStatus status,
                        @Param("paymentMethod") PaymentMethod paymentMethod,
                        @Param("fromDate") Instant fromDate,
                        @Param("toDate") Instant toDate,
                        Pageable pageable);

}
