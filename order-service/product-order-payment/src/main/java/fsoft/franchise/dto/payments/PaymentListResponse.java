package fsoft.franchise.dto.payments;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentListResponse {
    private List<PaymentRecord> data;
    private PaginationInfo pagination;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaymentRecord {
        private String transactionId;
        private String orderId;
        private String orderNumber;
        private String customerName;
        private String paymentMethod;
        private BigDecimal amountPaid;
        private Instant paymentDate;
        private String status;
        private String errorMessage;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaginationInfo {
        private int currentPage;
        private int totalPages;
        private long totalElements;
        private int pageSize;
    }
}

