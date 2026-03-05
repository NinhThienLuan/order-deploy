package fsoft.franchise.dto.orders;

import com.fasterxml.jackson.annotation.JsonFormat;
import fsoft.franchise.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/** Một item trong danh sách Order History. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderHistoryItem {
    private UUID id;
    private BigDecimal totalAmount;
    private OrderStatus status;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
}
