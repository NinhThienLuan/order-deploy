package fsoft.franchise.controller;

import fsoft.franchise.common.response.ApiResponse;
import fsoft.franchise.exception.CommonErrorCode;
import fsoft.franchise.dto.products.*;
import fsoft.franchise.enums.ProductType;
import fsoft.franchise.service.ProductImageService;
import fsoft.franchise.service.ProductService;
import fsoft.franchise.service.ProductVariantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Admin product management endpoints — requires ADMIN role (enforced
 * via SecurityConfig).
 *
 * GET /admin/v1/products — paginated list (all products, not just active)
 * GET /admin/v1/products/{id} — full product detail
 * POST /admin/v1/products — create product
 * PUT /admin/v1/products/{id} — update product
 * DELETE /admin/v1/products/{id} — soft-delete product
 * PATCH /admin/v1/products/{id}/active — toggle active flag
 */
@RestController
@RequestMapping("/api/v1/admin/products")
@RequiredArgsConstructor
@Tag(name = "Admin — Product Management", description = "Consolidated product, variant, and image management APIs. Permission: ADMIN only.")
@PreAuthorize("hasRole('ADMIN')")
public class AdminProductController {

        private final ProductService productService;
        private final ProductVariantService variantService;
        private final ProductImageService imageService;

        @GetMapping
        @Operation(summary = "Get paginated product list (admin view)", description = "Permission: ADMIN.")
        public ResponseEntity<ApiResponse<Page<ProductSummaryResponse>>> getProducts(
                        HttpServletRequest request,
                        @RequestParam(name = "page", defaultValue = "0") int page,
                        @RequestParam(name = "size", defaultValue = "12") int size,
                        @RequestParam(required = false) UUID categoryId,
                        @RequestParam(required = false) String search,
                        @RequestParam(required = false) ProductType type,
                        @RequestParam(required = false) Boolean active) {

                int safeSize = Math.min(size, 100);
                Page<ProductSummaryResponse> result = productService.getProducts(page, safeSize, categoryId, search,
                                type, active);
                return ResponseEntity.ok(ApiResponse.<Page<ProductSummaryResponse>>builder()
                                .code(200)
                                .message(CommonErrorCode.SUCCESS.getMessage())
                                .result(result)
                                .timestamp(Instant.now())
                                .path(request.getRequestURI())
                                .build());
        }

        @GetMapping("/{id}")
        @Operation(summary = "Get product detail by ID", description = "Permission: ADMIN.")
        public ResponseEntity<ApiResponse<ProductDetailResponse>> getProductById(
                        HttpServletRequest request,
                        @PathVariable("id") UUID id) {

                ProductDetailResponse result = productService.getProductById(id);
                return ResponseEntity.ok(ApiResponse.<ProductDetailResponse>builder()
                                .code(200)
                                .message(CommonErrorCode.SUCCESS.getMessage())
                                .result(result)
                                .timestamp(Instant.now())
                                .path(request.getRequestURI())
                                .build());
        }

        @PostMapping
        @Operation(summary = "Create a new product", description = "Permission: ADMIN.")
        public ResponseEntity<ApiResponse<ProductDetailResponse>> createProduct(
                        HttpServletRequest request,
                        @Valid @RequestBody ProductRequest body) {

                ProductDetailResponse result = productService.createProduct(body);
                return ResponseEntity.status(HttpStatus.CREATED)
                                .body(ApiResponse.<ProductDetailResponse>builder()
                                                .code(201)
                                                .message("Product created successfully")
                                                .result(result)
                                                .timestamp(Instant.now())
                                                .path(request.getRequestURI())
                                                .build());
        }

        @PutMapping("/{id}")
        @Operation(summary = "Update product", description = "Permission: ADMIN.")
        public ResponseEntity<ApiResponse<ProductDetailResponse>> updateProduct(
                        HttpServletRequest request,
                        @PathVariable("id") UUID id,
                        @Valid @RequestBody ProductRequest body) {

                ProductDetailResponse result = productService.updateProduct(id, body);
                return ResponseEntity.ok(ApiResponse.<ProductDetailResponse>builder()
                                .code(200)
                                .message("Product updated successfully")
                                .result(result)
                                .timestamp(Instant.now())
                                .path(request.getRequestURI())
                                .build());
        }

        @DeleteMapping("/{id}")
        @Operation(summary = "Soft-delete a product", description = "Permission: ADMIN.")
        public ResponseEntity<Void> deleteProduct(@PathVariable("id") UUID id) {
                productService.deleteProduct(id);
                return ResponseEntity.noContent().build();
        }

        @PatchMapping("/{id}/active")
        @Operation(summary = "Toggle product active/inactive status", description = "Permission: ADMIN.")
        public ResponseEntity<ApiResponse<ProductDetailResponse>> toggleActive(
                        HttpServletRequest request,
                        @PathVariable("id") UUID id) {

                ProductDetailResponse result = productService.toggleActive(id);
                return ResponseEntity.ok(ApiResponse.<ProductDetailResponse>builder()
                                .code(200)
                                .message("Product status toggled")
                                .result(result)
                                .timestamp(Instant.now())
                                .path(request.getRequestURI())
                                .build());
        }

        @PatchMapping("/{id}/recommend")
        @Operation(summary = "Set product recommendation flag")
        public ResponseEntity<ApiResponse<ProductDetailResponse>> setRecommended(
                        HttpServletRequest request,
                        @PathVariable("id") UUID id,
                        @Valid @RequestBody SetProductRecommendationRequest body) {

                ProductDetailResponse result = productService.setRecommended(id, body.getIsRecommended());
                return ResponseEntity.ok(ApiResponse.<ProductDetailResponse>builder()
                                .code(200)
                                .message("Product recommendation updated")
                                .result(result)
                                .timestamp(Instant.now())
                                .path(request.getRequestURI())
                                .build());
        }

        // ── Product Variants ───────────────────────────────────────────────────

        @GetMapping("/{productId}/variants")
        @Operation(summary = "Get all variants of a product", description = "Permission: ADMIN.")
        public ResponseEntity<ApiResponse<List<ProductVariantResponse>>> getVariants(
                        HttpServletRequest request,
                        @PathVariable("productId") UUID productId) {
                return ResponseEntity.ok(ApiResponse.<List<ProductVariantResponse>>builder()
                                .code(200)
                                .message("Get variants successfully")
                                .result(variantService.getVariantsByProductId(productId))
                                .timestamp(Instant.now())
                                .path(request.getRequestURI())
                                .build());
        }

        @PostMapping("/{productId}/variants")
        @Operation(summary = "Create a new variant for a product", description = "Permission: ADMIN.")
        public ResponseEntity<ApiResponse<ProductVariantResponse>> createVariant(
                        HttpServletRequest request,
                        @PathVariable("productId") UUID productId,
                        @Valid @RequestBody ProductVariantRequest body) {
                return ResponseEntity.status(HttpStatus.CREATED)
                                .body(ApiResponse.<ProductVariantResponse>builder()
                                                .code(201)
                                                .message("Variant created successfully")
                                                .result(variantService.createVariant(productId, body))
                                                .timestamp(Instant.now())
                                                .path(request.getRequestURI())
                                                .build());
        }

        @PutMapping("/{productId}/variants/{variantId}")
        @Operation(summary = "Update an existing variant", description = "Permission: ADMIN.")
        public ResponseEntity<ApiResponse<ProductVariantResponse>> updateVariant(
                        HttpServletRequest request,
                        @PathVariable("productId") UUID productId,
                        @PathVariable("variantId") UUID variantId,
                        @Valid @RequestBody ProductVariantRequest body) {
                return ResponseEntity.ok(ApiResponse.<ProductVariantResponse>builder()
                                .code(200)
                                .message("Variant updated successfully")
                                .result(variantService.updateVariant(productId, variantId, body))
                                .timestamp(Instant.now())
                                .path(request.getRequestURI())
                                .build());
        }

        @DeleteMapping("/{productId}/variants/{variantId}")
        @Operation(summary = "Soft-delete a variant", description = "Permission: ADMIN.")
        public ResponseEntity<Void> deleteVariant(
                        @PathVariable("productId") UUID productId,
                        @PathVariable("variantId") UUID variantId) {
                variantService.deleteVariant(productId, variantId);
                return ResponseEntity.noContent().build();
        }

        // ── Product Images ─────────────────────────────────────────────────────

        @GetMapping("/{productId}/images")
        @Operation(summary = "Get all images of a product", description = "Permission: ADMIN.")
        public ResponseEntity<ApiResponse<List<ProductDetailResponse.ImageInfo>>> getImages(
                        HttpServletRequest request,
                        @PathVariable("productId") UUID productId) {
                return ResponseEntity.ok(ApiResponse.<List<ProductDetailResponse.ImageInfo>>builder()
                                .code(200)
                                .message("Get images successfully")
                                .result(imageService.getImagesByProductId(productId))
                                .timestamp(Instant.now())
                                .path(request.getRequestURI())
                                .build());
        }

        @PostMapping(value = "/{productId}/images", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
        @Operation(summary = "Upload images for a product", description = "Permission: ADMIN.")
        public ResponseEntity<ApiResponse<List<ProductDetailResponse.ImageInfo>>> uploadImages(
                        HttpServletRequest request,
                        @PathVariable("productId") UUID productId,
                        @RequestParam("files") List<org.springframework.web.multipart.MultipartFile> files,
                        @RequestParam(name = "setPrimaryFirst", defaultValue = "false") boolean setPrimaryFirst) {
                return ResponseEntity.status(HttpStatus.CREATED)
                                .body(ApiResponse.<List<ProductDetailResponse.ImageInfo>>builder()
                                                .code(201)
                                                .message("Images uploaded successfully")
                                                .result(imageService.uploadImages(productId, files, setPrimaryFirst))
                                                .timestamp(Instant.now())
                                                .path(request.getRequestURI())
                                                .build());
        }

        @PatchMapping("/{productId}/images/{imageId}/primary")
        @Operation(summary = "Set an image as primary", description = "Permission: ADMIN.")
        public ResponseEntity<ApiResponse<ProductDetailResponse.ImageInfo>> setPrimaryImage(
                        HttpServletRequest request,
                        @PathVariable("productId") UUID productId,
                        @PathVariable("imageId") UUID imageId) {
                return ResponseEntity.ok(ApiResponse.<ProductDetailResponse.ImageInfo>builder()
                                .code(200)
                                .message("Primary image set successfully")
                                .result(imageService.setPrimaryImage(imageId, productId))
                                .timestamp(Instant.now())
                                .path(request.getRequestURI())
                                .build());
        }

        @DeleteMapping("/{productId}/images/{imageId}")
        @Operation(summary = "Delete a product image", description = "Permission: ADMIN.")
        public ResponseEntity<Void> deleteImage(
                        @PathVariable("productId") UUID productId,
                        @PathVariable("imageId") UUID imageId) {
                imageService.deleteImage(imageId);
                return ResponseEntity.noContent().build();
        }
}
