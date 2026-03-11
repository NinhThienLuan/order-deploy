package fsoft.franchise.common.config;

import fsoft.franchise.enums.PaymentMethod;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Externalised configuration for payment methods.
 * <p>
 * Each key under {@code payment.methods} corresponds to a
 * {@link PaymentMethod} enum value
 * (case-insensitive).
 * 
 * <pre>
 * payment.methods.momo.enabled=true
 * payment.methods.momo.name=MoMo
 * payment.methods.momo.description=Thanh toán qua MoMo
 * payment.methods.momo.icon=momo
 * payment.methods.momo.sub-options.captureWallet.enabled=true
 * </pre>
 */
@Configuration
@ConfigurationProperties(prefix = "payment")
@Getter
@Setter
public class PaymentMethodProperties {

    /**
     * Map of payment method code → config.
     * Keys should match {@code PaymentMethod} enum names (e.g. "momo", "cash").
     */
    private Map<String, MethodConfig> methods = new LinkedHashMap<>();

    @Getter
    @Setter
    public static class MethodConfig {
        private boolean enabled = true;
        private String name;
        private String description;
        private String icon;

        /** Sub-options keyed by MoMo request type code (e.g. "captureWallet"). */
        private Map<String, SubOptionConfig> subOptions = new LinkedHashMap<>();
    }

    @Getter
    @Setter
    public static class SubOptionConfig {
        private boolean enabled = true;
        private String name;
        private String description;
    }
}
