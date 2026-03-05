package fsoft.franchise.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "vnpay")
@Data
public class VNPayConfig {
    private String tmnCode;
    private String hashSecret;
    private String paymentUrl;
    private String returnUrl;
    private String version;
    private String command;
    private String currencyCode;
    private String locale;
    /**
     * Verify VNPay signature on webhook/return endpoints.
     * Default true; can be disabled in dev for Postman testing.
     */
    private boolean verifySignature = true;
}
