package fsoft.franchise.dto.payments;

import fsoft.franchise.enums.PaymentMethod;
import fsoft.franchise.enums.PaymentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

public record CreatePaymentRequest(

        @NotNull(message = "orderId is required")
        UUID orderId,

        @NotNull(message = "paymentMethod is required")
        PaymentMethod paymentMethod,

        @NotNull(message = "amount is required")
        @Positive(message = "amount must be positive")
        BigDecimal amount,

        @NotBlank(message = "transactionId is required")
        String transactionId,    // client tự generate, ví dụ: "TXN-20260222-001"

        @NotNull(message = "paymentType is required")
        PaymentType paymentType, //PRODUCT, INBOUND

        // Optional: MoMo sub-payment channel (captureWallet | payWithATM | payWithCC).
        // Null/blank defaults to captureWallet.
        String momoRequestType
) {
    public String resolvedMomoRequestType() {
        return (momoRequestType == null || momoRequestType.isBlank()) ? "captureWallet" : momoRequestType;
    }
}
