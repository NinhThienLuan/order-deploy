package fsoft.franchise.service;

import fsoft.franchise.dto.payments.*;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public interface PaymentService {

    PaymentListResponse getPayments(PaymentFilterRequest filter, UUID currentUserId, String role);

    PaymentResponse createPayment(CreatePaymentRequest request, String ipAddress);

    WebHookResponse processWebhook(Map<String, String> vnpayParams);

    /**
     * Get current payment status for an order (for "Cảm ơn" screen, frontend
     * polling after Online payment).
     * Returns the latest payment for the order.
     * FRANCHISE_ADMIN and STORE_MANAGER can view any order; CUSTOMER can only view
     * their own.
     */
    PaymentStatusResponse getPaymentStatus(UUID orderId, UUID currentUserId, String role);

    /**
     * Confirm a CASH payment (mark as PAID).
     * Admin/Manager only.
     */
    PaymentResponse confirmCashPayment(UUID paymentId);

    /**
     * List all transactions for admin reconciliation (đối soát dòng tiền).
     * Admin/Manager only.
     * Params giống GET /v1/orders: page (1-based), size, optional filters.
     */
    AdminTransactionListResponse getAdminTransactions(int page, int size,
            Optional<String> status, Optional<String> paymentMethod,
            Optional<LocalDate> fromDate, Optional<LocalDate> toDate);
}
