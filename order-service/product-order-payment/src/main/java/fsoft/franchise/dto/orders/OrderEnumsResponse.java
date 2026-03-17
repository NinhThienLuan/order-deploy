package fsoft.franchise.dto.orders;

import fsoft.franchise.enums.MomoRequestType;
import fsoft.franchise.enums.OrderStatus;
import fsoft.franchise.enums.PaymentMethod;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class OrderEnumsResponse {
    private List<OrderStatus> orderStatuses;
    private List<PaymentMethod> paymentMethods;
    private List<String> momoRequestTypes;
    private List<String> orderTypes;
}
