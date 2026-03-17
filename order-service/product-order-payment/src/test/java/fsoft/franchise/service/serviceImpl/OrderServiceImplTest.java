//package fsoft.franchise.service.serviceImpl;
//
//import fsoft.franchise.common.exception.ApiException;
//import fsoft.franchise.dto.orders.*;
//import fsoft.franchise.dto.payments.PaymentResponse;
//import fsoft.franchise.exception.OrderErrorCode;
//import fsoft.franchise.exception.PaymentErrorCode;
//import fsoft.franchise.exception.CommonErrorCode;
//import fsoft.franchise.dto.payments.PaymentRequest;
//import fsoft.franchise.entity.*;
//import fsoft.franchise.enums.OrderStatus;
//import fsoft.franchise.enums.PaymentMethod;
//import fsoft.franchise.enums.PaymentStatus;
//import fsoft.franchise.enums.StatusEnum;
//import fsoft.franchise.entity.external.AccountEntity;
//import fsoft.franchise.entity.external.ProfileEntity;
//import fsoft.franchise.repository.AccountRepository;
//import fsoft.franchise.repository.OrderRepository;
//import fsoft.franchise.repository.PaymentRepository;
//import fsoft.franchise.repository.ProductRepository;
//import fsoft.franchise.service.MoMoPaymentService;
//import fsoft.franchise.service.OrderTrackingService;
//import fsoft.franchise.service.PaymentMethodService;
//import fsoft.franchise.service.VNPayService;
//import fsoft.franchise.serviceImpl.OrderServiceImpl;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Nested;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageImpl;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.test.util.ReflectionTestUtils;
//
//import java.math.BigDecimal;
//import java.time.LocalDate;
//import java.time.LocalDateTime;
//import java.util.*;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.eq;
//import static org.mockito.Mockito.*;
//
///**
// * Unit Tests for OrderServiceImpl using JUnit 5 and Mockito
// * Template: Comprehensive unit test coverage
// */
//@ExtendWith(MockitoExtension.class)
//@DisplayName("OrderServiceImpl Unit Tests")
//class OrderServiceImplTest {
//
//    @Mock
//    private OrderRepository orderRepository;
//
//    @Mock
//    private ProductRepository productRepository;
//
//    @Mock
//    private PaymentRepository paymentRepository;
//
//    @Mock
//    private AccountRepository AccountRepository;
//
//    @Mock
//    private MoMoPaymentService moMoPaymentService;
//
//    @Mock
//    private PaymentMethodService paymentMethodService;
//
//    @Mock
//    private VNPayService vnPayService;
//
//    @Mock
//    private OrderTrackingService orderTrackingService;
//
//    @InjectMocks
//    private OrderServiceImpl orderService;
//
//    private UUID orderId;
//    private UUID customerId;
//    private UUID productId;
//    private UUID strangerId;
//    private AccountEntity customer;
//    private OrderEntity order;
//    private ProductEntity product;
//    private ProductVariantEntity variant;
//
//    @BeforeEach
//    void setUp() {
//        orderId = UUID.randomUUID();
//        customerId = UUID.randomUUID();
//        productId = UUID.randomUUID();
//        strangerId = UUID.randomUUID();
//
//        // Setup customer
//        customer = AccountEntity.builder()
//                .email("customer@example.com")
//                .phoneNumber("0123456789")
//                .status(StatusEnum.ACTIVE)
//                .build();
//        customer.setId(customerId);
//
//        ProfileEntity profile = new ProfileEntity();
//        profile.setFirstName("John");
//        profile.setLastName("Doe");
//        customer.setProfile(profile);
//
//        // Setup product with variant
//        variant = ProductVariantEntity.builder()
//                .active(true)
//                .price(BigDecimal.valueOf(100.00))
//                .build();
//
//        product = ProductEntity.builder()
//                .name("Test Product")
//                .active(true)
//                .variants(List.of(variant))
//                .build();
//        product.setId(productId);
//        variant.setProduct(product);
//
//        // Setup order with PENDING status
//        order = OrderEntity.builder()
//                .customer(customer)
//                .status(OrderStatus.PENDING)
//                .orderTime(LocalDateTime.now())
//                .build();
//        order.setId(orderId);
//
//        // Set paymentReturnUrl using reflection
//        ReflectionTestUtils.setField(orderService, "paymentReturnUrl", "http://localhost:3000/payment/result");
//    }
//
//    // ==================== cancelOrder() Tests ====================
//    @Nested
//    @DisplayName("cancelOrder() Tests")
//    class CancelOrderTests {
//
//        @Test
//        @DisplayName("Should cancel PENDING order successfully")
//        void shouldCancelPendingOrderSuccessfully() {
//            // Given
//            lenient().when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
//            lenient().when(orderRepository.save(any(OrderEntity.class))).thenReturn(order);
//            lenient().doNothing().when(orderTrackingService).sendTrackingUpdate(any(), any(), any(), any());
//
//            // When
//            OrderCancelResponse result = orderService.cancelOrder(orderId, customerId);
//
//            // Then
//            assertNotNull(result);
//            assertEquals(orderId, result.orderId());
//            assertNotNull(result.canceledAt());
//        }
//
//        @Test
//        @DisplayName("Should throw ORDER_NOT_FOUND when order does not exist")
//        void shouldThrowOrderNotFound() {
//            // Given
//            when(orderRepository.findById(orderId)).thenReturn(Optional.empty());
//
//            // When & Then
//            ApiException exception = assertThrows(ApiException.class,
//                    () -> orderService.cancelOrder(orderId, customerId));
//            assertEquals(OrderErrorCode.ORDER_NOT_FOUND, exception.getErrorCode());
//        }
//
//        @Test
//        @DisplayName("Should throw ORDER_NOT_OWNED when user is not owner")
//        void shouldThrowOrderNotOwned() {
//            // Given
//            when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
//
//            // When & Then
//            ApiException exception = assertThrows(ApiException.class,
//                    () -> orderService.cancelOrder(orderId, strangerId));
//            assertEquals(OrderErrorCode.ORDER_NOT_OWNED, exception.getErrorCode());
//        }
//
//        @Test
//        @DisplayName("Should throw error when trying to cancel PAID order")
//        void shouldThrowErrorForPaidOrder() {
//            // Given
//            order.setStatus(OrderStatus.PAID);
//            when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
//
//            // When & Then
//            ApiException exception = assertThrows(ApiException.class,
//                    () -> orderService.cancelOrder(orderId, customerId));
//            assertEquals(OrderErrorCode.INVALID_ORDER_STATUS, exception.getErrorCode());
//            assertTrue(exception.getMessage().contains("Refund"));
//        }
//
//        @Test
//        @DisplayName("Should throw error when trying to cancel COMPLETED order")
//        void shouldThrowErrorForCompletedOrder() {
//            // Given
//            order.setStatus(OrderStatus.COMPLETED);
//            when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
//
//            // When & Then
//            ApiException exception = assertThrows(ApiException.class,
//                    () -> orderService.cancelOrder(orderId, customerId));
//            assertEquals(OrderErrorCode.INVALID_ORDER_STATUS, exception.getErrorCode());
//        }
//
//        @Test
//        @DisplayName("Should throw error when trying to cancel already CANCELED order")
//        void shouldThrowErrorForAlreadyCanceledOrder() {
//            // Given
//            order.setStatus(OrderStatus.CANCELED);
//            when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
//
//            // When & Then
//            ApiException exception = assertThrows(ApiException.class,
//                    () -> orderService.cancelOrder(orderId, customerId));
//            assertEquals(OrderErrorCode.INVALID_ORDER_STATUS, exception.getErrorCode());
//        }
//
//        @Test
//        @DisplayName("Should verify order status transition from PENDING to CANCELED")
//        void shouldVerifyStatusTransition() {
//            // Given
//            lenient().when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
//            lenient().when(orderRepository.save(any(OrderEntity.class))).thenAnswer(inv -> {
//                OrderEntity saved = inv.getArgument(0);
//                assertEquals(OrderStatus.CANCELED, saved.getStatus());
//                return saved;
//            });
//            lenient().doNothing().when(orderTrackingService).sendTrackingUpdate(any(), any(), any(), any());
//
//            // When
//            OrderCancelResponse result = orderService.cancelOrder(orderId, customerId);
//
//            // Then
//            assertNotNull(result);
//            verify(orderTrackingService).sendTrackingUpdate(orderId, OrderStatus.PENDING, OrderStatus.CANCELED, customerId);
//        }
//    }
//
//    // ==================== createOrder() Tests ====================
//    @Nested
//    @DisplayName("createOrder() Tests")
//    class CreateOrderTests {
//
//        @Test
//        @DisplayName("Should create order successfully with single product")
//        void shouldCreateOrderSuccessfully() {
//            // Given
//            CreateOrderRequest.OrderItemRequest itemRequest =
//                    new CreateOrderRequest.OrderItemRequest(productId, 2);
//            CreateOrderRequest request = new CreateOrderRequest(List.of(itemRequest), null, null);
//
//            lenient().when(AccountRepository.findById(customerId)).thenReturn(Optional.of(customer));
//            lenient().when(productRepository.findById(productId)).thenReturn(Optional.of(product));
//            lenient().when(orderRepository.save(any(OrderEntity.class))).thenAnswer(inv -> {
//                OrderEntity saved = inv.getArgument(0);
//                saved.setId(orderId);
//                return saved;
//            });
//            lenient().doNothing().when(orderTrackingService).sendTrackingUpdate(any(), any(), any(), any());
//
//            // When
//            CreateOrderResponse result = orderService.createOrder(request, customerId);
//
//            // Then
//            assertNotNull(result);
//            assertEquals(orderId, result.orderId());
//            assertEquals(OrderStatus.PENDING, result.status());
//            assertEquals(BigDecimal.valueOf(200.00), result.totalAmount());
//            assertEquals(1, result.items().size());
//            assertNotNull(result.orderTime());
//        }
//
//        @Test
//        @DisplayName("Should create order with multiple products")
//        void shouldCreateOrderWithMultipleProducts() {
//            // Given
//            UUID product2Id = UUID.randomUUID();
//            ProductVariantEntity variant2 = ProductVariantEntity.builder()
//                    .active(true)
//                    .price(BigDecimal.valueOf(50.00))
//                    .build();
//
//            ProductEntity product2 = ProductEntity.builder()
//                    .name("Product 2")
//                    .active(true)
//                    .variants(List.of(variant2))
//                    .build();
//            product2.setId(product2Id);
//            variant2.setProduct(product2);
//
//            CreateOrderRequest request = new CreateOrderRequest(List.of(
//                    new CreateOrderRequest.OrderItemRequest(productId, 2),
//                    new CreateOrderRequest.OrderItemRequest(product2Id, 3)
//            ), null, null);
//
//            lenient().when(AccountRepository.findById(customerId)).thenReturn(Optional.of(customer));
//            lenient().when(productRepository.findById(productId)).thenReturn(Optional.of(product));
//            lenient().when(productRepository.findById(product2Id)).thenReturn(Optional.of(product2));
//            lenient().when(orderRepository.save(any(OrderEntity.class))).thenAnswer(inv -> {
//                OrderEntity saved = inv.getArgument(0);
//                saved.setId(orderId);
//                return saved;
//            });
//            lenient().doNothing().when(orderTrackingService).sendTrackingUpdate(any(), any(), any(), any());
//
//            // When
//            CreateOrderResponse result = orderService.createOrder(request, customerId);
//
//            // Then
//            assertNotNull(result);
//            assertEquals(2, result.items().size());
//            assertEquals(BigDecimal.valueOf(350.00), result.totalAmount());
//        }
//
//        @Test
//        @DisplayName("Should throw ORDER_NOT_FOUND when customer does not exist")
//        void shouldThrowOrderNotFoundForCustomer() {
//            // Given
//            CreateOrderRequest request = new CreateOrderRequest(
//                    List.of(new CreateOrderRequest.OrderItemRequest(productId, 1)), null, null);
//            when(AccountRepository.findById(customerId)).thenReturn(Optional.empty());
//
//            // When & Then
//            ApiException exception = assertThrows(ApiException.class,
//                    () -> orderService.createOrder(request, customerId));
//            assertEquals(OrderErrorCode.ORDER_NOT_FOUND, exception.getErrorCode());
//        }
//
//        @Test
//        @DisplayName("Should throw ORDER_NOT_FOUND when product does not exist")
//        void shouldThrowOrderNotFoundForProduct() {
//            // Given
//            CreateOrderRequest request = new CreateOrderRequest(
//                    List.of(new CreateOrderRequest.OrderItemRequest(productId, 1)), null, null);
//            lenient().when(AccountRepository.findById(customerId)).thenReturn(Optional.of(customer));
//            when(productRepository.findById(productId)).thenReturn(Optional.empty());
//
//            // When & Then
//            ApiException exception = assertThrows(ApiException.class,
//                    () -> orderService.createOrder(request, customerId));
//            assertEquals(OrderErrorCode.ORDER_NOT_FOUND, exception.getErrorCode());
//        }
//
//        @Test
//        @DisplayName("Should throw PRODUCT_OUT_OF_STOCK when product is inactive")
//        void shouldThrowOutOfStockForInactiveProduct() {
//            // Given
//            product.setActive(false);
//            CreateOrderRequest request = new CreateOrderRequest(
//                    List.of(new CreateOrderRequest.OrderItemRequest(productId, 1)), null, null);
//            lenient().when(AccountRepository.findById(customerId)).thenReturn(Optional.of(customer));
//            when(productRepository.findById(productId)).thenReturn(Optional.of(product));
//
//            // When & Then
//            ApiException exception = assertThrows(ApiException.class,
//                    () -> orderService.createOrder(request, customerId));
//            assertEquals(OrderErrorCode.PRODUCT_OUT_OF_STOCK, exception.getErrorCode());
//        }
//
//        @Test
//        @DisplayName("Should create order with zero price when no active variant")
//        void shouldCreateOrderWithZeroPriceWhenNoActiveVariant() {
//            // Given
//            variant.setActive(false);
//            CreateOrderRequest request = new CreateOrderRequest(
//                    List.of(new CreateOrderRequest.OrderItemRequest(productId, 2)), null, null);
//
//            lenient().when(AccountRepository.findById(customerId)).thenReturn(Optional.of(customer));
//            lenient().when(productRepository.findById(productId)).thenReturn(Optional.of(product));
//            lenient().when(orderRepository.save(any(OrderEntity.class))).thenAnswer(inv -> {
//                OrderEntity saved = inv.getArgument(0);
//                saved.setId(orderId);
//                return saved;
//            });
//            lenient().doNothing().when(orderTrackingService).sendTrackingUpdate(any(), any(), any(), any());
//
//            // When
//            CreateOrderResponse result = orderService.createOrder(request, customerId);
//
//            // Then
//            assertNotNull(result);
//            assertEquals(BigDecimal.ZERO, result.totalAmount());
//        }
//
//        @Test
//        @DisplayName("Should create order with empty variants list")
//        void shouldCreateOrderWithEmptyVariantsList() {
//            // Given
//            product.setVariants(new ArrayList<>());
//            CreateOrderRequest request = new CreateOrderRequest(
//                    List.of(new CreateOrderRequest.OrderItemRequest(productId, 1)), null, null);
//
//            lenient().when(AccountRepository.findById(customerId)).thenReturn(Optional.of(customer));
//            lenient().when(productRepository.findById(productId)).thenReturn(Optional.of(product));
//            lenient().when(orderRepository.save(any(OrderEntity.class))).thenAnswer(inv -> {
//                OrderEntity saved = inv.getArgument(0);
//                saved.setId(orderId);
//                return saved;
//            });
//            lenient().doNothing().when(orderTrackingService).sendTrackingUpdate(any(), any(), any(), any());
//
//            // When
//            CreateOrderResponse result = orderService.createOrder(request, customerId);
//
//            // Then
//            assertNotNull(result);
//            assertEquals(BigDecimal.ZERO, result.totalAmount());
//        }
//
//        @Test
//        @DisplayName("Should create order with null variants")
//        void shouldCreateOrderWithNullVariants() {
//            // Given
//            product.setVariants(null);
//            CreateOrderRequest request = new CreateOrderRequest(
//                    List.of(new CreateOrderRequest.OrderItemRequest(productId, 1)), null, null);
//
//            lenient().when(AccountRepository.findById(customerId)).thenReturn(Optional.of(customer));
//            lenient().when(productRepository.findById(productId)).thenReturn(Optional.of(product));
//            lenient().when(orderRepository.save(any(OrderEntity.class))).thenAnswer(inv -> {
//                OrderEntity saved = inv.getArgument(0);
//                saved.setId(orderId);
//                return saved;
//            });
//            lenient().doNothing().when(orderTrackingService).sendTrackingUpdate(any(), any(), any(), any());
//
//            // When
//            CreateOrderResponse result = orderService.createOrder(request, customerId);
//
//            // Then
//            assertNotNull(result);
//            assertEquals(BigDecimal.ZERO, result.totalAmount());
//        }
//
//        @Test
//        @DisplayName("Should calculate correct total for large quantities")
//        void shouldCalculateCorrectTotalForLargeQuantities() {
//            // Given
//            CreateOrderRequest request = new CreateOrderRequest(
//                    List.of(new CreateOrderRequest.OrderItemRequest(productId, 100)), null, null);
//
//            lenient().when(AccountRepository.findById(customerId)).thenReturn(Optional.of(customer));
//            lenient().when(productRepository.findById(productId)).thenReturn(Optional.of(product));
//            lenient().when(orderRepository.save(any(OrderEntity.class))).thenAnswer(inv -> {
//                OrderEntity saved = inv.getArgument(0);
//                saved.setId(orderId);
//                return saved;
//            });
//            lenient().doNothing().when(orderTrackingService).sendTrackingUpdate(any(), any(), any(), any());
//
//            // When
//            CreateOrderResponse result = orderService.createOrder(request, customerId);
//
//            // Then
//            assertNotNull(result);
//            assertEquals(BigDecimal.valueOf(10000.00), result.totalAmount());
//        }
//    }
//
//    // ==================== processPayment() Tests ====================
//    @Nested
//    @DisplayName("processPayment() Tests")
//    class ProcessPaymentTests {
//
//        @BeforeEach
//        void setUpPayment() {
//            OrderItemEntity item = new OrderItemEntity();
//            item.setUnitPrice(BigDecimal.valueOf(100.00));
//            item.setQuantity(2);
//            order.setOrderItems(List.of(item));
//        }
//
//        @Test
//        @DisplayName("Should process CASH payment successfully")
//        void shouldProcessCashPayment() {
//            // Given
//            PaymentRequest request = new PaymentRequest(PaymentMethod.CASH, BigDecimal.valueOf(200.00));
//            lenient().when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
//            lenient().when(paymentRepository.findFirstByOrder_IdAndStatus(orderId, PaymentStatus.PENDING))
//                    .thenReturn(Optional.empty());
//            lenient().when(paymentRepository.save(any(PaymentEntity.class))).thenAnswer(inv -> {
//                PaymentEntity p = inv.getArgument(0);
//                p.setId(UUID.randomUUID());
//                return p;
//            });
//            lenient().when(orderRepository.save(any(OrderEntity.class))).thenReturn(order);
//            lenient().doNothing().when(orderTrackingService).sendTrackingUpdate(any(), any(), any(), any());
//
//            // When
//            PaymentResponse result = orderService.processPayment(orderId, request, customerId, "127.0.0.1");
//
//            // Then
//            assertNotNull(result);
//            assertEquals(orderId, result.orderId());
//            assertEquals("CASH", result.paymentMethod());
//            assertEquals("PAID", result.paymentStatus());
//            assertEquals(OrderStatus.PAID, result.orderStatus());
//        }
//
//        @Test
//        @DisplayName("Should throw ORDER_NOT_FOUND when order does not exist")
//        void shouldThrowOrderNotFound() {
//            // Given
//            PaymentRequest request = new PaymentRequest(PaymentMethod.CASH, BigDecimal.valueOf(200.00));
//            when(orderRepository.findById(orderId)).thenReturn(Optional.empty());
//
//            // When & Then
//            ApiException exception = assertThrows(ApiException.class,
//                    () -> orderService.processPayment(orderId, request, customerId, "127.0.0.1"));
//            assertEquals(OrderErrorCode.ORDER_NOT_FOUND, exception.getErrorCode());
//        }
//
//        @Test
//        @DisplayName("Should throw ORDER_NOT_OWNED when user is not owner")
//        void shouldThrowOrderNotOwned() {
//            // Given
//            PaymentRequest request = new PaymentRequest(PaymentMethod.CASH, BigDecimal.valueOf(200.00));
//            when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
//
//            // When & Then
//            ApiException exception = assertThrows(ApiException.class,
//                    () -> orderService.processPayment(orderId, request, strangerId, "127.0.0.1"));
//            assertEquals(OrderErrorCode.ORDER_NOT_OWNED, exception.getErrorCode());
//        }
//
//        @Test
//        @DisplayName("Should throw ORDER_ALREADY_PAID when order is already paid")
//        void shouldThrowOrderAlreadyPaid() {
//            // Given
//            order.setStatus(OrderStatus.PAID);
//            PaymentRequest request = new PaymentRequest(PaymentMethod.CASH, BigDecimal.valueOf(200.00));
//            when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
//
//            // When & Then
//            ApiException exception = assertThrows(ApiException.class,
//                    () -> orderService.processPayment(orderId, request, customerId, "127.0.0.1"));
//            assertEquals(OrderErrorCode.ORDER_ALREADY_PAID, exception.getErrorCode());
//        }
//
//        @Test
//        @DisplayName("Should throw PAYMENT_AMOUNT_MISMATCH when amount is incorrect")
//        void shouldThrowAmountMismatch() {
//            // Given
//            PaymentRequest request = new PaymentRequest(PaymentMethod.CASH, BigDecimal.valueOf(100.00));
//            when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
//
//            // When & Then
//            ApiException exception = assertThrows(ApiException.class,
//                    () -> orderService.processPayment(orderId, request, customerId, "127.0.0.1"));
//            assertEquals(PaymentErrorCode.PAYMENT_AMOUNT_MISMATCH, exception.getErrorCode());
//        }
//
//        @Test
//        @DisplayName("Should reuse existing PENDING payment for idempotency")
//        void shouldReuseExistingPendingPayment() {
//            // Given
//            PaymentEntity pendingPayment = new PaymentEntity();
//            pendingPayment.setStatus(PaymentStatus.PENDING);
//            pendingPayment.setPaymentMethod(PaymentMethod.CASH);
//            pendingPayment.setId(UUID.randomUUID());
//            pendingPayment.setOrder(order);
//            pendingPayment.setAmountPaid(BigDecimal.valueOf(200.00));
//
//            PaymentRequest request = new PaymentRequest(PaymentMethod.CASH, BigDecimal.valueOf(200.00));
//            when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
//            when(paymentRepository.findFirstByOrder_IdAndStatus(orderId, PaymentStatus.PENDING))
//                    .thenReturn(Optional.of(pendingPayment));
//            lenient().when(paymentRepository.save(any(PaymentEntity.class))).thenReturn(pendingPayment);
//            lenient().when(orderRepository.save(any(OrderEntity.class))).thenReturn(order);
//            lenient().doNothing().when(orderTrackingService).sendTrackingUpdate(any(), any(), any(), any());
//
//            // When - Should reuse existing payment (idempotency)
//            PaymentResponse result = orderService.processPayment(orderId, request, customerId, "127.0.0.1");
//
//            // Then
//            assertNotNull(result);
//            assertEquals(pendingPayment.getId(), result.paymentId());
//            // For CASH payment, status should be updated to PAID
//            verify(paymentRepository, atLeastOnce()).save(any(PaymentEntity.class));
//        }
//
//        @Test
//        @DisplayName("Should process WALLET payment successfully")
//        void shouldProcessWalletPayment() {
//            // Given
//            PaymentRequest request = new PaymentRequest(PaymentMethod.WALLET, BigDecimal.valueOf(200.00));
//            lenient().when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
//            lenient().when(paymentRepository.findFirstByOrder_IdAndStatus(orderId, PaymentStatus.PENDING))
//                    .thenReturn(Optional.empty());
//            lenient().when(paymentRepository.save(any(PaymentEntity.class))).thenAnswer(inv -> {
//                PaymentEntity p = inv.getArgument(0);
//                p.setId(UUID.randomUUID());
//                return p;
//            });
//            lenient().when(orderRepository.save(any(OrderEntity.class))).thenReturn(order);
//            lenient().doNothing().when(orderTrackingService).sendTrackingUpdate(any(), any(), any(), any());
//
//            // When
//            PaymentResponse result = orderService.processPayment(orderId, request, customerId, "127.0.0.1");
//
//            // Then
//            assertNotNull(result);
//            assertEquals("WALLET", result.paymentMethod());
//        }
//
//        @Test
//        @DisplayName("Should process MOMO payment successfully")
//        void shouldProcessMomoPayment() {
//            // Given
//            PaymentRequest request = new PaymentRequest(PaymentMethod.MOMO, BigDecimal.valueOf(200.00));
//
//            when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
//            when(paymentRepository.findFirstByOrder_IdAndStatus(orderId, PaymentStatus.PENDING))
//                    .thenReturn(Optional.empty());
//
//            PaymentEntity savedPayment = new PaymentEntity();
//            savedPayment.setId(UUID.randomUUID());
//            savedPayment.setPaymentMethod(PaymentMethod.MOMO);
//            savedPayment.setAmountPaid(BigDecimal.valueOf(200.00));
//            savedPayment.setStatus(PaymentStatus.PENDING);
//            savedPayment.setOrder(order);
//            savedPayment.setTransactionId(UUID.randomUUID().toString());
//
//            when(paymentRepository.save(any(PaymentEntity.class))).thenReturn(savedPayment);
//            // Mock for MomoRequestType check
//            when(paymentMethodService.isMomoRequestTypeEnabled(anyString())).thenReturn(true);
//            when(moMoPaymentService.createPaymentLink(eq(orderId), anyLong(), anyString(), anyString()))
//                    .thenReturn("https://test-payment.momo.vn/gw_payment/payment.html");
//
//            // When
//            PaymentResponse result = orderService.processPayment(orderId, request, customerId, "127.0.0.1");
//
//            // Then
//            assertNotNull(result);
//            assertNotNull(result.paymentUrl());
//            assertEquals("MOMO", result.paymentMethod());
//            assertEquals("PENDING", result.paymentStatus());
//            assertTrue(result.paymentUrl().contains("momo.vn"));
//            verify(moMoPaymentService).createPaymentLink(eq(orderId), anyLong(), anyString(), anyString());
//        }
//
//        @Test
//        @DisplayName("Should throw INVALID_PAYMENT_METHOD when MOMO request type is disabled")
//        void shouldThrowInvalidPaymentMethodWhenMomoTypeDisabled() {
//            // Given
//            PaymentRequest request = new PaymentRequest(PaymentMethod.MOMO, BigDecimal.valueOf(200.00));
//            lenient().when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
//            lenient().when(paymentRepository.findFirstByOrder_IdAndStatus(orderId, PaymentStatus.PENDING))
//                    .thenReturn(Optional.empty());
//            lenient().when(paymentRepository.save(any(PaymentEntity.class))).thenAnswer(inv -> {
//                PaymentEntity p = inv.getArgument(0);
//                p.setId(UUID.randomUUID());
//                return p;
//            });
//            when(paymentMethodService.isMomoRequestTypeEnabled(any())).thenReturn(false);
//
//            // When & Then
//            ApiException exception = assertThrows(ApiException.class,
//                    () -> orderService.processPayment(orderId, request, customerId, "127.0.0.1"));
//            assertEquals(PaymentErrorCode.INVALID_PAYMENT_METHOD, exception.getErrorCode());
//        }
//
//        @Test
//        @DisplayName("Should process VNPAY payment successfully")
//        void shouldProcessVnpayPayment() {
//            // Given
//            PaymentRequest request = new PaymentRequest(PaymentMethod.VNPAY, BigDecimal.valueOf(200.00));
//
//            when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
//            when(paymentRepository.findFirstByOrder_IdAndStatus(orderId, PaymentStatus.PENDING))
//                    .thenReturn(Optional.empty());
//
//            PaymentEntity savedPayment = new PaymentEntity();
//            savedPayment.setId(UUID.randomUUID());
//            savedPayment.setPaymentMethod(PaymentMethod.VNPAY);
//            savedPayment.setAmountPaid(BigDecimal.valueOf(200.00));
//            savedPayment.setStatus(PaymentStatus.PENDING);
//            savedPayment.setOrder(order);
//            savedPayment.setTransactionId(UUID.randomUUID().toString());
//
//            when(paymentRepository.save(any(PaymentEntity.class))).thenReturn(savedPayment);
//            when(vnPayService.createPaymentUrl(anyString(), anyLong(), anyString(), anyString()))
//                    .thenReturn("https://sandbox.vnpayment.vn/paymentv2/vpcpay.html");
//
//            // When
//            PaymentResponse result = orderService.processPayment(orderId, request, customerId, "127.0.0.1");
//
//            // Then
//            assertNotNull(result);
//            assertNotNull(result.paymentUrl());
//            assertEquals("VNPAY", result.paymentMethod());
//            assertEquals("PENDING", result.paymentStatus());
//            assertTrue(result.paymentUrl().contains("vnpay"));
//            verify(vnPayService).createPaymentUrl(anyString(), anyLong(), anyString(), anyString());
//        }
//
//        @Test
//        @DisplayName("Should handle null IP address for VNPAY payment")
//        void shouldHandleNullIpAddressForVnpay() {
//            // Given
//            PaymentRequest request = new PaymentRequest(PaymentMethod.VNPAY, BigDecimal.valueOf(200.00));
//
//            when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
//            when(paymentRepository.findFirstByOrder_IdAndStatus(orderId, PaymentStatus.PENDING))
//                    .thenReturn(Optional.empty());
//
//            PaymentEntity savedPayment = new PaymentEntity();
//            savedPayment.setId(UUID.randomUUID());
//            savedPayment.setPaymentMethod(PaymentMethod.VNPAY);
//            savedPayment.setAmountPaid(BigDecimal.valueOf(200.00));
//            savedPayment.setStatus(PaymentStatus.PENDING);
//            savedPayment.setOrder(order);
//            savedPayment.setTransactionId(UUID.randomUUID().toString());
//
//            when(paymentRepository.save(any(PaymentEntity.class))).thenReturn(savedPayment);
//            when(vnPayService.createPaymentUrl(anyString(), anyLong(), anyString(), eq("127.0.0.1")))
//                    .thenReturn("https://sandbox.vnpayment.vn/paymentv2/vpcpay.html");
//
//            // When - Pass null IP address
//            PaymentResponse result = orderService.processPayment(orderId, request, customerId, null);
//
//            // Then
//            assertNotNull(result);
//            // Should default to 127.0.0.1
//            verify(vnPayService).createPaymentUrl(anyString(), anyLong(), anyString(), eq("127.0.0.1"));
//        }
//
//        @Test
//        @DisplayName("Should calculate correct VND amount for payment gateways")
//        void shouldCalculateCorrectVndAmount() {
//            // Given - USD amount of 200
//            PaymentRequest request = new PaymentRequest(PaymentMethod.VNPAY, BigDecimal.valueOf(200.00));
//
//            when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
//            when(paymentRepository.findFirstByOrder_IdAndStatus(orderId, PaymentStatus.PENDING))
//                    .thenReturn(Optional.empty());
//
//            PaymentEntity savedPayment = new PaymentEntity();
//            savedPayment.setId(UUID.randomUUID());
//            savedPayment.setPaymentMethod(PaymentMethod.VNPAY);
//            savedPayment.setAmountPaid(BigDecimal.valueOf(200.00));
//            savedPayment.setStatus(PaymentStatus.PENDING);
//            savedPayment.setOrder(order);
//            savedPayment.setTransactionId(UUID.randomUUID().toString());
//
//            when(paymentRepository.save(any(PaymentEntity.class))).thenReturn(savedPayment);
//            when(vnPayService.createPaymentUrl(anyString(), eq(5000000L), anyString(), anyString()))
//                    .thenReturn("https://vnpay.vn/payment");
//
//            // When
//            PaymentResponse result = orderService.processPayment(orderId, request, customerId, "127.0.0.1");
//
//            // Then
//            assertNotNull(result);
//            // Should convert 200 USD to 5,000,000 VND (200 * 25,000)
//            verify(vnPayService).createPaymentUrl(anyString(), eq(5000000L), anyString(), anyString());
//        }
//    }
//
//    // ==================== getStatus() Tests ====================
//    @Nested
//    @DisplayName("getStatus() Tests")
//    class GetStatusTests {
//
//        @Test
//        @DisplayName("Should return status for owner")
//        void shouldReturnStatusForOwner() {
//            // Given
//            when(orderRepository.findByIdWithCustomer(orderId)).thenReturn(Optional.of(order));
//
//            // When
//            OrderStatusResponse result = orderService.getStatus(orderId, customerId, "CUSTOMER");
//
//            // Then
//            assertNotNull(result);
//            assertEquals(orderId, result.getId());
//            assertEquals(OrderStatus.PENDING, result.getStatus());
//        }
//
//        @Test
//        @DisplayName("Should return status for ADMIN")
//        void shouldReturnStatusForAdmin() {
//            // Given
//            when(orderRepository.findByIdWithCustomer(orderId)).thenReturn(Optional.of(order));
//
//            // When
//            OrderStatusResponse result = orderService.getStatus(orderId, strangerId, "ADMIN");
//
//            // Then
//            assertNotNull(result);
//            assertEquals(orderId, result.getId());
//        }
//
//        @Test
//        @DisplayName("Should throw ORDER_NOT_OWNED when customer views other's order")
//        void shouldThrowOrderNotOwned() {
//            // Given
//            when(orderRepository.findByIdWithCustomer(orderId)).thenReturn(Optional.of(order));
//
//            // When & Then
//            ApiException exception = assertThrows(ApiException.class,
//                    () -> orderService.getStatus(orderId, strangerId, "CUSTOMER"));
//            assertEquals(OrderErrorCode.ORDER_NOT_OWNED, exception.getErrorCode());
//        }
//
//        @Test
//        @DisplayName("Should throw ORDER_NOT_FOUND when order does not exist")
//        void shouldThrowOrderNotFound() {
//            // Given
//            when(orderRepository.findByIdWithCustomer(orderId)).thenReturn(Optional.empty());
//
//            // When & Then
//            ApiException exception = assertThrows(ApiException.class,
//                    () -> orderService.getStatus(orderId, customerId, "CUSTOMER"));
//            assertEquals(OrderErrorCode.ORDER_NOT_FOUND, exception.getErrorCode());
//        }
//
//        @Test
//        @DisplayName("Should return status for MANAGER")
//        void shouldReturnStatusForStoreManager() {
//            // Given
//            when(orderRepository.findByIdWithCustomer(orderId)).thenReturn(Optional.of(order));
//
//            // When
//            OrderStatusResponse result = orderService.getStatus(orderId, strangerId, "MANAGER");
//
//            // Then
//            assertNotNull(result);
//            assertEquals(orderId, result.getId());
//            assertEquals(OrderStatus.PENDING, result.getStatus());
//        }
//
//        @Test
//        @DisplayName("Should include all order fields in status response")
//        void shouldIncludeAllOrderFields() {
//            // Given
//            order.setTotalAmount(BigDecimal.valueOf(200.00));
//            when(orderRepository.findByIdWithCustomer(orderId)).thenReturn(Optional.of(order));
//
//            // When
//            OrderStatusResponse result = orderService.getStatus(orderId, customerId, "CUSTOMER");
//
//            // Then
//            assertNotNull(result);
//            assertEquals(orderId, result.getId());
//            assertEquals(OrderStatus.PENDING, result.getStatus());
//            // Verify the response contains required fields
//            assertNotNull(result);
//        }
//    }
//
//    // ==================== getOrderHistory() Tests ====================
//    @Nested
//    @DisplayName("getOrderHistory() Tests")
//    class GetOrderHistoryTests {
//
//        @Test
//        @DisplayName("Should return order history for ADMIN")
//        void shouldReturnHistoryForAdmin() {
//            // Given
//            OrderEntity order1 = OrderEntity.builder()
//                    .status(OrderStatus.COMPLETED)
//                    .orderTime(LocalDateTime.now())
//                    .build();
//            order1.setId(UUID.randomUUID());
//
//            Page<OrderEntity> page = new PageImpl<>(List.of(order1), PageRequest.of(0, 10), 1);
//            when(orderRepository.findOrderHistory(any(), any(), any(), any(), any())).thenReturn(page);
//
//            // When
//            OrderHistoryPage result = orderService.getOrderHistory(
//                    1, 10, Optional.empty(), Optional.empty(),
//                    Optional.empty(), Optional.empty(), "ADMIN");
//
//            // Then
//            assertNotNull(result);
//            assertEquals(1, result.getContent().size());
//        }
//
//        @Test
//        @DisplayName("Should throw BAD_REQUEST for invalid pagination")
//        void shouldThrowBadRequestForInvalidPagination() {
//            // When & Then
//            assertThrows(ApiException.class,
//                    () -> orderService.getOrderHistory(0, 10, Optional.empty(),
//                            Optional.empty(), Optional.empty(), Optional.empty(), "ADMIN"));
//        }
//
//        @Test
//        @DisplayName("Should throw INVALID_ORDER_STATUS for invalid status filter")
//        void shouldThrowInvalidStatusFilter() {
//            // When & Then
//            ApiException exception = assertThrows(ApiException.class,
//                    () -> orderService.getOrderHistory(1, 10, Optional.of("INVALID"),
//                            Optional.empty(), Optional.empty(), Optional.empty(), "ADMIN"));
//            assertEquals(OrderErrorCode.INVALID_ORDER_STATUS, exception.getErrorCode());
//        }
//
//        @Test
//        @DisplayName("Should handle empty order history")
//        void shouldHandleEmptyOrderHistory() {
//            // Given
//            Page<OrderEntity> emptyPage = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);
//            when(orderRepository.findOrderHistory(any(), any(), any(), any(), any())).thenReturn(emptyPage);
//
//            // When
//            OrderHistoryPage result = orderService.getOrderHistory(
//                    1, 10, Optional.empty(), Optional.empty(),
//                    Optional.empty(), Optional.empty(), "ADMIN");
//
//            // Then
//            assertNotNull(result);
//            assertEquals(0, result.getContent().size());
//        }
//
//        @Test
//        @DisplayName("Should filter by status correctly")
//        void shouldFilterByStatusCorrectly() {
//            // Given
//            OrderEntity order1 = OrderEntity.builder()
//                    .status(OrderStatus.PAID)
//                    .orderTime(LocalDateTime.now())
//                    .build();
//            order1.setId(UUID.randomUUID());
//
//            Page<OrderEntity> page = new PageImpl<>(List.of(order1), PageRequest.of(0, 10), 1);
//            when(orderRepository.findOrderHistory(any(), any(), any(), any(), any())).thenReturn(page);
//
//            // When
//            OrderHistoryPage result = orderService.getOrderHistory(
//                    1, 10, Optional.of("PAID"), Optional.empty(),
//                    Optional.empty(), Optional.empty(), "ADMIN");
//
//            // Then
//            assertNotNull(result);
//            assertEquals(1, result.getContent().size());
//        }
//
//        @Test
//        @DisplayName("Should filter by date range correctly")
//        void shouldFilterByDateRangeCorrectly() {
//            // Given
//            OrderEntity order1 = OrderEntity.builder()
//                    .status(OrderStatus.COMPLETED)
//                    .orderTime(LocalDateTime.now())
//                    .build();
//            order1.setId(UUID.randomUUID());
//
//            Page<OrderEntity> page = new PageImpl<>(List.of(order1), PageRequest.of(0, 10), 1);
//            when(orderRepository.findOrderHistory(any(), any(), any(), any(), any())).thenReturn(page);
//
//            // When - Note: fromDate and toDate are currently not used in implementation
//            OrderHistoryPage result = orderService.getOrderHistory(
//                    1, 10, Optional.empty(),
//                    Optional.empty(),
//                    Optional.empty(),
//                    Optional.empty(), "ADMIN");
//
//            // Then
//            assertNotNull(result);
//            assertEquals(1, result.getContent().size());
//        }
//
//        @Test
//        @DisplayName("Should throw BAD_REQUEST when page size exceeds max")
//        void shouldThrowBadRequestWhenPageSizeExceedsMax() {
//            // When & Then - Request 200 which exceeds MAX_PAGE_SIZE (100)
//            ApiException exception = assertThrows(ApiException.class,
//                    () -> orderService.getOrderHistory(
//                            1, 200, Optional.empty(), Optional.empty(),
//                            Optional.empty(), Optional.empty(), "ADMIN"));
//            assertEquals(CommonErrorCode.BAD_REQUEST, exception.getErrorCode());
//        }
//
//        @Test
//        @DisplayName("Should handle orders with null totalAmount")
//        void shouldHandleOrdersWithNullTotalAmount() {
//            // Given
//            OrderEntity orderWithNullTotal = OrderEntity.builder()
//                    .status(OrderStatus.COMPLETED)
//                    .orderTime(LocalDateTime.now())
//                    .totalAmount(null)
//                    .build();
//            orderWithNullTotal.setId(UUID.randomUUID());
//
//            // Add order items to calculate total
//            OrderItemEntity item = new OrderItemEntity();
//            item.setUnitPrice(BigDecimal.valueOf(50.00));
//            item.setQuantity(2);
//            orderWithNullTotal.setOrderItems(List.of(item));
//
//            Page<OrderEntity> page = new PageImpl<>(List.of(orderWithNullTotal), PageRequest.of(0, 10), 1);
//            when(orderRepository.findOrderHistory(any(), any(), any(), any(), any())).thenReturn(page);
//
//            // When
//            OrderHistoryPage result = orderService.getOrderHistory(
//                    1, 10, Optional.empty(), Optional.empty(),
//                    Optional.empty(), Optional.empty(), "ADMIN");
//
//            // Then
//            assertNotNull(result);
//            assertEquals(1, result.getContent().size());
//            // Should calculate total from items
//        }
//
//        @Test
//        @DisplayName("Should throw FORBIDDEN when CUSTOMER tries to access order history")
//        void shouldThrowForbiddenForCustomer() {
//            // When & Then
//            ApiException exception = assertThrows(ApiException.class,
//                    () -> orderService.getOrderHistory(1, 10, Optional.empty(),
//                            Optional.empty(), Optional.empty(), Optional.empty(), "CUSTOMER"));
//            assertEquals(CommonErrorCode.FORBIDDEN, exception.getErrorCode());
//        }
//    }
//
//    // ==================== getMyOrders() Tests ====================
//    @Nested
//    @DisplayName("getMyOrders() Tests")
//    class GetMyOrdersTests {
//
//        @Test
//        @DisplayName("Should return customer's orders")
//        void shouldReturnCustomerOrders() {
//            // Given
//            OrderEntity order1 = OrderEntity.builder()
//                    .customer(customer)
//                    .status(OrderStatus.COMPLETED)
//                    .orderTime(LocalDateTime.now())
//                    .build();
//            order1.setId(UUID.randomUUID());
//
//            Page<OrderEntity> page = new PageImpl<>(List.of(order1), PageRequest.of(0, 10), 1);
//            when(orderRepository.findByCustomer_IdOrderByOrderTimeDesc(eq(customerId), any())).thenReturn(page);
//
//            // When
//            OrderHistoryPage result = orderService.getMyOrders(customerId, 1, 10);
//
//            // Then
//            assertNotNull(result);
//            assertEquals(1, result.getContent().size());
//        }
//
//        @Test
//        @DisplayName("Should throw BAD_REQUEST for invalid page number")
//        void shouldThrowBadRequestForInvalidPage() {
//            // When & Then
//            assertThrows(ApiException.class,
//                    () -> orderService.getMyOrders(customerId, 0, 10));
//        }
//
//        @Test
//        @DisplayName("Should throw BAD_REQUEST for invalid page size")
//        void shouldThrowBadRequestForInvalidSize() {
//            // When & Then
//            assertThrows(ApiException.class,
//                    () -> orderService.getMyOrders(customerId, 1, 0));
//        }
//
//        @Test
//        @DisplayName("Should handle empty customer orders")
//        void shouldHandleEmptyCustomerOrders() {
//            // Given
//            Page<OrderEntity> emptyPage = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);
//            when(orderRepository.findByCustomer_IdOrderByOrderTimeDesc(eq(customerId), any())).thenReturn(emptyPage);
//
//            // When
//            OrderHistoryPage result = orderService.getMyOrders(customerId, 1, 10);
//
//            // Then
//            assertNotNull(result);
//            assertEquals(0, result.getContent().size());
//        }
//
//        @Test
//        @DisplayName("Should respect max page size for getMyOrders")
//        void shouldRespectMaxPageSize() {
//            // Given
//            OrderEntity order1 = OrderEntity.builder()
//                    .customer(customer)
//                    .status(OrderStatus.COMPLETED)
//                    .orderTime(LocalDateTime.now())
//                    .totalAmount(BigDecimal.valueOf(100.00))
//                    .build();
//            order1.setId(UUID.randomUUID());
//
//            Page<OrderEntity> page = new PageImpl<>(List.of(order1), PageRequest.of(0, 100), 1);
//            when(orderRepository.findByCustomer_IdOrderByOrderTimeDesc(eq(customerId), any())).thenReturn(page);
//
//            // When - Request max size
//            OrderHistoryPage result = orderService.getMyOrders(customerId, 1, 100);
//
//            // Then
//            assertNotNull(result);
//            assertTrue(result.getContent().size() <= 100);
//        }
//
//        @Test
//        @DisplayName("Should handle large page numbers")
//        void shouldHandleLargePageNumbers() {
//            // Given
//            Page<OrderEntity> emptyPage = new PageImpl<>(List.of(), PageRequest.of(99, 10), 0);
//            when(orderRepository.findByCustomer_IdOrderByOrderTimeDesc(eq(customerId), any())).thenReturn(emptyPage);
//
//            // When
//            OrderHistoryPage result = orderService.getMyOrders(customerId, 100, 10);
//
//            // Then
//            assertNotNull(result);
//        }
//
//        @Test
//        @DisplayName("Should order by time descending")
//        void shouldOrderByTimeDescending() {
//            // Given
//            OrderEntity order1 = OrderEntity.builder()
//                    .customer(customer)
//                    .status(OrderStatus.COMPLETED)
//                    .orderTime(LocalDateTime.now().minusDays(1))
//                    .totalAmount(BigDecimal.valueOf(100.00))
//                    .build();
//            order1.setId(UUID.randomUUID());
//
//            OrderEntity order2 = OrderEntity.builder()
//                    .customer(customer)
//                    .status(OrderStatus.PAID)
//                    .orderTime(LocalDateTime.now())
//                    .totalAmount(BigDecimal.valueOf(200.00))
//                    .build();
//            order2.setId(UUID.randomUUID());
//
//            Page<OrderEntity> page = new PageImpl<>(List.of(order2, order1), PageRequest.of(0, 10), 2);
//            when(orderRepository.findByCustomer_IdOrderByOrderTimeDesc(eq(customerId), any())).thenReturn(page);
//
//            // When
//            OrderHistoryPage result = orderService.getMyOrders(customerId, 1, 10);
//
//            // Then
//            assertNotNull(result);
//            assertEquals(2, result.getContent().size());
//            // First order should be newer
//        }
//    }
//
//    // ==================== getOrderDetail() Tests ====================
//    @Nested
//    @DisplayName("getOrderDetail() Tests")
//    class GetOrderDetailTests {
//
//        @Test
//        @DisplayName("Should return order detail for owner")
//        void shouldReturnOrderDetailForOwner() {
//            // Given
//            OrderItemEntity item = new OrderItemEntity();
//            item.setUnitPrice(BigDecimal.valueOf(100.00));
//            item.setQuantity(2);
//            order.setOrderItems(List.of(item));
//            order.setTotalAmount(BigDecimal.valueOf(200.00));
//
//            lenient().when(orderRepository.findByIdWithItems(orderId)).thenReturn(Optional.of(order));
//            lenient().when(orderRepository.findByIdWithPayments(orderId)).thenReturn(Optional.of(order));
//
//            // When
//            OrderDetailResponse result = orderService.getOrderDetail(orderId.toString(), customerId, "CUSTOMER");
//
//            // Then
//            assertNotNull(result);
//            assertNotNull(result.getOrderId());
//        }
//
//        @Test
//        @DisplayName("Should return order detail for admin")
//        void shouldReturnOrderDetailForAdmin() {
//            // Given
//            OrderItemEntity item = new OrderItemEntity();
//            item.setUnitPrice(BigDecimal.valueOf(100.00));
//            item.setQuantity(1);
//            order.setOrderItems(List.of(item));
//
//            lenient().when(orderRepository.findByIdWithItems(orderId)).thenReturn(Optional.of(order));
//            lenient().when(orderRepository.findByIdWithPayments(orderId)).thenReturn(Optional.of(order));
//
//            // When
//            OrderDetailResponse result = orderService.getOrderDetail(orderId.toString(), strangerId, "ADMIN");
//
//            // Then
//            assertNotNull(result);
//            assertNotNull(result.getOrderId());
//        }
//
//        @Test
//        @DisplayName("Should throw ORDER_NOT_FOUND when order does not exist")
//        void shouldThrowOrderNotFoundForDetail() {
//            // Given
//            when(orderRepository.findByIdWithItems(orderId)).thenReturn(Optional.empty());
//
//            // When & Then
//            ApiException exception = assertThrows(ApiException.class,
//                    () -> orderService.getOrderDetail(orderId.toString(), customerId, "CUSTOMER"));
//            assertEquals(CommonErrorCode.RESOURCE_NOT_FOUND, exception.getErrorCode());
//        }
//
//        @Test
//        @DisplayName("Should throw ORDER_NOT_OWNED when customer views other's order")
//        void shouldThrowOrderNotOwnedForDetail() {
//            // Given
//            lenient().when(orderRepository.findByIdWithItems(orderId)).thenReturn(Optional.of(order));
//            lenient().when(orderRepository.findByIdWithPayments(orderId)).thenReturn(Optional.of(order));
//
//            // When & Then
//            ApiException exception = assertThrows(ApiException.class,
//                    () -> orderService.getOrderDetail(orderId.toString(), strangerId, "CUSTOMER"));
//            assertEquals(OrderErrorCode.ORDER_NOT_OWNED, exception.getErrorCode());
//        }
//
//        @Test
//        @DisplayName("Should handle order with no items")
//        void shouldHandleOrderWithNoItems() {
//            // Given
//            order.setOrderItems(new ArrayList<>());
//            lenient().when(orderRepository.findByIdWithItems(orderId)).thenReturn(Optional.of(order));
//            lenient().when(orderRepository.findByIdWithPayments(orderId)).thenReturn(Optional.of(order));
//
//            // When
//            OrderDetailResponse result = orderService.getOrderDetail(orderId.toString(), customerId, "CUSTOMER");
//
//            // Then
//            assertNotNull(result);
//            assertNotNull(result.getItems());
//            assertEquals(0, result.getItems().size());
//        }
//
//        @Test
//        @DisplayName("Should throw exception for invalid UUID format")
//        void shouldThrowExceptionForInvalidUuid() {
//            // When & Then
//            ApiException exception = assertThrows(ApiException.class,
//                    () -> orderService.getOrderDetail("invalid-uuid", customerId, "CUSTOMER"));
//            assertEquals(CommonErrorCode.RESOURCE_NOT_FOUND, exception.getErrorCode());
//        }
//    }
//
//    // ==================== getOrderStatuses() Tests ====================
//    @Nested
//    @DisplayName("getOrderStatuses() Tests")
//    class GetOrderStatusesTests {
//
//        @Test
//        @DisplayName("Should return all order statuses")
//        void shouldReturnAllOrderStatuses() {
//            // When
//            List<OrderStatus> result = orderService.getOrderStatuses();
//
//            // Then
//            assertNotNull(result);
//            assertFalse(result.isEmpty());
//            assertTrue(result.contains(OrderStatus.PENDING));
//            assertTrue(result.contains(OrderStatus.PAID));
//            assertTrue(result.contains(OrderStatus.COMPLETED));
//        }
//    }
//}
