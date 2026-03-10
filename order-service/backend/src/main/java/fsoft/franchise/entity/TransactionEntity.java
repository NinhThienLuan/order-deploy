package fsoft.franchise.entity;

import fsoft.franchise.enums.TransactionType;
import fsoft.franchise.infrastructure.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

/**
 * transactions — gateway money-flow ledger entry (immutable receipt).
 *
 * A transaction is only created when money actually moved (SUCCESS path).
 * Gateway response codes and bank info are stored on PaymentEntity instead,
 * so failure codes are not lost when no transaction is written.
 *
 * VNPay field mapping:
 * vnpTxnRef ← vnp_TxnRef (merchant's own reference, sent when creating payment)
 * vnpTransactionNo ← vnp_TransactionNo (VNPay's unique transaction ID, required
 * for refund API)
 */
@Entity
@Table(name = "transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class TransactionEntity extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id", nullable = false)
    private PaymentEntity payment;

    // Merchant's own reference sent to VNPay/MoMo (usually orderId or paymentId)
    @Column(name = "vnp_txn_ref")
    private String vnpTxnRef;

    // VNPay/MoMo's unique transaction ID — required for refund API calls
    @Column(name = "vnp_transaction_no")
    private String vnpTransactionNo;

    // PAYMENT | REFUND
    @Enumerated(EnumType.STRING)
    @Column(name = "type", length = 10, nullable = false)
    private TransactionType type;

    @Column(name = "amount", precision = 12, scale = 2, nullable = false)
    private BigDecimal amount;

    @OneToOne(mappedBy = "transaction")
    private RefundEntity refund;
}
