package fsoft.franchise.controller;

import fsoft.franchise.common.response.ApiResponse;
import fsoft.franchise.exception.CommonErrorCode;
import fsoft.franchise.dto.products.ProductRequest;
import fsoft.franchise.dto.products.ProductDetailResponse;
import fsoft.franchise.dto.products.ProductSummaryResponse;
import fsoft.franchise.service.ProductService;
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
import java.util.UUID;

/**
 * Admin product management endpoints — requires FRANCHISE_ADMIN role (enforced
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
@Tag(name = "Admin — Product", description = "Admin product management API")
// TODO: re-enable before production — temporarily disabled for UI dev
// @PreAuthorize("hasRole('FRANCHISE_ADMIN')")
public class AdminProductController {

        private final ProductService productService;

        @GetMapping
        @Operation(summary = "Get paginated product list (admin view)")
        public ResponseEntity<ApiResponse<Page<ProductSummaryResponse>>> getProducts(
                        HttpServletRequest request,
                        @RequestParam(name = "page", defaultValue = "0") int page,
                        @RequestParam(name = "size", defaultValue = "12") int size,
                        @RequestParam(required = false) UUID categoryId,
                        @RequestParam(required = false) String search,
                        @RequestParam(required = false) String type,
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
        @Operation(summary = "Get product detail by ID")
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
        @Operation(summary = "Create a new product")
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
        @Operation(summary = "Update product")
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
        @Operation(summary = "Soft-delete a product")
        public ResponseEntity<Void> deleteProduct(@PathVariable("id") UUID id) {
                productService.deleteProduct(id);
                return ResponseEntity.noContent().build();
        }

        @PatchMapping("/{id}/active")
        @Operation(summary = "Toggle product active/inactive status")
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
}
