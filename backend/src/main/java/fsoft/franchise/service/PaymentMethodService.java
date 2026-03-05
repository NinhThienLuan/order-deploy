package fsoft.franchise.service;

import fsoft.franchise.dto.payments.PaymentMethodResponse;

import java.util.List;

/**
 * Provides the list of available payment methods.
 * Reads from externalised configuration rather than hard-coding.
 */
public interface PaymentMethodService {

    /** Return all configured payment methods (including disabled ones). */
    List<PaymentMethodResponse> getAllPaymentMethods();

    /** Check whether a given MoMo request type code is enabled. */
    boolean isMomoRequestTypeEnabled(String momoCode);
}
