package fsoft.franchise.controller;

import fsoft.franchise.common.response.ApiResponse;
import fsoft.franchise.common.config.VNPayConfig;
import fsoft.franchise.security.JwtService;
import fsoft.franchise.dto.payments.CreatePaymentRequest;
import fsoft.franchise.dto.payments.PaymentFilterRequest;
import fsoft.franchise.dto.payments.PaymentListResponse;
import fsoft.franchise.dto.payments.PaymentMethodResponse;
import fsoft.franchise.dto.payments.PaymentResponse;
import fsoft.franchise.dto.payments.PaymentStatusResponse;
import fsoft.franchise.dto.payments.WebHookResponse;
import fsoft.franchise.service.PaymentMethodService;
import fsoft.franchise.service.PaymentService;
import fsoft.franchise.service.VNPayService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Tag(name = "Payments", description = "Payment methods, history, status, creation, and gateway webhooks")
public class PaymentController {

        private final PaymentService paymentService;
        private final PaymentMethodService paymentMethodService;
        private final JwtService jwtService;
        private final VNPayService vnPayService;
        private final VNPayConfig vnPayConfig;

        @Value("${momo.return-url:http://localhost:3000/payment/result}")
        private String frontendReturnBase;

        /**
         * GET /v1/payments/methods
         * Returns all available payment methods with descriptions and sub-options.
         * Public endpoint — no auth required so FE can render payment options before
         * checkout.
         */
        @GetMapping("/methods")
        @Operation(summary = "Get payment methods", description = "Returns all enabled payment methods with sub-options. Public — no auth required.")
        public ResponseEntity<ApiResponse<List<PaymentMethodResponse>>> getPaymentMethods() {
                List<PaymentMethodResponse> methods = paymentMethodService.getAllPaymentMethods();
                return ResponseEntity.ok(
                                ApiResponse.<List<PaymentMethodResponse>>builder()
                                                .code(200)
                                                .message("Payment methods loaded successfully")
                                                .result(methods)
                                                .build());
        }

        /**
         * GET /v1/payments
         * Get payment history for current user (CUSTOMER) or all payments
         * (FRANCHISE_ADMIN/STORE_MANAGER).
         */
        @GetMapping
        @Operation(summary = "Get payment history", description = "Paginated payment history. Customers see only their own; admins see all.")
        @PreAuthorize("hasAnyRole('CUSTOMER', 'FRANCHISE_ADMIN', 'STORE_MANAGER')")
        public ResponseEntity<ApiResponse<PaymentListResponse>> getPayments(
                        PaymentFilterRequest filter,
                        HttpServletRequest request) {

                String token = jwtService.getTokenFromRequest(request);
                UUID userId = UUID.fromString(jwtService.getUid(token));
                String role = jwtService.getPrimaryRole(token);

                // Validate date range
                if (filter.getFromDate() != null && filter.getToDate() != null
                                && filter.getFromDate().isAfter(filter.getToDate())) {
                        return ResponseEntity.badRequest()
                                        .body(ApiResponse.<PaymentListResponse>builder()
                                                        .code(400)
                                                        .message("Invalid date format. Use ISO 8601.")
                                                        .build());
                }

                PaymentListResponse data = paymentService.getPayments(filter, userId, role);
                return ResponseEntity.ok(
                                ApiResponse.<PaymentListResponse>builder()
                                                .code(1000)
                                                .message("Payment log loaded successfully.")
                                                .result(data)
                                                .build());
        }

        /**
         * GET /v1/payments/{order_id}/status
         * Check current payment status for an order. Requires auth: CUSTOMER may only
         * view own orders;
         * FRANCHISE_ADMIN and STORE_MANAGER may view any. 400 Invalid order id, 403
         * Access denied, 404 Payment not found.
         */
        @GetMapping("/{orderId}/status")
        @Operation(summary = "Get payment status", description = "Returns the current payment status for a given order. Customers may only view their own orders.")
        @PreAuthorize("hasAnyRole('CUSTOMER', 'FRANCHISE_ADMIN', 'STORE_MANAGER')")
        public ResponseEntity<ApiResponse<PaymentStatusResponse>> getPaymentStatus(
                        @PathVariable String orderId,
                        HttpServletRequest request) {
                UUID orderUuid;
                try {
                        orderUuid = UUID.fromString(orderId);
                } catch (IllegalArgumentException e) {
                        return ResponseEntity.badRequest()
                                        .body(ApiResponse.<PaymentStatusResponse>builder()
                                                        .code(400)
                                                        .message("Invalid order id")
                                                        .result(null)
                                                        .build());
                }
                String token = jwtService.getTokenFromRequest(request);
                UUID userId = UUID.fromString(jwtService.getUid(token));
                String role = jwtService.getPrimaryRole(token);
                PaymentStatusResponse result = paymentService.getPaymentStatus(orderUuid, userId, role);
                return ResponseEntity.ok(
                                ApiResponse.<PaymentStatusResponse>builder()
                                                .code(200)
                                                .message("Get payment status successfully")
                                                .result(result)
                                                .build());
        }

        @PostMapping("/create")
        @Operation(summary = "Create payment", description = "Initiate a new payment and return a gateway URL or confirmation.")
        public ResponseEntity<ApiResponse<PaymentResponse>> createPayment(
                        @RequestBody @Valid CreatePaymentRequest request,
                        HttpServletRequest httpRequest) {
                String ip = httpRequest.getRemoteAddr();
                return ResponseEntity.ok(ApiResponse.<PaymentResponse>builder()
                                .code(200)
                                .message("Payment created successfully")
                                .result(paymentService.createPayment(request, ip))
                                .build());
        }

        @PostMapping("/webhook")
        @Operation(summary = "VNPay webhook", description = "Receives VNPay payment notification. Verifies signature and updates payment status.")
        public ResponseEntity<ApiResponse<WebHookResponse>> handleWebhook(
                        @RequestBody Map<String, String> params) {

                boolean valid = !vnPayConfig.isVerifySignature() || vnPayService.verifySignature(params);
                if (!valid)
                        return ResponseEntity.badRequest()
                                        .body(ApiResponse.<WebHookResponse>builder()
                                                        .code(400)
                                                        .message("Invalid signature")
                                                        .build());

                // LUÔN return 200
                return ResponseEntity.ok(
                                ApiResponse.<WebHookResponse>builder()
                                                .code(200)
                                                .message("Webhook processed successfully")
                                                .result(paymentService.processWebhook(params))
                                                .build());
        }

        @GetMapping("/vnpay-return")
        @Operation(summary = "VNPay return redirect", description = "VNPay redirects the user here after payment. Redirects to frontend success or failure page.")
        public ResponseEntity<Void> handleReturn(
                        @RequestParam Map<String, String> params) {
                boolean valid = !vnPayConfig.isVerifySignature() || vnPayService.verifySignature(params);
                boolean success = "00".equals(params.get("vnp_ResponseCode"));

                // Derive frontend base from momo.return-url (e.g.
                // http://host:3000/payment/result → http://host:3000)
                String frontendBase = frontendReturnBase.replaceAll("/payment/.*$", "");
                String redirect = (valid && success)
                                ? frontendBase + "/payment/success"
                                : frontendBase + "/payment/failed";

                return ResponseEntity.status(HttpStatus.FOUND)
                                .header("Location", redirect)
                                .build();
        }
}
