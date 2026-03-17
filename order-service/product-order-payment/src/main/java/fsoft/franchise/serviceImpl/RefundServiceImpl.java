package fsoft.franchise.serviceImpl;

import fsoft.franchise.common.exception.ApiException;
import fsoft.franchise.exception.OrderErrorCode;
import fsoft.franchise.exception.RefundErrorCode;
import fsoft.franchise.dto.payments.OrderRefundResponse;
import fsoft.franchise.dto.payments.RefundRequest;
import fsoft.franchise.dto.payments.RefundResponse;
import fsoft.franchise.entity.OrderEntity;
import fsoft.franchise.entity.PaymentEntity;
import fsoft.franchise.entity.RefundEntity;
import fsoft.franchise.entity.TransactionEntity;
import fsoft.franchise.enums.OrderStatus;
import fsoft.franchise.enums.PaymentStatus;
import fsoft.franchise.enums.RefundStatus;
import fsoft.franchise.enums.TransactionType;
import fsoft.franchise.repository.OrderRepository;
import fsoft.franchise.repository.PaymentRepository;
import fsoft.franchise.repository.RefundRepository;
import fsoft.franchise.repository.TransactionRepository;
import fsoft.franchise.service.OrderTrackingService;
import fsoft.franchise.service.RefundService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class RefundServiceImpl implements RefundService {

    private final RefundRepository refundRepository;
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final TransactionRepository transactionRepository;
    private final OrderTrackingService orderTrackingService;

    @Override
    @Transactional
    public RefundResponse createRefundRequest(RefundRequest requestDTO, UUID customerId) {
        OrderEntity order = orderRepository.findById(requestDTO.orderId())
                .orElseThrow(() -> new ApiException(OrderErrorCode.ORDER_NOT_FOUND));

        if (!order.getCustomerId().equals(customerId)) {
            throw new ApiException(OrderErrorCode.ORDER_NOT_OWNED);
        }
        if (order.getStatus() != OrderStatus.PAID
                && order.getStatus() != OrderStatus.COMPLETED
                && order.getStatus() != OrderStatus.READY) {
            throw new ApiException(RefundErrorCode.INVALID_ORDER_STATUS_FOR_REFUND);
        }

        if (refundRepository.existsByOrderId(requestDTO.orderId())) {
            throw new ApiException(RefundErrorCode.REFUND_ALREADY_EXISTS);
        }

        PaymentEntity payment = order.getPayments().stream()
                .filter(p -> p.getStatus() == PaymentStatus.PAID)
                .findFirst()
                .orElseThrow(() -> new ApiException(RefundErrorCode.PAYMENT_NOT_FOUND));

        if (requestDTO.amount().compareTo(order.getTotalAmount()) != 0) {
            throw new ApiException(RefundErrorCode.INVALID_REFUND_AMOUNT,
                    String.format("Refund amount must equal order total amount. Expected: %s, Got: %s",
                            order.getTotalAmount(), requestDTO.amount()));
        }

        RefundEntity refund = RefundEntity.builder()
                .order(order)
                .payment(payment)
                .amount(requestDTO.amount())
                .reason(requestDTO.reason())
                .status(RefundStatus.PENDING)
                .build();

        RefundEntity savedRefund = refundRepository.save(refund);
        return mapToResponseDTO(savedRefund);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RefundResponse> getAllPendingRefunds() {
        List<RefundEntity> pendingRefunds = refundRepository.findByStatus(RefundStatus.PENDING);
        return pendingRefunds.stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public RefundResponse approveRefund(UUID refundId) {
        RefundEntity refund = refundRepository.findById(refundId)
                .orElseThrow(() -> new ApiException(RefundErrorCode.REFUND_NOT_FOUND));

        if (refund.getStatus() != RefundStatus.PENDING) {
            throw new ApiException(RefundErrorCode.REFUND_ALREADY_PROCESSED);
        }

        PaymentEntity payment = refund.getPayment();
        if (payment == null) {
            throw new ApiException(RefundErrorCode.PAYMENT_NOT_FOUND);
        }

        TransactionEntity refundTxn = TransactionEntity.builder()
                .payment(payment)
                .type(TransactionType.REFUND)
                .amount(refund.getAmount().negate()) // Negative amount = money returned
                .build();

        TransactionEntity savedRefundTxn = transactionRepository.save(refundTxn);

        refund.setStatus(RefundStatus.APPROVED);
        refund.setTransaction(savedRefundTxn);
        refundRepository.save(refund);

        payment.setStatus(PaymentStatus.REFUNDED);
        paymentRepository.save(payment);

        OrderEntity order = refund.getOrder();
        order.setStatus(OrderStatus.REFUNDED);
        orderRepository.save(order);

        return mapToResponseDTO(refund);
    }

    @Override
    @Transactional
    public RefundResponse declineRefund(UUID refundId, String declineReason) {
        RefundEntity refund = refundRepository.findById(refundId)
                .orElseThrow(() -> new ApiException(RefundErrorCode.REFUND_NOT_FOUND));

        if (refund.getStatus() != RefundStatus.PENDING) {
            throw new ApiException(RefundErrorCode.REFUND_ALREADY_PROCESSED);
        }

        refund.setStatus(RefundStatus.REJECTED);
        refund.setDeclineReason(declineReason);
        RefundEntity savedRefund = refundRepository.save(refund);

        return mapToResponseDTO(savedRefund);
    }

    @Override
    @Transactional
    public OrderRefundResponse processOrderRefund(UUID orderId, UUID performedBy) {
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ApiException(OrderErrorCode.ORDER_NOT_FOUND));

        if (order.getStatus() != OrderStatus.PAID
                && order.getStatus() != OrderStatus.COMPLETED
                && order.getStatus() != OrderStatus.READY) {
            throw new ApiException(RefundErrorCode.INVALID_ORDER_STATUS_FOR_REFUND);
        }

        if (refundRepository.existsByOrderId(orderId)) {
            throw new ApiException(RefundErrorCode.REFUND_ALREADY_EXISTS);
        }

        PaymentEntity payment = order.getPayments().stream()
                .filter(p -> p.getStatus() == PaymentStatus.PAID)
                .findFirst()
                .orElseThrow(() -> new ApiException(RefundErrorCode.PAYMENT_NOT_FOUND));

        BigDecimal amount = order.getTotalAmount();
        RefundEntity refund = RefundEntity.builder()
                .order(order)
                .payment(payment)
                .amount(amount)
                .reason("Admin/Staff refund")
                .status(RefundStatus.APPROVED)
                .build();

        RefundEntity savedRefund = refundRepository.save(refund);

        TransactionEntity refundTxn = TransactionEntity.builder()
                .payment(payment)
                .type(TransactionType.REFUND)
                .amount(amount.negate())
                .build();
        TransactionEntity savedTxn = transactionRepository.save(refundTxn);
        savedRefund.setTransaction(savedTxn);
        refundRepository.save(savedRefund);

        payment.setStatus(PaymentStatus.REFUNDED);
        paymentRepository.save(payment);

        OrderStatus previousStatus = order.getStatus();
        order.setStatus(OrderStatus.REFUNDED);
        orderRepository.save(order);

        orderTrackingService.sendTrackingUpdate(orderId, previousStatus, OrderStatus.REFUNDED, performedBy);

        LocalDateTime refundTime = savedRefund.getCreatedAt() != null ? savedRefund.getCreatedAt()
                : LocalDateTime.now();
        return new OrderRefundResponse(order.getId(), OrderStatus.REFUNDED, amount, refundTime);
    }

    private RefundResponse mapToResponseDTO(RefundEntity refund) {
        return new RefundResponse(
                refund.getId(),
                refund.getOrder().getId(),
                refund.getOrder().getOrderNumber(),
                refund.getAmount(),
                refund.getReason(),
                refund.getStatus(),
                refund.getDeclineReason(),
                refund.getCreatedAt(),
                refund.getUpdatedAt());
    }
}
