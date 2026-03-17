package fsoft.franchise.entity;

import fsoft.franchise.enums.PaymentMethod;
import fsoft.franchise.enums.PaymentStatus;
import fsoft.franchise.enums.PaymentType;
import fsoft.franchise.infrastructure.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "payments")
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class PaymentEntity extends BaseEntity {

    // nullable = true: INBOUND / OUTBOUND payments have no linked order.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = true)
    private OrderEntity order;

    @Column(name = "transaction_id", length = 100)
    private String transactionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", length = 20, nullable = false)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_type", length = 25, nullable = false)
    private PaymentType paymentType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private PaymentStatus status;

    @Column(name = "amount_paid", precision = 12, scale = 2)
    private BigDecimal amountPaid;

    @Column(name = "error_message", length = 500)
    private String errorMessage;

    @OneToOne(mappedBy = "payment")
    private RefundEntity refund;

    @Column(name = "payment_date")
    private LocalDateTime paymentDate;

    @Column(name = "payment_url", columnDefinition = "TEXT")
    private String paymentUrl;

    @Column(name = "expired_at")
    private LocalDateTime expiredAt;

    // Gateway response code stored at payment level so failure codes are not lost
    // (e.g. VNPay "24" = cancelled, MoMo non-zero resultCode)
    @Column(name = "vnp_response_code", length = 10)
    private String vnpResponseCode;

    // Bank / instrument used (e.g. "NCB", "VCB", "VNPAYQR", "MOMO")
    @Column(name = "vnp_bank_code", length = 20)
    private String vnpBankCode;

//    // Bug #5: Track which staff member confirmed a cash payment
//    @Column(name = "confirmed_by_staff_id")
//    private UUID confirmedByStaffId;
}
