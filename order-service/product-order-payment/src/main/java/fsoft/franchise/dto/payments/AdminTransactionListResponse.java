package fsoft.franchise.dto.payments;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Response for GET /admin/transactions.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AdminTransactionListResponse {

    private List<TransactionItem> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class TransactionItem {
        private UUID transactionId;
        private UUID orderId;
        private UUID customerId;
        private UUID paymentId;
        private String paymentMethod;
        private String paymentStatus;
        private BigDecimal amountPaid;
        private String vnpTxnRef;
        private String vnpTransactionNo;
        private String vnpResponseCode;
        private String vnpBankCode;
        private Instant createdDate;
    }
}
