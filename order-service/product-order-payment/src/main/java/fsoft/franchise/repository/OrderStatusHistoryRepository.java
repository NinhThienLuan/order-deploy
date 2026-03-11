package fsoft.franchise.repository;

import fsoft.franchise.entity.OrderStatusHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository cho bảng order_status_history.
 * Query timeline tracking theo orderId, sắp xếp theo thời gian tăng dần.
 */
@Repository
public interface OrderStatusHistoryRepository extends JpaRepository<OrderStatusHistoryEntity, UUID> {

    /**
     * Lấy toàn bộ lịch sử thay đổi status của 1 đơn hàng, theo thứ tự thời gian.
     * → Dùng để hiển thị timeline cho khách hàng.
     */
    List<OrderStatusHistoryEntity> findByOrder_IdOrderByChangedAtAsc(UUID orderId);
}
