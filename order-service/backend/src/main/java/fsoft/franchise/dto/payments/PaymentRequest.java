package fsoft.franchise.dto.payments;

import fsoft.franchise.enums.MomoRequestType;
import fsoft.franchise.enums.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

/**
 * Request DTO for payment.
 * <p>
 * When paymentMethod = MOMO, optionally set {@code momoRequestType} to choose
 * the payment channel. Valid values are defined in {@link MomoRequestType}.
 */
public record PaymentRequest(
        @NotNull(message = "Payment method is required") PaymentMethod paymentMethod,

        @NotNull(message = "Amount is required") @Positive(message = "Amount must be positive") BigDecimal amount,

        String momoRequestType) {

    /** Default constructor without momoRequestType (backwards compatible) */
    public PaymentRequest(PaymentMethod paymentMethod, BigDecimal amount) {
        this(paymentMethod, amount, null);
    }

    /**
     * Resolve the MoMo request type.
     * Returns the matching {@link MomoRequestType}, or the default
     * ({@code captureWallet})
     * if not specified or unrecognised.
     */
    public MomoRequestType resolvedMomoRequestType() {
        MomoRequestType resolved = MomoRequestType.fromMomoCode(momoRequestType);
        return resolved != null ? resolved : MomoRequestType.defaultType();
    }

    public static boolean isValid(String method) {
        if (method == null)
            return false;
        for (PaymentMethod pm : PaymentMethod.values()) {
            if (pm.name().equalsIgnoreCase(method.replace(" ", "_"))) {
                return true;
            }
        }
        return false;
    }
}
