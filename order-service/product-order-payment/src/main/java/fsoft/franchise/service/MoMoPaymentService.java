package fsoft.franchise.service;

import fsoft.franchise.enums.MomoRequestType;

import java.util.Map;
import java.util.UUID;

/**
 * Service interface for MoMo payment gateway operations.
 */
public interface MoMoPaymentService {

    /**
     * Create a MoMo payment link for the given order.
     *
     * @param orderId     the order UUID
     * @param amount      amount in VND (long, no decimals)
     * @param orderInfo   description shown on MoMo payment page
     * @param requestType MoMo request type code from
     *                    {@link MomoRequestType#getMomoCode()}
     * @return the MoMo pay URL that the frontend should redirect to
     */
    String createPaymentLink(UUID orderId, long amount, String orderInfo, String requestType);

    /**
     * Verify the HMAC signature of a MoMo IPN (Instant Payment Notification)
     * callback.
     *
     * @param params all query/body parameters from MoMo callback
     * @return true if the signature is valid
     */
    boolean verifyCallback(Map<String, String> params);
}
