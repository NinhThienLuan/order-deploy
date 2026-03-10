package fsoft.franchise.dto.payments;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.Instant;

/**
 * Filter for GET /admin/transactions (list all transactions for reconciliation).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminTransactionFilterRequest {

    private String status;
    private String paymentMethod;
    private String responseCode;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private Instant fromDate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private Instant toDate;

    @Builder.Default
    private int page = 0;

    @Builder.Default
    private int size = 20;
}
