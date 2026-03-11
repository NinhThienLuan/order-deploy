package fsoft.franchise.serviceImpl;

import fsoft.franchise.common.config.VNPayConfig;
import fsoft.franchise.service.VNPayService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VNPayServiceImpl implements VNPayService {

    private final VNPayConfig config;

    @Override
    public String createPaymentUrl(String orderId, long amount,
                                   String transactionId, String ipAddress) {
        Map<String, String> params = new TreeMap<>();
        params.put("vnp_Version", config.getVersion());
        params.put("vnp_Command", config.getCommand());
        params.put("vnp_TmnCode", config.getTmnCode());
        params.put("vnp_Amount", String.valueOf(amount * 100));
        params.put("vnp_CurrCode", config.getCurrencyCode());
        params.put("vnp_TxnRef", transactionId);
        params.put("vnp_OrderInfo", "Payment for order " + orderId);
        params.put("vnp_OrderType", "other");
        params.put("vnp_Locale", config.getLocale());
        params.put("vnp_ReturnUrl", config.getReturnUrl());
        params.put("vnp_IpAddr", ipAddress);
        params.put("vnp_CreateDate",
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));

        String queryString = buildQueryString(params);
        String secureHash = hmacSHA512(config.getHashSecret(), queryString);

        return config.getPaymentUrl() + "?" + queryString + "&vnp_SecureHash=" + secureHash;
    }

    @Override
    public boolean verifySignature(Map<String, String> params) {
        String receivedHash = params.get("vnp_SecureHash");

        Map<String, String> filteredParams = new TreeMap<>(params);
        filteredParams.remove("vnp_SecureHash");
        filteredParams.remove("vnp_SecureHashType");

        String queryString = buildQueryString(filteredParams);
        String calculatedHash = hmacSHA512(config.getHashSecret(), queryString);

        return calculatedHash.equalsIgnoreCase(receivedHash);
    }

    private String buildQueryString(Map<String, String> params) {
        return params.entrySet().stream()
                .map(e -> e.getKey() + "=" +
                        URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8))
                .collect(Collectors.joining("&"));
    }

    private String hmacSHA512(String key, String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA512");
            mac.init(new SecretKeySpec(
                    key.getBytes(StandardCharsets.UTF_8), "HmacSHA512"));
            byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("HMAC error", e);
        }
    }
}
