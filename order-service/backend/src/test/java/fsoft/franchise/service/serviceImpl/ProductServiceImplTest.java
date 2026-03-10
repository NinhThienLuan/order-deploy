//package fsoft.franchise.service.serviceImpl;
//
//import fsoft.franchise.common.exception.ApiException;
//import fsoft.franchise.exception.ProductErrorCode;
//import fsoft.franchise.dto.products.ProductDetailResponse;
//import fsoft.franchise.dto.products.ProductSummaryResponse;
//import fsoft.franchise.entity.CategoryEntity;
//import fsoft.franchise.entity.ProductEntity;
//import fsoft.franchise.entity.ProductImageEntity;
//import fsoft.franchise.entity.ProductVariantEntity;
//import fsoft.franchise.repository.ProductRepository;
//import fsoft.franchise.serviceImpl.ProductServiceImpl;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Nested;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.ArgumentCaptor;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageImpl;
//import org.springframework.data.domain.Pageable;
//import org.springframework.data.jpa.domain.Specification;
//
//import java.math.BigDecimal;
//import java.time.LocalDateTime;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Optional;
//import java.util.UUID;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.*;
//
///**
// * Unit Tests for ProductServiceImpl using JUnit 5 and Mockito
// *
// * Coverage target: 80-90%
// * Test all public methods with edge cases and error scenarios
// *
// * @author Dev Team
// * @version 1.0
// * @since 2026-03-04
// */
//@ExtendWith(MockitoExtension.class)
//@DisplayName("ProductServiceImpl Unit Tests")
//class ProductServiceImplTest {
//
//    // ==================== Mocks ====================
//    @Mock
//    private ProductRepository productRepository;
//
//    @InjectMocks
//    private ProductServiceImpl productService;
//
//    // ==================== Test Data ====================
//    private UUID productId;
//    private UUID categoryId;
//    private ProductEntity product;
//    private CategoryEntity category;
//    private List<ProductVariantEntity> variants;
//    private List<ProductImageEntity> images;
//
//    @BeforeEach
//    void setUp() {
//        // Initialize IDs
//        productId = UUID.randomUUID();
//        categoryId = UUID.randomUUID();
//
//        // Setup category
//        category = new CategoryEntity();
//        category.setId(categoryId);
//        category.setName("Coffee");
//        category.setDescription("Hot and cold coffee drinks");
//
//        // Setup product
//        product = new ProductEntity();
//        product.setId(productId);
//        product.setName("Cappuccino");
//        product.setDescription("Classic Italian coffee with steamed milk");
//        product.setType("MASTER"); // String type, not enum
//        product.setActive(true);
//        product.setDeleteAt(null);
//        product.setCategory(category);
//        product.setCreatedAt(LocalDateTime.now());
//
//        // Setup variants
//        variants = new ArrayList<>();
//
//        ProductVariantEntity smallVariant = new ProductVariantEntity();
//        smallVariant.setId(UUID.randomUUID());
//        smallVariant.setSizeName("Small");
//        smallVariant.setPrice(BigDecimal.valueOf(45000));
//        smallVariant.setActive(true);
//        smallVariant.setProduct(product);
//        variants.add(smallVariant);
//
//        ProductVariantEntity mediumVariant = new ProductVariantEntity();
//        mediumVariant.setId(UUID.randomUUID());
//        mediumVariant.setSizeName("Medium");
//        mediumVariant.setPrice(BigDecimal.valueOf(55000));
//        mediumVariant.setActive(true);
//        mediumVariant.setProduct(product);
//        variants.add(mediumVariant);
//
//        ProductVariantEntity largeVariant = new ProductVariantEntity();
//        largeVariant.setId(UUID.randomUUID());
//        largeVariant.setSizeName("Large");
//        largeVariant.setPrice(BigDecimal.valueOf(65000));
//        largeVariant.setActive(true);
//        largeVariant.setProduct(product);
//        variants.add(largeVariant);
//
//        product.setVariants(variants);
//
//        // Setup images
//        images = new ArrayList<>();
//
//        ProductImageEntity primaryImage = new ProductImageEntity();
//        primaryImage.setId(UUID.randomUUID());
//        primaryImage.setImageUrl("https://example.com/images/cappuccino-primary.jpg");
//        primaryImage.setIsPrimary(true);
//        primaryImage.setProduct(product);
//        images.add(primaryImage);
//
//        ProductImageEntity secondaryImage = new ProductImageEntity();
//        secondaryImage.setId(UUID.randomUUID());
//        secondaryImage.setImageUrl("https://example.com/images/cappuccino-alt.jpg");
//        secondaryImage.setIsPrimary(false);
//        secondaryImage.setProduct(product);
//        images.add(secondaryImage);
//
//        product.setImages(images);
//    }
//
//    // ==================== getProducts() Tests ====================
//    @Nested
//    @DisplayName("getProducts() Tests")
//    class GetProductsTests {
//
//        @Test
//        @DisplayName("Should return paginated products successfully")
//        void shouldReturnPaginatedProductsSuccessfully() {
//            // Given
//            Page<ProductEntity> page = new PageImpl<>(List.of(product));
//            when(productRepository.findAll(any(Specification.class), any(Pageable.class)))
//                    .thenReturn(page);
//
//            // When
//            Page<ProductSummaryResponse> result = productService.getProducts(0, 12, null, null, null);
//
//            // Then
//            assertNotNull(result);
//            assertEquals(1, result.getTotalElements());
//            assertEquals(1, result.getContent().size());
//
//            ProductSummaryResponse summary = result.getContent().get(0);
//            assertEquals(productId, summary.getId());
//            assertEquals("Cappuccino", summary.getName());
//            assertEquals("Classic Italian coffee with steamed milk", summary.getDescription());
//            assertEquals("MASTER", summary.getType()); // String, not enum
//            assertEquals(categoryId, summary.getCategoryId());
//            assertEquals("Coffee", summary.getCategoryName());
//            assertEquals(BigDecimal.valueOf(45000), summary.getBasePrice()); // Lowest price
//            assertTrue(summary.getActive());
//            assertNotNull(summary.getPrimaryImageUrl());
//
//            verify(productRepository).findAll(any(Specification.class), any(Pageable.class));
//        }
//
//        @Test
//        @DisplayName("Should filter by categoryId")
//        void shouldFilterByCategoryId() {
//            // Given
//            Page<ProductEntity> page = new PageImpl<>(List.of(product));
//            when(productRepository.findAll(any(Specification.class), any(Pageable.class)))
//                    .thenReturn(page);
//
//            // When
//            Page<ProductSummaryResponse> result = productService.getProducts(0, 12, categoryId, null, null);
//
//            // Then
//            assertNotNull(result);
//            assertEquals(1, result.getTotalElements());
//
//            ArgumentCaptor<Specification<ProductEntity>> specCaptor = ArgumentCaptor.forClass(Specification.class);
//            ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
//            verify(productRepository).findAll(specCaptor.capture(), pageableCaptor.capture());
//
//            // Verify pageable
//            Pageable capturedPageable = pageableCaptor.getValue();
//            assertEquals(0, capturedPageable.getPageNumber());
//            assertEquals(12, capturedPageable.getPageSize());
//        }
//
//        @Test
//        @DisplayName("Should filter by search keyword in name")
//        void shouldFilterBySearchKeywordInName() {
//            // Given
//            Page<ProductEntity> page = new PageImpl<>(List.of(product));
//            when(productRepository.findAll(any(Specification.class), any(Pageable.class)))
//                    .thenReturn(page);
//
//            // When
//            Page<ProductSummaryResponse> result = productService.getProducts(0, 12, null, "cappuccino", null);
//
//            // Then
//            assertNotNull(result);
//            assertEquals(1, result.getTotalElements());
//            verify(productRepository).findAll(any(Specification.class), any(Pageable.class));
//        }
//
//        @Test
//        @DisplayName("Should filter by search keyword in description")
//        void shouldFilterBySearchKeywordInDescription() {
//            // Given
//            Page<ProductEntity> page = new PageImpl<>(List.of(product));
//            when(productRepository.findAll(any(Specification.class), any(Pageable.class)))
//                    .thenReturn(page);
//
//            // When
//            Page<ProductSummaryResponse> result = productService.getProducts(0, 12, null, "italian", null);
//
//            // Then
//            assertNotNull(result);
//            assertEquals(1, result.getTotalElements());
//        }
//
//        @Test
//        @DisplayName("Should filter by product type")
//        void shouldFilterByProductType() {
//            // Given
//            Page<ProductEntity> page = new PageImpl<>(List.of(product));
//            when(productRepository.findAll(any(Specification.class), any(Pageable.class)))
//                    .thenReturn(page);
//
//            // When
//            Page<ProductSummaryResponse> result = productService.getProducts(0, 12, null, null, "MASTER");
//
//            // Then
//            assertNotNull(result);
//            assertEquals(1, result.getTotalElements());
//        }
//
//        @Test
//        @DisplayName("Should handle case-insensitive type filter")
//        void shouldHandleCaseInsensitiveTypeFilter() {
//            // Given
//            Page<ProductEntity> page = new PageImpl<>(List.of(product));
//            when(productRepository.findAll(any(Specification.class), any(Pageable.class)))
//                    .thenReturn(page);
//
//            // When
//            Page<ProductSummaryResponse> result = productService.getProducts(0, 12, null, null, "master");
//
//            // Then
//            assertNotNull(result);
//            assertEquals(1, result.getTotalElements());
//        }
//
//        @Test
//        @DisplayName("Should return empty page when no products found")
//        void shouldReturnEmptyPageWhenNoProductsFound() {
//            // Given
//            Page<ProductEntity> emptyPage = new PageImpl<>(List.of());
//            when(productRepository.findAll(any(Specification.class), any(Pageable.class)))
//                    .thenReturn(emptyPage);
//
//            // When
//            Page<ProductSummaryResponse> result = productService.getProducts(0, 12, null, "nonexistent", null);
//
//            // Then
//            assertNotNull(result);
//            assertEquals(0, result.getTotalElements());
//            assertTrue(result.getContent().isEmpty());
//        }
//
//        @Test
//        @DisplayName("Should apply all filters together")
//        void shouldApplyAllFiltersTogether() {
//            // Given
//            Page<ProductEntity> page = new PageImpl<>(List.of(product));
//            when(productRepository.findAll(any(Specification.class), any(Pageable.class)))
//                    .thenReturn(page);
//
//            // When
//            Page<ProductSummaryResponse> result = productService.getProducts(
//                    0, 12, categoryId, "cappuccino", "MASTER");
//
//            // Then
//            assertNotNull(result);
//            assertEquals(1, result.getTotalElements());
//        }
//
//        @Test
//        @DisplayName("Should use primary image as primaryImageUrl")
//        void shouldUsePrimaryImageAsPrimaryImageUrl() {
//            // Given
//            Page<ProductEntity> page = new PageImpl<>(List.of(product));
//            when(productRepository.findAll(any(Specification.class), any(Pageable.class)))
//                    .thenReturn(page);
//
//            // When
//            Page<ProductSummaryResponse> result = productService.getProducts(0, 12, null, null, null);
//
//            // Then
//            ProductSummaryResponse summary = result.getContent().get(0);
//            assertEquals("https://example.com/images/cappuccino-primary.jpg", summary.getPrimaryImageUrl());
//        }
//
//        @Test
//        @DisplayName("Should use first image when no primary image")
//        void shouldUseFirstImageWhenNoPrimaryImage() {
//            // Given
//            images.get(0).setIsPrimary(false); // Remove primary flag
//            Page<ProductEntity> page = new PageImpl<>(List.of(product));
//            when(productRepository.findAll(any(Specification.class), any(Pageable.class)))
//                    .thenReturn(page);
//
//            // When
//            Page<ProductSummaryResponse> result = productService.getProducts(0, 12, null, null, null);
//
//            // Then
//            ProductSummaryResponse summary = result.getContent().get(0);
//            assertNotNull(summary.getPrimaryImageUrl());
//            assertEquals("https://example.com/images/cappuccino-primary.jpg", summary.getPrimaryImageUrl());
//        }
//
//        @Test
//        @DisplayName("Should handle product with no images")
//        void shouldHandleProductWithNoImages() {
//            // Given
//            product.setImages(null);
//            Page<ProductEntity> page = new PageImpl<>(List.of(product));
//            when(productRepository.findAll(any(Specification.class), any(Pageable.class)))
//                    .thenReturn(page);
//
//            // When
//            Page<ProductSummaryResponse> result = productService.getProducts(0, 12, null, null, null);
//
//            // Then
//            ProductSummaryResponse summary = result.getContent().get(0);
//            assertNull(summary.getPrimaryImageUrl());
//        }
//
//        @Test
//        @DisplayName("Should handle product with empty images list")
//        void shouldHandleProductWithEmptyImagesList() {
//            // Given
//            product.setImages(new ArrayList<>());
//            Page<ProductEntity> page = new PageImpl<>(List.of(product));
//            when(productRepository.findAll(any(Specification.class), any(Pageable.class)))
//                    .thenReturn(page);
//
//            // When
//            Page<ProductSummaryResponse> result = productService.getProducts(0, 12, null, null, null);
//
//            // Then
//            ProductSummaryResponse summary = result.getContent().get(0);
//            assertNull(summary.getPrimaryImageUrl());
//        }
//
//        @Test
//        @DisplayName("Should calculate base price as minimum variant price")
//        void shouldCalculateBasePriceAsMinimumVariantPrice() {
//            // Given
//            Page<ProductEntity> page = new PageImpl<>(List.of(product));
//            when(productRepository.findAll(any(Specification.class), any(Pageable.class)))
//                    .thenReturn(page);
//
//            // When
//            Page<ProductSummaryResponse> result = productService.getProducts(0, 12, null, null, null);
//
//            // Then
//            ProductSummaryResponse summary = result.getContent().get(0);
//            assertEquals(BigDecimal.valueOf(45000), summary.getBasePrice()); // Smallest price
//        }
//
//        @Test
//        @DisplayName("Should handle product with no variants")
//        void shouldHandleProductWithNoVariants() {
//            // Given
//            product.setVariants(null);
//            Page<ProductEntity> page = new PageImpl<>(List.of(product));
//            when(productRepository.findAll(any(Specification.class), any(Pageable.class)))
//                    .thenReturn(page);
//
//            // When
//            Page<ProductSummaryResponse> result = productService.getProducts(0, 12, null, null, null);
//
//            // Then
//            ProductSummaryResponse summary = result.getContent().get(0);
//            assertNull(summary.getBasePrice());
//        }
//
//        @Test
//        @DisplayName("Should only include active variants in base price calculation")
//        void shouldOnlyIncludeActiveVariantsInBasePriceCalculation() {
//            // Given
//            variants.get(0).setActive(false); // Deactivate smallest variant
//            Page<ProductEntity> page = new PageImpl<>(List.of(product));
//            when(productRepository.findAll(any(Specification.class), any(Pageable.class)))
//                    .thenReturn(page);
//
//            // When
//            Page<ProductSummaryResponse> result = productService.getProducts(0, 12, null, null, null);
//
//            // Then
//            ProductSummaryResponse summary = result.getContent().get(0);
//            assertEquals(BigDecimal.valueOf(55000), summary.getBasePrice()); // Next smallest active
//        }
//
//        @Test
//        @DisplayName("Should handle product with null category")
//        void shouldHandleProductWithNullCategory() {
//            // Given
//            product.setCategory(null);
//            Page<ProductEntity> page = new PageImpl<>(List.of(product));
//            when(productRepository.findAll(any(Specification.class), any(Pageable.class)))
//                    .thenReturn(page);
//
//            // When
//            Page<ProductSummaryResponse> result = productService.getProducts(0, 12, null, null, null);
//
//            // Then
//            ProductSummaryResponse summary = result.getContent().get(0);
//            assertNull(summary.getCategoryId());
//            assertNull(summary.getCategoryName());
//        }
//
//        @Test
//        @DisplayName("Should sort by createdAt descending")
//        void shouldSortByCreatedAtDescending() {
//            // Given
//            Page<ProductEntity> page = new PageImpl<>(List.of(product));
//            when(productRepository.findAll(any(Specification.class), any(Pageable.class)))
//                    .thenReturn(page);
//
//            // When
//            productService.getProducts(0, 12, null, null, null);
//
//            // Then
//            ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
//            verify(productRepository).findAll(any(Specification.class), pageableCaptor.capture());
//
//            Pageable pageable = pageableCaptor.getValue();
//            assertTrue(pageable.getSort().isSorted());
//        }
//
//        @Test
//        @DisplayName("Should handle blank search string")
//        void shouldHandleBlankSearchString() {
//            // Given
//            Page<ProductEntity> page = new PageImpl<>(List.of(product));
//            when(productRepository.findAll(any(Specification.class), any(Pageable.class)))
//                    .thenReturn(page);
//
//            // When
//            Page<ProductSummaryResponse> result = productService.getProducts(0, 12, null, "   ", null);
//
//            // Then
//            assertNotNull(result);
//            verify(productRepository).findAll(any(Specification.class), any(Pageable.class));
//        }
//
//        @Test
//        @DisplayName("Should handle blank type string")
//        void shouldHandleBlankTypeString() {
//            // Given
//            Page<ProductEntity> page = new PageImpl<>(List.of(product));
//            when(productRepository.findAll(any(Specification.class), any(Pageable.class)))
//                    .thenReturn(page);
//
//            // When
//            Page<ProductSummaryResponse> result = productService.getProducts(0, 12, null, null, "   ");
//
//            // Then
//            assertNotNull(result);
//            verify(productRepository).findAll(any(Specification.class), any(Pageable.class));
//        }
//
//        @Test
//        @DisplayName("Should handle product with all variants inactive")
//        void shouldHandleProductWithAllVariantsInactive() {
//            // Given
//            variants.forEach(v -> v.setActive(false)); // Deactivate all variants
//            Page<ProductEntity> page = new PageImpl<>(List.of(product));
//            when(productRepository.findAll(any(Specification.class), any(Pageable.class)))
//                    .thenReturn(page);
//
//            // When
//            Page<ProductSummaryResponse> result = productService.getProducts(0, 12, null, null, null);
//
//            // Then
//            ProductSummaryResponse summary = result.getContent().get(0);
//            assertNull(summary.getBasePrice()); // No active variants = null price
//        }
//
//        @Test
//        @DisplayName("Should handle empty variants list for base price")
//        void shouldHandleEmptyVariantsListForBasePrice() {
//            // Given
//            product.setVariants(new ArrayList<>());
//            Page<ProductEntity> page = new PageImpl<>(List.of(product));
//            when(productRepository.findAll(any(Specification.class), any(Pageable.class)))
//                    .thenReturn(page);
//
//            // When
//            Page<ProductSummaryResponse> result = productService.getProducts(0, 12, null, null, null);
//
//            // Then
//            ProductSummaryResponse summary = result.getContent().get(0);
//            assertNull(summary.getBasePrice());
//        }
//
//        @Test
//        @DisplayName("Should handle variant with null active flag")
//        void shouldHandleVariantWithNullActiveFlag() {
//            // Given
//            variants.get(0).setActive(null); // Null active flag
//            Page<ProductEntity> page = new PageImpl<>(List.of(product));
//            when(productRepository.findAll(any(Specification.class), any(Pageable.class)))
//                    .thenReturn(page);
//
//            // When
//            Page<ProductSummaryResponse> result = productService.getProducts(0, 12, null, null, null);
//
//            // Then
//            ProductSummaryResponse summary = result.getContent().get(0);
//            // Should filter out variant with null active (Boolean.TRUE.equals check)
//            assertEquals(BigDecimal.valueOf(55000), summary.getBasePrice()); // Next smallest
//        }
//
//        @Test
//        @DisplayName("Should handle image with null isPrimary flag")
//        void shouldHandleImageWithNullIsPrimaryFlag() {
//            // Given
//            images.get(0).setIsPrimary(null); // Null isPrimary flag
//            Page<ProductEntity> page = new PageImpl<>(List.of(product));
//            when(productRepository.findAll(any(Specification.class), any(Pageable.class)))
//                    .thenReturn(page);
//
//            // When
//            Page<ProductSummaryResponse> result = productService.getProducts(0, 12, null, null, null);
//
//            // Then
//            ProductSummaryResponse summary = result.getContent().get(0);
//            // Should fallback to first image
//            assertNotNull(summary.getPrimaryImageUrl());
//        }
//
//        @Test
//        @DisplayName("Should handle multiple products in page")
//        void shouldHandleMultipleProductsInPage() {
//            // Given
//            ProductEntity product2 = new ProductEntity();
//            product2.setId(UUID.randomUUID());
//            product2.setName("Latte");
//            product2.setType("MASTER");
//            product2.setActive(true);
//            product2.setImages(new ArrayList<>());
//            product2.setVariants(new ArrayList<>());
//
//            Page<ProductEntity> page = new PageImpl<>(List.of(product, product2));
//            when(productRepository.findAll(any(Specification.class), any(Pageable.class)))
//                    .thenReturn(page);
//
//            // When
//            Page<ProductSummaryResponse> result = productService.getProducts(0, 12, null, null, null);
//
//            // Then
//            assertEquals(2, result.getTotalElements());
//            assertEquals(2, result.getContent().size());
//            assertEquals("Cappuccino", result.getContent().get(0).getName());
//            assertEquals("Latte", result.getContent().get(1).getName());
//        }
//
//        @Test
//        @DisplayName("Should handle page size parameter")
//        void shouldHandlePageSizeParameter() {
//            // Given
//            Page<ProductEntity> page = new PageImpl<>(List.of(product));
//            when(productRepository.findAll(any(Specification.class), any(Pageable.class)))
//                    .thenReturn(page);
//
//            // When
//            productService.getProducts(0, 24, null, null, null);
//
//            // Then
//            ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
//            verify(productRepository).findAll(any(Specification.class), pageableCaptor.capture());
//
//            Pageable pageable = pageableCaptor.getValue();
//            assertEquals(0, pageable.getPageNumber());
//            assertEquals(24, pageable.getPageSize());
//        }
//
//        @Test
//        @DisplayName("Should handle second page request")
//        void shouldHandleSecondPageRequest() {
//            // Given
//            Page<ProductEntity> page = new PageImpl<>(List.of(product));
//            when(productRepository.findAll(any(Specification.class), any(Pageable.class)))
//                    .thenReturn(page);
//
//            // When
//            productService.getProducts(1, 12, null, null, null);
//
//            // Then
//            ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
//            verify(productRepository).findAll(any(Specification.class), pageableCaptor.capture());
//
//            Pageable pageable = pageableCaptor.getValue();
//            assertEquals(1, pageable.getPageNumber());
//        }
//    }
//
//    // ==================== getProductById() Tests ====================
//    @Nested
//    @DisplayName("getProductById() Tests")
//    class GetProductByIdTests {
//
//        @Test
//        @DisplayName("Should return product detail successfully")
//        void shouldReturnProductDetailSuccessfully() {
//            // Given
//            when(productRepository.findById(productId)).thenReturn(Optional.of(product));
//
//            // When
//            ProductDetailResponse result = productService.getProductById(productId);
//
//            // Then
//            assertNotNull(result);
//            assertEquals(productId, result.getId());
//            assertEquals("Cappuccino", result.getName());
//            assertEquals("Classic Italian coffee with steamed milk", result.getDescription());
//            assertEquals("MASTER", result.getType()); // String, not enum
//            assertEquals(categoryId, result.getCategoryId());
//            assertEquals("Coffee", result.getCategoryName());
//            assertTrue(result.getActive());
//
//            // Verify variants
//            assertEquals(3, result.getVariants().size());
//            assertEquals("Small", result.getVariants().get(0).getSizeName());
//            assertEquals(BigDecimal.valueOf(45000), result.getVariants().get(0).getPrice());
//
//            // Verify images
//            assertEquals(2, result.getImages().size());
//            assertTrue(result.getImages().get(0).getIsPrimary()); // Primary first
//            assertFalse(result.getImages().get(1).getIsPrimary());
//
//            verify(productRepository).findById(productId);
//        }
//
//        @Test
//        @DisplayName("Should throw PRODUCT_NOT_FOUND when product does not exist")
//        void shouldThrowProductNotFoundWhenProductDoesNotExist() {
//            // Given
//            when(productRepository.findById(productId)).thenReturn(Optional.empty());
//
//            // When & Then
//            ApiException exception = assertThrows(ApiException.class,
//                    () -> productService.getProductById(productId));
//
//            assertEquals(ProductErrorCode.PRODUCT_NOT_FOUND, exception.getErrorCode());
//            verify(productRepository).findById(productId);
//        }
//
//        @Test
//        @DisplayName("Should sort variants by price ascending")
//        void shouldSortVariantsByPriceAscending() {
//            // Given
//            when(productRepository.findById(productId)).thenReturn(Optional.of(product));
//
//            // When
//            ProductDetailResponse result = productService.getProductById(productId);
//
//            // Then
//            assertEquals(3, result.getVariants().size());
//            assertEquals(BigDecimal.valueOf(45000), result.getVariants().get(0).getPrice());
//            assertEquals(BigDecimal.valueOf(55000), result.getVariants().get(1).getPrice());
//            assertEquals(BigDecimal.valueOf(65000), result.getVariants().get(2).getPrice());
//        }
//
//        @Test
//        @DisplayName("Should sort images with primary first")
//        void shouldSortImagesWithPrimaryFirst() {
//            // Given
//            when(productRepository.findById(productId)).thenReturn(Optional.of(product));
//
//            // When
//            ProductDetailResponse result = productService.getProductById(productId);
//
//            // Then
//            assertEquals(2, result.getImages().size());
//            assertTrue(result.getImages().get(0).getIsPrimary());
//            assertEquals("https://example.com/images/cappuccino-primary.jpg",
//                    result.getImages().get(0).getImageUrl());
//        }
//
//        @Test
//        @DisplayName("Should only include active variants")
//        void shouldOnlyIncludeActiveVariants() {
//            // Given
//            variants.get(1).setActive(false); // Deactivate medium variant
//            when(productRepository.findById(productId)).thenReturn(Optional.of(product));
//
//            // When
//            ProductDetailResponse result = productService.getProductById(productId);
//
//            // Then
//            assertEquals(2, result.getVariants().size()); // Only 2 active variants
//            assertEquals("Small", result.getVariants().get(0).getSizeName());
//            assertEquals("Large", result.getVariants().get(1).getSizeName());
//        }
//
//        @Test
//        @DisplayName("Should handle product with no variants")
//        void shouldHandleProductWithNoVariants() {
//            // Given
//            product.setVariants(null);
//            when(productRepository.findById(productId)).thenReturn(Optional.of(product));
//
//            // When
//            ProductDetailResponse result = productService.getProductById(productId);
//
//            // Then
//            assertNotNull(result.getVariants());
//            assertTrue(result.getVariants().isEmpty());
//        }
//
//        @Test
//        @DisplayName("Should handle product with empty variants list")
//        void shouldHandleProductWithEmptyVariantsList() {
//            // Given
//            product.setVariants(new ArrayList<>());
//            when(productRepository.findById(productId)).thenReturn(Optional.of(product));
//
//            // When
//            ProductDetailResponse result = productService.getProductById(productId);
//
//            // Then
//            assertNotNull(result.getVariants());
//            assertTrue(result.getVariants().isEmpty());
//        }
//
//        @Test
//        @DisplayName("Should handle product with no images")
//        void shouldHandleProductWithNoImages() {
//            // Given
//            product.setImages(null);
//            when(productRepository.findById(productId)).thenReturn(Optional.of(product));
//
//            // When
//            ProductDetailResponse result = productService.getProductById(productId);
//
//            // Then
//            assertNotNull(result.getImages());
//            assertTrue(result.getImages().isEmpty());
//        }
//
//        @Test
//        @DisplayName("Should handle product with empty images list")
//        void shouldHandleProductWithEmptyImagesList() {
//            // Given
//            product.setImages(new ArrayList<>());
//            when(productRepository.findById(productId)).thenReturn(Optional.of(product));
//
//            // When
//            ProductDetailResponse result = productService.getProductById(productId);
//
//            // Then
//            assertNotNull(result.getImages());
//            assertTrue(result.getImages().isEmpty());
//        }
//
//        @Test
//        @DisplayName("Should handle product with null category")
//        void shouldHandleProductWithNullCategory() {
//            // Given
//            product.setCategory(null);
//            when(productRepository.findById(productId)).thenReturn(Optional.of(product));
//
//            // When
//            ProductDetailResponse result = productService.getProductById(productId);
//
//            // Then
//            assertNull(result.getCategoryId());
//            assertNull(result.getCategoryName());
//        }
//
//        @Test
//        @DisplayName("Should map all variant fields correctly")
//        void shouldMapAllVariantFieldsCorrectly() {
//            // Given
//            when(productRepository.findById(productId)).thenReturn(Optional.of(product));
//
//            // When
//            ProductDetailResponse result = productService.getProductById(productId);
//
//            // Then
//            var variant = result.getVariants().get(0);
//            assertNotNull(variant.getId());
//            assertEquals("Small", variant.getSizeName());
//            assertEquals(BigDecimal.valueOf(45000), variant.getPrice());
//            assertTrue(variant.getActive());
//        }
//
//        @Test
//        @DisplayName("Should map all image fields correctly")
//        void shouldMapAllImageFieldsCorrectly() {
//            // Given
//            when(productRepository.findById(productId)).thenReturn(Optional.of(product));
//
//            // When
//            ProductDetailResponse result = productService.getProductById(productId);
//
//            // Then
//            var image = result.getImages().get(0);
//            assertEquals("https://example.com/images/cappuccino-primary.jpg", image.getImageUrl());
//            assertTrue(image.getIsPrimary());
//        }
//
//        @Test
//        @DisplayName("Should handle SIGNATURE product type")
//        void shouldHandleSignatureProductType() {
//            // Given
//            product.setType("SIGNATURE"); // String, not enum
//            when(productRepository.findById(productId)).thenReturn(Optional.of(product));
//
//            // When
//            ProductDetailResponse result = productService.getProductById(productId);
//
//            // Then
//            assertEquals("SIGNATURE", result.getType()); // String, not enum
//        }
//
//        @Test
//        @DisplayName("Should handle variant with null active flag")
//        void shouldHandleVariantWithNullActiveFlag() {
//            // Given
//            variants.get(1).setActive(null); // Null active flag
//            when(productRepository.findById(productId)).thenReturn(Optional.of(product));
//
//            // When
//            ProductDetailResponse result = productService.getProductById(productId);
//
//            // Then
//            // Should filter out variant with null active (Boolean.TRUE.equals check)
//            assertEquals(2, result.getVariants().size());
//            assertEquals("Small", result.getVariants().get(0).getSizeName());
//            assertEquals("Large", result.getVariants().get(1).getSizeName());
//        }
//
//        @Test
//        @DisplayName("Should handle image with null isPrimary flag")
//        void shouldHandleImageWithNullIsPrimaryFlag() {
//            // Given
//            images.get(0).setIsPrimary(null); // Null isPrimary flag
//            when(productRepository.findById(productId)).thenReturn(Optional.of(product));
//
//            // When
//            ProductDetailResponse result = productService.getProductById(productId);
//
//            // Then
//            assertEquals(2, result.getImages().size());
//            // Null isPrimary should sort after true, before false
//        }
//
//        @Test
//        @DisplayName("Should handle all variants inactive")
//        void shouldHandleAllVariantsInactive() {
//            // Given
//            variants.forEach(v -> v.setActive(false));
//            when(productRepository.findById(productId)).thenReturn(Optional.of(product));
//
//            // When
//            ProductDetailResponse result = productService.getProductById(productId);
//
//            // Then
//            assertTrue(result.getVariants().isEmpty());
//        }
//
//        @Test
//        @DisplayName("Should handle mixed active and inactive variants")
//        void shouldHandleMixedActiveAndInactiveVariants() {
//            // Given
//            variants.get(0).setActive(false);
//            variants.get(2).setActive(false);
//            when(productRepository.findById(productId)).thenReturn(Optional.of(product));
//
//            // When
//            ProductDetailResponse result = productService.getProductById(productId);
//
//            // Then
//            assertEquals(1, result.getVariants().size());
//            assertEquals("Medium", result.getVariants().get(0).getSizeName());
//        }
//
//        @Test
//        @DisplayName("Should preserve image order with multiple non-primary images")
//        void shouldPreserveImageOrderWithMultipleNonPrimaryImages() {
//            // Given
//            ProductImageEntity thirdImage = new ProductImageEntity();
//            thirdImage.setId(UUID.randomUUID());
//            thirdImage.setImageUrl("https://example.com/images/cappuccino-third.jpg");
//            thirdImage.setIsPrimary(false);
//            images.add(thirdImage);
//            when(productRepository.findById(productId)).thenReturn(Optional.of(product));
//
//            // When
//            ProductDetailResponse result = productService.getProductById(productId);
//
//            // Then
//            assertEquals(3, result.getImages().size());
//            assertTrue(result.getImages().get(0).getIsPrimary()); // Primary first
//            assertFalse(result.getImages().get(1).getIsPrimary());
//            assertFalse(result.getImages().get(2).getIsPrimary());
//        }
//
//        @Test
//        @DisplayName("Should handle variant price with different BigDecimal scales")
//        void shouldHandleVariantPriceWithDifferentBigDecimalScales() {
//            // Given
//            variants.get(0).setPrice(new BigDecimal("45000.00"));
//            variants.get(1).setPrice(new BigDecimal("55000.0"));
//            variants.get(2).setPrice(new BigDecimal("65000"));
//            when(productRepository.findById(productId)).thenReturn(Optional.of(product));
//
//            // When
//            ProductDetailResponse result = productService.getProductById(productId);
//
//            // Then
//            assertEquals(3, result.getVariants().size());
//            // Should sort correctly regardless of scale
//            assertTrue(result.getVariants().get(0).getPrice().compareTo(
//                    result.getVariants().get(1).getPrice()) < 0);
//        }
//
//        @Test
//        @DisplayName("Should handle single variant")
//        void shouldHandleSingleVariant() {
//            // Given
//            product.setVariants(List.of(variants.get(0)));
//            when(productRepository.findById(productId)).thenReturn(Optional.of(product));
//
//            // When
//            ProductDetailResponse result = productService.getProductById(productId);
//
//            // Then
//            assertEquals(1, result.getVariants().size());
//            assertEquals("Small", result.getVariants().get(0).getSizeName());
//        }
//
//        @Test
//        @DisplayName("Should handle single image")
//        void shouldHandleSingleImage() {
//            // Given
//            product.setImages(List.of(images.get(0)));
//            when(productRepository.findById(productId)).thenReturn(Optional.of(product));
//
//            // When
//            ProductDetailResponse result = productService.getProductById(productId);
//
//            // Then
//            assertEquals(1, result.getImages().size());
//            assertTrue(result.getImages().get(0).getIsPrimary());
//        }
//
//        @Test
//        @DisplayName("Should handle product with inactive flag false")
//        void shouldHandleProductWithInactiveFlagFalse() {
//            // Given
//            product.setActive(false);
//            when(productRepository.findById(productId)).thenReturn(Optional.of(product));
//
//            // When
//            ProductDetailResponse result = productService.getProductById(productId);
//
//            // Then
//            assertFalse(result.getActive());
//        }
//
//        @Test
//        @DisplayName("Should handle product with null active flag")
//        void shouldHandleProductWithNullActiveFlag() {
//            // Given
//            product.setActive(null);
//            when(productRepository.findById(productId)).thenReturn(Optional.of(product));
//
//            // When
//            ProductDetailResponse result = productService.getProductById(productId);
//
//            // Then
//            assertNull(result.getActive());
//        }
//    }
//}
//
