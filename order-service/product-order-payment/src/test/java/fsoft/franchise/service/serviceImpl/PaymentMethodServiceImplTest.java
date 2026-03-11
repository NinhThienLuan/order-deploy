package fsoft.franchise.service.serviceImpl;

import fsoft.franchise.common.config.PaymentMethodProperties;
import fsoft.franchise.common.config.PaymentMethodProperties.MethodConfig;
import fsoft.franchise.common.config.PaymentMethodProperties.SubOptionConfig;
import fsoft.franchise.dto.payments.PaymentMethodResponse;
import fsoft.franchise.serviceImpl.PaymentMethodServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit Tests for PaymentMethodServiceImpl using JUnit 5 and Mockito
 *
 * Coverage target: 80-90%
 * Test all public methods with edge cases and error scenarios
 *
 * @author Dev Team
 * @version 1.0
 * @since 2026-03-04
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentMethodServiceImpl Unit Tests")
class PaymentMethodServiceImplTest {

    // ==================== Mocks ====================
    @Mock
    private PaymentMethodProperties properties;

    @InjectMocks
    private PaymentMethodServiceImpl paymentMethodService;

    // ==================== Test Data ====================
    private Map<String, MethodConfig> methodsMap;
    private MethodConfig momoConfig;
    private MethodConfig cashConfig;
    private MethodConfig vnpayConfig;
    private Map<String, SubOptionConfig> momoSubOptions;

    @BeforeEach
    void setUp() {
        // Initialize test data
        methodsMap = new LinkedHashMap<>();

        // Setup MoMo config with sub-options
        momoConfig = new MethodConfig();
        momoConfig.setEnabled(true);
        momoConfig.setName("MoMo");
        momoConfig.setDescription("Thanh toán qua MoMo");
        momoConfig.setIcon("momo");

        momoSubOptions = new LinkedHashMap<>();

        SubOptionConfig captureWallet = new SubOptionConfig();
        captureWallet.setEnabled(true);
        captureWallet.setName("QR / Ví MoMo");
        captureWallet.setDescription("Quét mã QR hoặc mở app MoMo");
        momoSubOptions.put("captureWallet", captureWallet);

        SubOptionConfig payWithATM = new SubOptionConfig();
        payWithATM.setEnabled(false);
        payWithATM.setName("Thẻ ATM nội địa");
        payWithATM.setDescription("Vietcombank, BIDV, MB...");
        momoSubOptions.put("payWithATM", payWithATM);

        momoConfig.setSubOptions(momoSubOptions);
        methodsMap.put("momo", momoConfig);

        // Setup CASH config (no sub-options)
        cashConfig = new MethodConfig();
        cashConfig.setEnabled(true);
        cashConfig.setName("Tiền mặt");
        cashConfig.setDescription("Thanh toán bằng tiền mặt");
        cashConfig.setIcon("cash");
        cashConfig.setSubOptions(new LinkedHashMap<>());
        methodsMap.put("cash", cashConfig);

        // Setup VNPay config
        vnpayConfig = new MethodConfig();
        vnpayConfig.setEnabled(true);
        vnpayConfig.setName("VNPay");
        vnpayConfig.setDescription("Thanh toán qua VNPay");
        vnpayConfig.setIcon("vnpay");
        vnpayConfig.setSubOptions(new LinkedHashMap<>());
        methodsMap.put("vnpay", vnpayConfig);

        // Mock properties
        when(properties.getMethods()).thenReturn(methodsMap);
    }

    // ==================== getAllPaymentMethods() Tests ====================
    @Nested
    @DisplayName("getAllPaymentMethods() Tests")
    class GetAllPaymentMethodsTests {

        @Test
        @DisplayName("Should return all enabled payment methods")
        void shouldReturnAllEnabledPaymentMethods() {
            // When
            List<PaymentMethodResponse> result = paymentMethodService.getAllPaymentMethods();

            // Then
            assertNotNull(result);
            assertEquals(3, result.size());

            // Verify method codes are uppercase
            assertTrue(result.stream().anyMatch(m -> m.getCode().equals("MOMO")));
            assertTrue(result.stream().anyMatch(m -> m.getCode().equals("CASH")));
            assertTrue(result.stream().anyMatch(m -> m.getCode().equals("VNPAY")));

            verify(properties).getMethods();
        }

        @Test
        @DisplayName("Should map method config to response DTO correctly")
        void shouldMapMethodConfigToResponseDtoCorrectly() {
            // When
            List<PaymentMethodResponse> result = paymentMethodService.getAllPaymentMethods();

            // Then
            PaymentMethodResponse momo = result.stream()
                    .filter(m -> m.getCode().equals("MOMO"))
                    .findFirst()
                    .orElse(null);

            assertNotNull(momo);
            assertEquals("MOMO", momo.getCode());
            assertEquals("MoMo", momo.getName());
            assertEquals("Thanh toán qua MoMo", momo.getDescription());
            assertEquals("momo", momo.getIcon());
            assertNull(momo.getEnabled()); // null when true (JsonInclude)
        }

        @Test
        @DisplayName("Should include sub-options for methods with sub-options")
        void shouldIncludeSubOptionsForMethodsWithSubOptions() {
            // When
            List<PaymentMethodResponse> result = paymentMethodService.getAllPaymentMethods();

            // Then
            PaymentMethodResponse momo = result.stream()
                    .filter(m -> m.getCode().equals("MOMO"))
                    .findFirst()
                    .orElse(null);

            assertNotNull(momo);
            assertNotNull(momo.getSubOptions());
            assertEquals(2, momo.getSubOptions().size());

            // Verify sub-options
            PaymentMethodResponse.SubOption captureWallet = momo.getSubOptions().stream()
                    .filter(s -> s.getCode().equals("captureWallet"))
                    .findFirst()
                    .orElse(null);

            assertNotNull(captureWallet);
            assertEquals("captureWallet", captureWallet.getCode());
            assertEquals("QR / Ví MoMo", captureWallet.getName());
            assertEquals("Quét mã QR hoặc mở app MoMo", captureWallet.getDescription());
            assertNull(captureWallet.getEnabled()); // null when true
        }

        @Test
        @DisplayName("Should set enabled=false for disabled sub-options")
        void shouldSetEnabledFalseForDisabledSubOptions() {
            // When
            List<PaymentMethodResponse> result = paymentMethodService.getAllPaymentMethods();

            // Then
            PaymentMethodResponse momo = result.stream()
                    .filter(m -> m.getCode().equals("MOMO"))
                    .findFirst()
                    .orElse(null);

            PaymentMethodResponse.SubOption payWithATM = momo.getSubOptions().stream()
                    .filter(s -> s.getCode().equals("payWithATM"))
                    .findFirst()
                    .orElse(null);

            assertNotNull(payWithATM);
            assertNotNull(payWithATM.getEnabled());
            assertFalse(payWithATM.getEnabled());
        }

        @Test
        @DisplayName("Should return null sub-options for methods without sub-options")
        void shouldReturnNullSubOptionsForMethodsWithoutSubOptions() {
            // When
            List<PaymentMethodResponse> result = paymentMethodService.getAllPaymentMethods();

            // Then
            PaymentMethodResponse cash = result.stream()
                    .filter(m -> m.getCode().equals("CASH"))
                    .findFirst()
                    .orElse(null);

            assertNotNull(cash);
            assertNull(cash.getSubOptions()); // null when no sub-options
        }

        @Test
        @DisplayName("Should handle empty methods map")
        void shouldHandleEmptyMethodsMap() {
            // Given
            when(properties.getMethods()).thenReturn(new LinkedHashMap<>());

            // When
            List<PaymentMethodResponse> result = paymentMethodService.getAllPaymentMethods();

            // Then
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Should convert method code to uppercase")
        void shouldConvertMethodCodeToUppercase() {
            // When
            List<PaymentMethodResponse> result = paymentMethodService.getAllPaymentMethods();

            // Then
            // Keys in methodsMap are lowercase, but response should be uppercase
            assertTrue(result.stream().allMatch(m -> m.getCode().equals(m.getCode().toUpperCase())));
            assertTrue(result.stream().anyMatch(m -> m.getCode().equals("MOMO")));
            assertTrue(result.stream().anyMatch(m -> m.getCode().equals("CASH")));
            assertTrue(result.stream().anyMatch(m -> m.getCode().equals("VNPAY")));
        }

        @Test
        @DisplayName("Should set enabled=false for disabled methods")
        void shouldSetEnabledFalseForDisabledMethods() {
            // Given
            MethodConfig disabledMethod = new MethodConfig();
            disabledMethod.setEnabled(false);
            disabledMethod.setName("PayOS");
            disabledMethod.setDescription("Thanh toán qua PayOS");
            disabledMethod.setIcon("payos");
            methodsMap.put("payos", disabledMethod);

            // When
            List<PaymentMethodResponse> result = paymentMethodService.getAllPaymentMethods();

            // Then
            PaymentMethodResponse payos = result.stream()
                    .filter(m -> m.getCode().equals("PAYOS"))
                    .findFirst()
                    .orElse(null);

            assertNotNull(payos);
            assertNotNull(payos.getEnabled());
            assertFalse(payos.getEnabled());
        }

        @Test
        @DisplayName("Should handle null sub-options map")
        void shouldHandleNullSubOptionsMap() {
            // Given
            cashConfig.setSubOptions(null);

            // When
            List<PaymentMethodResponse> result = paymentMethodService.getAllPaymentMethods();

            // Then
            PaymentMethodResponse cash = result.stream()
                    .filter(m -> m.getCode().equals("CASH"))
                    .findFirst()
                    .orElse(null);

            assertNotNull(cash);
            assertNull(cash.getSubOptions());
        }

        @Test
        @DisplayName("Should preserve insertion order from LinkedHashMap")
        void shouldPreserveInsertionOrder() {
            // Given - methodsMap is LinkedHashMap, order: momo, cash, vnpay

            // When
            List<PaymentMethodResponse> result = paymentMethodService.getAllPaymentMethods();

            // Then - should preserve order
            assertEquals("MOMO", result.get(0).getCode());
            assertEquals("CASH", result.get(1).getCode());
            assertEquals("VNPAY", result.get(2).getCode());
        }

        @Test
        @DisplayName("Should handle method with all null fields except enabled")
        void shouldHandleMethodWithNullFields() {
            // Given
            MethodConfig nullFieldsConfig = new MethodConfig();
            nullFieldsConfig.setEnabled(true);
            nullFieldsConfig.setName(null);
            nullFieldsConfig.setDescription(null);
            nullFieldsConfig.setIcon(null);
            methodsMap.put("test", nullFieldsConfig);

            // When
            List<PaymentMethodResponse> result = paymentMethodService.getAllPaymentMethods();

            // Then
            PaymentMethodResponse test = result.stream()
                    .filter(m -> m.getCode().equals("TEST"))
                    .findFirst()
                    .orElse(null);

            assertNotNull(test);
            assertEquals("TEST", test.getCode());
            assertNull(test.getName());
            assertNull(test.getDescription());
            assertNull(test.getIcon());
        }

        @Test
        @DisplayName("Should handle multiple sub-options")
        void shouldHandleMultipleSubOptions() {
            // Given - Add more sub-options to momo
            SubOptionConfig payWithCC = new SubOptionConfig();
            payWithCC.setEnabled(true);
            payWithCC.setName("Thẻ quốc tế");
            payWithCC.setDescription("Visa, Mastercard, JCB");
            momoSubOptions.put("payWithCC", payWithCC);

            // When
            List<PaymentMethodResponse> result = paymentMethodService.getAllPaymentMethods();

            // Then
            PaymentMethodResponse momo = result.stream()
                    .filter(m -> m.getCode().equals("MOMO"))
                    .findFirst()
                    .orElse(null);

            assertNotNull(momo.getSubOptions());
            assertEquals(3, momo.getSubOptions().size());
        }
    }

    // ==================== isMomoRequestTypeEnabled() Tests ====================
    @Nested
    @DisplayName("isMomoRequestTypeEnabled() Tests")
    class IsMomoRequestTypeEnabledTests {

        @Test
        @DisplayName("Should return true for enabled MoMo request type")
        void shouldReturnTrueForEnabledMomoRequestType() {
            // When
            boolean result = paymentMethodService.isMomoRequestTypeEnabled("captureWallet");

            // Then
            assertTrue(result);
        }

        @Test
        @DisplayName("Should return false for disabled MoMo request type")
        void shouldReturnFalseForDisabledMomoRequestType() {
            // When
            boolean result = paymentMethodService.isMomoRequestTypeEnabled("payWithATM");

            // Then
            assertFalse(result);
        }

        @Test
        @DisplayName("Should return false when MoMo config is null")
        void shouldReturnFalseWhenMomoConfigIsNull() {
            // Given
            methodsMap.remove("momo");

            // When
            boolean result = paymentMethodService.isMomoRequestTypeEnabled("captureWallet");

            // Then
            assertFalse(result);
        }

        @Test
        @DisplayName("Should return false when MoMo is disabled")
        void shouldReturnFalseWhenMomoIsDisabled() {
            // Given
            momoConfig.setEnabled(false);

            // When
            boolean result = paymentMethodService.isMomoRequestTypeEnabled("captureWallet");

            // Then
            assertFalse(result);
        }

        @Test
        @DisplayName("Should return true when sub-options is null (allow all)")
        void shouldReturnTrueWhenSubOptionsIsNull() {
            // Given
            momoConfig.setSubOptions(null);

            // When
            boolean result = paymentMethodService.isMomoRequestTypeEnabled("anyCode");

            // Then
            assertTrue(result); // No sub-options configured → allow all
        }

        @Test
        @DisplayName("Should return true when sub-options is empty (allow all)")
        void shouldReturnTrueWhenSubOptionsIsEmpty() {
            // Given
            momoConfig.setSubOptions(new LinkedHashMap<>());

            // When
            boolean result = paymentMethodService.isMomoRequestTypeEnabled("anyCode");

            // Then
            assertTrue(result); // Empty sub-options → allow all
        }

        @Test
        @DisplayName("Should return false for non-existent request type")
        void shouldReturnFalseForNonExistentRequestType() {
            // When
            boolean result = paymentMethodService.isMomoRequestTypeEnabled("nonExistent");

            // Then
            assertFalse(result);
        }

        @Test
        @DisplayName("Should handle case-sensitive request type codes")
        void shouldHandleCaseSensitiveRequestTypeCodes() {
            // When
            boolean result1 = paymentMethodService.isMomoRequestTypeEnabled("captureWallet");
            boolean result2 = paymentMethodService.isMomoRequestTypeEnabled("CaptureWallet");

            // Then
            assertTrue(result1);
            assertFalse(result2); // Case-sensitive, should not match
        }

        @Test
        @DisplayName("Should return false for null request type")
        void shouldReturnFalseForNullRequestType() {
            // When
            boolean result = paymentMethodService.isMomoRequestTypeEnabled(null);

            // Then
            assertFalse(result);
        }

        @Test
        @DisplayName("Should return false for empty string request type")
        void shouldReturnFalseForEmptyStringRequestType() {
            // When
            boolean result = paymentMethodService.isMomoRequestTypeEnabled("");

            // Then
            assertFalse(result);
        }

        @Test
        @DisplayName("Should verify all enabled sub-options")
        void shouldVerifyAllEnabledSubOptions() {
            // Given - Add more enabled sub-options
            SubOptionConfig payWithCC = new SubOptionConfig();
            payWithCC.setEnabled(true);
            momoSubOptions.put("payWithCC", payWithCC);

            // When & Then
            assertTrue(paymentMethodService.isMomoRequestTypeEnabled("captureWallet"));
            assertTrue(paymentMethodService.isMomoRequestTypeEnabled("payWithCC"));
            assertFalse(paymentMethodService.isMomoRequestTypeEnabled("payWithATM")); // disabled
        }

        @Test
        @DisplayName("Should handle sub-option with enabled=true explicitly")
        void shouldHandleSubOptionWithEnabledTrue() {
            // Given
            SubOptionConfig explicitEnabled = new SubOptionConfig();
            explicitEnabled.setEnabled(true);
            momoSubOptions.put("explicit", explicitEnabled);

            // When
            boolean result = paymentMethodService.isMomoRequestTypeEnabled("explicit");

            // Then
            assertTrue(result);
        }

        @Test
        @DisplayName("Should handle sub-option with enabled=false explicitly")
        void shouldHandleSubOptionWithEnabledFalse() {
            // Given
            SubOptionConfig explicitDisabled = new SubOptionConfig();
            explicitDisabled.setEnabled(false);
            momoSubOptions.put("disabled", explicitDisabled);

            // When
            boolean result = paymentMethodService.isMomoRequestTypeEnabled("disabled");

            // Then
            assertFalse(result);
        }

        @Test
        @DisplayName("Should return consistent results for multiple calls")
        void shouldReturnConsistentResultsForMultipleCalls() {
            // When - Call multiple times
            boolean result1 = paymentMethodService.isMomoRequestTypeEnabled("captureWallet");
            boolean result2 = paymentMethodService.isMomoRequestTypeEnabled("captureWallet");
            boolean result3 = paymentMethodService.isMomoRequestTypeEnabled("captureWallet");

            // Then - Should be consistent
            assertTrue(result1);
            assertTrue(result2);
            assertTrue(result3);
        }

        @Test
        @DisplayName("Should check MoMo enabled status before sub-options")
        void shouldCheckMomoEnabledStatusBeforeSubOptions() {
            // Given - MoMo disabled but sub-option enabled
            momoConfig.setEnabled(false);

            // When
            boolean result = paymentMethodService.isMomoRequestTypeEnabled("captureWallet");

            // Then - Should return false because MoMo itself is disabled
            assertFalse(result);
        }
    }
}

