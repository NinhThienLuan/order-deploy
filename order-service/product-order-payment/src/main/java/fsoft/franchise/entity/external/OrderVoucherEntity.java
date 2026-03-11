package fsoft.franchise.entity.external;

import fsoft.franchise.entity.OrderEntity;
import fsoft.franchise.infrastructure.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * order_vouchers — junction: one order can use multiple vouchers.
 * Stores the actual amount saved per voucher application.
 */
@Entity
@Table(name = "order_vouchers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class OrderVoucherEntity extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private OrderEntity order;

    // FK → vouchers.id (stub domain — stored as raw UUID until Voucher entity is
    // created)
    @Column(name = "voucher_id", nullable = false)
    private UUID voucherId;

    @Column(name = "applied_at")
    private LocalDateTime appliedAt;

    // Actual amount reduced on this order by this voucher
    @Column(name = "amount_saved", precision = 12, scale = 2)
    private BigDecimal amountSaved;
}
