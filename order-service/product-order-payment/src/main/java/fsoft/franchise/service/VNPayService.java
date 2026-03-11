package fsoft.franchise.service;

import java.util.Map;

public interface VNPayService {
    String createPaymentUrl(String orderId, long amount,
                            String transactionId, String ipAddress);
    boolean verifySignature(Map<String, String> params);
}
