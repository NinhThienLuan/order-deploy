package fsoft.franchise.dto.payments;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Response for GET /payments/{order_id}/status.
 * Matches spec: orderId, paymentId, paymentMethod, status, amountPaid, transaction (VNPAY info).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PaymentStatusResponse {

    private UUID orderId;
    private UUID paymentId;
    private String paymentMethod;
    private String status;
    private BigDecimal amountPaid;
    private TransactionInfo transaction;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class TransactionInfo {
        private String vnpTxnRef;
        private String vnpTransactionNo;
        private String vnpResponseCode;
        private String vnpBankCode;
    }
}
