package fsoft.franchise.dto.orders;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/** Result cho GET /v1/orders: content + pagination + tổng tiền theo filter. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderHistoryPage {
    private List<OrderHistoryItem> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    /** Tổng giá tiền của tất cả đơn hàng thỏa bộ lọc (status, storeId, fromDate, toDate). */
    private BigDecimal totalAmount;
}
