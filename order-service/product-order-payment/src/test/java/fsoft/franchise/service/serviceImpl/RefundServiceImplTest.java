// package fsoft.franchise.service.serviceImpl;

// import fsoft.franchise.common.exception.ApiException;
// import fsoft.franchise.exception.OrderErrorCode;
// import fsoft.franchise.exception.RefundErrorCode;
// import fsoft.franchise.dto.payments.RefundRequest;
// import fsoft.franchise.dto.payments.RefundResponse;
// import fsoft.franchise.entity.OrderEntity;
// import fsoft.franchise.entity.PaymentEntity;
// import fsoft.franchise.entity.RefundEntity;
// import fsoft.franchise.entity.TransactionEntity;
// import fsoft.franchise.enums.OrderStatus;
// import fsoft.franchise.enums.PaymentStatus;
// import fsoft.franchise.enums.RefundStatus;
// import fsoft.franchise.enums.TransactionType;
// import fsoft.franchise.entity.external.AccountEntity;
// import fsoft.franchise.repository.OrderRepository;
// import fsoft.franchise.repository.PaymentRepository;
// import fsoft.franchise.repository.RefundRepository;
// import fsoft.franchise.repository.TransactionRepository;
// import fsoft.franchise.serviceImpl.RefundServiceImpl;
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.DisplayName;
// import org.junit.jupiter.api.Nested;
// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.api.extension.ExtendWith;
// import org.mockito.ArgumentCaptor;
// import org.mockito.InjectMocks;
// import org.mockito.Mock;
// import org.mockito.junit.jupiter.MockitoExtension;

// import java.math.BigDecimal;
// import java.time.LocalDateTime;
// import java.util.ArrayList;
// import java.util.List;
// import java.util.Optional;
// import java.util.UUID;

// import static org.junit.jupiter.api.Assertions.*;
// import static org.mockito.ArgumentMatchers.any;
// import static org.mockito.Mockito.*;

// /**
// * Unit Tests for RefundServiceImpl using JUnit 5 and Mockito
// * Coverage target: 75-85%
// * Test all public methods with edge cases and error scenarios
// *
// * @author Dev Team
// * @version 1.0
// * @since 2026-03-02
// */
// @ExtendWith(MockitoExtension.class)
// @DisplayName("RefundServiceImpl Unit Tests")
// class RefundServiceImplTest {

// // ==================== Mocks ====================
// @Mock
// private RefundRepository refundRepository;

// @Mock
// private OrderRepository orderRepository;

// @Mock
// private PaymentRepository paymentRepository;

// @Mock
// private TransactionRepository transactionRepository;

// @InjectMocks
// private RefundServiceImpl refundService;

// // ==================== Test Data ====================
// private UUID customerId;
// private UUID orderId;
// private UUID refundId;
// private UUID paymentId;
// private OrderEntity order;
// private AccountEntity customer;
// private PaymentEntity payment;
// private RefundEntity refund;
// private RefundRequest refundRequest;

// @BeforeEach
// void setUp() {
// // Initialize IDs
// customerId = UUID.randomUUID();
// orderId = UUID.randomUUID();
// refundId = UUID.randomUUID();
// paymentId = UUID.randomUUID();

// // Setup customer
// customer = new AccountEntity();
// customer.setId(customerId);
// customer.setEmail("customer@example.com");

// // Setup payment
// payment = new PaymentEntity();
// payment.setId(paymentId);
// payment.setStatus(PaymentStatus.PAID);

// // Setup order
// order = new OrderEntity();
// order.setId(orderId);
// order.setOrderNumber("ORD-2026-001");
// order.setCustomer(customer);
// order.setStatus(OrderStatus.PAID);
// order.setTotalAmount(BigDecimal.valueOf(100000));
// order.setPayments(List.of(payment));
// order.setCreatedAt(LocalDateTime.now());

// // Setup refund request DTO
// refundRequest = new RefundRequest(
// orderId,
// BigDecimal.valueOf(100000),
// "Product quality issue");

// // Setup refund entity
// refund = RefundEntity.builder()
// .id(refundId)
// .order(order)
// .payment(payment)
// .amount(BigDecimal.valueOf(100000))
// .reason("Product quality issue")
// .status(RefundStatus.PENDING)
// .createdAt(LocalDateTime.now())
// .updatedAt(LocalDateTime.now())
// .build();
// }

// // ==================== createRefundRequest() Tests ====================
// @Nested
// @DisplayName("createRefundRequest() Tests")
// class CreateRefundRequestTests {

// @Test
// @DisplayName("Should create refund request successfully for PAID order")
// void shouldCreateRefundRequestSuccessfullyForPaidOrder() {
// // Given
// when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
// when(refundRepository.existsByOrderId(orderId)).thenReturn(false);
// when(refundRepository.save(any(RefundEntity.class))).thenReturn(refund);

// // When
// RefundResponse result = refundService.createRefundRequest(refundRequest,
// customerId);

// // Then
// assertNotNull(result);
// assertEquals(refundId, result.refundId());
// assertEquals(orderId, result.orderId());
// assertEquals("ORD-2026-001", result.orderNumber());
// assertEquals(BigDecimal.valueOf(100000), result.amount());
// assertEquals("Product quality issue", result.reason());
// assertEquals(RefundStatus.PENDING, result.status());

// // Verify interactions
// verify(orderRepository).findById(orderId);
// verify(refundRepository).existsByOrderId(orderId);
// verify(refundRepository).save(any(RefundEntity.class));
// }

// @Test
// @DisplayName("Should create refund request for COMPLETED order")
// void shouldCreateRefundRequestForCompletedOrder() {
// // Given
// order.setStatus(OrderStatus.COMPLETED);
// when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
// when(refundRepository.existsByOrderId(orderId)).thenReturn(false);
// when(refundRepository.save(any(RefundEntity.class))).thenReturn(refund);

// // When
// RefundResponse result = refundService.createRefundRequest(refundRequest,
// customerId);

// // Then
// assertNotNull(result);
// assertEquals(RefundStatus.PENDING, result.status());
// }

// @Test
// @DisplayName("Should create refund request for READY order")
// void shouldCreateRefundRequestForReadyOrder() {
// // Given
// order.setStatus(OrderStatus.READY);
// when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
// when(refundRepository.existsByOrderId(orderId)).thenReturn(false);
// when(refundRepository.save(any(RefundEntity.class))).thenReturn(refund);

// // When
// RefundResponse result = refundService.createRefundRequest(refundRequest,
// customerId);

// // Then
// assertNotNull(result);
// assertEquals(RefundStatus.PENDING, result.status());
// }

// @Test
// @DisplayName("Should throw ORDER_NOT_FOUND when order does not exist")
// void shouldThrowOrderNotFoundWhenOrderDoesNotExist() {
// // Given
// when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

// // When & Then
// ApiException exception = assertThrows(ApiException.class,
// () -> refundService.createRefundRequest(refundRequest, customerId));

// assertEquals(OrderErrorCode.ORDER_NOT_FOUND, exception.getErrorCode());
// verify(orderRepository).findById(orderId);
// verify(refundRepository, never()).save(any());
// }

// @Test
// @DisplayName("Should throw ORDER_NOT_OWNED when customer does not own order")
// void shouldThrowOrderNotOwnedWhenCustomerDoesNotOwnOrder() {
// // Given
// UUID differentCustomerId = UUID.randomUUID();
// when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

// // When & Then
// ApiException exception = assertThrows(ApiException.class,
// () -> refundService.createRefundRequest(refundRequest, differentCustomerId));

// assertEquals(OrderErrorCode.ORDER_NOT_OWNED, exception.getErrorCode());
// verify(orderRepository).findById(orderId);
// verify(refundRepository, never()).save(any());
// }

// @Test
// @DisplayName("Should throw INVALID_ORDER_STATUS for PENDING order")
// void shouldThrowInvalidOrderStatusForPendingOrder() {
// // Given
// order.setStatus(OrderStatus.PENDING);
// when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

// // When & Then
// ApiException exception = assertThrows(ApiException.class,
// () -> refundService.createRefundRequest(refundRequest, customerId));

// assertEquals(RefundErrorCode.INVALID_ORDER_STATUS_FOR_REFUND,
// exception.getErrorCode());
// verify(refundRepository, never()).save(any());
// }

// @Test
// @DisplayName("Should throw INVALID_ORDER_STATUS for CANCELED order")
// void shouldThrowInvalidOrderStatusForCanceledOrder() {
// // Given
// order.setStatus(OrderStatus.CANCELED);
// when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

// // When & Then
// ApiException exception = assertThrows(ApiException.class,
// () -> refundService.createRefundRequest(refundRequest, customerId));

// assertEquals(RefundErrorCode.INVALID_ORDER_STATUS_FOR_REFUND,
// exception.getErrorCode());
// }

// @Test
// @DisplayName("Should throw REFUND_ALREADY_EXISTS when refund exists for
// order")
// void shouldThrowRefundAlreadyExistsWhenRefundExistsForOrder() {
// // Given
// when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
// when(refundRepository.existsByOrderId(orderId)).thenReturn(true);

// // When & Then
// ApiException exception = assertThrows(ApiException.class,
// () -> refundService.createRefundRequest(refundRequest, customerId));

// assertEquals(RefundErrorCode.REFUND_ALREADY_EXISTS,
// exception.getErrorCode());
// verify(refundRepository, never()).save(any());
// }

// @Test
// @DisplayName("Should throw PAYMENT_NOT_FOUND when no paid payment exists")
// void shouldThrowPaymentNotFoundWhenNoPaidPaymentExists() {
// // Given
// payment.setStatus(PaymentStatus.PENDING);
// when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
// when(refundRepository.existsByOrderId(orderId)).thenReturn(false);

// // When & Then
// ApiException exception = assertThrows(ApiException.class,
// () -> refundService.createRefundRequest(refundRequest, customerId));

// assertEquals(RefundErrorCode.PAYMENT_NOT_FOUND, exception.getErrorCode());
// verify(refundRepository, never()).save(any());
// }

// @Test
// @DisplayName("Should use order total amount for refund")
// void shouldUseOrderTotalAmountForRefund() {
// // Given
// order.setTotalAmount(BigDecimal.valueOf(150000));

// // Create new request with matching amount
// RefundRequest customRequest = new RefundRequest(
// orderId,
// BigDecimal.valueOf(150000),
// "Product quality issue");

// when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
// when(refundRepository.existsByOrderId(orderId)).thenReturn(false);

// RefundEntity savedRefund = RefundEntity.builder()
// .id(refundId)
// .order(order)
// .payment(payment)
// .amount(BigDecimal.valueOf(150000))
// .reason("Product quality issue")
// .status(RefundStatus.PENDING)
// .createdAt(LocalDateTime.now())
// .build();

// when(refundRepository.save(any(RefundEntity.class))).thenReturn(savedRefund);

// // When
// RefundResponse result = refundService.createRefundRequest(customRequest,
// customerId);

// // Then
// assertEquals(BigDecimal.valueOf(150000), result.amount());
// }

// @Test
// @DisplayName("Should save refund with correct fields")
// void shouldSaveRefundWithCorrectFields() {
// // Given
// when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
// when(refundRepository.existsByOrderId(orderId)).thenReturn(false);
// when(refundRepository.save(any(RefundEntity.class))).thenReturn(refund);

// // When
// refundService.createRefundRequest(refundRequest, customerId);

// // Then
// ArgumentCaptor<RefundEntity> refundCaptor =
// ArgumentCaptor.forClass(RefundEntity.class);
// verify(refundRepository).save(refundCaptor.capture());

// RefundEntity savedRefund = refundCaptor.getValue();
// assertEquals(order, savedRefund.getOrder());
// assertEquals(payment, savedRefund.getPayment());
// assertEquals(order.getTotalAmount(), savedRefund.getAmount());
// assertEquals("Product quality issue", savedRefund.getReason());
// assertEquals(RefundStatus.PENDING, savedRefund.getStatus());
// }

// @Test
// @DisplayName("Should handle order with multiple payments")
// void shouldHandleOrderWithMultiplePayments() {
// // Given
// PaymentEntity failedPayment = new PaymentEntity();
// failedPayment.setId(UUID.randomUUID());
// failedPayment.setStatus(PaymentStatus.FAILED);

// order.setPayments(List.of(failedPayment, payment));

// when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
// when(refundRepository.existsByOrderId(orderId)).thenReturn(false);
// when(refundRepository.save(any(RefundEntity.class))).thenReturn(refund);

// // When
// RefundResponse result = refundService.createRefundRequest(refundRequest,
// customerId);

// // Then
// assertNotNull(result);
// // Should use the PAID payment, not the FAILED one
// }

// @Test
// @DisplayName("Should handle empty payments list")
// void shouldHandleEmptyPaymentsList() {
// // Given
// order.setPayments(new ArrayList<>());
// when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
// when(refundRepository.existsByOrderId(orderId)).thenReturn(false);

// // When & Then
// ApiException exception = assertThrows(ApiException.class,
// () -> refundService.createRefundRequest(refundRequest, customerId));

// assertEquals(RefundErrorCode.PAYMENT_NOT_FOUND, exception.getErrorCode());
// }
// }

// // ==================== getAllPendingRefunds() Tests ====================
// @Nested
// @DisplayName("getAllPendingRefunds() Tests")
// class GetAllPendingRefundsTests {

// @Test
// @DisplayName("Should return list of pending refunds")
// void shouldReturnListOfPendingRefunds() {
// // Given
// RefundEntity refund2 = RefundEntity.builder()
// .id(UUID.randomUUID())
// .order(order)
// .payment(payment)
// .amount(BigDecimal.valueOf(50000))
// .reason("Wrong item delivered")
// .status(RefundStatus.PENDING)
// .createdAt(LocalDateTime.now())
// .build();

// List<RefundEntity> pendingRefunds = List.of(refund, refund2);
// when(refundRepository.findByStatus(RefundStatus.PENDING)).thenReturn(pendingRefunds);

// // When
// List<RefundResponse> result = refundService.getAllPendingRefunds();

// // Then
// assertNotNull(result);
// assertEquals(2, result.size());
// assertEquals(refundId, result.get(0).refundId());
// assertEquals(RefundStatus.PENDING, result.get(0).status());
// assertEquals("Product quality issue", result.get(0).reason());

// verify(refundRepository).findByStatus(RefundStatus.PENDING);
// }

// @Test
// @DisplayName("Should return empty list when no pending refunds")
// void shouldReturnEmptyListWhenNoPendingRefunds() {
// // Given
// when(refundRepository.findByStatus(RefundStatus.PENDING)).thenReturn(new
// ArrayList<>());

// // When
// List<RefundResponse> result = refundService.getAllPendingRefunds();

// // Then
// assertNotNull(result);
// assertTrue(result.isEmpty());
// verify(refundRepository).findByStatus(RefundStatus.PENDING);
// }

// @Test
// @DisplayName("Should map all refund fields correctly")
// void shouldMapAllRefundFieldsCorrectly() {
// // Given
// when(refundRepository.findByStatus(RefundStatus.PENDING)).thenReturn(List.of(refund));

// // When
// List<RefundResponse> result = refundService.getAllPendingRefunds();

// // Then
// RefundResponse dto = result.get(0);
// assertEquals(refundId, dto.refundId());
// assertEquals(orderId, dto.orderId());
// assertEquals("ORD-2026-001", dto.orderNumber());
// assertEquals(BigDecimal.valueOf(100000), dto.amount());
// assertEquals("Product quality issue", dto.reason());
// assertEquals(RefundStatus.PENDING, dto.status());
// assertNotNull(dto.createdAt());
// }

// @Test
// @DisplayName("Should only return PENDING status refunds")
// void shouldOnlyReturnPendingStatusRefunds() {
// // Given
// when(refundRepository.findByStatus(RefundStatus.PENDING)).thenReturn(List.of(refund));

// // When
// refundService.getAllPendingRefunds();

// // Then
// verify(refundRepository).findByStatus(RefundStatus.PENDING);
// verify(refundRepository, never()).findByStatus(RefundStatus.APPROVED);
// verify(refundRepository, never()).findByStatus(RefundStatus.REJECTED);
// }
// }

// // ==================== approveRefund() Tests ====================
// @Nested
// @DisplayName("approveRefund() Tests")
// class ApproveRefundTests {

// @Test
// @DisplayName("Should approve refund successfully")
// void shouldApproveRefundSuccessfully() {
// // Given
// TransactionEntity transaction = TransactionEntity.builder()
// .id(UUID.randomUUID())
// .payment(payment)
// .type(TransactionType.REFUND)
// .amount(refund.getAmount().negate())
// .build();

// when(refundRepository.findById(refundId)).thenReturn(Optional.of(refund));
// when(transactionRepository.save(any(TransactionEntity.class))).thenReturn(transaction);
// when(paymentRepository.save(any(PaymentEntity.class))).thenReturn(payment);
// when(orderRepository.save(any(OrderEntity.class))).thenReturn(order);

// RefundEntity approvedRefund = RefundEntity.builder()
// .id(refundId)
// .order(order)
// .payment(payment)
// .amount(BigDecimal.valueOf(100000))
// .reason("Product quality issue")
// .status(RefundStatus.APPROVED)
// .transaction(transaction)
// .createdAt(refund.getCreatedAt())
// .updatedAt(LocalDateTime.now())
// .build();

// when(refundRepository.save(any(RefundEntity.class))).thenReturn(approvedRefund);

// // When
// RefundResponse result = refundService.approveRefund(refundId);

// // Then
// assertNotNull(result);
// assertEquals(refundId, result.refundId());
// assertEquals(RefundStatus.APPROVED, result.status());

// verify(refundRepository).findById(refundId);
// verify(transactionRepository).save(any(TransactionEntity.class));
// verify(paymentRepository).save(any(PaymentEntity.class));
// verify(orderRepository).save(any(OrderEntity.class));
// verify(refundRepository).save(any(RefundEntity.class));
// }

// @Test
// @DisplayName("Should throw REFUND_NOT_FOUND when refund does not exist")
// void shouldThrowRefundNotFoundWhenRefundDoesNotExist() {
// // Given
// when(refundRepository.findById(refundId)).thenReturn(Optional.empty());

// // When & Then
// ApiException exception = assertThrows(ApiException.class,
// () -> refundService.approveRefund(refundId));

// assertEquals(RefundErrorCode.REFUND_NOT_FOUND, exception.getErrorCode());
// verify(refundRepository).findById(refundId);
// verify(refundRepository, never()).save(any());
// }

// @Test
// @DisplayName("Should throw REFUND_ALREADY_PROCESSED when refund is APPROVED")
// void shouldThrowRefundAlreadyProcessedWhenRefundIsApproved() {
// // Given
// refund.setStatus(RefundStatus.APPROVED);
// when(refundRepository.findById(refundId)).thenReturn(Optional.of(refund));

// // When & Then
// ApiException exception = assertThrows(ApiException.class,
// () -> refundService.approveRefund(refundId));

// assertEquals(RefundErrorCode.REFUND_ALREADY_PROCESSED,
// exception.getErrorCode());
// verify(refundRepository, never()).save(any());
// }

// @Test
// @DisplayName("Should throw REFUND_ALREADY_PROCESSED when refund is REJECTED")
// void shouldThrowRefundAlreadyProcessedWhenRefundIsRejected() {
// // Given
// refund.setStatus(RefundStatus.REJECTED);
// when(refundRepository.findById(refundId)).thenReturn(Optional.of(refund));

// // When & Then
// ApiException exception = assertThrows(ApiException.class,
// () -> refundService.approveRefund(refundId));

// assertEquals(RefundErrorCode.REFUND_ALREADY_PROCESSED,
// exception.getErrorCode());
// }

// @Test
// @DisplayName("Should update refund status to APPROVED")
// void shouldUpdateRefundStatusToApproved() {
// // Given
// TransactionEntity transaction = TransactionEntity.builder()
// .id(UUID.randomUUID())
// .payment(payment)
// .type(TransactionType.REFUND)
// .amount(refund.getAmount().negate())
// .build();

// when(refundRepository.findById(refundId)).thenReturn(Optional.of(refund));
// when(transactionRepository.save(any(TransactionEntity.class))).thenReturn(transaction);
// when(paymentRepository.save(any(PaymentEntity.class))).thenReturn(payment);
// when(orderRepository.save(any(OrderEntity.class))).thenReturn(order);
// when(refundRepository.save(any(RefundEntity.class))).thenAnswer(i ->
// i.getArgument(0));

// // When
// refundService.approveRefund(refundId);

// // Then
// ArgumentCaptor<RefundEntity> captor =
// ArgumentCaptor.forClass(RefundEntity.class);
// verify(refundRepository).save(captor.capture());
// assertEquals(RefundStatus.APPROVED, captor.getValue().getStatus());
// }

// @Test
// @DisplayName("Should preserve refund fields after approval")
// void shouldPreserveRefundFieldsAfterApproval() {
// // Given
// TransactionEntity transaction = TransactionEntity.builder()
// .id(UUID.randomUUID())
// .payment(payment)
// .type(TransactionType.REFUND)
// .amount(refund.getAmount().negate())
// .build();

// when(refundRepository.findById(refundId)).thenReturn(Optional.of(refund));
// when(transactionRepository.save(any(TransactionEntity.class))).thenReturn(transaction);
// when(paymentRepository.save(any(PaymentEntity.class))).thenReturn(payment);
// when(orderRepository.save(any(OrderEntity.class))).thenReturn(order);
// when(refundRepository.save(any(RefundEntity.class))).thenAnswer(i ->
// i.getArgument(0));

// // When
// RefundResponse result = refundService.approveRefund(refundId);

// // Then
// assertEquals(refundId, result.refundId());
// assertEquals(orderId, result.orderId());
// assertEquals(BigDecimal.valueOf(100000), result.amount());
// assertEquals("Product quality issue", result.reason());
// }
// }

// // ==================== declineRefund() Tests ====================
// @Nested
// @DisplayName("declineRefund() Tests")
// class DeclineRefundTests {

// @Test
// @DisplayName("Should decline refund successfully with reason")
// void shouldDeclineRefundSuccessfullyWithReason() {
// // Given
// String declineReason = "Exceeds refund policy timeframe";
// when(refundRepository.findById(refundId)).thenReturn(Optional.of(refund));

// RefundEntity declinedRefund = RefundEntity.builder()
// .id(refundId)
// .order(order)
// .payment(payment)
// .amount(BigDecimal.valueOf(100000))
// .reason("Product quality issue")
// .status(RefundStatus.REJECTED)
// .declineReason(declineReason)
// .createdAt(refund.getCreatedAt())
// .updatedAt(LocalDateTime.now())
// .build();

// when(refundRepository.save(any(RefundEntity.class))).thenReturn(declinedRefund);

// // When
// RefundResponse result = refundService.declineRefund(refundId, declineReason);

// // Then
// assertNotNull(result);
// assertEquals(refundId, result.refundId());
// assertEquals(RefundStatus.REJECTED, result.status());
// assertEquals(declineReason, result.declineReason());

// verify(refundRepository).findById(refundId);
// verify(refundRepository).save(any(RefundEntity.class));
// }

// @Test
// @DisplayName("Should throw REFUND_NOT_FOUND when refund does not exist")
// void shouldThrowRefundNotFoundWhenRefundDoesNotExist() {
// // Given
// when(refundRepository.findById(refundId)).thenReturn(Optional.empty());

// // When & Then
// ApiException exception = assertThrows(ApiException.class,
// () -> refundService.declineRefund(refundId, "Invalid reason"));

// assertEquals(RefundErrorCode.REFUND_NOT_FOUND, exception.getErrorCode());
// verify(refundRepository).findById(refundId);
// verify(refundRepository, never()).save(any());
// }

// @Test
// @DisplayName("Should throw REFUND_ALREADY_PROCESSED when refund is APPROVED")
// void shouldThrowRefundAlreadyProcessedWhenRefundIsApproved() {
// // Given
// refund.setStatus(RefundStatus.APPROVED);
// when(refundRepository.findById(refundId)).thenReturn(Optional.of(refund));

// // When & Then
// ApiException exception = assertThrows(ApiException.class,
// () -> refundService.declineRefund(refundId, "Too late"));

// assertEquals(RefundErrorCode.REFUND_ALREADY_PROCESSED,
// exception.getErrorCode());
// verify(refundRepository, never()).save(any());
// }

// @Test
// @DisplayName("Should throw REFUND_ALREADY_PROCESSED when refund is REJECTED")
// void shouldThrowRefundAlreadyProcessedWhenRefundIsRejected() {
// // Given
// refund.setStatus(RefundStatus.REJECTED);
// when(refundRepository.findById(refundId)).thenReturn(Optional.of(refund));

// // When & Then
// ApiException exception = assertThrows(ApiException.class,
// () -> refundService.declineRefund(refundId, "Already rejected"));

// assertEquals(RefundErrorCode.REFUND_ALREADY_PROCESSED,
// exception.getErrorCode());
// }

// @Test
// @DisplayName("Should update refund status to REJECTED and set decline
// reason")
// void shouldUpdateRefundStatusToRejectedAndSetDeclineReason() {
// // Given
// String declineReason = "Product received in good condition";
// when(refundRepository.findById(refundId)).thenReturn(Optional.of(refund));
// when(refundRepository.save(any(RefundEntity.class))).thenAnswer(i ->
// i.getArgument(0));

// // When
// refundService.declineRefund(refundId, declineReason);

// // Then
// ArgumentCaptor<RefundEntity> captor =
// ArgumentCaptor.forClass(RefundEntity.class);
// verify(refundRepository).save(captor.capture());

// RefundEntity savedRefund = captor.getValue();
// assertEquals(RefundStatus.REJECTED, savedRefund.getStatus());
// assertEquals(declineReason, savedRefund.getDeclineReason());
// }

// @Test
// @DisplayName("Should handle empty decline reason")
// void shouldHandleEmptyDeclineReason() {
// // Given
// String emptyReason = "";
// when(refundRepository.findById(refundId)).thenReturn(Optional.of(refund));

// RefundEntity declinedRefund = RefundEntity.builder()
// .id(refundId)
// .order(order)
// .payment(payment)
// .amount(BigDecimal.valueOf(100000))
// .reason("Product quality issue")
// .status(RefundStatus.REJECTED)
// .declineReason(emptyReason)
// .createdAt(refund.getCreatedAt())
// .build();

// when(refundRepository.save(any(RefundEntity.class))).thenReturn(declinedRefund);

// // When
// RefundResponse result = refundService.declineRefund(refundId, emptyReason);

// // Then
// assertEquals(RefundStatus.REJECTED, result.status());
// assertEquals(emptyReason, result.declineReason());
// }

// @Test
// @DisplayName("Should handle null decline reason")
// void shouldHandleNullDeclineReason() {
// // Given
// when(refundRepository.findById(refundId)).thenReturn(Optional.of(refund));

// RefundEntity declinedRefund = RefundEntity.builder()
// .id(refundId)
// .order(order)
// .payment(payment)
// .amount(BigDecimal.valueOf(100000))
// .reason("Product quality issue")
// .status(RefundStatus.REJECTED)
// .declineReason(null)
// .createdAt(refund.getCreatedAt())
// .build();

// when(refundRepository.save(any(RefundEntity.class))).thenReturn(declinedRefund);

// // When
// RefundResponse result = refundService.declineRefund(refundId, null);

// // Then
// assertEquals(RefundStatus.REJECTED, result.status());
// assertNull(result.declineReason());
// }

// @Test
// @DisplayName("Should preserve refund fields after decline")
// void shouldPreserveRefundFieldsAfterDecline() {
// // Given
// String declineReason = "Insufficient proof";
// when(refundRepository.findById(refundId)).thenReturn(Optional.of(refund));
// when(refundRepository.save(any(RefundEntity.class))).thenAnswer(i ->
// i.getArgument(0));

// // When
// RefundResponse result = refundService.declineRefund(refundId, declineReason);

// // Then
// assertEquals(refundId, result.refundId());
// assertEquals(orderId, result.orderId());
// assertEquals(BigDecimal.valueOf(100000), result.amount());
// assertEquals("Product quality issue", result.reason());
// }
// }
// }
