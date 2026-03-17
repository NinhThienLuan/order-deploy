package fsoft.franchise.service;

import fsoft.franchise.dto.payments.*;
import java.util.UUID;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public interface PaymentService {

     PaymentListResponse getPayments(PaymentFilterRequest filter, UUID
     currentUserId, String role);

    /**
     * Initiate payment for an order. Returns a gateway URL for online methods (VNPay, MoMo)
     * or auto-confirms for CASH/WALLET.
     * Validates owner, amount, and order status.
     */
    PaymentResponse processPayment(UUID orderId, PaymentRequest request, UUID customerId, String ipAddress);

    WebHookResponse processWebhook(Map<String, String> vnpayParams);

    /**
     * Get current payment status for an order (for "Cảm ơn" screen, frontend
     * polling after Online payment).
     * Returns the latest payment for the order.
     * ADMIN and MANAGER can view any order; CUSTOMER can only view
     * their own.
     */
    PaymentStatusResponse getPaymentStatus(UUID orderId, UUID currentUserId, String role);

    /**
     * Confirm a CASH payment (mark as PAID).
     * Admin/Manager only. Records which staff confirmed the payment.
     */
//    PaymentResponse confirmCashPayment(UUID paymentId, UUID staffId);


    PaymentResponse createInboundPayment(CreateInboundPaymentRequest request, String ipAddress);

    /**
     * List all transactions for admin reconciliation (đối soát dòng tiền).
     * Admin/Manager only.
     * Params giống GET /v1/orders: page (1-based), size, optional filters.
     */
    AdminTransactionListResponse getAdminTransactions(int page, int size,
            Optional<String> status, Optional<String> paymentMethod,
            Optional<LocalDate> fromDate, Optional<LocalDate> toDate);
}
