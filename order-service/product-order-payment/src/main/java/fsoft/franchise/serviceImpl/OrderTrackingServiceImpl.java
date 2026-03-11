package fsoft.franchise.serviceImpl;

import fsoft.franchise.common.exception.ApiException;
import fsoft.franchise.exception.OrderErrorCode;
import fsoft.franchise.dto.payments.OrderTrackingDTO;
import fsoft.franchise.dto.payments.OrderTrackingTimelineDTO;
import fsoft.franchise.entity.OrderEntity;
import fsoft.franchise.entity.OrderStatusHistoryEntity;
import fsoft.franchise.enums.OrderStatus;
import fsoft.franchise.repository.OrderRepository;
import fsoft.franchise.repository.OrderStatusHistoryRepository;
import fsoft.franchise.service.OrderTrackingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;


/**
 * Implementation cho real-time order tracking.
 *
 * <h3>Tổng quan 2 chức năng chính:</h3>
 *
 * <p>
 * <b>1. Push real-time (WebSocket):</b><br>
 * Khi nhân viên đổi status → server push message ngay tới khách hàng đang mở
 * app.
 * Khách thấy thay đổi NGAY LẬP TỨC không cần refresh.
 * </p>
 *
 * <p>
 * <b>2. Timeline tracking (REST API):</b><br>
 * Khách mở trang tracking → gọi GET API → nhận danh sách các bước
 * (Ordered ✅ → Paid ✅ → Preparing ⏳ → Ready ○ → Completed ○).
 * Data lấy từ bảng order_status_history.
 * </p>
 *
 * <h3>Luồng trạng thái (state machine):</h3>
 * 
 * <pre>
 *   PENDING ──(thanh toán)──▶ PAID ──▶ PREPARING ──▶ READY ──▶ COMPLETED
 *      │
 *      └──(hủy)──▶ CANCELED
 * </pre>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OrderTrackingServiceImpl implements OrderTrackingService {

    private final SimpMessagingTemplate messagingTemplate;
    private final OrderRepository orderRepository;
    private final OrderStatusHistoryRepository historyRepository;

    /** Bản đồ chuyển trạng thái hợp lệ (Admin chỉ dùng phần này) */
    private static final Map<OrderStatus, Set<OrderStatus>> VALID_TRANSITIONS = Map.of(
            OrderStatus.PAID, Set.of(OrderStatus.PREPARING),
            OrderStatus.PREPARING, Set.of(OrderStatus.READY),
            OrderStatus.READY, Set.of(OrderStatus.COMPLETED));

    /** Message mô tả cho từng trạng thái */
    private static final Map<OrderStatus, String> STATUS_MESSAGES = Map.of(
            OrderStatus.PENDING, "Đơn hàng đã được tạo",
            OrderStatus.PAID, "Đơn hàng đã thanh toán thành công",
            OrderStatus.PREPARING, "Đơn hàng đang được chuẩn bị",
            OrderStatus.READY, "Đơn hàng đã sẵn sàng để giao/nhận",
            OrderStatus.COMPLETED, "Đơn hàng đã hoàn tất",
            OrderStatus.CANCELED, "Đơn hàng đã bị hủy");

    /**
     * Thứ tự các bước trong timeline (không bao gồm CANCELED vì nó là nhánh riêng).
     * Dùng để render timeline cho khách hàng.
     */
    private static final List<OrderStatus> TIMELINE_STEPS = List.of(
            OrderStatus.PENDING,
            OrderStatus.PAID,
            OrderStatus.PREPARING,
            OrderStatus.READY,
            OrderStatus.COMPLETED);

    // ======================== 1. PUSH REAL-TIME ========================

    /**
     * Gửi tracking event qua WebSocket + lưu vào bảng history.
     *
     * <p>
     * Flow khi method này được gọi:
     * </p>
     * <ol>
     * <li>Tạo record trong bảng order_status_history (để timeline có data)</li>
     * <li>Build OrderTrackingDTO (JSON message)</li>
     * <li>SimpMessagingTemplate push tới /topic/orders/{orderId}</li>
     * <li>Tất cả client đang subscribe → nhận message ngay</li>
     * </ol>
     */
    @Override
    @Transactional
    public void sendTrackingUpdate(UUID orderId, OrderStatus previousStatus, OrderStatus newStatus, UUID updatedBy) {
        // 1. Lưu vào history (timeline data)
        OrderEntity order = orderRepository.findById(orderId).orElse(null);
        if (order != null) {
            OrderStatusHistoryEntity history = OrderStatusHistoryEntity.builder()
                    .order(order)
                    .status(newStatus)
                    .changedAt(LocalDateTime.now())
                    .changedBy(updatedBy)
                    .note(STATUS_MESSAGES.getOrDefault(newStatus, "Trạng thái đơn hàng đã thay đổi"))
                    .build();
            historyRepository.save(history);
        }

        // 2. Push WebSocket message
        OrderTrackingDTO trackingDTO = OrderTrackingDTO.builder()
                .orderId(orderId)
                .previousStatus(previousStatus)
                .currentStatus(newStatus)
                .message(STATUS_MESSAGES.getOrDefault(newStatus, "Trạng thái đơn hàng đã thay đổi"))
                .updatedAt(LocalDateTime.now())
                .updatedBy(updatedBy)
                .build();

        String destination = "/topic/orders/" + orderId;
        messagingTemplate.convertAndSend(destination, trackingDTO);

        log.info("Tracking update sent: order={}, {} -> {}", orderId, previousStatus, newStatus);
    }

    // ======================== 2. UPDATE STATUS (ADMIN) ========================

    /**
     * Admin/Store Manager cập nhật status.
     * Validate state machine → update DB → lưu history → push WebSocket.
     */
    @Override
    @Transactional
    public void updateOrderStatus(UUID orderId, OrderStatus newStatus, UUID updatedBy) {
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ApiException(OrderErrorCode.ORDER_NOT_FOUND));

        OrderStatus currentStatus = order.getStatus();

        // Validate transition
        Set<OrderStatus> allowedNextStatuses = VALID_TRANSITIONS.get(currentStatus);
        if (allowedNextStatuses == null || !allowedNextStatuses.contains(newStatus)) {
            throw new ApiException(OrderErrorCode.INVALID_ORDER_STATUS,
                    String.format("Cannot transition from %s to %s. Allowed: %s",
                            currentStatus, newStatus,
                            allowedNextStatuses != null ? allowedNextStatuses : "none"));
        }

        // Update DB
        order.setStatus(newStatus);
        orderRepository.save(order);

        log.info("Order {} status updated: {} -> {} by {}", orderId, currentStatus, newStatus, updatedBy);

        // Lưu history + push WebSocket
        sendTrackingUpdate(orderId, currentStatus, newStatus, updatedBy);
    }

    // ======================== 3. GET TIMELINE (CUSTOMER) ========================

    /**
     * Lấy timeline tracking — đây là API cho KHÁCH HÀNG xem tiến trình đơn hàng.
     *
     * <p>
     * Cách hoạt động:
     * </p>
     * <ol>
     * <li>Query bảng order_status_history → lấy các bước đã hoàn thành</li>
     * <li>So với danh sách TIMELINE_STEPS đầy đủ</li>
     * <li>Đánh dấu: completed=true (đã qua), current=true (đang ở), completed=false
     * (chưa tới)</li>
     * </ol>
     *
     * <p>
     * Kết quả trả về giống hình tracking DHL/Shopee:
     * </p>
     * 
     * <pre>
     *   ✅ Đơn hàng đã tạo        — 10:00
     *   ✅ Đã thanh toán           — 10:05
     *   ⏳ Đang chuẩn bị           — 10:15   ← ĐANG Ở ĐÂY
     *   ○  Sẵn sàng giao
     *   ○  Hoàn tất
     * </pre>
     */
    @Override
    @Transactional(readOnly = true)
    public OrderTrackingTimelineDTO getTrackingTimeline(UUID orderId, UUID currentUserId, String role) {
        // 1. Tìm order + check quyền
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ApiException(OrderErrorCode.ORDER_NOT_FOUND));

        boolean isManager = "FRANCHISE_ADMIN".equalsIgnoreCase(role)
                || "STORE_MANAGER".equalsIgnoreCase(role);
        if (!isManager && !order.getCustomer().getId().equals(currentUserId)) {
            throw new ApiException(OrderErrorCode.ORDER_NOT_OWNED);
        }

        // 2. Lấy history từ DB
        List<OrderStatusHistoryEntity> historyList = historyRepository.findByOrder_IdOrderByChangedAtAsc(orderId);

        // Tạo map: status → timestamp (lấy thời điểm đầu tiên đạt status đó)
        Map<OrderStatus, LocalDateTime> statusTimestamps = new LinkedHashMap<>();
        for (OrderStatusHistoryEntity h : historyList) {
            statusTimestamps.putIfAbsent(h.getStatus(), h.getChangedAt());
        }

        // 3. Nếu đơn bị CANCELED → timeline riêng
        OrderStatus currentStatus = order.getStatus();
        if (currentStatus == OrderStatus.CANCELED) {
            return buildCanceledTimeline(orderId, currentStatus, statusTimestamps, order);
        }

        // 4. Build timeline bình thường
        int currentIndex = TIMELINE_STEPS.indexOf(currentStatus);
        List<OrderTrackingTimelineDTO.TimelineStep> steps = new ArrayList<>();

        for (int i = 0; i < TIMELINE_STEPS.size(); i++) {
            OrderStatus stepStatus = TIMELINE_STEPS.get(i);
            boolean completed = i <= currentIndex;
            boolean isCurrent = i == currentIndex;

            // Timestamp: lấy từ history, hoặc orderTime cho PENDING
            LocalDateTime timestamp = statusTimestamps.get(stepStatus);
            if (timestamp == null && stepStatus == OrderStatus.PENDING && order.getOrderTime() != null) {
                timestamp = order.getOrderTime();
            }

            steps.add(OrderTrackingTimelineDTO.TimelineStep.builder()
                    .status(stepStatus)
                    .completed(completed)
                    .current(isCurrent)
                    .timestamp(completed ? timestamp : null)
                    .message(STATUS_MESSAGES.getOrDefault(stepStatus, stepStatus.name()))
                    .build());
        }

        return OrderTrackingTimelineDTO.builder()
                .orderId(orderId)
                .currentStatus(currentStatus)
                .timeline(steps)
                .build();
    }

    /** Build timeline cho đơn bị hủy */
    private OrderTrackingTimelineDTO buildCanceledTimeline(
            UUID orderId, OrderStatus currentStatus,
            Map<OrderStatus, LocalDateTime> timestamps, OrderEntity order) {

        List<OrderTrackingTimelineDTO.TimelineStep> steps = new ArrayList<>();

        // Thêm PENDING (luôn có)
        steps.add(OrderTrackingTimelineDTO.TimelineStep.builder()
                .status(OrderStatus.PENDING)
                .completed(true)
                .current(false)
                .timestamp(timestamps.getOrDefault(OrderStatus.PENDING, order.getOrderTime()))
                .message(STATUS_MESSAGES.get(OrderStatus.PENDING))
                .build());

        // Thêm CANCELED
        steps.add(OrderTrackingTimelineDTO.TimelineStep.builder()
                .status(OrderStatus.CANCELED)
                .completed(true)
                .current(true)
                .timestamp(timestamps.get(OrderStatus.CANCELED))
                .message(STATUS_MESSAGES.get(OrderStatus.CANCELED))
                .build());

        return OrderTrackingTimelineDTO.builder()
                .orderId(orderId)
                .currentStatus(currentStatus)
                .timeline(steps)
                .build();
    }
}
