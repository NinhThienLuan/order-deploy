package fsoft.franchise.entity;

import fsoft.franchise.enums.OrderStatus;
import fsoft.franchise.infrastructure.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Lưu lại MỖI LẦN trạng thái đơn hàng thay đổi.
 *
 * <p>
 * Đây chính là dữ liệu để hiển thị timeline tracking cho khách hàng:
 * </p>
 * 
 * <pre>
 *   ✅ Ordered         — Nov 20, 10:00
 *   ✅ Order Ready     — Nov 20, 10:35
 *   ✅ Shipped         — Nov 21, 08:00
 *   ⏳ Out for delivery — Nov 21, 14:00   ← đang ở bước này
 *   ○  Delivered
 * </pre>
 *
 * <p>
 * Mỗi row = 1 bước trong timeline.
 * Khách hàng gọi GET API → lấy list các row này → hiển thị timeline.
 * </p>
 */
@Entity
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@SuperBuilder
@Table(name = "order_status_history")
public class OrderStatusHistoryEntity extends BaseEntity {

    /** Đơn hàng liên quan */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private OrderEntity order;

    /** Trạng thái tại bước này */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    /** Thời điểm chuyển sang trạng thái này */
    @Column(name = "changed_at", nullable = false)
    private LocalDateTime changedAt;

    /** Ai thực hiện thay đổi (null = system tự động, VD: payment callback) */
    @Column(name = "changed_by")
    private UUID changedBy;

    /** Ghi chú (VD: "Đơn hàng đang được chuẩn bị") */
    private String note;
}
