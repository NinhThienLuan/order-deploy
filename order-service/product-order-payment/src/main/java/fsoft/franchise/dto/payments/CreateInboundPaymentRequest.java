package fsoft.franchise.dto.payments;

import fsoft.franchise.enums.PaymentMethod;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;


public record CreateInboundPaymentRequest(

        @NotNull(message = "Order Id is required") String orderId, //INBOUND OrderId

        @NotNull(message = "paymentMethod is required") PaymentMethod paymentMethod,

        @NotNull(message = "amount is required") @Positive(message = "amount must be positive") BigDecimal amount,

        // Optional: MoMo sub-payment channel (captureWallet | payWithATM | payWithCC).
        // Null/blank defaults to captureWallet.
        String momoRequestType
) {
        public String resolvedMomoRequestType() {
        return (momoRequestType == null || momoRequestType.isBlank()) ? "captureWallet" : momoRequestType;
    }
}
