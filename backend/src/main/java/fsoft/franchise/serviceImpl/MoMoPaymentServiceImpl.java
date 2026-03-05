package fsoft.franchise.serviceImpl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fsoft.franchise.common.config.MoMoConfig;
import fsoft.franchise.common.exception.ApiException;
import fsoft.franchise.exception.PaymentErrorCode;
import fsoft.franchise.service.MoMoPaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * MoMo payment gateway integration using the MoMo AIO v2 API.
 * <p>
 * Uses the "captureWallet" request type which redirects the user to MoMo's
 * web/app payment page.
 * <p>
 * Sandbox endpoint: https://test-payment.momo.vn/v2/gateway/api/create
 * Production endpoint: https://payment.momo.vn/v2/gateway/api/create
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MoMoPaymentServiceImpl implements MoMoPaymentService {

    private final MoMoConfig moMoConfig;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public String createPaymentLink(UUID orderId, long amount, String orderInfo, String requestType) {
        // requestType is already validated by OrderServiceImpl / PaymentMethodService
        final String resolvedType = requestType;
        try {
            String requestId = UUID.randomUUID().toString();
            String momoOrderId = "MOMO-" + orderId + "-" + System.currentTimeMillis();
            String extraData = Base64.getEncoder().encodeToString(
                    ("{\"orderId\":\"" + orderId + "\"}").getBytes(StandardCharsets.UTF_8));

            // Build raw signature string per MoMo spec
            String rawSignature = "accessKey=" + moMoConfig.getAccessKey()
                    + "&amount=" + amount
                    + "&extraData=" + extraData
                    + "&ipnUrl=" + moMoConfig.getNotifyUrl()
                    + "&orderId=" + momoOrderId
                    + "&orderInfo=" + orderInfo
                    + "&partnerCode=" + moMoConfig.getPartnerCode()
                    + "&redirectUrl=" + moMoConfig.getReturnUrl()
                    + "&requestId=" + requestId
                    + "&requestType=" + resolvedType;

            String signature = hmacSHA256(moMoConfig.getSecretKey(), rawSignature);

            // Build request body
            Map<String, Object> requestBody = new LinkedHashMap<>();
            requestBody.put("partnerCode", moMoConfig.getPartnerCode());
            requestBody.put("accessKey", moMoConfig.getAccessKey());
            requestBody.put("requestId", requestId);
            requestBody.put("amount", amount);
            requestBody.put("orderId", momoOrderId);
            requestBody.put("orderInfo", orderInfo);
            requestBody.put("redirectUrl", moMoConfig.getReturnUrl());
            requestBody.put("ipnUrl", moMoConfig.getNotifyUrl());
            requestBody.put("extraData", extraData);
            requestBody.put("requestType", resolvedType);
            requestBody.put("signature", signature);
            requestBody.put("lang", "vi");

            String jsonBody = OBJECT_MAPPER.writeValueAsString(requestBody);
            log.info("MoMo create payment request: {}", jsonBody);

            // Send HTTP POST to MoMo
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(moMoConfig.getEndpoint() + "/create"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            log.info("MoMo create payment response: {}", response.body());

            JsonNode jsonResponse = OBJECT_MAPPER.readTree(response.body());
            int resultCode = jsonResponse.get("resultCode").asInt();

            if (resultCode == 0) {
                return jsonResponse.get("payUrl").asText();
            } else {
                String message = jsonResponse.has("message") ? jsonResponse.get("message").asText() : "Unknown error";
                log.error("MoMo payment creation failed. resultCode={}, message={}", resultCode, message);
                throw new ApiException(PaymentErrorCode.PAYMENT_PROVIDER_ERROR,
                        "MoMo error (" + resultCode + "): " + message);
            }

        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error creating MoMo payment link", e);
            throw new ApiException(PaymentErrorCode.PAYMENT_PROVIDER_ERROR,
                    "Failed to connect to MoMo: " + e.getMessage());
        }
    }

    @Override
    public boolean verifyCallback(Map<String, String> params) {
        try {
            String receivedSignature = params.get("signature");
            if (receivedSignature == null || receivedSignature.isBlank()) {
                return false;
            }

            // Build raw signature in the same order as MoMo IPN spec
            String rawSignature = "accessKey=" + moMoConfig.getAccessKey()
                    + "&amount=" + params.get("amount")
                    + "&extraData=" + params.get("extraData")
                    + "&message=" + params.get("message")
                    + "&orderId=" + params.get("orderId")
                    + "&orderInfo=" + params.get("orderInfo")
                    + "&orderType=" + params.get("orderType")
                    + "&partnerCode=" + params.get("partnerCode")
                    + "&payType=" + params.get("payType")
                    + "&requestId=" + params.get("requestId")
                    + "&responseTime=" + params.get("responseTime")
                    + "&resultCode=" + params.get("resultCode")
                    + "&transId=" + params.get("transId");

            String computedSignature = hmacSHA256(moMoConfig.getSecretKey(), rawSignature);
            return computedSignature.equals(receivedSignature);
        } catch (Exception e) {
            log.error("Error verifying MoMo callback signature", e);
            return false;
        }
    }

    /**
     * Compute HMAC SHA-256 hex string.
     */
    private String hmacSHA256(String key, String data) throws Exception {
        Mac hmac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        hmac.init(secretKeySpec);
        byte[] hash = hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1)
                hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
