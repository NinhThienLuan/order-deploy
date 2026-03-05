package fsoft.franchise.dto.orders;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response wrapper cho Get Order Status API theo spec: result, isSuccess, statusCode, message.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderStatusApiResponse {
    private OrderStatusResponse result;
    private boolean isSuccess;
    private int statusCode;
    private String message;
}
