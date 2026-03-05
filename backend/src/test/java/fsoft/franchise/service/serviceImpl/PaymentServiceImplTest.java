// package fsoft.franchise.service.serviceImpl;

// import fsoft.franchise.common.exception.ApiException;
// import fsoft.franchise.dto.payments.*;
// import fsoft.franchise.exception.CommonErrorCode;
// import fsoft.franchise.exception.PaymentErrorCode;
// import fsoft.franchise.dto.payments.PaymentStatusResponse;
// import fsoft.franchise.entity.OrderEntity;
// import fsoft.franchise.entity.PaymentEntity;
// import fsoft.franchise.entity.TransactionEntity;
// import fsoft.franchise.enums.OrderStatus;
// import fsoft.franchise.enums.PaymentMethod;
// import fsoft.franchise.enums.PaymentStatus;
// import fsoft.franchise.entity.external.AccountEntity;
// import fsoft.franchise.entity.external.ProfileEntity;
// import fsoft.franchise.enums.TransactionStatus;
// import fsoft.franchise.enums.TransactionType;
// import fsoft.franchise.exception.OrderErrorCode;
// import fsoft.franchise.repository.PaymentRepository;
// import fsoft.franchise.repository.TransactionRepository;
// import fsoft.franchise.service.VNPayService;
// import fsoft.franchise.serviceImpl.PaymentServiceImpl;
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.DisplayName;
// import org.junit.jupiter.api.Nested;
// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.api.extension.ExtendWith;
// import org.mockito.InjectMocks;
// import org.mockito.Mock;
// import org.mockito.junit.jupiter.MockitoExtension;
// import org.springframework.data.domain.Page;
// import org.springframework.data.domain.PageImpl;

// import java.math.BigDecimal;
// import java.time.LocalDate;
// import java.time.LocalDateTime;
// import java.util.*;

// import static org.junit.jupiter.api.Assertions.*;
// import static org.mockito.ArgumentMatchers.any;
// import static org.mockito.ArgumentMatchers.eq;
// import static org.mockito.Mockito.*;

// /**
//  * Unit Tests for PaymentServiceImpl using JUnit 5 and Mockito
//  *
//  * Coverage target: 75-85%
//  * Test all public methods with edge cases and error scenarios
//  *
//  * @author Dev Team
//  * @version 1.0
//  * @since 2026-03-02
//  */
// @ExtendWith(MockitoExtension.class)
// @DisplayName("PaymentServiceImpl Unit Tests")
// class PaymentServiceImplTest {

//     // ==================== Mocks ====================
//     @Mock
//     private PaymentRepository paymentRepository;

//     @Mock
//     private TransactionRepository transactionRepository;

//     @Mock
//     private VNPayService vnPayService;

//     @Mock
//     private fsoft.franchise.repository.OrderRepository orderRepository;

//     @InjectMocks
//     private PaymentServiceImpl paymentService;

//     // ==================== Test Data ====================
//     private UUID paymentId;
//     private UUID orderId;
//     private UUID customerId;
//     private UUID transactionId;
//     private PaymentEntity payment;
//     private OrderEntity order;
//     private AccountEntity customer;
//     private TransactionEntity transaction;
//     private PaymentFilterRequest filterRequest;

//     @BeforeEach
//     void setUp() {
//         paymentId = UUID.randomUUID();
//         orderId = UUID.randomUUID();
//         customerId = UUID.randomUUID();
//         transactionId = UUID.randomUUID();

//         // Setup customer
//         ProfileEntity profile = new ProfileEntity();
//         profile.setFirstName("John");
//         profile.setLastName("Doe");

//         customer = AccountEntity.builder()
//                 .email("customer@example.com")
//                 .build();
//         customer.setId(customerId);
//         customer.setProfile(profile);

//         // Setup order
//         order = OrderEntity.builder()
//                 .customer(customer)
//                 .status(OrderStatus.PAID)
//                 .orderNumber("ORD-001")
//                 .build();
//         order.setId(orderId);

//         // Setup payment
//         payment = new PaymentEntity();
//         payment.setId(paymentId);
//         payment.setOrder(order);
//         payment.setPaymentMethod(PaymentMethod.VNPAY);
//         payment.setStatus(PaymentStatus.PENDING);
//         payment.setCreatedAt(LocalDateTime.now());

//         // Setup transaction
//         transaction = new TransactionEntity();
//         transaction.setId(transactionId);
//         transaction.setPayment(payment);
//         transaction.setAmount(BigDecimal.valueOf(100.00));
//         transaction.setVnpTxnRef("TXN-001");
//         transaction.setVnpTransactionNo("VNP-12345");
//         transaction.setVnpResponseCode("00");
//         transaction.setVnpBankCode("NCB");
//         transaction.setCreatedAt(LocalDateTime.now());

//         // Setup filter request
//         filterRequest = new PaymentFilterRequest();
//         filterRequest.setPage(0);
//         filterRequest.setSize(10);
//     }

//     // ==================== getPayments() Tests ====================
//     @Nested
//     @DisplayName("getPayments() Tests")
//     class GetPaymentsTests {

//         @Test
//         @DisplayName("Should return payments for FRANCHISE_ADMIN role")
//         void shouldReturnPaymentsForFranchiseAdmin() {
//             // Given
//             Page<PaymentEntity> page = new PageImpl<>(List.of(payment));
//             when(paymentRepository.findByAdminFilters(any(), any(), any(), any(), any(), any(), any()))
//                     .thenReturn(page);
//             when(transactionRepository.findByPayment_IdOrderByCreatedAtDesc(paymentId))
//                     .thenReturn(List.of(transaction));

//             // When
//             PaymentListResponse result = paymentService.getPayments(filterRequest, customerId, "FRANCHISE_ADMIN");

//             // Then
//             assertNotNull(result);
//             assertNotNull(result.getData());
//             assertEquals(1, result.getData().size());
//             assertNotNull(result.getPagination());

//             PaymentListResponse.PaymentRecord record = result.getData().get(0);
//             assertEquals(orderId.toString(), record.getOrderId());
//             assertEquals("ORD-001", record.getOrderNumber());
//             assertEquals("VNPAY", record.getPaymentMethod());
//             assertEquals(BigDecimal.valueOf(100.00), record.getAmountPaid());
//             assertEquals("PENDING", record.getStatus());

//             verify(paymentRepository).findByAdminFilters(any(), any(), any(), any(), any(), any(), any());
//         }

//         @Test
//         @DisplayName("Should return payments for CUSTOMER role")
//         void shouldReturnPaymentsForCustomer() {
//             // Given
//             Page<PaymentEntity> page = new PageImpl<>(List.of(payment));
//             when(paymentRepository.findByCustomerFilters(any(), any(), any(), any(), any(), any()))
//                     .thenReturn(page);
//             when(transactionRepository.findByPayment_IdOrderByCreatedAtDesc(paymentId))
//                     .thenReturn(List.of(transaction));

//             // When
//             PaymentListResponse result = paymentService.getPayments(filterRequest, customerId, "CUSTOMER");

//             // Then
//             assertNotNull(result);
//             assertEquals(1, result.getData().size());

//             // Customer should NOT see error messages
//             PaymentListResponse.PaymentRecord record = result.getData().get(0);
//             assertNull(record.getErrorMessage());

//             verify(paymentRepository).findByCustomerFilters(eq(customerId), any(), any(), any(), any(), any());
//         }

//         @Test
//         @DisplayName("Should include error message for FRANCHISE_ADMIN")
//         void shouldIncludeErrorMessageForFranchiseAdmin() {
//             // Given
//             transaction.setVnpResponseCode("99"); // Failed transaction
//             Page<PaymentEntity> page = new PageImpl<>(List.of(payment));
//             when(paymentRepository.findByAdminFilters(any(), any(), any(), any(), any(), any(), any()))
//                     .thenReturn(page);
//             when(transactionRepository.findByPayment_IdOrderByCreatedAtDesc(paymentId))
//                     .thenReturn(List.of(transaction));

//             // When
//             PaymentListResponse result = paymentService.getPayments(filterRequest, customerId, "FRANCHISE_ADMIN");

//             // Then
//             PaymentListResponse.PaymentRecord record = result.getData().get(0);
//             assertEquals("99", record.getErrorMessage());
//         }

//         @Test
//         @DisplayName("Should clamp page size to MAX_PAGE_SIZE")
//         void shouldClampPageSizeToMax() {
//             // Given
//             filterRequest.setSize(100); // Greater than MAX_PAGE_SIZE (50)
//             Page<PaymentEntity> page = new PageImpl<>(List.of());
//             when(paymentRepository.findByCustomerFilters(any(), any(), any(), any(), any(), any()))
//                     .thenReturn(page);

//             // When
//             paymentService.getPayments(filterRequest, customerId, "CUSTOMER");

//             // Then
//             verify(paymentRepository).findByCustomerFilters(any(), any(), any(), any(), any(),
//                     argThat(pageable -> pageable.getPageSize() == 50));
//         }

//         @Test
//         @DisplayName("Should handle empty payment list")
//         void shouldHandleEmptyPaymentList() {
//             // Given
//             Page<PaymentEntity> page = new PageImpl<>(List.of());
//             when(paymentRepository.findByCustomerFilters(any(), any(), any(), any(), any(), any()))
//                     .thenReturn(page);

//             // When
//             PaymentListResponse result = paymentService.getPayments(filterRequest, customerId, "CUSTOMER");

//             // Then
//             assertNotNull(result);
//             assertTrue(result.getData().isEmpty());
//             assertEquals(0, result.getPagination().getTotalElements());
//         }

//         @Test
//         @DisplayName("Should resolve customer name from profile")
//         void shouldResolveCustomerNameFromProfile() {
//             // Given
//             Page<PaymentEntity> page = new PageImpl<>(List.of(payment));
//             when(paymentRepository.findByCustomerFilters(any(), any(), any(), any(), any(), any()))
//                     .thenReturn(page);
//             when(transactionRepository.findByPayment_IdOrderByCreatedAtDesc(paymentId))
//                     .thenReturn(List.of(transaction));

//             // When
//             PaymentListResponse result = paymentService.getPayments(filterRequest, customerId, "CUSTOMER");

//             // Then
//             PaymentListResponse.PaymentRecord record = result.getData().get(0);
//             assertEquals("John Doe", record.getCustomerName());
//         }

//         @Test
//         @DisplayName("Should use email as fallback when profile is null")
//         void shouldUseEmailAsFallbackWhenProfileIsNull() {
//             // Given
//             customer.setProfile(null);
//             Page<PaymentEntity> page = new PageImpl<>(List.of(payment));
//             when(paymentRepository.findByCustomerFilters(any(), any(), any(), any(), any(), any()))
//                     .thenReturn(page);
//             when(transactionRepository.findByPayment_IdOrderByCreatedAtDesc(paymentId))
//                     .thenReturn(List.of(transaction));

//             // When
//             PaymentListResponse result = paymentService.getPayments(filterRequest, customerId, "CUSTOMER");

//             // Then
//             PaymentListResponse.PaymentRecord record = result.getData().get(0);
//             assertEquals("customer@example.com", record.getCustomerName());
//         }

//         @Test
//         @DisplayName("Should auto-fill toDate when only fromDate is provided")
//         void shouldAutoFillToDateWhenOnlyFromDateProvided() {
//             // Given
//             filterRequest.setFromDate(java.time.Instant.now().minus(7, java.time.temporal.ChronoUnit.DAYS));
//             filterRequest.setToDate(null); // Only fromDate provided

//             Page<PaymentEntity> page = new PageImpl<>(List.of());
//             when(paymentRepository.findByCustomerFilters(any(), any(), any(), any(), any(), any()))
//                     .thenReturn(page);

//             // When
//             PaymentListResponse result = paymentService.getPayments(filterRequest, customerId, "CUSTOMER");

//             // Then - Should auto-fill toDate to now
//             assertNotNull(result);
//             verify(paymentRepository).findByCustomerFilters(
//                     eq(customerId),
//                     any(),
//                     any(),
//                     any(), // fromDate
//                     any(), // toDate (auto-filled)
//                     any()
//             );
//         }

//         @Test
//         @DisplayName("Should auto-fill fromDate when only toDate is provided")
//         void shouldAutoFillFromDateWhenOnlyToDateProvided() {
//             // Given
//             filterRequest.setFromDate(null); // Only toDate provided
//             filterRequest.setToDate(java.time.Instant.now());

//             Page<PaymentEntity> page = new PageImpl<>(List.of());
//             when(paymentRepository.findByCustomerFilters(any(), any(), any(), any(), any(), any()))
//                     .thenReturn(page);

//             // When
//             PaymentListResponse result = paymentService.getPayments(filterRequest, customerId, "CUSTOMER");

//             // Then - Should auto-fill fromDate to 30 days ago
//             assertNotNull(result);
//             verify(paymentRepository).findByCustomerFilters(
//                     eq(customerId),
//                     any(),
//                     any(),
//                     any(), // fromDate (auto-filled to 30 days ago)
//                     any(), // toDate
//                     any()
//             );
//         }

//         @Test
//         @DisplayName("Should handle invalid UUID in orderId filter gracefully")
//         void shouldHandleInvalidUuidInOrderIdFilter() {
//             // Given
//             filterRequest.setOrderId("invalid-uuid-format");
//             Page<PaymentEntity> page = new PageImpl<>(List.of());
//             when(paymentRepository.findByCustomerFilters(any(), any(), any(), any(), any(), any()))
//                     .thenReturn(page);

//             // When
//             PaymentListResponse result = paymentService.getPayments(filterRequest, customerId, "CUSTOMER");

//             // Then - Should pass null for orderIdFilter instead of throwing exception
//             assertNotNull(result);
//             verify(paymentRepository).findByCustomerFilters(
//                     eq(customerId),
//                     eq(null), // orderIdFilter should be null for invalid UUID
//                     any(),
//                     any(),
//                     any(),
//                     any()
//             );
//         }

//         @Test
//         @DisplayName("Should parse valid orderId filter")
//         void shouldParseValidOrderIdFilter() {
//             // Given
//             filterRequest.setOrderId(orderId.toString());
//             Page<PaymentEntity> page = new PageImpl<>(List.of());
//             when(paymentRepository.findByCustomerFilters(any(), any(), any(), any(), any(), any()))
//                     .thenReturn(page);

//             // When
//             PaymentListResponse result = paymentService.getPayments(filterRequest, customerId, "CUSTOMER");

//             // Then
//             assertNotNull(result);
//             verify(paymentRepository).findByCustomerFilters(
//                     eq(customerId),
//                     eq(orderId), // Valid UUID parsed
//                     any(),
//                     any(),
//                     any(),
//                     any()
//             );
//         }

//         @Test
//         @DisplayName("Should handle invalid payment status filter gracefully")
//         void shouldHandleInvalidPaymentStatusFilter() {
//             // Given
//             filterRequest.setStatus("INVALID_STATUS");
//             Page<PaymentEntity> page = new PageImpl<>(List.of());
//             when(paymentRepository.findByCustomerFilters(any(), any(), any(), any(), any(), any()))
//                     .thenReturn(page);

//             // When
//             PaymentListResponse result = paymentService.getPayments(filterRequest, customerId, "CUSTOMER");

//             // Then - Should pass null for invalid status
//             assertNotNull(result);
//             verify(paymentRepository).findByCustomerFilters(
//                     eq(customerId),
//                     any(),
//                     eq(null), // Invalid status → null
//                     any(),
//                     any(),
//                     any()
//             );
//         }

//         @Test
//         @DisplayName("Should parse valid payment status filter")
//         void shouldParseValidPaymentStatusFilter() {
//             // Given
//             filterRequest.setStatus("PAID");
//             Page<PaymentEntity> page = new PageImpl<>(List.of());
//             when(paymentRepository.findByCustomerFilters(any(), any(), any(), any(), any(), any()))
//                     .thenReturn(page);

//             // When
//             PaymentListResponse result = paymentService.getPayments(filterRequest, customerId, "CUSTOMER");

//             // Then
//             assertNotNull(result);
//             verify(paymentRepository).findByCustomerFilters(
//                     eq(customerId),
//                     any(),
//                     eq(PaymentStatus.PAID), // Valid status parsed
//                     any(),
//                     any(),
//                     any()
//             );
//         }

//         @Test
//         @DisplayName("Should handle payment with null order")
//         void shouldHandlePaymentWithNullOrder() {
//             // Given
//             payment.setOrder(null); // Null order
//             Page<PaymentEntity> page = new PageImpl<>(List.of(payment));
//             when(paymentRepository.findByCustomerFilters(any(), any(), any(), any(), any(), any()))
//                     .thenReturn(page);
//             when(transactionRepository.findByPayment_IdOrderByCreatedAtDesc(paymentId))
//                     .thenReturn(List.of(transaction));

//             // When
//             PaymentListResponse result = paymentService.getPayments(filterRequest, customerId, "CUSTOMER");

//             // Then
//             assertNotNull(result);
//             PaymentListResponse.PaymentRecord record = result.getData().get(0);
//             assertNull(record.getOrderId());
//             assertNull(record.getOrderNumber());
//             assertNull(record.getCustomerName());
//         }

//         @Test
//         @DisplayName("Should handle payment with null transaction")
//         void shouldHandlePaymentWithNullTransaction() {
//             // Given
//             Page<PaymentEntity> page = new PageImpl<>(List.of(payment));
//             when(paymentRepository.findByCustomerFilters(any(), any(), any(), any(), any(), any()))
//                     .thenReturn(page);
//             when(transactionRepository.findByPayment_IdOrderByCreatedAtDesc(paymentId))
//                     .thenReturn(List.of()); // No transactions

//             // When
//             PaymentListResponse result = paymentService.getPayments(filterRequest, customerId, "CUSTOMER");

//             // Then
//             assertNotNull(result);
//             PaymentListResponse.PaymentRecord record = result.getData().get(0);
//             assertNull(record.getAmountPaid());
//             assertNull(record.getErrorMessage());
//         }

//         @Test
//         @DisplayName("Should recognize STORE_MANAGER as admin role")
//         void shouldRecognizeStoreManagerAsAdminRole() {
//             // Given
//             Page<PaymentEntity> page = new PageImpl<>(List.of(payment));
//             when(paymentRepository.findByAdminFilters(any(), any(), any(), any(), any(), any(), any()))
//                     .thenReturn(page);
//             when(transactionRepository.findByPayment_IdOrderByCreatedAtDesc(paymentId))
//                     .thenReturn(List.of(transaction));

//             // When
//             PaymentListResponse result = paymentService.getPayments(filterRequest, customerId, "STORE_MANAGER");

//             // Then
//             assertNotNull(result);
//             // Should use admin filters
//             verify(paymentRepository).findByAdminFilters(any(), any(), any(), any(), any(), any(), any());
//             verify(paymentRepository, never()).findByCustomerFilters(any(), any(), any(), any(), any(), any());
//         }
//     }

//     // ==================== getPaymentStatus() Tests ====================
//     @Nested
//     @DisplayName("getPaymentStatus() Tests")
//     class GetPaymentStatusTests {

//         @Test
//         @DisplayName("Should return payment status for FRANCHISE_ADMIN")
//         void shouldReturnPaymentStatusForAdmin() {
//             // Given
//             when(paymentRepository.findByOrder_IdOrderByPaymentDateDesc(orderId))
//                     .thenReturn(List.of(payment));
//             when(transactionRepository.findByPayment_IdOrderByCreatedAtDesc(paymentId))
//                     .thenReturn(List.of(transaction));

//             // When
//             PaymentStatusResponse result = paymentService.getPaymentStatus(orderId, customerId, "FRANCHISE_ADMIN");

//             // Then
//             assertNotNull(result);
//             assertEquals(orderId, result.getOrderId());
//             assertEquals(paymentId, result.getPaymentId());
//             assertEquals("VNPAY", result.getPaymentMethod());
//             assertEquals("PENDING", result.getStatus());
//             assertEquals(BigDecimal.valueOf(100.00), result.getAmountPaid());
//         }

//         @Test
//         @DisplayName("Should throw PAYMENT_NOT_FOUND when no payments exist")
//         void shouldThrowPaymentNotFoundWhenNoPayments() {
//             // Given
//             when(paymentRepository.findByOrder_IdOrderByPaymentDateDesc(orderId))
//                     .thenReturn(List.of());

//             // When & Then
//             ApiException exception = assertThrows(ApiException.class,
//                     () -> paymentService.getPaymentStatus(orderId, customerId, "CUSTOMER"));

//             assertEquals(PaymentErrorCode.PAYMENT_NOT_FOUND, exception.getErrorCode());
//         }

//         @Test
//         @DisplayName("Should throw PAYMENT_ACCESS_DENIED when customer tries to view other's payment")
//         void shouldThrowAccessDeniedForNonOwner() {
//             // Given
//             UUID strangerId = UUID.randomUUID();
//             when(paymentRepository.findByOrder_IdOrderByPaymentDateDesc(orderId))
//                     .thenReturn(List.of(payment));

//             // When & Then
//             ApiException exception = assertThrows(ApiException.class,
//                     () -> paymentService.getPaymentStatus(orderId, strangerId, "CUSTOMER"));

//             assertEquals(PaymentErrorCode.PAYMENT_ACCESS_DENIED, exception.getErrorCode());
//         }

//         @Test
//         @DisplayName("Should allow STORE_MANAGER to view any payment")
//         void shouldAllowStoreManagerToViewAnyPayment() {
//             // Given
//             UUID strangerId = UUID.randomUUID();
//             when(paymentRepository.findByOrder_IdOrderByPaymentDateDesc(orderId))
//                     .thenReturn(List.of(payment));
//             when(transactionRepository.findByPayment_IdOrderByCreatedAtDesc(paymentId))
//                     .thenReturn(List.of(transaction));

//             // When
//             PaymentStatusResponse result = paymentService.getPaymentStatus(orderId, strangerId, "STORE_MANAGER");

//             // Then
//             assertNotNull(result);
//             assertEquals(orderId, result.getOrderId());
//             // STORE_MANAGER can view any payment
//         }

//         @Test
//         @DisplayName("Should handle payment with null order gracefully")
//         void shouldHandlePaymentWithNullOrder() {
//             // Given
//             payment.setOrder(null);
//             when(paymentRepository.findByOrder_IdOrderByPaymentDateDesc(orderId))
//                     .thenReturn(List.of(payment));
//             when(transactionRepository.findByPayment_IdOrderByCreatedAtDesc(paymentId))
//                     .thenReturn(List.of(transaction));

//             // When
//             PaymentStatusResponse result = paymentService.getPaymentStatus(orderId, customerId, "FRANCHISE_ADMIN");

//             // Then
//             assertNotNull(result);
//             assertNull(result.getOrderId());
//         }

//         @Test
//         @DisplayName("Should handle payment with null transaction")
//         void shouldHandlePaymentWithNullTransaction() {
//             // Given
//             when(paymentRepository.findByOrder_IdOrderByPaymentDateDesc(orderId))
//                     .thenReturn(List.of(payment));
//             when(transactionRepository.findByPayment_IdOrderByCreatedAtDesc(paymentId))
//                     .thenReturn(List.of()); // No transactions

//             // When
//             PaymentStatusResponse result = paymentService.getPaymentStatus(orderId, customerId, "FRANCHISE_ADMIN");

//             // Then
//             assertNotNull(result);
//             assertNull(result.getAmountPaid());
//             assertNotNull(result.getTransaction());
//             assertNull(result.getTransaction().getVnpTxnRef());
//         }

//         @Test
//         @DisplayName("Should return latest payment when multiple payments exist")
//         void shouldReturnLatestPaymentWhenMultipleExist() {
//             // Given
//             PaymentEntity oldPayment = new PaymentEntity();
//             oldPayment.setId(UUID.randomUUID());
//             oldPayment.setOrder(order);
//             oldPayment.setPaymentMethod(PaymentMethod.CASH);
//             oldPayment.setStatus(PaymentStatus.FAILED);

//             // payment is the latest one
//             when(paymentRepository.findByOrder_IdOrderByPaymentDateDesc(orderId))
//                     .thenReturn(List.of(payment, oldPayment)); // payment first (latest)
//             when(transactionRepository.findByPayment_IdOrderByCreatedAtDesc(paymentId))
//                     .thenReturn(List.of(transaction));

//             // When
//             PaymentStatusResponse result = paymentService.getPaymentStatus(orderId, customerId, "CUSTOMER");

//             // Then
//             assertNotNull(result);
//             assertEquals(paymentId, result.getPaymentId());
//             assertEquals("VNPAY", result.getPaymentMethod());
//             assertEquals("PENDING", result.getStatus());
//         }
//     }

//     // ==================== getAdminTransactions() Tests ====================
//     @Nested
//     @DisplayName("getAdminTransactions() Tests")
//     class GetAdminTransactionsTests {

//         @Test
//         @DisplayName("Should return admin transactions successfully")
//         void shouldReturnAdminTransactionsSuccessfully() {
//             // Given
//             Page<PaymentEntity> page = new PageImpl<>(List.of(payment));
//             when(paymentRepository.findByAdminTransactionFilters(any(), any(), any(), any(), any()))
//                     .thenReturn(page);
//             when(transactionRepository.findByPayment_IdOrderByCreatedAtDesc(paymentId))
//                     .thenReturn(List.of(transaction));

//             // When
//             AdminTransactionListResponse result = paymentService.getAdminTransactions(
//                     1, 10, Optional.empty(), Optional.empty(),
//                     Optional.empty(), Optional.empty());

//             // Then
//             assertNotNull(result);
//             assertEquals(1, result.getContent().size());
//             assertEquals(1, result.getPage());
//             assertEquals(10, result.getSize());
//         }

//         @Test
//         @DisplayName("Should throw BAD_REQUEST for invalid page number")
//         void shouldThrowBadRequestForInvalidPage() {
//             // When & Then
//             assertThrows(ApiException.class,
//                     () -> paymentService.getAdminTransactions(0, 10, Optional.empty(),
//                             Optional.empty(), Optional.empty(), Optional.empty()));
//         }

//         @Test
//         @DisplayName("Should throw VALIDATION_FAILED for invalid date range")
//         void shouldThrowValidationFailedForInvalidDateRange() {
//             // Given
//             LocalDate fromDate = LocalDate.now();
//             LocalDate toDate = LocalDate.now().minusDays(1);

//             // When & Then
//             ApiException exception = assertThrows(ApiException.class,
//                     () -> paymentService.getAdminTransactions(1, 10, Optional.empty(),
//                             Optional.empty(), Optional.of(fromDate), Optional.of(toDate)));

//             assertEquals(CommonErrorCode.VALIDATION_FAILED, exception.getErrorCode());
//         }

//         @Test
//         @DisplayName("Should handle empty transaction list")
//         void shouldHandleEmptyTransactionList() {
//             // Given
//             Page<PaymentEntity> page = new PageImpl<>(List.of());
//             when(paymentRepository.findByAdminTransactionFilters(any(), any(), any(), any(), any()))
//                     .thenReturn(page);

//             // When
//             AdminTransactionListResponse result = paymentService.getAdminTransactions(
//                     1, 10, Optional.empty(), Optional.empty(),
//                     Optional.empty(), Optional.empty());

//             // Then
//             assertNotNull(result);
//             assertEquals(0, result.getContent().size());
//             assertEquals(0, result.getTotalElements());
//         }

//         @Test
//         @DisplayName("Should filter by payment method")
//         void shouldFilterByPaymentMethod() {
//             // Given
//             Page<PaymentEntity> page = new PageImpl<>(List.of(payment));
//             when(paymentRepository.findByAdminTransactionFilters(any(), any(), any(), any(), any()))
//                     .thenReturn(page);
//             when(transactionRepository.findByPayment_IdOrderByCreatedAtDesc(paymentId))
//                     .thenReturn(List.of(transaction));

//             // When
//             AdminTransactionListResponse result = paymentService.getAdminTransactions(
//                     1, 10, Optional.of("VNPAY"), Optional.empty(),
//                     Optional.empty(), Optional.empty());

//             // Then
//             assertNotNull(result);
//             assertEquals(1, result.getContent().size());
//             verify(paymentRepository).findByAdminTransactionFilters(any(), any(), any(), any(), any());
//         }

//         @Test
//         @DisplayName("Should include transaction details")
//         void shouldIncludeTransactionDetails() {
//             // Given
//             Page<PaymentEntity> page = new PageImpl<>(List.of(payment));
//             when(paymentRepository.findByAdminTransactionFilters(any(), any(), any(), any(), any()))
//                     .thenReturn(page);
//             when(transactionRepository.findByPayment_IdOrderByCreatedAtDesc(paymentId))
//                     .thenReturn(List.of(transaction));

//             // When
//             AdminTransactionListResponse result = paymentService.getAdminTransactions(
//                     1, 10, Optional.empty(), Optional.empty(),
//                     Optional.empty(), Optional.empty());

//             // Then
//             AdminTransactionListResponse.TransactionItem item = result.getContent().get(0);
//             assertEquals(orderId, item.getOrderId());
//             assertEquals(paymentId, item.getPaymentId());
//             assertEquals("VNPAY", item.getPaymentMethod());
//             assertEquals("PENDING", item.getPaymentStatus());
//             assertEquals(BigDecimal.valueOf(100.00), item.getAmountPaid());
//             assertEquals("TXN-001", item.getVnpTxnRef());
//             assertEquals("VNP-12345", item.getVnpTransactionNo());
//             assertEquals("00", item.getVnpResponseCode());
//             assertEquals("NCB", item.getVnpBankCode());
//         }

//         @Test
//         @DisplayName("Should validate page size")
//         void shouldValidatePageSize() {
//             // When & Then - Page size too large
//             assertThrows(ApiException.class,
//                     () -> paymentService.getAdminTransactions(1, 101, Optional.empty(),
//                             Optional.empty(), Optional.empty(), Optional.empty()));

//             // Page size too small
//             assertThrows(ApiException.class,
//                     () -> paymentService.getAdminTransactions(1, 0, Optional.empty(),
//                             Optional.empty(), Optional.empty(), Optional.empty()));
//         }

//         @Test
//         @DisplayName("Should handle null payment method filter")
//         void shouldHandleNullPaymentMethodFilter() {
//             // Given
//             Page<PaymentEntity> page = new PageImpl<>(List.of());
//             when(paymentRepository.findByAdminTransactionFilters(any(), any(), any(), any(), any()))
//                     .thenReturn(page);

//             // When
//             AdminTransactionListResponse result = paymentService.getAdminTransactions(
//                     1, 10, Optional.empty(), Optional.empty(),
//                     Optional.empty(), Optional.empty());

//             // Then
//             assertNotNull(result);
//             verify(paymentRepository).findByAdminTransactionFilters(any(), any(), any(), any(), any());
//         }

//         @Test
//         @DisplayName("Should apply date range filter correctly")
//         void shouldApplyDateRangeFilterCorrectly() {
//             // Given
//             LocalDate fromDate = LocalDate.now().minusDays(7);
//             LocalDate toDate = LocalDate.now();
//             Page<PaymentEntity> page = new PageImpl<>(List.of(payment));

//             when(paymentRepository.findByAdminTransactionFilters(any(), any(), any(), any(), any()))
//                     .thenReturn(page);
//             when(transactionRepository.findByPayment_IdOrderByCreatedAtDesc(paymentId))
//                     .thenReturn(List.of(transaction));

//             // When
//             AdminTransactionListResponse result = paymentService.getAdminTransactions(
//                     1, 10, Optional.empty(), Optional.empty(),
//                     Optional.of(fromDate), Optional.of(toDate));

//             // Then
//             assertNotNull(result);
//             assertEquals(1, result.getContent().size());
//         }

//         @Test
//         @DisplayName("Should handle invalid payment status string")
//         void shouldHandleInvalidPaymentStatus() {
//             // Given
//             Page<PaymentEntity> page = new PageImpl<>(List.of());
//             when(paymentRepository.findByAdminTransactionFilters(any(), any(), any(), any(), any()))
//                     .thenReturn(page);

//             // When - Invalid status should be treated as null (no filter)
//             AdminTransactionListResponse result = paymentService.getAdminTransactions(
//                     1, 10, Optional.of("INVALID_STATUS"), Optional.empty(),
//                     Optional.empty(), Optional.empty());

//             // Then
//             assertNotNull(result);
//             verify(paymentRepository).findByAdminTransactionFilters(
//                     eq(null), // Invalid status → null
//                     any(),
//                     any(),
//                     any(),
//                     any()
//             );
//         }

//         @Test
//         @DisplayName("Should handle invalid payment method string")
//         void shouldHandleInvalidPaymentMethod() {
//             // Given
//             Page<PaymentEntity> page = new PageImpl<>(List.of());
//             when(paymentRepository.findByAdminTransactionFilters(any(), any(), any(), any(), any()))
//                     .thenReturn(page);

//             // When - Invalid payment method should be treated as null
//             AdminTransactionListResponse result = paymentService.getAdminTransactions(
//                     1, 10, Optional.empty(), Optional.of("INVALID_METHOD"),
//                     Optional.empty(), Optional.empty());

//             // Then
//             assertNotNull(result);
//             verify(paymentRepository).findByAdminTransactionFilters(
//                     any(),
//                     eq(null), // Invalid method → null
//                     any(),
//                     any(),
//                     any()
//             );
//         }

//         @Test
//         @DisplayName("Should parse valid payment status enum")
//         void shouldParseValidPaymentStatus() {
//             // Given
//             Page<PaymentEntity> page = new PageImpl<>(List.of());
//             when(paymentRepository.findByAdminTransactionFilters(any(), any(), any(), any(), any()))
//                     .thenReturn(page);

//             // When
//             AdminTransactionListResponse result = paymentService.getAdminTransactions(
//                     1, 10, Optional.of("PAID"), Optional.empty(),
//                     Optional.empty(), Optional.empty());

//             // Then
//             assertNotNull(result);
//             verify(paymentRepository).findByAdminTransactionFilters(
//                     eq(PaymentStatus.PAID), // Valid status parsed
//                     any(),
//                     any(),
//                     any(),
//                     any()
//             );
//         }

//         @Test
//         @DisplayName("Should parse valid payment method enum")
//         void shouldParseValidPaymentMethod() {
//             // Given
//             Page<PaymentEntity> page = new PageImpl<>(List.of());
//             when(paymentRepository.findByAdminTransactionFilters(any(), any(), any(), any(), any()))
//                     .thenReturn(page);

//             // When
//             AdminTransactionListResponse result = paymentService.getAdminTransactions(
//                     1, 10, Optional.empty(), Optional.of("CASH"),
//                     Optional.empty(), Optional.empty());

//             // Then
//             assertNotNull(result);
//             verify(paymentRepository).findByAdminTransactionFilters(
//                     any(),
//                     eq(PaymentMethod.CASH), // Valid method parsed
//                     any(),
//                     any(),
//                     any()
//             );
//         }

//         @Test
//         @DisplayName("Should handle blank status string")
//         void shouldHandleBlankStatusString() {
//             // Given
//             Page<PaymentEntity> page = new PageImpl<>(List.of());
//             when(paymentRepository.findByAdminTransactionFilters(any(), any(), any(), any(), any()))
//                     .thenReturn(page);

//             // When - Blank string should be treated as null
//             AdminTransactionListResponse result = paymentService.getAdminTransactions(
//                     1, 10, Optional.of("  "), Optional.empty(),
//                     Optional.empty(), Optional.empty());

//             // Then
//             assertNotNull(result);
//             verify(paymentRepository).findByAdminTransactionFilters(
//                     eq(null), // Blank → null
//                     any(),
//                     any(),
//                     any(),
//                     any()
//             );
//         }

//         @Test
//         @DisplayName("Should handle payment with null order in transaction item")
//         void shouldHandlePaymentWithNullOrderInTransactionItem() {
//             // Given
//             payment.setOrder(null);
//             Page<PaymentEntity> page = new PageImpl<>(List.of(payment));
//             when(paymentRepository.findByAdminTransactionFilters(any(), any(), any(), any(), any()))
//                     .thenReturn(page);
//             when(transactionRepository.findByPayment_IdOrderByCreatedAtDesc(paymentId))
//                     .thenReturn(List.of(transaction));

//             // When
//             AdminTransactionListResponse result = paymentService.getAdminTransactions(
//                     1, 10, Optional.empty(), Optional.empty(),
//                     Optional.empty(), Optional.empty());

//             // Then
//             assertNotNull(result);
//             AdminTransactionListResponse.TransactionItem item = result.getContent().get(0);
//             assertNull(item.getOrderId());
//             assertNull(item.getCustomerId());
//         }

//         @Test
//         @DisplayName("Should handle payment with null customer in order")
//         void shouldHandlePaymentWithNullCustomerInOrder() {
//             // Given
//             order.setCustomer(null);
//             Page<PaymentEntity> page = new PageImpl<>(List.of(payment));
//             when(paymentRepository.findByAdminTransactionFilters(any(), any(), any(), any(), any()))
//                     .thenReturn(page);
//             when(transactionRepository.findByPayment_IdOrderByCreatedAtDesc(paymentId))
//                     .thenReturn(List.of(transaction));

//             // When
//             AdminTransactionListResponse result = paymentService.getAdminTransactions(
//                     1, 10, Optional.empty(), Optional.empty(),
//                     Optional.empty(), Optional.empty());

//             // Then
//             assertNotNull(result);
//             AdminTransactionListResponse.TransactionItem item = result.getContent().get(0);
//             assertNull(item.getCustomerId());
//         }

//         @Test
//         @DisplayName("Should handle payment with no transaction")
//         void shouldHandlePaymentWithNoTransaction() {
//             // Given
//             Page<PaymentEntity> page = new PageImpl<>(List.of(payment));
//             when(paymentRepository.findByAdminTransactionFilters(any(), any(), any(), any(), any()))
//                     .thenReturn(page);
//             when(transactionRepository.findByPayment_IdOrderByCreatedAtDesc(paymentId))
//                     .thenReturn(List.of()); // No transactions

//             // When
//             AdminTransactionListResponse result = paymentService.getAdminTransactions(
//                     1, 10, Optional.empty(), Optional.empty(),
//                     Optional.empty(), Optional.empty());

//             // Then
//             assertNotNull(result);
//             AdminTransactionListResponse.TransactionItem item = result.getContent().get(0);
//             assertNull(item.getTransactionId());
//             assertNull(item.getAmountPaid());
//             assertNull(item.getVnpTxnRef());
//             assertNull(item.getVnpTransactionNo());
//         }

//         @Test
//         @DisplayName("Should handle case-insensitive payment status")
//         void shouldHandleCaseInsensitivePaymentStatus() {
//             // Given
//             Page<PaymentEntity> page = new PageImpl<>(List.of());
//             when(paymentRepository.findByAdminTransactionFilters(any(), any(), any(), any(), any()))
//                     .thenReturn(page);

//             // When - lowercase should work
//             AdminTransactionListResponse result = paymentService.getAdminTransactions(
//                     1, 10, Optional.of("paid"), Optional.empty(),
//                     Optional.empty(), Optional.empty());

//             // Then
//             assertNotNull(result);
//             verify(paymentRepository).findByAdminTransactionFilters(
//                     eq(PaymentStatus.PAID), // Case-insensitive parsing
//                     any(),
//                     any(),
//                     any(),
//                     any()
//             );
//         }

//         @Test
//         @DisplayName("Should handle case-insensitive payment method")
//         void shouldHandleCaseInsensitivePaymentMethod() {
//             // Given
//             Page<PaymentEntity> page = new PageImpl<>(List.of());
//             when(paymentRepository.findByAdminTransactionFilters(any(), any(), any(), any(), any()))
//                     .thenReturn(page);

//             // When - lowercase should work
//             AdminTransactionListResponse result = paymentService.getAdminTransactions(
//                     1, 10, Optional.empty(), Optional.of("vnpay"),
//                     Optional.empty(), Optional.empty());

//             // Then
//             assertNotNull(result);
//             verify(paymentRepository).findByAdminTransactionFilters(
//                     any(),
//                     eq(PaymentMethod.VNPAY), // Case-insensitive parsing
//                     any(),
//                     any(),
//                     any()
//             );
//         }
//     }

//     // ==================== createPayment() Tests ====================
//     @Nested
//     @DisplayName("createPayment() Tests")
//     class CreatePaymentTests {

//         private CreatePaymentRequestDTO createPaymentRequest;
//         private String ipAddress = "127.0.0.1";

//         @BeforeEach
//         void setUpCreatePayment() {
//             order.setTotalAmount(BigDecimal.valueOf(100.00));
//             payment.setAmountPaid(BigDecimal.valueOf(100.00));
//             createPaymentRequest = new CreatePaymentRequestDTO(
//                     orderId,
//                     PaymentMethod.VNPAY,
//                     BigDecimal.valueOf(100.00),
//                     "TXN-" + System.currentTimeMillis()
//             );
//         }

//         @Test
//         @DisplayName("Should create payment successfully with valid request")
//         void shouldCreatePaymentSuccessfully() {
//             // Given
//             when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
//             when(paymentRepository.existsByOrder_IdAndStatus(orderId, PaymentStatus.PENDING))
//                     .thenReturn(false);
//             when(transactionRepository.existsByVnpTxnRef(anyString())).thenReturn(false);
//             when(paymentRepository.save(any(PaymentEntity.class))).thenAnswer(inv -> {
//                 PaymentEntity p = inv.getArgument(0);
//                 p.setId(paymentId);
//                 return p;
//             });
//             when(transactionRepository.save(any(TransactionEntity.class))).thenAnswer(inv -> inv.getArgument(0));
//             when(vnPayService.createPaymentUrl(anyString(), anyLong(), anyString(), anyString()))
//                     .thenReturn("https://sandbox.vnpayment.vn/paymentv2/vpcpay.html?vnp_...");

//             // When
//             PaymentResponseDTO result =
//                     paymentService.createPayment(createPaymentRequest, ipAddress);

//             // Then
//             assertNotNull(result);
//             assertEquals(paymentId, result.paymentId());
//             assertEquals(orderId, result.orderId());
//             assertEquals("VNPay", result.paymentMethod());
//             assertEquals(BigDecimal.valueOf(100.00), result.amountPaid());
//             assertEquals("PENDING", result.paymentStatus());
//             assertNotNull(result.paymentUrl());
//             assertNotNull(result.expiredAt());

//             verify(orderRepository).findById(orderId);
//             verify(paymentRepository).existsByOrder_IdAndStatus(orderId, PaymentStatus.PENDING);
//             verify(transactionRepository).existsByVnpTxnRef(anyString());
//             verify(paymentRepository).save(any(PaymentEntity.class));
//             verify(transactionRepository).save(any(TransactionEntity.class));
//             verify(vnPayService).createPaymentUrl(anyString(), anyLong(), anyString(), anyString());
//         }

//         @Test
//         @DisplayName("Should throw ORDER_NOT_FOUND when order does not exist")
//         void shouldThrowOrderNotFoundWhenCreatingPayment() {
//             // Given
//             when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

//             // When & Then
//             ApiException exception = assertThrows(ApiException.class,
//                     () -> paymentService.createPayment(createPaymentRequest, ipAddress));

//             assertEquals(OrderErrorCode.ORDER_NOT_FOUND, exception.getErrorCode());
//             verify(paymentRepository, never()).save(any());
//             verify(transactionRepository, never()).save(any());
//         }

//         @Test
//         @DisplayName("Should throw error when pending payment already exists")
//         void shouldThrowErrorWhenPendingPaymentExists() {
//             // Given
//             when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
//             when(paymentRepository.existsByOrder_IdAndStatus(orderId, PaymentStatus.PENDING))
//                     .thenReturn(true);

//             // When & Then
//             ApiException exception = assertThrows(ApiException.class,
//                     () -> paymentService.createPayment(createPaymentRequest, ipAddress));

//             assertEquals(PaymentErrorCode.PAYMENT_PROVIDER_ERROR, exception.getErrorCode());
//             assertTrue(exception.getMessage().contains("already exists"));
//             verify(paymentRepository, never()).save(any());
//         }

//         @Test
//         @DisplayName("Should throw error when duplicate transaction ID exists")
//         void shouldThrowErrorWhenDuplicateTransactionId() {
//             // Given
//             when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
//             when(paymentRepository.existsByOrder_IdAndStatus(orderId, PaymentStatus.PENDING))
//                     .thenReturn(false);
//             when(transactionRepository.existsByVnpTxnRef(anyString())).thenReturn(true);

//             // When & Then
//             ApiException exception = assertThrows(ApiException.class,
//                     () -> paymentService.createPayment(createPaymentRequest, ipAddress));

//             assertEquals(PaymentErrorCode.PAYMENT_PROVIDER_ERROR, exception.getErrorCode());
//             assertTrue(exception.getMessage().contains("Duplicate transactionId"));
//             verify(paymentRepository, never()).save(any());
//         }

//         @Test
//         @DisplayName("Should save payment with correct status and expiry")
//         void shouldSavePaymentWithCorrectStatusAndExpiry() {
//             // Given
//             when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
//             when(paymentRepository.existsByOrder_IdAndStatus(orderId, PaymentStatus.PENDING))
//                     .thenReturn(false);
//             when(transactionRepository.existsByVnpTxnRef(anyString())).thenReturn(false);

//             org.mockito.ArgumentCaptor<PaymentEntity> paymentCaptor =
//                     org.mockito.ArgumentCaptor.forClass(PaymentEntity.class);
//             when(paymentRepository.save(paymentCaptor.capture())).thenAnswer(inv -> {
//                 PaymentEntity p = inv.getArgument(0);
//                 p.setId(paymentId);
//                 return p;
//             });
//             when(transactionRepository.save(any(TransactionEntity.class))).thenAnswer(inv -> inv.getArgument(0));
//             when(vnPayService.createPaymentUrl(anyString(), anyLong(), anyString(), anyString()))
//                     .thenReturn("https://sandbox.vnpayment.vn/...");

//             // When
//             paymentService.createPayment(createPaymentRequest, ipAddress);

//             // Then
//             PaymentEntity capturedPayment = paymentCaptor.getValue();
//             assertEquals(PaymentStatus.PENDING, capturedPayment.getStatus());
//             assertEquals(PaymentMethod.VNPAY, capturedPayment.getPaymentMethod());
//             assertNotNull(capturedPayment.getExpiredAt());
//         }

//         @Test
//         @DisplayName("Should create transaction with correct details")
//         void shouldCreateTransactionWithCorrectDetails() {
//             // Given
//             when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
//             when(paymentRepository.existsByOrder_IdAndStatus(orderId, PaymentStatus.PENDING))
//                     .thenReturn(false);
//             when(transactionRepository.existsByVnpTxnRef(anyString())).thenReturn(false);
//             when(paymentRepository.save(any(PaymentEntity.class))).thenAnswer(inv -> {
//                 PaymentEntity p = inv.getArgument(0);
//                 p.setId(paymentId);
//                 return p;
//             });

//             org.mockito.ArgumentCaptor<TransactionEntity> txnCaptor =
//                     org.mockito.ArgumentCaptor.forClass(TransactionEntity.class);
//             when(transactionRepository.save(txnCaptor.capture())).thenAnswer(inv -> inv.getArgument(0));
//             when(vnPayService.createPaymentUrl(anyString(), anyLong(), anyString(), anyString()))
//                     .thenReturn("https://sandbox.vnpayment.vn/...");

//             // When
//             paymentService.createPayment(createPaymentRequest, ipAddress);

//             // Then
//             TransactionEntity capturedTxn = txnCaptor.getValue();
//             assertEquals(createPaymentRequest.transactionId(), capturedTxn.getVnpTxnRef());
//             assertEquals(BigDecimal.valueOf(100.00), capturedTxn.getAmount());
//             assertEquals(TransactionStatus.PENDING, capturedTxn.getStatus());
//             assertEquals(TransactionType.PAYMENT, capturedTxn.getType());
//         }
//     }

//     // ==================== processWebhook() Tests ====================
//     @Nested
//     @DisplayName("processWebhook() Tests")
//     class ProcessWebhookTests {

//         private Map<String, String> webhookParams;
//         private String txnRef = "TXN-" + orderId;

//         @BeforeEach
//         void setUpWebhook() {
//             webhookParams = new HashMap<>();
//             webhookParams.put("vnp_TxnRef", txnRef);
//             webhookParams.put("vnp_ResponseCode", "00");
//             webhookParams.put("vnp_Amount", "10000000"); // 100000.00 VND
//             webhookParams.put("vnp_TransactionNo", "VNP-123456");
//             webhookParams.put("vnp_BankCode", "NCB");

//             transaction.setVnpTxnRef(txnRef);
//             transaction.setStatus(TransactionStatus.PENDING);
//             transaction.setPayment(payment);
//             payment.setOrder(order);
//         }

//         @Test
//         @DisplayName("Should process successful webhook and update payment to PAID")
//         void shouldProcessSuccessfulWebhook() {
//             // Given
//             when(transactionRepository.findByVnpTxnRef(txnRef))
//                     .thenReturn(Optional.of(transaction));
//             when(transactionRepository.save(any(TransactionEntity.class))).thenReturn(transaction);
//             when(paymentRepository.save(any(PaymentEntity.class))).thenReturn(payment);
//             when(orderRepository.save(any(OrderEntity.class))).thenReturn(order);

//             // When
//             WebHookResponseDTO result =
//                     paymentService.processWebhook(webhookParams);

//             // Then
//             assertNotNull(result);
//             assertEquals(paymentId, result.getPaymentId());
//             assertEquals(orderId, result.getOrderId());
//             assertEquals(txnRef, result.getTransactionId());
//             assertEquals("PAID", result.getStatus());
//             assertNotNull(result.getProcessedAt());

//             verify(transactionRepository).save(any(TransactionEntity.class));
//             verify(paymentRepository).save(any(PaymentEntity.class));
//             verify(orderRepository).save(any(OrderEntity.class));
//         }

//         @Test
//         @DisplayName("Should process failed webhook and update payment to FAILED")
//         void shouldProcessFailedWebhook() {
//             // Given
//             webhookParams.put("vnp_ResponseCode", "24"); // Failed response code
//             when(transactionRepository.findByVnpTxnRef(txnRef))
//                     .thenReturn(Optional.of(transaction));
//             when(transactionRepository.save(any(TransactionEntity.class))).thenReturn(transaction);
//             when(paymentRepository.save(any(PaymentEntity.class))).thenReturn(payment);

//             // When
//             WebHookResponseDTO result =
//                     paymentService.processWebhook(webhookParams);

//             // Then
//             assertNotNull(result);
//             assertEquals("FAILED", result.getStatus());

//             verify(transactionRepository).save(any(TransactionEntity.class));
//             verify(paymentRepository).save(any(PaymentEntity.class));
//             verify(orderRepository, never()).save(any(OrderEntity.class)); // Order not updated for failed payment
//         }

//         @Test
//         @DisplayName("Should throw PAYMENT_NOT_FOUND when transaction does not exist")
//         void shouldThrowPaymentNotFoundForWebhook() {
//             // Given
//             when(transactionRepository.findByVnpTxnRef(txnRef)).thenReturn(Optional.empty());

//             // When & Then
//             ApiException exception = assertThrows(ApiException.class,
//                     () -> paymentService.processWebhook(webhookParams));

//             assertEquals(PaymentErrorCode.PAYMENT_NOT_FOUND, exception.getErrorCode());
//             verify(transactionRepository, never()).save(any());
//             verify(paymentRepository, never()).save(any());
//         }

//         @Test
//         @DisplayName("Should be idempotent and not reprocess completed webhook")
//         void shouldBeIdempotentForCompletedWebhook() {
//             // Given - Transaction already processed
//             transaction.setStatus(TransactionStatus.SUCCESS);
//             when(transactionRepository.findByVnpTxnRef(txnRef))
//                     .thenReturn(Optional.of(transaction));

//             // When
//             WebHookResponseDTO result =
//                     paymentService.processWebhook(webhookParams);

//             // Then
//             assertNotNull(result);
//             assertEquals(paymentId, result.getPaymentId());
//             assertEquals("SUCCESS", result.getStatus());

//             // Should NOT save again
//             verify(transactionRepository, never()).save(any());
//             verify(paymentRepository, never()).save(any());
//             verify(orderRepository, never()).save(any());
//         }

//         @Test
//         @DisplayName("Should update transaction with webhook details")
//         void shouldUpdateTransactionWithWebhookDetails() {
//             // Given
//             when(transactionRepository.findByVnpTxnRef(txnRef))
//                     .thenReturn(Optional.of(transaction));

//             org.mockito.ArgumentCaptor<TransactionEntity> txnCaptor =
//                     org.mockito.ArgumentCaptor.forClass(TransactionEntity.class);
//             when(transactionRepository.save(txnCaptor.capture())).thenReturn(transaction);
//             when(paymentRepository.save(any(PaymentEntity.class))).thenReturn(payment);
//             when(orderRepository.save(any(OrderEntity.class))).thenReturn(order);

//             // When
//             paymentService.processWebhook(webhookParams);

//             // Then
//             TransactionEntity capturedTxn = txnCaptor.getValue();
//             assertEquals("VNP-123456", capturedTxn.getVnpTransactionNo());
//             assertEquals("00", capturedTxn.getVnpResponseCode());
//             assertEquals("NCB", capturedTxn.getVnpBankCode());
//             assertEquals(0, new BigDecimal("100000.00").compareTo(capturedTxn.getAmount()));
//             assertEquals(TransactionStatus.SUCCESS, capturedTxn.getStatus());
//         }

//         @Test
//         @DisplayName("Should update payment status to PAID for successful webhook")
//         void shouldUpdatePaymentStatusToPaidForSuccessfulWebhook() {
//             // Given
//             when(transactionRepository.findByVnpTxnRef(txnRef))
//                     .thenReturn(Optional.of(transaction));
//             when(transactionRepository.save(any(TransactionEntity.class))).thenReturn(transaction);

//             org.mockito.ArgumentCaptor<PaymentEntity> paymentCaptor =
//                     org.mockito.ArgumentCaptor.forClass(PaymentEntity.class);
//             when(paymentRepository.save(paymentCaptor.capture())).thenReturn(payment);
//             when(orderRepository.save(any(OrderEntity.class))).thenReturn(order);

//             // When
//             paymentService.processWebhook(webhookParams);

//             // Then
//             PaymentEntity capturedPayment = paymentCaptor.getValue();
//             assertEquals(PaymentStatus.PAID, capturedPayment.getStatus());
//         }

//         @Test
//         @DisplayName("Should update order status to PAID for successful webhook")
//         void shouldUpdateOrderStatusToPaidForSuccessfulWebhook() {
//             // Given
//             when(transactionRepository.findByVnpTxnRef(txnRef))
//                     .thenReturn(Optional.of(transaction));
//             when(transactionRepository.save(any(TransactionEntity.class))).thenReturn(transaction);
//             when(paymentRepository.save(any(PaymentEntity.class))).thenReturn(payment);

//             org.mockito.ArgumentCaptor<OrderEntity> orderCaptor =
//                     org.mockito.ArgumentCaptor.forClass(OrderEntity.class);
//             when(orderRepository.save(orderCaptor.capture())).thenReturn(order);

//             // When
//             paymentService.processWebhook(webhookParams);

//             // Then
//             OrderEntity capturedOrder = orderCaptor.getValue();
//             assertEquals(OrderStatus.PAID, capturedOrder.getStatus());
//         }

//         @Test
//         @DisplayName("Should handle webhook with null amount")
//         void shouldHandleWebhookWithNullAmount() {
//             // Given
//             webhookParams.remove("vnp_Amount"); // Null amount
//             when(transactionRepository.findByVnpTxnRef(txnRef))
//                     .thenReturn(Optional.of(transaction));
//             when(transactionRepository.save(any(TransactionEntity.class))).thenReturn(transaction);
//             when(paymentRepository.save(any(PaymentEntity.class))).thenReturn(payment);
//             when(orderRepository.save(any(OrderEntity.class))).thenReturn(order);

//             // When
//             WebHookResponseDTO result =
//                     paymentService.processWebhook(webhookParams);

//             // Then
//             assertNotNull(result);
//             assertEquals("PAID", result.getStatus());
//         }
//     }
// }
