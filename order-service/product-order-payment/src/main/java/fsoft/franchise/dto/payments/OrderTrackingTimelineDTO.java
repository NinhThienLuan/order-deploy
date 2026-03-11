package fsoft.franchise.dto.payments;

import com.fasterxml.jackson.annotation.JsonFormat;
import fsoft.franchise.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Response cho GET /v1/orders/{id}/tracking — timeline tracking đơn hàng.
 *
 * <p>
 * Trả về cho khách hàng xem timeline giống hình tracking DHL:
 * </p>
 * 
 * <pre>
 *   {
 *     "orderId": "...",
 *     "currentStatus": "PREPARING",
 *     "timeline": [
 *       { "status": "PENDING",   "completed": true,  "timestamp": "...", "message": "Đơn hàng đã tạo" },
 *       { "status": "PAID",      "completed": true,  "timestamp": "...", "message": "Đã thanh toán" },
 *       { "status": "PREPARING", "completed": true,  "timestamp": "...", "message": "Đang chuẩn bị" },
 *       { "status": "READY",     "completed": false, "timestamp": null,  "message": "Sẵn sàng giao" },
 *       { "status": "COMPLETED", "completed": false, "timestamp": null,  "message": "Hoàn tất" }
 *     ]
 *   }
 * </pre>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderTrackingTimelineDTO {

    private UUID orderId;
    private OrderStatus currentStatus;
    private List<TimelineStep> timeline;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TimelineStep {
        /** Tên trạng thái */
        private OrderStatus status;

        /** Đã hoàn thành bước này chưa? (true = ✅, false = ○) */
        private boolean completed;

        /** Đang ở bước này? (true = ⏳ current step) */
        private boolean current;

        /** Thời điểm đạt trạng thái này (null nếu chưa đạt) */
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime timestamp;

        /** Mô tả */
        private String message;
    }
}
