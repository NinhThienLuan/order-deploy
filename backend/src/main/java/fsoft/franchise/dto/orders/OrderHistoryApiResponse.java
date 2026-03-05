package fsoft.franchise.dto.orders;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Response wrapper cho Get Order History API. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderHistoryApiResponse {
    OrderHistoryPage result;
    private boolean isSuccess;
    private int statusCode;
    private String message;
}
