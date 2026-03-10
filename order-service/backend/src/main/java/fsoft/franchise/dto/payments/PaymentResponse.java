package fsoft.franchise.dto.payments;

import com.fasterxml.jackson.annotation.JsonInclude;
import fsoft.franchise.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for payment operations.
 * When paymentMethod is MOMO, paymentUrl contains the MoMo redirect URL.
 * For CASH or other synchronous methods, paymentUrl is null.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record PaymentResponse(
                UUID paymentId,
                UUID orderId,
                String paymentMethod,
                BigDecimal amountPaid,
                String paymentStatus,
                OrderStatus orderStatus,
                String paymentUrl,
                LocalDateTime expiredAt,
                LocalDateTime createdAt) {

        /**
         * Constructor without paymentUrl/expiredAt/createdAt (backwards compatible for
         * non-redirect payments)
         */
        public PaymentResponse(UUID paymentId, UUID orderId, String paymentMethod,
                        BigDecimal amountPaid, String paymentStatus, OrderStatus orderStatus) {
                this(paymentId, orderId, paymentMethod, amountPaid, paymentStatus, orderStatus, null, null, null);
        }

        public PaymentResponse(UUID paymentId, UUID orderId, String paymentMethod,
                        BigDecimal amountPaid, String paymentStatus, OrderStatus orderStatus,
                        String paymentUrl) {
                this(paymentId, orderId, paymentMethod, amountPaid, paymentStatus, orderStatus, paymentUrl, null, null);
        }
}
