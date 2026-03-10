package fsoft.franchise.controller;

import fsoft.franchise.common.config.MoMoConfig;
import fsoft.franchise.entity.PaymentEntity;
import fsoft.franchise.entity.TransactionEntity;
import fsoft.franchise.enums.*;
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
    private final MoMoConfig moMoConfig;
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

        // 3. Find the PENDING *MOMO* payment for this order (must filter by MOMO to
        // avoid
        // accidentally processing a VNPay/PayOS payment that belongs to a different
        // tab)
        List<PaymentEntity> payments = paymentRepository.findByOrder_IdOrderByPaymentDateDesc(orderId);
        PaymentEntity payment = payments.stream()
                .filter(p -> p.getStatus() == PaymentStatus.PENDING
                        && p.getPaymentMethod() == PaymentMethod.MOMO)
                .findFirst()
                .orElse(null);

        // 4. Determine success/failure from resultCode
        int resultCode = Integer.parseInt(params.getOrDefault("resultCode", "-1"));
        boolean success = (resultCode == 0);

        // 4.5. Handle locally-cancelled MoMo payment that was still paid on gateway
        // side
        // Scenario: user opened Tab2 (VNPay) → old MoMo payment locally FAILED →
        // user still paid on MoMo QR in Tab1 → IPN arrives here
        if (payment == null && success) {
            // Look for the latest MOMO payment (FAILED due to local cancellation)
            PaymentEntity cancelledMomoPayment = payments.stream()
                    .filter(p -> p.getPaymentMethod() == PaymentMethod.MOMO
                            && p.getStatus() == PaymentStatus.FAILED)
                    .findFirst()
                    .orElse(null);

            if (cancelledMomoPayment != null) {
                boolean orderAlreadyPaid = payments.stream()
                        .anyMatch(p -> p.getStatus() == PaymentStatus.PAID);

                if (orderAlreadyPaid) {
                    // Order already paid by another gateway → log for manual refund reconciliation
                    log.warn("DOUBLE PAYMENT DETECTED for orderId={}. MoMo payment {} succeeded on gateway "
                            + "but order was already paid by another method. Manual refund required.",
                            orderId, cancelledMomoPayment.getId());
                    // Record transaction for finance reconciliation (store response code on
                    // payment)
                    cancelledMomoPayment.setVnpResponseCode(String.valueOf(resultCode));
                    cancelledMomoPayment.setVnpBankCode(params.getOrDefault("payType", "MOMO"));
                    paymentRepository.save(cancelledMomoPayment);
                    transactionRepository.save(TransactionEntity.builder()
                            .payment(cancelledMomoPayment)
                            .vnpTxnRef(params.get("orderId"))
                            .vnpTransactionNo(params.get("transId"))
                            .type(TransactionType.PAYMENT)
                            .amount(new BigDecimal(params.getOrDefault("amount", "0")))
                            .build());
                    response.put("resultCode", 0);
                    response.put("message", "OK (duplicate — order already paid)");
                    return ResponseEntity.ok(response);
                } else {
                    // No other PAID payment → re-accept this cancelled MoMo payment
                    log.info("Accepting locally-cancelled MoMo payment {} for orderId={}", cancelledMomoPayment.getId(),
                            orderId);
                    payment = cancelledMomoPayment;
                    // Fail any other PENDING payments for this order to prevent triple payment
                    final PaymentEntity acceptedPayment = payment;
                    payments.stream()
                            .filter(p -> p.getStatus() == PaymentStatus.PENDING
                                    && !p.getId().equals(acceptedPayment.getId()))
                            .forEach(p -> {
                                p.setStatus(PaymentStatus.FAILED);
                                paymentRepository.save(p);
                            });
                }
            }
        }

        if (payment == null) {
            log.warn("No PENDING payment found for orderId={}", orderId);
            response.put("resultCode", 0);
            response.put("message", "OK (no pending payment)");
            return ResponseEntity.ok(response);
        }

        // 5. Store gateway response code on payment (preserves failure codes; success =
        // "0" for MoMo)
        payment.setVnpResponseCode(String.valueOf(resultCode));
        payment.setVnpBankCode(params.getOrDefault("payType", "MOMO"));

        // 6. Create transaction record (success only — transactions are immutable
        // ledger entries)
        if (success) {
            transactionRepository.save(TransactionEntity.builder()
                    .payment(payment)
                    .vnpTxnRef(params.get("orderId"))
                    .vnpTransactionNo(params.get("transId"))
                    .type(TransactionType.PAYMENT)
                    .amount(new BigDecimal(params.getOrDefault("amount", "0")))
                    .build());
        }

        // 7. Update payment status
        if (success) {
            payment.setStatus(PaymentStatus.PAID);
            payment.setPaymentDate(LocalDateTime.now());
            payment.setTransactionId(params.get("transId"));
            paymentRepository.save(payment);

            // 8. Update order status — guard: skip if already PAID or CANCELED (Bug #2 +
            // Bug #5)
            var order = payment.getOrder();
            if (order.getStatus() != OrderStatus.PAID && order.getStatus() != OrderStatus.CANCELED) {
                OrderStatus previousStatus = order.getStatus();
                order.setStatus(OrderStatus.PAID);
                orderRepository.save(order);
                orderTrackingService.sendTrackingUpdate(order.getId(), previousStatus, OrderStatus.PAID, null);
            }
            log.info("MoMo payment SUCCESS for orderId={}, transId={}", orderId, params.get("transId"));
        } else {
            payment.setStatus(PaymentStatus.FAILED);
            payment.setErrorMessage("MoMo resultCode=" + resultCode + ": " + params.getOrDefault("message", ""));
            paymentRepository.save(payment);

            // Reset order PROCESSING → PENDING so user can retry with another payment
            // method
            // (mirrors VNPay's processWebhook failure behaviour)
            var failOrder = payment.getOrder();
            if (failOrder != null && failOrder.getStatus() == OrderStatus.PROCESSING) {
                failOrder.setStatus(OrderStatus.PENDING);
                orderRepository.save(failOrder);
                log.info("MoMo payment FAILED: reset orderId={} → PENDING so user can retry", orderId);
            }
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
     * MoMo redirects the user here after payment.
     * Đây là fallback: nếu IPN callback không tới được (firewall, HTTPS
     * requirement...),
     * endpoint này cũng update DB khi user quay về.
     * Sau khi xử lý xong → redirect tới frontend.
     */
    @GetMapping("/return")
    @Operation(summary = "MoMo return redirect", description = "MoMo redirects the user here after payment. Updates DB as fallback and redirects to frontend.")
    @Transactional
    public ResponseEntity<Void> handleReturn(@RequestParam Map<String, String> params) {
        log.info("MoMo return redirect received: {}", params);

        boolean valid = moMoPaymentService.verifyCallback(params);
        int resultCode = Integer.parseInt(params.getOrDefault("resultCode", "-1"));
        boolean success = valid && resultCode == 0;

        // === FALLBACK: update DB nếu IPN chưa cập nhật ===
        if (success) {
            try {
                String extraData = params.get("extraData");
                String decoded = new String(Base64.getDecoder().decode(extraData), StandardCharsets.UTF_8);
                String orderIdStr = decoded.split("\"orderId\"\\s*:\\s*\"")[1].split("\"")[0];
                UUID orderId = UUID.fromString(orderIdStr);

                List<PaymentEntity> payments = paymentRepository.findByOrder_IdOrderByPaymentDateDesc(orderId);
                PaymentEntity payment = payments.stream()
                        .filter(p -> p.getStatus() == PaymentStatus.PENDING
                                && p.getPaymentMethod() == PaymentMethod.MOMO)
                        .findFirst()
                        .orElse(null);

                // Handle locally-cancelled MoMo payment (same logic as IPN callback)
                if (payment == null) {
                    PaymentEntity cancelledMomoPayment = payments.stream()
                            .filter(p -> p.getPaymentMethod() == PaymentMethod.MOMO
                                    && p.getStatus() == PaymentStatus.FAILED)
                            .findFirst()
                            .orElse(null);
                    if (cancelledMomoPayment != null) {
                        boolean orderAlreadyPaid = payments.stream()
                                .anyMatch(p -> p.getStatus() == PaymentStatus.PAID);
                        if (orderAlreadyPaid) {
                            log.warn("Return: DOUBLE PAYMENT DETECTED for orderId={}. Needs manual refund.", orderId);
                            cancelledMomoPayment.setVnpResponseCode(String.valueOf(resultCode));
                            cancelledMomoPayment.setVnpBankCode(params.getOrDefault("payType", "MOMO"));
                            paymentRepository.save(cancelledMomoPayment);
                            transactionRepository.save(TransactionEntity.builder()
                                    .payment(cancelledMomoPayment)
                                    .vnpTxnRef(params.get("orderId"))
                                    .vnpTransactionNo(params.get("transId"))
                                    .type(TransactionType.PAYMENT)
                                    .amount(new BigDecimal(params.getOrDefault("amount", "0")))
                                    .build());
                        } else {
                            log.info("Return fallback: re-accepting locally-cancelled MoMo payment for orderId={}",
                                    orderId);
                            payment = cancelledMomoPayment;
                            // Fail any other PENDING payment for this order
                            final PaymentEntity accepted = payment;
                            payments.stream()
                                    .filter(p -> p.getStatus() == PaymentStatus.PENDING
                                            && !p.getId().equals(accepted.getId()))
                                    .forEach(p -> {
                                        p.setStatus(PaymentStatus.FAILED);
                                        paymentRepository.save(p);
                                    });
                        }
                    }
                }

                if (payment != null) {
                    log.info("Return fallback: updating payment status for orderId={}", orderId);

                    // Bug #1: Idempotency check — IPN may have already created the transaction
                    String momoTransId = params.get("transId");
                    boolean alreadyRecorded = momoTransId != null
                            && transactionRepository.existsByVnpTransactionNo(momoTransId);

                    if (!alreadyRecorded) {
                        // Store gateway response on payment (preserves regardless of success/failure)
                        payment.setVnpResponseCode(String.valueOf(resultCode));
                        payment.setVnpBankCode(params.getOrDefault("payType", "MOMO"));

                        transactionRepository.save(TransactionEntity.builder()
                                .payment(payment)
                                .vnpTxnRef(params.get("orderId"))
                                .vnpTransactionNo(momoTransId)
                                .type(TransactionType.PAYMENT)
                                .amount(new BigDecimal(params.getOrDefault("amount", "0")))
                                .build());

                        payment.setStatus(PaymentStatus.PAID);
                        payment.setPaymentDate(LocalDateTime.now());
                        payment.setTransactionId(momoTransId);
                        paymentRepository.save(payment);

                        // Bug #2 + Bug #5: guard — do NOT override PAID or CANCELED order
                        var order = payment.getOrder();
                        if (order.getStatus() != OrderStatus.PAID && order.getStatus() != OrderStatus.CANCELED) {
                            OrderStatus previousStatus = order.getStatus();
                            order.setStatus(OrderStatus.PAID);
                            orderRepository.save(order);
                            orderTrackingService.sendTrackingUpdate(order.getId(), previousStatus, OrderStatus.PAID,
                                    null);
                        }
                        log.info("Return fallback: payment updated successfully for orderId={}", orderId);
                    } else {
                        log.info("Return: transaction already recorded by IPN for transId={} — skipping duplicate",
                                momoTransId);
                    }
                } else {
                    log.info("Return: payment already processed for orderId={} (IPN arrived first)", orderId);
                }
            } catch (Exception e) {
                log.error("Return fallback: failed to update payment", e);
            }
        } else if (valid) {
            // MoMo returned with non-success resultCode (user cancelled, payment failed,
            // etc.)
            // Mark the PENDING MOMO payment as FAILED so user can switch to a different
            // method
            try {
                String extraData = params.get("extraData");
                String decoded = new String(Base64.getDecoder().decode(extraData), StandardCharsets.UTF_8);
                String orderIdStr = decoded.split("\"orderId\"\\s*:\\s*\"")[1].split("\"")[0];
                UUID orderId = UUID.fromString(orderIdStr);

                paymentRepository.findByOrder_IdOrderByPaymentDateDesc(orderId).stream()
                        .filter(p -> p.getStatus() == PaymentStatus.PENDING
                                && p.getPaymentMethod() == PaymentMethod.MOMO)
                        .findFirst()
                        .ifPresent(p -> {
                            p.setStatus(PaymentStatus.FAILED);
                            p.setErrorMessage(
                                    "MoMo resultCode=" + resultCode + ": "
                                            + params.getOrDefault("message", ""));
                            paymentRepository.save(p);
                            log.info("Return: marked MOMO payment {} as FAILED for orderId={} (resultCode={})",
                                    p.getId(), orderId, resultCode);

                            // Reset order PROCESSING → PENDING so user can retry
                            var retryOrder = p.getOrder();
                            if (retryOrder != null && retryOrder.getStatus() == OrderStatus.PROCESSING) {
                                retryOrder.setStatus(OrderStatus.PENDING);
                                orderRepository.save(retryOrder);
                                log.info("Return cancel: reset orderId={} → PENDING", orderId);
                            }
                        });
            } catch (Exception e) {
                log.error("Return: failed to mark cancelled MOMO payment as FAILED", e);
            }
        }

        // Redirect to frontend with MoMo params so PaymentResultPage can render
        String frontendUrl = moMoConfig.getFrontendUrl() != null
                ? moMoConfig.getFrontendUrl()
                : "http://localhost:3000/payment/result";
        String frontendBase = frontendUrl.replaceAll("/payment/.*$", "");
        String redirectUrl = success
                ? frontendBase + "/payment/result?resultCode=0&orderId=" + params.getOrDefault("orderId", "")
                        + "&extraData="
                        + java.net.URLEncoder.encode(params.getOrDefault("extraData", ""), StandardCharsets.UTF_8)
                        + "&transId=" + params.getOrDefault("transId", "")
                        + "&amount=" + params.getOrDefault("amount", "")
                : frontendBase + "/payment/result?resultCode=" + resultCode
                        + "&message="
                        + java.net.URLEncoder.encode(params.getOrDefault("message", ""), StandardCharsets.UTF_8);

        return ResponseEntity.status(org.springframework.http.HttpStatus.FOUND)
                .header("Location", redirectUrl)
                .build();
    }
}
