package fsoft.franchise.service.serviceImpl;

import fsoft.franchise.common.config.VNPayConfig;
import fsoft.franchise.serviceImpl.VNPayServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit Tests for VNPayServiceImpl using JUnit 5 and Mockito
 *
 * Coverage target: 90-95%
 * Test payment URL creation and signature verification for VNPay integration
 *
 * @author Dev Team
 * @version 1.0
 * @since 2026-03-04
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("VNPayServiceImpl Unit Tests")
class VNPayServiceImplTest {

    // ==================== Mocks ====================
    @Mock
    private VNPayConfig vnPayConfig;

    private VNPayServiceImpl vnPayService;

    // ==================== Test Data ====================
    private static final String TEST_TMN_CODE = "9PJF2F6F";
    private static final String TEST_HASH_SECRET = "PSCVGW8CL9PVQXY3HKS8ASF1XUR8VYGI";
    private static final String TEST_PAYMENT_URL = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html";
    private static final String TEST_RETURN_URL = "http://localhost:8080/v1/payments/vnpay-return";
    private static final String TEST_VERSION = "2.1.0";
    private static final String TEST_COMMAND = "pay";
    private static final String TEST_CURRENCY_CODE = "VND";
    private static final String TEST_LOCALE = "vn";

    private String orderId;
    private long amount;
    private String transactionId;
    private String ipAddress;
    private Map<String, String> vnpayParams;

    @BeforeEach
    void setUp() {
        // Setup test data
        orderId = "550e8400-e29b-41d4-a716-446655440000";
        amount = 100000L;
        transactionId = "TXN-2026-001";
        ipAddress = "192.168.1.1";

        // Setup VNPayConfig mock - use lenient() for methods called multiple times
        lenient().when(vnPayConfig.getTmnCode()).thenReturn(TEST_TMN_CODE);
        lenient().when(vnPayConfig.getHashSecret()).thenReturn(TEST_HASH_SECRET);
        lenient().when(vnPayConfig.getPaymentUrl()).thenReturn(TEST_PAYMENT_URL);
        lenient().when(vnPayConfig.getReturnUrl()).thenReturn(TEST_RETURN_URL);
        lenient().when(vnPayConfig.getVersion()).thenReturn(TEST_VERSION);
        lenient().when(vnPayConfig.getCommand()).thenReturn(TEST_COMMAND);
        lenient().when(vnPayConfig.getCurrencyCode()).thenReturn(TEST_CURRENCY_CODE);
        lenient().when(vnPayConfig.getLocale()).thenReturn(TEST_LOCALE);

        // Manually create service with mocked config
        vnPayService = new VNPayServiceImpl(vnPayConfig);

        // Setup VNPay callback params
        vnpayParams = new TreeMap<>();
        vnpayParams.put("vnp_TmnCode", TEST_TMN_CODE);
        vnpayParams.put("vnp_Amount", "10000000"); // 100000 * 100
        vnpayParams.put("vnp_TxnRef", transactionId);
        vnpayParams.put("vnp_OrderInfo", "Payment for order " + orderId);
        vnpayParams.put("vnp_ResponseCode", "00");
        vnpayParams.put("vnp_TransactionNo", "14523876");
        vnpayParams.put("vnp_BankCode", "NCB");
    }

    // ==================== createPaymentUrl() Tests ====================
    @Nested
    @DisplayName("createPaymentUrl() Tests")
    class CreatePaymentUrlTests {

        @Test
        @DisplayName("Should create payment URL successfully")
        void shouldCreatePaymentUrlSuccessfully() {
            // When
            String result = vnPayService.createPaymentUrl(orderId, amount, transactionId, ipAddress);

            // Then
            assertNotNull(result);
            assertTrue(result.startsWith(TEST_PAYMENT_URL + "?"));
            assertTrue(result.contains("vnp_TmnCode=" + TEST_TMN_CODE));
            assertTrue(result.contains("vnp_Amount=" + (amount * 100)));
            assertTrue(result.contains("vnp_TxnRef=" + transactionId));
            assertTrue(result.contains("vnp_SecureHash="));

            // Verify config methods called
            verify(vnPayConfig).getTmnCode();
            verify(vnPayConfig).getHashSecret();
            verify(vnPayConfig).getPaymentUrl();
            verify(vnPayConfig).getReturnUrl();
            verify(vnPayConfig).getVersion();
            verify(vnPayConfig).getCommand();
            verify(vnPayConfig).getCurrencyCode();
            verify(vnPayConfig).getLocale();
        }

        @Test
        @DisplayName("Should include all required VNPay parameters")
        void shouldIncludeAllRequiredVnpayParameters() {
            // When
            String result = vnPayService.createPaymentUrl(orderId, amount, transactionId, ipAddress);

            // Then
            assertTrue(result.contains("vnp_Version="));
            assertTrue(result.contains("vnp_Command="));
            assertTrue(result.contains("vnp_TmnCode="));
            assertTrue(result.contains("vnp_Amount="));
            assertTrue(result.contains("vnp_CurrCode="));
            assertTrue(result.contains("vnp_TxnRef="));
            assertTrue(result.contains("vnp_OrderInfo="));
            assertTrue(result.contains("vnp_OrderType="));
            assertTrue(result.contains("vnp_Locale="));
            assertTrue(result.contains("vnp_ReturnUrl="));
            assertTrue(result.contains("vnp_IpAddr="));
            assertTrue(result.contains("vnp_CreateDate="));
            assertTrue(result.contains("vnp_SecureHash="));
        }

        @Test
        @DisplayName("Should multiply amount by 100 for VNPay format")
        void shouldMultiplyAmountBy100ForVnpayFormat() {
            // Given
            long testAmount = 500000L;

            // When
            String result = vnPayService.createPaymentUrl(orderId, testAmount, transactionId, ipAddress);

            // Then
            assertTrue(result.contains("vnp_Amount=" + (testAmount * 100)));
        }

        @Test
        @DisplayName("Should include order ID in vnp_OrderInfo")
        void shouldIncludeOrderIdInVnpOrderInfo() {
            // When
            String result = vnPayService.createPaymentUrl(orderId, amount, transactionId, ipAddress);

            // Then
            assertTrue(result.contains("vnp_OrderInfo=Payment+for+order+" + orderId));
        }

        @Test
        @DisplayName("Should use transaction ID as vnp_TxnRef")
        void shouldUseTransactionIdAsVnpTxnRef() {
            // When
            String result = vnPayService.createPaymentUrl(orderId, amount, transactionId, ipAddress);

            // Then
            assertTrue(result.contains("vnp_TxnRef=" + transactionId));
        }

        @Test
        @DisplayName("Should include IP address")
        void shouldIncludeIpAddress() {
            // When
            String result = vnPayService.createPaymentUrl(orderId, amount, transactionId, ipAddress);

            // Then
            assertTrue(result.contains("vnp_IpAddr=" + ipAddress));
        }

        @Test
        @DisplayName("Should set vnp_OrderType to 'other'")
        void shouldSetVnpOrderTypeToOther() {
            // When
            String result = vnPayService.createPaymentUrl(orderId, amount, transactionId, ipAddress);

            // Then
            assertTrue(result.contains("vnp_OrderType=other"));
        }

        @Test
        @DisplayName("Should include vnp_CreateDate in yyyyMMddHHmmss format")
        void shouldIncludeVnpCreateDateInCorrectFormat() {
            // When
            String result = vnPayService.createPaymentUrl(orderId, amount, transactionId, ipAddress);

            // Then
            assertTrue(result.contains("vnp_CreateDate="));
            // Extract createDate value
            String createDate = extractParamValue(result, "vnp_CreateDate");
            assertNotNull(createDate);
            assertEquals(14, createDate.length()); // yyyyMMddHHmmss = 14 digits
            assertTrue(createDate.matches("\\d{14}")); // All digits
        }

        @Test
        @DisplayName("Should generate valid HMAC-SHA512 signature")
        void shouldGenerateValidHmacSha512Signature() {
            // When
            String result = vnPayService.createPaymentUrl(orderId, amount, transactionId, ipAddress);

            // Then
            String secureHash = extractParamValue(result, "vnp_SecureHash");
            assertNotNull(secureHash);
            assertEquals(128, secureHash.length()); // SHA512 hex = 128 chars
            assertTrue(secureHash.matches("[0-9a-f]{128}")); // Lowercase hex
        }

        @Test
        @DisplayName("Should URL encode special characters in parameters")
        void shouldUrlEncodeSpecialCharactersInParameters() {
            // Given
            String specialOrderId = "order-123 with spaces & special=chars";

            // When
            String result = vnPayService.createPaymentUrl(specialOrderId, amount, transactionId, ipAddress);

            // Then
            assertFalse(result.contains(" ")); // Spaces should be encoded
            assertFalse(result.contains("&order")); // & should be encoded (except separators)
        }

        @Test
        @DisplayName("Should handle zero amount")
        void shouldHandleZeroAmount() {
            // Given
            long zeroAmount = 0L;

            // When
            String result = vnPayService.createPaymentUrl(orderId, zeroAmount, transactionId, ipAddress);

            // Then
            assertTrue(result.contains("vnp_Amount=0"));
        }

        @Test
        @DisplayName("Should handle large amount")
        void shouldHandleLargeAmount() {
            // Given
            long largeAmount = 999999999L;

            // When
            String result = vnPayService.createPaymentUrl(orderId, largeAmount, transactionId, ipAddress);

            // Then
            assertTrue(result.contains("vnp_Amount=" + (largeAmount * 100)));
        }

        @Test
        @DisplayName("Should use configured values from VNPayConfig")
        void shouldUseConfiguredValuesFromVnpayConfig() {
            // When
            String result = vnPayService.createPaymentUrl(orderId, amount, transactionId, ipAddress);

            // Then
            assertTrue(result.contains(TEST_TMN_CODE));
            assertTrue(result.contains(TEST_VERSION));
            assertTrue(result.contains(TEST_COMMAND));
            assertTrue(result.contains(TEST_CURRENCY_CODE));
            assertTrue(result.contains(TEST_LOCALE));
            assertTrue(result.startsWith(TEST_PAYMENT_URL));
        }

        @Test
        @DisplayName("Should handle IPv6 address")
        void shouldHandleIpv6Address() {
            // Given
            String ipv6Address = "2001:0db8:85a3:0000:0000:8a2e:0370:7334";

            // When
            String result = vnPayService.createPaymentUrl(orderId, amount, transactionId, ipv6Address);

            // Then
            assertNotNull(result);
            assertTrue(result.contains("vnp_IpAddr="));
        }
    }

    // ==================== verifySignature() Tests ====================
    @Nested
    @DisplayName("verifySignature() Tests")
    class VerifySignatureTests {


        @Test
        @DisplayName("Should verify valid signature successfully")
        void shouldVerifyValidSignatureSuccessfully() {
            // Given - Use fresh copy of params
            Map<String, String> testParams = new TreeMap<>(vnpayParams);
            String queryString = buildQueryString(testParams);
            String validHash = computeHmacSHA512(TEST_HASH_SECRET, queryString);
            testParams.put("vnp_SecureHash", validHash);

            // When
            boolean result = vnPayService.verifySignature(testParams);

            // Then
            assertTrue(result);
            verify(vnPayConfig).getHashSecret();
        }

        @Test
        @DisplayName("Should reject invalid signature")
        void shouldRejectInvalidSignature() {
            // Given - Use fresh copy
            Map<String, String> testParams = new TreeMap<>(vnpayParams);
            testParams.put("vnp_SecureHash", "invalid_signature_hash");

            // When
            boolean result = vnPayService.verifySignature(testParams);

            // Then
            assertFalse(result);
        }

        @Test
        @DisplayName("Should exclude vnp_SecureHash from signature calculation")
        void shouldExcludeVnpSecureHashFromSignatureCalculation() {
            // Given - Use fresh copy
            Map<String, String> testParams = new TreeMap<>(vnpayParams);
            String queryString = buildQueryString(testParams);
            String validHash = computeHmacSHA512(TEST_HASH_SECRET, queryString);
            testParams.put("vnp_SecureHash", validHash);

            // When
            boolean result = vnPayService.verifySignature(testParams);

            // Then
            assertTrue(result);
            // vnp_SecureHash should not be included in hash calculation
        }

        @Test
        @DisplayName("Should exclude vnp_SecureHashType from signature calculation")
        void shouldExcludeVnpSecureHashTypeFromSignatureCalculation() {
            // Given - Use fresh copy
            Map<String, String> testParams = new TreeMap<>(vnpayParams);
            String queryString = buildQueryString(testParams);
            String validHash = computeHmacSHA512(TEST_HASH_SECRET, queryString);
            testParams.put("vnp_SecureHash", validHash);
            testParams.put("vnp_SecureHashType", "HmacSHA512");

            // When
            boolean result = vnPayService.verifySignature(testParams);

            // Then
            assertTrue(result);
            // vnp_SecureHashType should not be included in hash calculation
        }

        @Test
        @DisplayName("Should handle case-insensitive hash comparison")
        void shouldHandleCaseInsensitiveHashComparison() {
            // Given - Use fresh copy
            Map<String, String> testParams = new TreeMap<>(vnpayParams);
            String queryString = buildQueryString(testParams);
            String validHash = computeHmacSHA512(TEST_HASH_SECRET, queryString);
            testParams.put("vnp_SecureHash", validHash.toUpperCase()); // Uppercase

            // When
            boolean result = vnPayService.verifySignature(testParams);

            // Then
            assertTrue(result); // Should still be valid (case-insensitive)
        }

        @Test
        @DisplayName("Should use TreeMap to maintain parameter order")
        void shouldUseTreeMapToMaintainParameterOrder() {
            // Given - Params in random order
            Map<String, String> randomOrderParams = new HashMap<>();
            randomOrderParams.put("vnp_TxnRef", transactionId);
            randomOrderParams.put("vnp_Amount", "10000000");
            randomOrderParams.put("vnp_BankCode", "NCB");
            randomOrderParams.put("vnp_TmnCode", TEST_TMN_CODE);
            randomOrderParams.put("vnp_OrderInfo", "Payment for order " + orderId);

            // Create valid hash with TreeMap (sorted)
            Map<String, String> sortedParams = new TreeMap<>(randomOrderParams);
            String queryString = buildQueryString(sortedParams);
            String validHash = computeHmacSHA512(TEST_HASH_SECRET, queryString);
            randomOrderParams.put("vnp_SecureHash", validHash);

            // When
            boolean result = vnPayService.verifySignature(randomOrderParams);

            // Then
            assertTrue(result); // Should handle sorting internally
        }

        @Test
        @DisplayName("Should reject signature with modified amount")
        void shouldRejectSignatureWithModifiedAmount() {
            // Given - Use fresh copy
            Map<String, String> testParams = new TreeMap<>(vnpayParams);
            String queryString = buildQueryString(testParams);
            String validHash = computeHmacSHA512(TEST_HASH_SECRET, queryString);
            testParams.put("vnp_SecureHash", validHash);

            // Modify amount after signature created
            testParams.put("vnp_Amount", "999999999");

            // When
            boolean result = vnPayService.verifySignature(testParams);

            // Then
            assertFalse(result); // Should reject modified data
        }

        @Test
        @DisplayName("Should reject signature with modified transaction ID")
        void shouldRejectSignatureWithModifiedTransactionId() {
            // Given - Use fresh copy
            Map<String, String> testParams = new TreeMap<>(vnpayParams);
            String queryString = buildQueryString(testParams);
            String validHash = computeHmacSHA512(TEST_HASH_SECRET, queryString);
            testParams.put("vnp_SecureHash", validHash);

            // Modify TxnRef after signature created
            testParams.put("vnp_TxnRef", "MODIFIED-TXN-ID");

            // When
            boolean result = vnPayService.verifySignature(testParams);

            // Then
            assertFalse(result); // Should reject modified data
        }

        @Test
        @DisplayName("Should reject empty signature")
        void shouldRejectEmptySignature() {
            // Given - Use fresh copy
            Map<String, String> testParams = new TreeMap<>(vnpayParams);
            testParams.put("vnp_SecureHash", "");

            // When
            boolean result = vnPayService.verifySignature(testParams);

            // Then
            assertFalse(result);
        }

        @Test
        @DisplayName("Should reject null signature")
        void shouldRejectNullSignature() {
            // Given - Use fresh copy
            Map<String, String> testParams = new TreeMap<>(vnpayParams);
            testParams.put("vnp_SecureHash", null);

            // When
            boolean result = vnPayService.verifySignature(testParams);

            // Then
            assertFalse(result);
        }

        @Test
        @DisplayName("Should handle empty params map")
        void shouldHandleEmptyParamsMap() {
            // Given
            Map<String, String> emptyParams = new TreeMap<>();
            emptyParams.put("vnp_SecureHash", "some_hash");

            // When
            boolean result = vnPayService.verifySignature(emptyParams);

            // Then
            assertFalse(result);
        }

        @Test
        @DisplayName("Should verify signature with all VNPay response fields")
        void shouldVerifySignatureWithAllVnpayResponseFields() {
            // Given - Complete VNPay response
            Map<String, String> completeParams = new TreeMap<>();
            completeParams.put("vnp_TmnCode", TEST_TMN_CODE);
            completeParams.put("vnp_Amount", "10000000");
            completeParams.put("vnp_BankCode", "NCB");
            completeParams.put("vnp_BankTranNo", "20260304123456");
            completeParams.put("vnp_CardType", "ATM");
            completeParams.put("vnp_OrderInfo", "Payment for order " + orderId);
            completeParams.put("vnp_PayDate", "20260304150000");
            completeParams.put("vnp_ResponseCode", "00");
            completeParams.put("vnp_TransactionNo", "14523876");
            completeParams.put("vnp_TxnRef", transactionId);

            String queryString = buildQueryString(completeParams);
            String validHash = computeHmacSHA512(TEST_HASH_SECRET, queryString);
            completeParams.put("vnp_SecureHash", validHash);

            // When
            boolean result = vnPayService.verifySignature(completeParams);

            // Then
            assertTrue(result);
        }

        @Test
        @DisplayName("Should handle URL-encoded parameter values")
        void shouldHandleUrlEncodedParameterValues() {
            // Given - Use fresh copy and modify
            Map<String, String> testParams = new TreeMap<>(vnpayParams);
            testParams.put("vnp_OrderInfo", "Payment+for+order+550e8400");

            String queryString = buildQueryString(testParams);
            String validHash = computeHmacSHA512(TEST_HASH_SECRET, queryString);
            testParams.put("vnp_SecureHash", validHash);

            // When
            boolean result = vnPayService.verifySignature(testParams);

            // Then
            assertTrue(result);
        }
    }

    // ==================== Helper Methods ====================

    /**
     * Extract parameter value from URL
     */
    private String extractParamValue(String url, String paramName) {
        String[] parts = url.split("[?&]");
        for (String part : parts) {
            if (part.startsWith(paramName + "=")) {
                return part.substring(paramName.length() + 1);
            }
        }
        return null;
    }

    /**
     * Build query string from params (sorted by key) with URL encoding
     * Matches implementation's buildQueryString exactly
     */
    private String buildQueryString(Map<String, String> params) {
        return params.entrySet().stream()
                .map(e -> e.getKey() + "=" +
                        URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8))
                .collect(Collectors.joining("&"));
    }

    /**
     * Compute HMAC-SHA512 hash
     */
    private String computeHmacSHA512(String key, String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA512");
            mac.init(new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512"));
            byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("HMAC error", e);
        }
    }
}

