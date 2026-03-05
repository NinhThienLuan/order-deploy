package fsoft.franchise.controller;

import fsoft.franchise.entity.PaymentEntity;
import fsoft.franchise.entity.TransactionEntity;
import fsoft.franchise.enums.OrderStatus;
import fsoft.franchise.enums.PaymentStatus;
import fsoft.franchise.enums.TransactionStatus;
import fsoft.franchise.enums.TransactionType;
import fsoft.franchise.repository.OrderRepository;
import fsoft.franchise.repository.PaymentRepository;
import fsoft.franchise.repository.TransactionRepository;
import fsoft.franchise.service.MoMoPaymentService;
import fsoft.franchise.service.OrderTrackingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Handles MoMo payment callbacks (IPN) and return URL redirects.
 * These endpoints are PUBLIC (no JWT required) because MoMo server calls them
 * directly.
 */
@RestController
@RequestMapping("/api/v1/payments/momo")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "MoMo Callbacks", description = "Public MoMo IPN and return URL endpoints — called by MoMo server, no JWT required")
public class MoMoCallbackController {

    private final MoMoPaymentService moMoPaymentService;
    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final TransactionRepository transactionRepository;
    // Push real-time tracking khi MoMo callback thanh toán thành công
    private final OrderTrackingService orderTrackingService;

    /**
     * POST /v1/payments/momo/callback
     * <p>
     * MoMo IPN (Instant Payment Notification) — called by MoMo server after
     * payment.
     * This is the authoritative source for payment status.
     */
    @PostMapping("/callback")
    @Operation(summary = "MoMo IPN callback", description = "Instant Payment Notification from MoMo server — authoritative source for payment status. Verifies signature, records transaction, and updates order status.")
    @Transactional
    public ResponseEntity<Map<String, Object>> handleCallback(@RequestBody Map<String, String> params) {
        log.info("MoMo IPN callback received: {}", params);

        Map<String, Object> response = new LinkedHashMap<>();

        // 1. Verify signature
        if (!moMoPaymentService.verifyCallback(params)) {
            log.warn("MoMo callback signature verification FAILED");
            response.put("resultCode", 1);
            response.put("message", "Invalid signature");
            return ResponseEntity.ok(response);
        }

        // 2. Extract order UUID from extraData
        UUID orderId;
        try {
            String extraData = params.get("extraData");
            String decoded = new String(Base64.getDecoder().decode(extraData), StandardCharsets.UTF_8);
            // Parse simple JSON {"orderId":"..."} without ObjectMapper
            String orderIdStr = decoded.split("\"orderId\"\\s*:\\s*\"")[1].split("\"")[0];
            orderId = UUID.fromString(orderIdStr);
        } catch (Exception e) {
            log.error("Failed to parse orderId from MoMo extraData", e);
            response.put("resultCode", 1);
            response.put("message", "Invalid extraData");
            return ResponseEntity.ok(response);
        }

        // 3. Find the PENDING payment for this order
        List<PaymentEntity> payments = paymentRepository.findByOrder_IdOrderByPaymentDateDesc(orderId);
        PaymentEntity payment = payments.stream()
                .filter(p -> p.getStatus() == PaymentStatus.PENDING)
                .findFirst()
                .orElse(null);

        if (payment == null) {
            log.warn("No PENDING payment found for orderId={}", orderId);
            response.put("resultCode", 0);
            response.put("message", "OK (no pending payment)");
            return ResponseEntity.ok(response);
        }

        // 4. Determine success/failure from resultCode
        int resultCode = Integer.parseInt(params.getOrDefault("resultCode", "-1"));
        boolean success = (resultCode == 0);

        // 5. Create transaction record
        TransactionEntity transaction = TransactionEntity.builder()
                .payment(payment)
                .vnpTxnRef(params.get("orderId")) // MoMo's orderId (our MOMO-xxx reference)
                .vnpTransactionNo(params.get("transId")) // MoMo's transaction ID
                .vnpResponseCode(String.valueOf(resultCode))
                .vnpBankCode(params.getOrDefault("payType", "MOMO"))
                .type(TransactionType.PAYMENT)
                .amount(new BigDecimal(params.getOrDefault("amount", "0")))
                .status(success ? TransactionStatus.SUCCESS : TransactionStatus.FAILED)
                .build();
        transactionRepository.save(transaction);

        // 6. Update payment status
        if (success) {
            payment.setStatus(PaymentStatus.PAID);
            payment.setPaymentDate(LocalDateTime.now());
            payment.setTransactionId(params.get("transId"));
            paymentRepository.save(payment);

            // 7. Update order status to PAID
            var order = payment.getOrder();
            if (order.getStatus() == OrderStatus.PENDING) {
                OrderStatus previousStatus = order.getStatus();
                order.setStatus(OrderStatus.PAID);
                orderRepository.save(order);

                // Push real-time notification: MoMo payment thành công
                orderTrackingService.sendTrackingUpdate(order.getId(), previousStatus, OrderStatus.PAID, null);
            }
            log.info("MoMo payment SUCCESS for orderId={}, transId={}", orderId, params.get("transId"));
        } else {
            payment.setStatus(PaymentStatus.FAILED);
            payment.setErrorMessage("MoMo resultCode=" + resultCode + ": " + params.getOrDefault("message", ""));
            paymentRepository.save(payment);
            log.info("MoMo payment FAILED for orderId={}, resultCode={}", orderId, resultCode);
        }

        // 8. Respond with success so MoMo stops retrying
        response.put("resultCode", 0);
        response.put("message", "OK");
        return ResponseEntity.ok(response);
    }

    /**
     * GET /v1/payments/momo/return
     * <p>
     * MoMo redirects the user here after payment. We just redirect to the frontend
     * with the payment result as query parameters.
     */
    @GetMapping("/return")
    @Operation(summary = "MoMo return redirect", description = "MoMo redirects the user here after payment. Verifies signature and returns payment result summary.")
    public ResponseEntity<Map<String, Object>> handleReturn(@RequestParam Map<String, String> params) {
        log.info("MoMo return redirect received: {}", params);

        // Verify signature for security
        boolean valid = moMoPaymentService.verifyCallback(params);
        int resultCode = Integer.parseInt(params.getOrDefault("resultCode", "-1"));

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", valid && resultCode == 0);
        result.put("resultCode", resultCode);
        result.put("message", params.getOrDefault("message", ""));
        result.put("momoOrderId", params.get("orderId"));
        result.put("transId", params.get("transId"));
        result.put("amount", params.get("amount"));

        return ResponseEntity.ok(result);
    }
}
