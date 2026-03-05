package fsoft.franchise.entity;

import fsoft.franchise.enums.TransactionStatus;
import fsoft.franchise.enums.TransactionType;
import fsoft.franchise.infrastructure.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

/**
 * transactions — VNPay money-flow ledger entry.
 *
 * VNPay field mapping:
 * vnpTxnRef ← vnp_TxnRef (merchant's own reference, sent when creating payment)
 * vnpTransactionNo← vnp_TransactionNo (VNPay's unique transaction ID, returned
 * in callback)
 * vnpResponseCode ← vnp_ResponseCode ("00" = success, "24" = cancelled, etc.)
 * vnpBankCode ← vnp_BankCode (bank/card that processed: "NCB", "VCB",
 * "VNPAYQR"...)
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

    // Merchant's own reference sent to VNPay (usually orderId or paymentId)
    @Column(name = "vnp_txn_ref")
    private String vnpTxnRef;

    // VNPay's unique transaction ID — required for refund API calls
    @Column(name = "vnp_transaction_no")
    private String vnpTransactionNo;

    // VNPay response code: "00" = success, "07" = suspicious, "24" = cancelled...
    @Column(name = "vnp_response_code", length = 10)
    private String vnpResponseCode;

    // Bank / instrument: "NCB", "VCB", "TCB", "VNPAYQR", "INTCARD"...
    @Column(name = "vnp_bank_code", length = 20)
    private String vnpBankCode;

    // PAYMENT | REFUND | TOPUP
    @Enumerated(EnumType.STRING)
    @Column(name = "type", length = 10, nullable = false)
    private TransactionType type;

    @Column(name = "amount", precision = 12, scale = 2, nullable = false)
    private BigDecimal amount;

    @OneToOne(mappedBy = "transaction")
    private  RefundEntity refund;

    // Our internal resolved status: PENDING | SUCCESS | FAILED
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 10, nullable = false)
    private TransactionStatus status;
}
