package fsoft.franchise.common.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * MoMo payment gateway configuration.
 * Values loaded from application-{profile}.properties.
 */
@Configuration
@ConfigurationProperties(prefix = "momo")
@Getter
@Setter
public class MoMoConfig {

    /** MoMo partner code (merchant ID) */
    private String partnerCode;

    /** Access key for API authentication */
    private String accessKey;

    /** Secret key for HMAC SHA-256 signature */
    private String secretKey;

    /** MoMo API base endpoint */
    private String endpoint;

    /**
     * URL MoMo redirects to after payment (backend endpoint for fallback DB update)
     */
    private String returnUrl;

    /** URL the backend redirects to after processing MoMo return (frontend) */
    private String frontendUrl;

    /** URL MoMo sends IPN callback to (backend) */
    private String notifyUrl;
}
