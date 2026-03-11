package fsoft.franchise.dto.orders;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/** Result cho GET /v1/orders: content + pagination. */
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
}
