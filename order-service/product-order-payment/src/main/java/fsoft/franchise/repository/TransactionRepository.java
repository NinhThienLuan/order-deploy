package fsoft.franchise.repository;

import fsoft.franchise.entity.TransactionEntity;
import fsoft.franchise.enums.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<TransactionEntity, UUID> {

    /**
     * All transactions for a payment, newest first. Used to resolve latest gateway
     * result.
     */
    List<TransactionEntity> findByPayment_IdOrderByCreatedAtDesc(UUID paymentId);

    /** All transactions for an order (via payment). */
    List<TransactionEntity> findByPayment_Order_IdOrderByCreatedAtDesc(UUID orderId);

    Optional<TransactionEntity> findByVnpTxnRef(String vnpTxnRef);

    /**
     * Check if there is any transaction with the given merchant reference
     * (vnp_TxnRef / transactionId).
     * Used to enforce idempotency at createPayment time.
     */
    boolean existsByVnpTxnRef(String vnpTxnRef);

    /** Find transactions by payment ID and type (e.g., PAYMENT, REFUND) */
    List<TransactionEntity> findByPayment_IdAndType(UUID paymentId, TransactionType type);

    /**
     * Idempotency check for MoMo return URL handler: if the IPN already created
     * a transaction for this MoMo transId, the return handler must not create
     * another.
     */
    boolean existsByVnpTransactionNo(String vnpTransactionNo);
}
