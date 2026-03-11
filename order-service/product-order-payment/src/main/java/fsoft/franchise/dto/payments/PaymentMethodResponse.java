package fsoft.franchise.dto.payments;

import com.fasterxml.jackson.annotation.JsonInclude;
import fsoft.franchise.enums.PaymentMethod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response DTO representing a single payment method (e.g. MOMO, CASH).
 * Returned by {@code GET /v1/payments/methods}.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PaymentMethodResponse {

    /** Enum code — matches {@link PaymentMethod}. */
    private String code;

    /** Display name (e.g. "MoMo", "Tiền mặt"). */
    private String name;

    /** Short description for the user. */
    private String description;

    /** Icon key for FE rendering. */
    private String icon;

    /**
     * Whether this method is currently enabled.
     * {@code null} or {@code true} → enabled. {@code false} → disabled.
     */
    private Boolean enabled;

    /**
     * Sub-options (e.g. MoMo QR, ATM, CC). Only present for gateways with multiple
     * channels.
     */
    private List<SubOption> subOptions;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class SubOption {
        private String code;
        private String name;
        private String description;
        private Boolean enabled;
    }
}
