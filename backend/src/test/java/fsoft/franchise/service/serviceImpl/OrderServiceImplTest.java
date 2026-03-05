// package fsoft.franchise.service.serviceImpl;

// import fsoft.franchise.common.exception.ApiException;
// import fsoft.franchise.dto.orders.*;
// import fsoft.franchise.dto.payments.PaymentResponseDTO;
// import fsoft.franchise.exception.CommonErrorCode;
// import fsoft.franchise.exception.OrderErrorCode;
// import fsoft.franchise.exception.PaymentErrorCode;
// import fsoft.franchise.dto.payments.PaymentRequestDTO;
// import fsoft.franchise.dto.orders.OrderHistoryPageDTO;
// import fsoft.franchise.entity.*;
// import fsoft.franchise.enums.OrderStatus;
// import fsoft.franchise.enums.PaymentMethod;
// import fsoft.franchise.enums.PaymentStatus;
// import fsoft.franchise.enums.StatusEnum;
// import fsoft.franchise.entity.external.AccountEntity;
// import fsoft.franchise.entity.external.ProfileEntity;
// import fsoft.franchise.repository.AccountRepository;
// import fsoft.franchise.repository.OrderRepository;
// import fsoft.franchise.repository.PaymentRepository;
// import fsoft.franchise.repository.ProductRepository;
// import fsoft.franchise.service.MoMoPaymentService;
// import fsoft.franchise.service.PaymentMethodService;
// import fsoft.franchise.serviceImpl.OrderServiceImpl;
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.DisplayName;
// import org.junit.jupiter.api.Nested;
// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.api.extension.ExtendWith;
// import org.junit.jupiter.params.ParameterizedTest;
// import org.junit.jupiter.params.provider.EnumSource;
// import org.mockito.ArgumentCaptor;
// import org.mockito.InjectMocks;
// import org.mockito.Mock;
// import org.mockito.junit.jupiter.MockitoExtension;
// import org.springframework.data.domain.Page;
// import org.springframework.data.domain.PageImpl;
// import org.springframework.data.domain.PageRequest;

// import java.math.BigDecimal;
// import java.time.LocalDate;
// import java.time.LocalDateTime;
// import java.util.*;

// import static org.junit.jupiter.api.Assertions.*;
// import static org.mockito.ArgumentMatchers.any;
// import static org.mockito.Mockito.*;

// /**
//  * Unit Tests for OrderServiceImpl using JUnit 5 and Mockito
//  *
//  * Coverage target: 75-80%
//  * Test all public methods with edge cases and error scenarios
//  *
//  * @author Dev Team
//  * @version 1.0
//  * @since 2026-03-02
//  */
// @ExtendWith(MockitoExtension.class)
// @DisplayName("OrderServiceImpl Unit Tests")
// class OrderServiceImplTest {

//      // ==================== Mocks ====================
//      @Mock
//      private OrderRepository orderRepository;

//      @Mock
//      private ProductRepository productRepository;

//      @Mock
//      private PaymentRepository paymentRepository;

//      @Mock
//      private AccountRepository AccountRepository; // Note: Viết HOA để match với field trong OrderServiceImpl

//      @Mock
//      private MoMoPaymentService moMoPaymentService;

//      @Mock
//      private PaymentMethodService paymentMethodService;

//      @InjectMocks
//      private OrderServiceImpl orderService;

//      // ==================== Test Data ====================
//      private UUID orderId;
//      private UUID customerId;
//      private UUID productId;
//      private UUID strangerId;
//      private AccountEntity customer;
//      private OrderEntity order;
//      private ProductEntity product;
//      private ProductVariantEntity variant;

//      @BeforeEach
//      void setUp() {
//          orderId = UUID.randomUUID();
//          customerId = UUID.randomUUID();
//          productId = UUID.randomUUID();
//          strangerId = UUID.randomUUID();

//          // Setup customer
//          customer = AccountEntity.builder()
//                  .email("customer@example.com")
//                  .phoneNumber("0123456789")
//                  .status(StatusEnum.ACTIVE)
//                  .build();
//          customer.setId(customerId);

//          ProfileEntity profile = new ProfileEntity();
//          profile.setFirstName("John");
//          profile.setLastName("Doe");
//          customer.setProfile(profile);

//          // Setup product with variant
//          variant = ProductVariantEntity.builder()
//                  .active(true)
//                  .price(BigDecimal.valueOf(100.00))
//                  .build();

//          product = ProductEntity.builder()
//                  .name("Test Product")
//                  .active(true)
//                  .variants(List.of(variant))
//                  .build();
//          product.setId(productId);
//          variant.setProduct(product);

//          // Setup order
//          order = OrderEntity.builder()
//                  .customer(customer)
//                  .status(OrderStatus.PAID)
//                  .orderTime(LocalDateTime.now())
//                  .orderType("POS")
//                  .orderNumber("ORD-001")
//                  .build();
//          order.setId(orderId);
//      }

//      // ==================== cancelOrder() Tests ====================
//      @Nested
//      @DisplayName("cancelOrder() Tests")
//      class CancelOrderTests {

//          @Test
//          @DisplayName("Should cancel order successfully when all validations pass")
//          void shouldCancelOrderSuccessfully() {
//              // Given
//              when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
//              when(orderRepository.save(any(OrderEntity.class))).thenReturn(order);

//              // When
//              OrderCancelDTO result = orderService.cancelOrder(orderId, customerId);

//              // Then
//              assertNotNull(result);
//              assertEquals(orderId, result.orderId());
//              assertNotNull(result.canceledAt());
//              verify(orderRepository).findById(orderId);
//              verify(orderRepository).save(order);
//              assertEquals(OrderStatus.CANCELED, order.getStatus());
//          }

//          @Test
//          @DisplayName("Should throw ORDER_NOT_FOUND when order does not exist")
//          void shouldThrowOrderNotFoundWhenOrderDoesNotExist() {
//              // Given
//              when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

//              // When & Then
//              ApiException exception = assertThrows(ApiException.class,
//                      () -> orderService.cancelOrder(orderId, customerId));

//              assertEquals(OrderErrorCode.ORDER_NOT_FOUND, exception.getErrorCode());
//              verify(orderRepository).findById(orderId);
//              verify(orderRepository, never()).save(any());
//          }

//          @Test
//          @DisplayName("Should throw ORDER_NOT_OWNED when user is not the owner")
//          void shouldThrowOrderNotOwnedWhenUserIsNotOwner() {
//              // Given
//              when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

//              // When & Then
//              ApiException exception = assertThrows(ApiException.class,
//                      () -> orderService.cancelOrder(orderId, strangerId));

//              assertEquals(OrderErrorCode.ORDER_NOT_OWNED, exception.getErrorCode());
//              verify(orderRepository).findById(orderId);
//              verify(orderRepository, never()).save(any());
//          }

//          @Test
//          @DisplayName("Should throw INVALID_ORDER_STATUS when order status is COMPLETED")
//          void shouldThrowInvalidOrderStatusWhenCompleted() {
//              // Given
//              order.setStatus(OrderStatus.COMPLETED);
//              when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

//              // When & Then
//              ApiException exception = assertThrows(ApiException.class,
//                      () -> orderService.cancelOrder(orderId, customerId));

//              assertEquals(OrderErrorCode.INVALID_ORDER_STATUS, exception.getErrorCode());
//              verify(orderRepository, never()).save(any());
//          }

//          @ParameterizedTest
//          @EnumSource(value = OrderStatus.class, names = {"PREPARING", "READY", "COMPLETED", "CANCELED"})
//          @DisplayName("Should reject cancellation for invalid statuses")
//          void shouldRejectCancellationForInvalidStatuses(OrderStatus status) {
//              // Given
//              order.setStatus(status);
//              when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

//              // When & Then
//              assertThrows(ApiException.class,
//                      () -> orderService.cancelOrder(orderId, customerId));
//          }

//          @Test
//          @DisplayName("Should update order status to CANCELED")
//          void shouldUpdateOrderStatusToCanceled() {
//              // Given
//              when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
//              when(orderRepository.save(any(OrderEntity.class))).thenReturn(order);

//              // When
//              orderService.cancelOrder(orderId, customerId);

//              // Then
//              ArgumentCaptor<OrderEntity> captor = ArgumentCaptor.forClass(OrderEntity.class);
//              verify(orderRepository).save(captor.capture());
//              assertEquals(OrderStatus.CANCELED, captor.getValue().getStatus());
//          }
//      }

//      // ==================== createOrder() Tests ====================
//      @Nested
//      @DisplayName("createOrder() Tests")
//      class CreateOrderTests {

//          @Test
//          @DisplayName("Should create order successfully with single product")
//          void shouldCreateOrderSuccessfullyWithSingleProduct() {
//              // Given
//              CreateOrderRequestDTO.OrderItemRequest itemRequest =
//                      new CreateOrderRequestDTO.OrderItemRequest(productId, 2);
//              CreateOrderRequestDTO request = new CreateOrderRequestDTO(List.of(itemRequest), null, null);

//              when(AccountRepository.findById(customerId)).thenReturn(Optional.of(customer));
//              when(productRepository.findById(productId)).thenReturn(Optional.of(product));
//              when(orderRepository.save(any(OrderEntity.class))).thenAnswer(invocation -> {
//                  OrderEntity saved = invocation.getArgument(0);
//                  saved.setId(orderId);
//                  return saved;
//              });

//              // When
//              CreateOrderResponseDTO result = orderService.createOrder(request, customerId);

//              // Then
//              assertNotNull(result);
//              assertEquals(orderId, result.orderId());
//              assertEquals(OrderStatus.PENDING, result.status());
//              assertEquals(BigDecimal.valueOf(200.00), result.totalAmount());
//              assertEquals(1, result.items().size());
//              assertNotNull(result.orderTime());

//              verify(AccountRepository).findById(customerId);
//              verify(productRepository).findById(productId);
//              verify(orderRepository).save(any(OrderEntity.class));
//          }

//          @Test
//          @DisplayName("Should create order with multiple products")
//          void shouldCreateOrderWithMultipleProducts() {
//              // Given
//              UUID product2Id = UUID.randomUUID();
//              ProductVariantEntity variant2 = ProductVariantEntity.builder()
//                      .active(true)
//                      .price(BigDecimal.valueOf(50.00))
//                      .build();

//              ProductEntity product2 = ProductEntity.builder()
//                      .name("Product 2")
//                      .active(true)
//                      .variants(List.of(variant2))
//                      .build();
//              product2.setId(product2Id);
//              variant2.setProduct(product2);

//              CreateOrderRequestDTO request = new CreateOrderRequestDTO(List.of(
//                      new CreateOrderRequestDTO.OrderItemRequest(productId, 2),
//                      new CreateOrderRequestDTO.OrderItemRequest(product2Id, 3)
//              ), null, null);

//              when(AccountRepository.findById(customerId)).thenReturn(Optional.of(customer));
//              when(productRepository.findById(productId)).thenReturn(Optional.of(product));
//              when(productRepository.findById(product2Id)).thenReturn(Optional.of(product2));
//              when(orderRepository.save(any(OrderEntity.class))).thenAnswer(inv -> {
//                  OrderEntity saved = inv.getArgument(0);
//                  saved.setId(orderId);
//                  return saved;
//              });

//              // When
//              CreateOrderResponseDTO result = orderService.createOrder(request, customerId);

//              // Then
//              assertNotNull(result);
//              assertEquals(2, result.items().size());
//              assertEquals(BigDecimal.valueOf(350.00), result.totalAmount()); // 2*100 + 3*50
//          }

//          @Test
//          @DisplayName("Should throw ORDER_NOT_FOUND when customer does not exist")
//          void shouldThrowOrderNotFoundWhenCustomerDoesNotExist() {
//              // Given
//              CreateOrderRequestDTO request = new CreateOrderRequestDTO(
//                      List.of(new CreateOrderRequestDTO.OrderItemRequest(productId, 1)), null, null);

//              when(AccountRepository.findById(customerId)).thenReturn(Optional.empty());

//              // When & Then
//              ApiException exception = assertThrows(ApiException.class,
//                      () -> orderService.createOrder(request, customerId));

//              assertEquals(OrderErrorCode.ORDER_NOT_FOUND, exception.getErrorCode());
//              verify(productRepository, never()).findById(any());
//          }

//          @Test
//          @DisplayName("Should throw ORDER_NOT_FOUND when product does not exist")
//          void shouldThrowOrderNotFoundWhenProductDoesNotExist() {
//              // Given
//              CreateOrderRequestDTO request = new CreateOrderRequestDTO(
//                      List.of(new CreateOrderRequestDTO.OrderItemRequest(productId, 1)), null, null);

//              when(AccountRepository.findById(customerId)).thenReturn(Optional.of(customer));
//              when(productRepository.findById(productId)).thenReturn(Optional.empty());

//              // When & Then
//              ApiException exception = assertThrows(ApiException.class,
//                      () -> orderService.createOrder(request, customerId));

//              assertEquals(OrderErrorCode.ORDER_NOT_FOUND, exception.getErrorCode());
//          }

//          @Test
//          @DisplayName("Should throw PRODUCT_OUT_OF_STOCK when product is not active")
//          void shouldThrowProductOutOfStockWhenProductNotActive() {
//              // Given
//              product.setActive(false);
//              CreateOrderRequestDTO request = new CreateOrderRequestDTO(
//                      List.of(new CreateOrderRequestDTO.OrderItemRequest(productId, 1)), null, null);

//              when(AccountRepository.findById(customerId)).thenReturn(Optional.of(customer));
//              when(productRepository.findById(productId)).thenReturn(Optional.of(product));

//              // When & Then
//              ApiException exception = assertThrows(ApiException.class,
//                      () -> orderService.createOrder(request, customerId));

//              assertEquals(OrderErrorCode.PRODUCT_OUT_OF_STOCK, exception.getErrorCode());
//          }

//          @Test
//          @DisplayName("Should handle product with no active variants - price zero")
//          void shouldHandleProductWithNoActiveVariants() {
//              // Given
//              variant.setActive(false);
//              CreateOrderRequestDTO request = new CreateOrderRequestDTO(
//                      List.of(new CreateOrderRequestDTO.OrderItemRequest(productId, 2)), null, null);

//              when(AccountRepository.findById(customerId)).thenReturn(Optional.of(customer));
//              when(productRepository.findById(productId)).thenReturn(Optional.of(product));
//              when(orderRepository.save(any(OrderEntity.class))).thenAnswer(inv -> {
//                  OrderEntity saved = inv.getArgument(0);
//                  saved.setId(orderId);
//                  return saved;
//              });

//              // When
//              CreateOrderResponseDTO result = orderService.createOrder(request, customerId);

//              // Then
//              assertNotNull(result);
//              assertEquals(BigDecimal.ZERO, result.totalAmount());
//          }

//          @Test
//          @DisplayName("Should calculate total amount correctly")
//          void shouldCalculateTotalAmountCorrectly() {
//              // Given
//              CreateOrderRequestDTO request = new CreateOrderRequestDTO(
//                      List.of(new CreateOrderRequestDTO.OrderItemRequest(productId, 5)), null, null);

//              when(AccountRepository.findById(customerId)).thenReturn(Optional.of(customer));
//              when(productRepository.findById(productId)).thenReturn(Optional.of(product));
//              when(orderRepository.save(any(OrderEntity.class))).thenAnswer(inv -> {
//                  OrderEntity saved = inv.getArgument(0);
//                  saved.setId(orderId);
//                  return saved;
//              });

//              // When
//              CreateOrderResponseDTO result = orderService.createOrder(request, customerId);

//              // Then
//              assertEquals(BigDecimal.valueOf(500.00), result.totalAmount());
//          }

//          @Test
//          @DisplayName("Should set order status to PENDING")
//          void shouldSetOrderStatusToPending() {
//              // Given
//              CreateOrderRequestDTO request = new CreateOrderRequestDTO(
//                      List.of(new CreateOrderRequestDTO.OrderItemRequest(productId, 1)), null, null);

//              when(AccountRepository.findById(customerId)).thenReturn(Optional.of(customer));
//              when(productRepository.findById(productId)).thenReturn(Optional.of(product));
//              when(orderRepository.save(any(OrderEntity.class))).thenAnswer(inv -> {
//                  OrderEntity saved = inv.getArgument(0);
//                  saved.setId(orderId);
//                  return saved;
//              });

//              // When
//              CreateOrderResponseDTO result = orderService.createOrder(request, customerId);

//              // Then
//              assertEquals(OrderStatus.PENDING, result.status());
//          }
//      }

//      // ==================== processPayment() Tests ====================
//      @Nested
//      @DisplayName("processPayment() Tests")
//      class ProcessPaymentTests {

//          private OrderItemEntity orderItem;

//          @BeforeEach
//          void setUpPayment() {
//              order.setStatus(OrderStatus.PENDING);
//              orderItem = new OrderItemEntity();
//              orderItem.setUnitPrice(BigDecimal.valueOf(100.00));
//              orderItem.setQuantity(2);
//              orderItem.setOrder(order);
//              order.setOrderItems(List.of(orderItem));
//          }

//          @Test
//          @DisplayName("Should process payment successfully")
//          void shouldProcessPaymentSuccessfully() {
//              // Given
//              PaymentRequestDTO request = new PaymentRequestDTO(
//                      PaymentMethod.CASH, BigDecimal.valueOf(200.00));

//              when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
//              when(paymentRepository.findFirstByOrder_IdAndStatus(orderId, PaymentStatus.PENDING))
//                      .thenReturn(Optional.empty()); // No existing pending payment
//              when(paymentRepository.save(any(PaymentEntity.class))).thenAnswer(inv -> {
//                  PaymentEntity payment = inv.getArgument(0);
//                  if (payment.getId() == null) {
//                      payment.setId(UUID.randomUUID());
//                  }
//                  return payment; // Return the same entity so status changes are preserved
//              });
//              when(orderRepository.save(any(OrderEntity.class))).thenReturn(order);

//              // When
//              PaymentResponseDTO result = orderService.processPayment(orderId, request, customerId);

//              // Then
//              assertNotNull(result);
//              assertEquals(orderId, result.orderId());
//              assertEquals("CASH", result.paymentMethod());
//              assertEquals(BigDecimal.valueOf(200.00), result.amountPaid());
//              assertEquals("PAID", result.paymentStatus()); // CASH được paid ngay lập tức
//              assertEquals(OrderStatus.PAID, result.orderStatus());

//              verify(orderRepository).findById(orderId);
//              verify(paymentRepository, atLeastOnce()).save(any(PaymentEntity.class));
//              verify(orderRepository).save(order);
//          }

//          @Test
//          @DisplayName("Should throw ORDER_NOT_FOUND when order does not exist")
//          void shouldThrowOrderNotFoundForPayment() {
//              // Given
//              PaymentRequestDTO request = new PaymentRequestDTO(
//                      PaymentMethod.CASH, BigDecimal.valueOf(200.00));

//              when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

//              // When & Then
//              ApiException exception = assertThrows(ApiException.class,
//                      () -> orderService.processPayment(orderId, request, customerId));

//              assertEquals(OrderErrorCode.ORDER_NOT_FOUND, exception.getErrorCode());
//          }

//          @Test
//          @DisplayName("Should throw ORDER_NOT_OWNED when user is not owner")
//          void shouldThrowOrderNotOwnedForPayment() {
//              // Given
//              PaymentRequestDTO request = new PaymentRequestDTO(
//                      PaymentMethod.CASH, BigDecimal.valueOf(200.00));

//              when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

//              // When & Then
//              ApiException exception = assertThrows(ApiException.class,
//                      () -> orderService.processPayment(orderId, request, strangerId));

//              assertEquals(OrderErrorCode.ORDER_NOT_OWNED, exception.getErrorCode());
//          }

//          @Test
//          @DisplayName("Should throw ORDER_ALREADY_PAID when order is already paid")
//          void shouldThrowOrderAlreadyPaid() {
//              // Given
//              order.setStatus(OrderStatus.PAID);
//              PaymentRequestDTO request = new PaymentRequestDTO(
//                      PaymentMethod.CASH, BigDecimal.valueOf(200.00));

//              when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

//              // When & Then
//              ApiException exception = assertThrows(ApiException.class,
//                      () -> orderService.processPayment(orderId, request, customerId));

//              assertEquals(OrderErrorCode.ORDER_ALREADY_PAID, exception.getErrorCode());
//          }

//          @Test
//          @DisplayName("Should throw INVALID_ORDER_STATUS when order is not pending")
//          void shouldThrowInvalidOrderStatusForPayment() {
//              // Given
//              order.setStatus(OrderStatus.COMPLETED);
//              PaymentRequestDTO request = new PaymentRequestDTO(
//                      PaymentMethod.CASH, BigDecimal.valueOf(200.00));

//              when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

//              // When & Then
//              ApiException exception = assertThrows(ApiException.class,
//                      () -> orderService.processPayment(orderId, request, customerId));

//              assertEquals(OrderErrorCode.INVALID_ORDER_STATUS, exception.getErrorCode());
//          }

//          @Test
//          @DisplayName("Should throw PAYMENT_AMOUNT_MISMATCH when amount does not match")
//          void shouldThrowPaymentAmountMismatch() {
//              // Given
//              PaymentRequestDTO request = new PaymentRequestDTO(
//                      PaymentMethod.CASH, BigDecimal.valueOf(100.00)); // Wrong amount

//              when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

//              // When & Then
//              ApiException exception = assertThrows(ApiException.class,
//                      () -> orderService.processPayment(orderId, request, customerId));

//              assertEquals(PaymentErrorCode.PAYMENT_AMOUNT_MISMATCH, exception.getErrorCode());
//          }

//          @ParameterizedTest
//          @EnumSource(value = PaymentMethod.class, names = {"MOMO"}, mode = EnumSource.Mode.EXCLUDE)
//          @DisplayName("Should accept all valid payment methods and mark as PAID immediately (except MOMO)")
//          void shouldAcceptAllPaymentMethodsAndMarkPaid(PaymentMethod method) {
//              // Given
//              PaymentRequestDTO request = new PaymentRequestDTO(method, BigDecimal.valueOf(200.00));

//              when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
//              when(paymentRepository.findFirstByOrder_IdAndStatus(orderId, PaymentStatus.PENDING))
//                      .thenReturn(Optional.empty());
//              when(paymentRepository.save(any(PaymentEntity.class))).thenAnswer(inv -> {
//                  PaymentEntity payment = inv.getArgument(0);
//                  if (payment.getId() == null) {
//                      payment.setId(UUID.randomUUID());
//                  }
//                  return payment;
//              });
//              when(orderRepository.save(any(OrderEntity.class))).thenReturn(order);

//              // When
//              PaymentResponseDTO result = orderService.processPayment(orderId, request, customerId);

//              // Then
//              assertNotNull(result);
//              assertEquals(method.toString(), result.paymentMethod());
//              assertEquals("PAID", result.paymentStatus()); // Non-MOMO methods are PAID immediately
//              assertEquals(OrderStatus.PAID, result.orderStatus());
//          }

//          @Test
//          @DisplayName("Should generate transaction ID for payment")
//          void shouldGenerateTransactionId() {
//              // Given
//              PaymentRequestDTO request = new PaymentRequestDTO(
//                      PaymentMethod.VNPAY, BigDecimal.valueOf(200.00));

//              when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
//              when(paymentRepository.findFirstByOrder_IdAndStatus(orderId, PaymentStatus.PENDING))
//                      .thenReturn(Optional.empty());

//              ArgumentCaptor<PaymentEntity> paymentCaptor = ArgumentCaptor.forClass(PaymentEntity.class);
//              when(paymentRepository.save(paymentCaptor.capture())).thenAnswer(inv -> inv.getArgument(0));
//              when(orderRepository.save(any(OrderEntity.class))).thenReturn(order);

//              // When
//              orderService.processPayment(orderId, request, customerId);

//              // Then
//              PaymentEntity capturedPayment = paymentCaptor.getValue();
//              assertNotNull(capturedPayment.getTransactionId());
//          }

//          @Test
//          @DisplayName("Should update order status to PAID after payment")
//          void shouldUpdateOrderStatusToPaid() {
//              // Given
//              PaymentRequestDTO request = new PaymentRequestDTO(
//                      PaymentMethod.CASH, BigDecimal.valueOf(200.00));

//              when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
//              when(paymentRepository.findFirstByOrder_IdAndStatus(orderId, PaymentStatus.PENDING))
//                      .thenReturn(Optional.empty());
//              when(paymentRepository.save(any(PaymentEntity.class))).thenAnswer(inv -> {
//                  PaymentEntity payment = inv.getArgument(0);
//                  if (payment.getId() == null) {
//                      payment.setId(UUID.randomUUID());
//                  }
//                  return payment;
//              });
//              when(orderRepository.save(any(OrderEntity.class))).thenReturn(order);

//              // When
//              orderService.processPayment(orderId, request, customerId);

//              // Then
//              ArgumentCaptor<OrderEntity> orderCaptor = ArgumentCaptor.forClass(OrderEntity.class);
//              verify(orderRepository).save(orderCaptor.capture());
//              assertEquals(OrderStatus.PAID, orderCaptor.getValue().getStatus());
//          }
//      }

//      // ==================== getStatus() Tests ====================
//      @Nested
//      @DisplayName("getStatus() Tests")
//      class GetStatusTests {

//          @Test
//          @DisplayName("Should return status for customer's own order")
//          void shouldReturnStatusForOwnOrder() {
//              // Given
//              when(orderRepository.findByIdWithCustomer(orderId)).thenReturn(Optional.of(order));

//              // When
//              OrderStatusResponseDTO result = orderService.getStatus(orderId, customerId, "CUSTOMER");

//              // Then
//              assertNotNull(result);
//              assertEquals(orderId, result.getId());
//              assertEquals(OrderStatus.PAID, result.getStatus());
//              assertNotNull(result.getLastUpdated());
//          }

//          @Test
//          @DisplayName("Should allow FRANCHISE_ADMIN to view any order")
//          void shouldAllowFranchiseAdminToViewAnyOrder() {
//              // Given
//              when(orderRepository.findByIdWithCustomer(orderId)).thenReturn(Optional.of(order));

//              // When
//              OrderStatusResponseDTO result = orderService.getStatus(orderId, strangerId, "FRANCHISE_ADMIN");

//              // Then
//              assertNotNull(result);
//              assertEquals(orderId, result.getId());
//          }

//          @Test
//          @DisplayName("Should allow STORE_MANAGER to view any order")
//          void shouldAllowStoreManagerToViewAnyOrder() {
//              // Given
//              when(orderRepository.findByIdWithCustomer(orderId)).thenReturn(Optional.of(order));

//              // When
//              OrderStatusResponseDTO result = orderService.getStatus(orderId, strangerId, "STORE_MANAGER");

//              // Then
//              assertNotNull(result);
//              assertEquals(orderId, result.getId());
//          }

//          @Test
//          @DisplayName("Should throw ORDER_NOT_OWNED when customer tries to view others order")
//          void shouldThrowOrderNotOwnedWhenCustomerViewsOthersOrder() {
//              // Given
//              when(orderRepository.findByIdWithCustomer(orderId)).thenReturn(Optional.of(order));

//              // When & Then
//              ApiException exception = assertThrows(ApiException.class,
//                      () -> orderService.getStatus(orderId, strangerId, "CUSTOMER"));

//              assertEquals(OrderErrorCode.ORDER_NOT_OWNED, exception.getErrorCode());
//          }

//          @Test
//          @DisplayName("Should throw ORDER_NOT_FOUND when order does not exist")
//          void shouldThrowOrderNotFoundForStatus() {
//              // Given
//              when(orderRepository.findByIdWithCustomer(orderId)).thenReturn(Optional.empty());

//              // When & Then
//              ApiException exception = assertThrows(ApiException.class,
//                      () -> orderService.getStatus(orderId, customerId, "CUSTOMER"));

//              assertEquals(OrderErrorCode.ORDER_NOT_FOUND, exception.getErrorCode());
//          }
//      }

//      // ==================== getOrderHistory() Tests ====================
//      @Nested
//      @DisplayName("getOrderHistory() Tests")
//      class GetOrderHistoryTests {

//          @Test
//          @DisplayName("Should return order history for FRANCHISE_ADMIN")
//          void shouldReturnOrderHistoryForFranchiseAdmin() {
//              // Given
//              OrderEntity order1 = OrderEntity.builder()
//                      .status(OrderStatus.COMPLETED)
//                      .orderTime(LocalDateTime.now())
//                      .build();
//              order1.setId(UUID.randomUUID());

//              Page<OrderEntity> page = new PageImpl<>(List.of(order1), PageRequest.of(0, 10), 1);

//              when(orderRepository.findOrderHistory(any(), any(), any(), any(), any())).thenReturn(page);

//              // When
//              OrderHistoryPageDTO result = orderService.getOrderHistory(
//                      1, 10, Optional.empty(), Optional.empty(),
//                      Optional.empty(), Optional.empty(), "FRANCHISE_ADMIN");

//              // Then
//              assertNotNull(result);
//              assertEquals(1, result.getContent().size());
//              assertEquals(1, result.getPage());
//              assertEquals(10, result.getSize());
//              assertEquals(1, result.getTotalElements());
//          }

//          @Test
//          @DisplayName("Should return order history for STORE_MANAGER")
//          void shouldReturnOrderHistoryForStoreManager() {
//              // Given
//              Page<OrderEntity> page = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);
//              when(orderRepository.findOrderHistory(any(), any(), any(), any(), any())).thenReturn(page);

//              // When
//              OrderHistoryPageDTO result = orderService.getOrderHistory(
//                      1, 10, Optional.empty(), Optional.empty(),
//                      Optional.empty(), Optional.empty(), "STORE_MANAGER");

//              // Then
//              assertNotNull(result);
//              assertEquals(0, result.getTotalElements());
//          }

//          @Test
//          @DisplayName("Should throw FORBIDDEN for non-admin/manager roles")
//          void shouldThrowForbiddenForNonAdminRoles() {
//              // When & Then
//              ApiException exception = assertThrows(ApiException.class,
//                      () -> orderService.getOrderHistory(1, 10, Optional.empty(),
//                              Optional.empty(), Optional.empty(), Optional.empty(), "CUSTOMER"));

//              assertEquals(CommonErrorCode.FORBIDDEN, exception.getErrorCode());
//          }

//          @Test
//          @DisplayName("Should throw BAD_REQUEST for invalid pagination")
//          void shouldThrowBadRequestForInvalidPagination() {
//              // When & Then
//              assertThrows(ApiException.class,
//                      () -> orderService.getOrderHistory(0, 10, Optional.empty(),
//                              Optional.empty(), Optional.empty(), Optional.empty(), "FRANCHISE_ADMIN"));

//              assertThrows(ApiException.class,
//                      () -> orderService.getOrderHistory(1, 0, Optional.empty(),
//                              Optional.empty(), Optional.empty(), Optional.empty(), "FRANCHISE_ADMIN"));

//              assertThrows(ApiException.class,
//                      () -> orderService.getOrderHistory(1, 101, Optional.empty(),
//                              Optional.empty(), Optional.empty(), Optional.empty(), "FRANCHISE_ADMIN"));
//          }

//          @Test
//          @DisplayName("Should throw BAD_REQUEST for invalid date range")
//          void shouldThrowBadRequestForInvalidDateRange() {
//              // Given
//              LocalDate fromDate = LocalDate.now();
//              LocalDate toDate = LocalDate.now().minusDays(1);

//              // When & Then
//              ApiException exception = assertThrows(ApiException.class,
//                      () -> orderService.getOrderHistory(1, 10, Optional.empty(),
//                              Optional.empty(), Optional.of(fromDate), Optional.of(toDate), "FRANCHISE_ADMIN"));

//              assertEquals(CommonErrorCode.BAD_REQUEST, exception.getErrorCode());
//          }

//          @Test
//          @DisplayName("Should throw INVALID_ORDER_STATUS for invalid status filter")
//          void shouldThrowInvalidOrderStatusForInvalidFilter() {
//              // When & Then
//              ApiException exception = assertThrows(ApiException.class,
//                      () -> orderService.getOrderHistory(1, 10, Optional.of("INVALID_STATUS"),
//                              Optional.empty(), Optional.empty(), Optional.empty(), "FRANCHISE_ADMIN"));

//              assertEquals(OrderErrorCode.INVALID_ORDER_STATUS, exception.getErrorCode());
//          }

//          @Test
//          @DisplayName("Should filter by status correctly")
//          void shouldFilterByStatusCorrectly() {
//              // Given
//              Page<OrderEntity> page = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);
//              when(orderRepository.findOrderHistory(eq(OrderStatus.COMPLETED), any(), any(), any(), any()))
//                      .thenReturn(page);

//              // When
//              OrderHistoryPageDTO result = orderService.getOrderHistory(
//                      1, 10, Optional.of("COMPLETED"), Optional.empty(),
//                      Optional.empty(), Optional.empty(), "FRANCHISE_ADMIN");

//              // Then
//              assertNotNull(result);
//              verify(orderRepository).findOrderHistory(eq(OrderStatus.COMPLETED), any(), any(), any(), any());
//          }
//      }

//      // ==================== getOrderDetail() Tests ====================
//      @Nested
//      @DisplayName("getOrderDetail() Tests")
//      class GetOrderDetailTests {

//          @BeforeEach
//          void setUpOrderDetail() {
//              OrderItemEntity item = new OrderItemEntity();
//              item.setQuantity(2);
//              item.setUnitPrice(BigDecimal.valueOf(100.00));
//              item.setProductVariant(variant);
//              order.setOrderItems(List.of(item));

//              PaymentEntity payment = new PaymentEntity();
//              payment.setId(UUID.randomUUID());
//              payment.setPaymentMethod(PaymentMethod.CASH);
//              payment.setAmountPaid(BigDecimal.valueOf(200.00));
//              payment.setStatus(PaymentStatus.PENDING);
//              order.setPayments(List.of(payment));
//          }

//          @Test
//          @DisplayName("Should return order detail for owner")
//          void shouldReturnOrderDetailForOwner() {
//              // Given
//              when(orderRepository.findByIdWithItems(orderId)).thenReturn(Optional.of(order));
//              when(orderRepository.findByIdWithPayments(orderId)).thenReturn(Optional.of(order));

//              // When
//              OrderDetailResponse result = orderService.getOrderDetail(orderId.toString(), customerId, "CUSTOMER");

//              // Then
//              assertNotNull(result);
//              assertEquals(orderId, result.getOrderId());
//              assertNotNull(result.getCustomer());
//              assertEquals(String.valueOf(customerId), result.getCustomer().getCustomerId());
//              assertEquals(1, result.getItems().size());
//              assertNotNull(result.getPayment());
//              assertEquals(BigDecimal.valueOf(200.00), result.getPricing().getTotalAmount());
//          }

//          @Test
//          @DisplayName("Should throw RESOURCE_NOT_FOUND for invalid UUID")
//          void shouldThrowResourceNotFoundForInvalidUuid() {
//              // When & Then
//              ApiException exception = assertThrows(ApiException.class,
//                      () -> orderService.getOrderDetail("invalid-uuid", customerId, "CUSTOMER"));

//              assertEquals(CommonErrorCode.RESOURCE_NOT_FOUND, exception.getErrorCode());
//          }

//          @Test
//          @DisplayName("Should throw RESOURCE_NOT_FOUND when order does not exist")
//          void shouldThrowResourceNotFoundWhenOrderDoesNotExist() {
//              // Given
//              when(orderRepository.findByIdWithItems(orderId)).thenReturn(Optional.empty());

//              // When & Then
//              ApiException exception = assertThrows(ApiException.class,
//                      () -> orderService.getOrderDetail(orderId.toString(), customerId, "CUSTOMER"));

//              assertEquals(CommonErrorCode.RESOURCE_NOT_FOUND, exception.getErrorCode());
//          }

//          @Test
//          @DisplayName("Should throw ORDER_NOT_OWNED when user is not owner")
//          void shouldThrowOrderNotOwnedForDetail() {
//              // Given
//              when(orderRepository.findByIdWithItems(orderId)).thenReturn(Optional.of(order));
//              when(orderRepository.findByIdWithPayments(orderId)).thenReturn(Optional.of(order));

//              // When & Then
//              ApiException exception = assertThrows(ApiException.class,
//                      () -> orderService.getOrderDetail(orderId.toString(), strangerId, "CUSTOMER"));

//              assertEquals(OrderErrorCode.ORDER_NOT_OWNED, exception.getErrorCode());
//          }

//          @Test
//          @DisplayName("Should handle order with no payments")
//          void shouldHandleOrderWithNoPayments() {
//              // Given
//              order.setPayments(null);
//              when(orderRepository.findByIdWithItems(orderId)).thenReturn(Optional.of(order));
//              when(orderRepository.findByIdWithPayments(orderId)).thenReturn(Optional.of(order));

//              // When
//              OrderDetailResponse result = orderService.getOrderDetail(orderId.toString(), customerId, "CUSTOMER");

//              // Then
//              assertNotNull(result);
//              assertNotNull(result.getPayment());
//              assertEquals("Pending", result.getPayment().getPaymentStatus());
//              assertEquals(BigDecimal.ZERO, result.getPayment().getAmountPaid());
//          }

//          @Test
//          @DisplayName("Should calculate pricing correctly")
//          void shouldCalculatePricingCorrectly() {
//              // Given
//              when(orderRepository.findByIdWithItems(orderId)).thenReturn(Optional.of(order));
//              when(orderRepository.findByIdWithPayments(orderId)).thenReturn(Optional.of(order));

//              // When
//              OrderDetailResponse result = orderService.getOrderDetail(orderId.toString(), customerId, "CUSTOMER");

//              // Then
//              assertNotNull(result.getPricing());
//              assertEquals(BigDecimal.valueOf(200.00), result.getPricing().getSubtotal());
//              assertEquals(BigDecimal.ZERO, result.getPricing().getDiscount());
//              assertEquals(BigDecimal.valueOf(200.00), result.getPricing().getTotalAmount());
//          }
//      }

//      // ==================== Integration Tests ====================
//      @Nested
//      @DisplayName("Integration-like Scenarios")
//      class IntegrationTests {

//          @Test
//          @DisplayName("Full flow: Create Order → Process Payment → Cancel Order")
//          void fullOrderFlowTest() {
//              // Step 1: Create Order
//              CreateOrderRequestDTO.OrderItemRequest itemRequest =
//                      new CreateOrderRequestDTO.OrderItemRequest(productId, 2);
//              CreateOrderRequestDTO createRequest = new CreateOrderRequestDTO(List.of(itemRequest), null, null);

//              when(AccountRepository.findById(customerId)).thenReturn(Optional.of(customer));
//              when(productRepository.findById(productId)).thenReturn(Optional.of(product));
//              when(orderRepository.save(any(OrderEntity.class))).thenAnswer(inv -> {
//                  OrderEntity saved = inv.getArgument(0);
//                  saved.setId(orderId);
//                  return saved;
//              });

//              CreateOrderResponseDTO createResult = orderService.createOrder(createRequest, customerId);
//              assertNotNull(createResult);
//              assertEquals(OrderStatus.PENDING, createResult.status());

//              // Step 2: Process Payment
//              order.setStatus(OrderStatus.PENDING);
//              OrderItemEntity item = new OrderItemEntity();
//              item.setUnitPrice(BigDecimal.valueOf(100.00));
//              item.setQuantity(2);
//              order.setOrderItems(List.of(item));

//              PaymentRequestDTO paymentRequest = new PaymentRequestDTO(
//                      PaymentMethod.CASH, BigDecimal.valueOf(200.00));

//              when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
//              when(paymentRepository.findFirstByOrder_IdAndStatus(orderId, PaymentStatus.PENDING))
//                      .thenReturn(Optional.empty());
//              when(paymentRepository.save(any(PaymentEntity.class))).thenAnswer(inv -> {
//                  PaymentEntity payment = inv.getArgument(0);
//                  if (payment.getId() == null) {
//                      payment.setId(UUID.randomUUID());
//                  }
//                  return payment;
//              });

//              PaymentResponseDTO paymentResult = orderService.processPayment(orderId, paymentRequest, customerId);
//              assertNotNull(paymentResult);
//              assertEquals(OrderStatus.PAID, paymentResult.orderStatus());

//              // Step 3: Cancel Order
//              order.setStatus(OrderStatus.PAID);
//              OrderCancelDTO cancelResult = orderService.cancelOrder(orderId, customerId);
//              assertNotNull(cancelResult);
//              assertEquals(OrderStatus.CANCELED, order.getStatus());
//          }

//          @Test
//          @DisplayName("Should maintain data consistency across operations")
//          void shouldMaintainDataConsistency() {
//              // Given
//              CreateOrderRequestDTO request = new CreateOrderRequestDTO(
//                      List.of(new CreateOrderRequestDTO.OrderItemRequest(productId, 3)), null, null);

//              when(AccountRepository.findById(customerId)).thenReturn(Optional.of(customer));
//              when(productRepository.findById(productId)).thenReturn(Optional.of(product));
//              when(orderRepository.save(any(OrderEntity.class))).thenAnswer(inv -> {
//                  OrderEntity saved = inv.getArgument(0);
//                  saved.setId(orderId);
//                  return saved;
//              });

//              // When
//              CreateOrderResponseDTO result = orderService.createOrder(request, customerId);

//              // Then - Verify all fields are consistent
//              assertEquals(BigDecimal.valueOf(300.00), result.totalAmount());
//              assertEquals(1, result.items().size());

//              CreateOrderResponseDTO.OrderItemResponse item = result.items().get(0);
//              assertEquals(productId, item.productId());
//              assertEquals(3, item.quantity());
//              assertEquals(BigDecimal.valueOf(100.00), item.unitPrice());
//              assertEquals(BigDecimal.valueOf(300.00), item.subtotal());
//          }
//      }
// }
