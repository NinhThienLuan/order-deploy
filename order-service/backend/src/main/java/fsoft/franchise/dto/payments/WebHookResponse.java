package fsoft.franchise.dto.payments;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

// model/dto/response/WebHookResponse.java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebHookResponse {

    private UUID paymentId;
    private UUID orderId;
    private String transactionId;   // vnp_TxnRef
    private String status;          // SUCCESS / FAILED
    private LocalDateTime processedAt;
}
