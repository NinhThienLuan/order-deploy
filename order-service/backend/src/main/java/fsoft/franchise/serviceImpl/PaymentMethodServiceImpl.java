package fsoft.franchise.serviceImpl;

import fsoft.franchise.common.config.PaymentMethodProperties;
import fsoft.franchise.common.config.PaymentMethodProperties.MethodConfig;
import fsoft.franchise.common.config.PaymentMethodProperties.SubOptionConfig;
import fsoft.franchise.dto.payments.PaymentMethodResponse;
import fsoft.franchise.dto.payments.PaymentMethodResponse.SubOption;
import fsoft.franchise.service.PaymentMethodService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Builds the payment methods list from {@link PaymentMethodProperties}.
 * No business-logic strings are hard-coded — everything comes from
 * {@code application-*.properties}.
 */
@Service
@RequiredArgsConstructor
public class PaymentMethodServiceImpl implements PaymentMethodService {

    private final PaymentMethodProperties properties;

    @Override
    public List<PaymentMethodResponse> getAllPaymentMethods() {
        List<PaymentMethodResponse> result = new ArrayList<>();

        for (Map.Entry<String, MethodConfig> entry : properties.getMethods().entrySet()) {
            String code = entry.getKey().toUpperCase();
            MethodConfig cfg = entry.getValue();

            List<SubOption> subOptions = null;
            if (cfg.getSubOptions() != null && !cfg.getSubOptions().isEmpty()) {
                subOptions = new ArrayList<>();
                for (Map.Entry<String, SubOptionConfig> subEntry : cfg.getSubOptions().entrySet()) {
                    SubOptionConfig sub = subEntry.getValue();
                    subOptions.add(SubOption.builder()
                            .code(subEntry.getKey())
                            .name(sub.getName())
                            .description(sub.getDescription())
                            .enabled(sub.isEnabled() ? null : false) // null when true → omitted by JsonInclude
                            .build());
                }
            }

            result.add(PaymentMethodResponse.builder()
                    .code(code)
                    .name(cfg.getName())
                    .description(cfg.getDescription())
                    .icon(cfg.getIcon())
                    .enabled(cfg.isEnabled() ? null : false)
                    .subOptions(subOptions)
                    .build());
        }

        return result;
    }

    @Override
    public boolean isMomoRequestTypeEnabled(String momoCode) {
        MethodConfig momoConfig = properties.getMethods().get("momo");
        if (momoConfig == null || !momoConfig.isEnabled()) {
            return false;
        }
        if (momoConfig.getSubOptions() == null || momoConfig.getSubOptions().isEmpty()) {
            return true; // no sub-options configured → allow all
        }
        SubOptionConfig sub = momoConfig.getSubOptions().get(momoCode);
        return sub != null && sub.isEnabled();
    }
}
