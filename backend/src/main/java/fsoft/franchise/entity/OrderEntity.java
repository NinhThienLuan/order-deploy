package fsoft.franchise.entity;

import fsoft.franchise.enums.OrderStatus;
import fsoft.franchise.entity.external.AccountEntity;
import fsoft.franchise.infrastructure.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.BatchSize;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@EqualsAndHashCode(callSuper = false)
@RequiredArgsConstructor
@SuperBuilder
@Table(name = "orders")
public class OrderEntity extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private AccountEntity customer;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private OrderStatus status; // PENDING, PAID, PREPARING, READY, COMPLETE, CANCEL
    private String orderType;
    private String orderNumber;
    private String deliveryAddress;

    @Column(name = "store_id")
    private Long storeId;

    @Column(name = "order_time")
    private LocalDateTime orderTime;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    @BatchSize(size = 10)
    private List<OrderItemEntity> orderItems;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    @BatchSize(size = 10)
    private List<PaymentEntity> payments;

    @OneToOne(mappedBy = "order")
    private  RefundEntity refund;

    @Column(name = "total_amount")
    private BigDecimal totalAmount;
}
