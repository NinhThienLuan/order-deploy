package fsoft.franchise.service;

import fsoft.franchise.dto.payments.OrderTrackingTimelineDTO;
import fsoft.franchise.dto.payments.UpdateOrderStatusResponse;
import fsoft.franchise.enums.OrderStatus;

import java.util.UUID;

/**
 * Service chịu trách nhiệm:
 * 1. Push real-time tracking events qua WebSocket
 * 2. Lưu lịch sử thay đổi status (timeline)
 * 3. Cung cấp timeline cho khách hàng xem
 */
public interface OrderTrackingService {

    /**
     * Gửi tracking event khi trạng thái đơn hàng thay đổi.
     * Đồng thời lưu vào bảng order_status_history.
     */
    void sendTrackingUpdate(UUID orderId, OrderStatus previousStatus, OrderStatus newStatus, UUID updatedBy);

    /**
     * Cập nhật trạng thái đơn hàng trong DB + push real-time notification.
     * Validate luồng trạng thái hợp lệ (PAID→PREPARING→READY→COMPLETED).
     * Chỉ ADMIN và MANAGER được gọi.
     *
     * @return Thông tin đơn hàng sau khi cập nhật (orderId, status, lastUpdated)
     */
    UpdateOrderStatusResponse updateOrderStatus(UUID orderId, OrderStatus newStatus, UUID updatedBy);

    /**
     * Lấy timeline tracking của đơn hàng — hiển thị cho khách hàng.
     * Trả về danh sách các bước + bước nào đã hoàn thành + bước hiện tại.
     *
     * @param orderId       ID đơn hàng
     * @param currentUserId ID user đang request (để check quyền)
     * @param role          Role của user
     */
    OrderTrackingTimelineDTO getTrackingTimeline(UUID orderId, UUID currentUserId, String role);
}
