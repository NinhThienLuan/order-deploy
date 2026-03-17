package fsoft.franchise.entity;

import fsoft.franchise.enums.OrderStatus;
import fsoft.franchise.infrastructure.BaseEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.BatchSize;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Data
@EqualsAndHashCode(callSuper = false)
@RequiredArgsConstructor
@SuperBuilder
@Table(name = "orders")
public class OrderEntity extends BaseEntity {

    @Column(name = "customer_id")
    private UUID customerId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private OrderStatus status; // PENDING, PAID, PREPARING, READY, COMPLETE, CANCEL
    private String orderType;
    private String orderNumber;
    private String deliveryAddress;

    @Column(name = "recipient_name")
    private String recipientName;

    @Column(name = "recipient_phone")
    private String recipientPhone;

    @Column(name = "store_id")
    private UUID storeId;

    @Column(name = "order_time")
    private LocalDateTime orderTime;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @BatchSize(size = 10)
    private List<OrderItemEntity> orderItems;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    @BatchSize(size = 10)
    private List<PaymentEntity> payments;

    @OneToOne(mappedBy = "order")
    private RefundEntity refund;

    @Column(name = "total_amount")
    private BigDecimal totalAmount;

    private String note;

    @Column(name = "is_flagged", nullable = false)
    @Builder.Default
    private Boolean isFlagged = false;

    @Column(name = "flag_reason", length = 500)
    private String flagReason;

    @Column(name = "flagged_by")
    private UUID flaggedBy;

    @Column(name = "flagged_at")
    private LocalDateTime flaggedAt;
}
