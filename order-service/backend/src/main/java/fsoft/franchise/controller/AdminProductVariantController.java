package fsoft.franchise.controller;

import fsoft.franchise.common.response.ApiResponse;
import fsoft.franchise.exception.CommonErrorCode;
import fsoft.franchise.dto.products.ProductVariantRequest;
import fsoft.franchise.dto.products.ProductVariantResponse;
import fsoft.franchise.service.ProductVariantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Admin variant management endpoints — requires FRANCHISE_ADMIN role (enforced
 * via SecurityConfig).
 *
 * GET /admin/v1/products/{productId}/variants — list variants of a product
 * POST /admin/v1/products/{productId}/variants — create variant
 * PUT /admin/v1/products/{productId}/variants/{variantId} — update variant
 * DELETE /admin/v1/products/{productId}/variants/{variantId} — soft-delete
 * variant
 */
@RestController
@RequestMapping("/api/v1/admin/products")
@RequiredArgsConstructor
@Tag(name = "Admin — Product Variant", description = "Admin variant management API")
// TODO: re-enable before production — temporarily disabled for UI dev
// @PreAuthorize("hasAnyRole('FRANCHISE_ADMIN', 'STORE_MANAGER')")
public class AdminProductVariantController {

        private final ProductVariantService productVariantService;

        @GetMapping("/{productId}/variants")
        @Operation(summary = "Get active variants of a product")
        public ResponseEntity<ApiResponse<List<ProductVariantResponse>>> getVariants(
                        HttpServletRequest request,
                        @PathVariable("productId") UUID productId) {

                List<ProductVariantResponse> result = productVariantService.getVariantsByProductId(productId);
                return ResponseEntity.ok(ApiResponse.<List<ProductVariantResponse>>builder()
                                .code(200)
                                .message(CommonErrorCode.SUCCESS.getMessage())
                                .result(result)
                                .timestamp(Instant.now())
                                .path(request.getRequestURI())
                                .build());
        }

        @PostMapping("/{productId}/variants")
        @Operation(summary = "Create a variant for a product (with optional ingredients)")
        public ResponseEntity<ApiResponse<ProductVariantResponse>> createVariant(
                        HttpServletRequest request,
                        @PathVariable("productId") UUID productId,
                        @Valid @RequestBody ProductVariantRequest body) {

                ProductVariantResponse result = productVariantService.createVariant(productId, body);
                return ResponseEntity.status(HttpStatus.CREATED)
                                .body(ApiResponse.<ProductVariantResponse>builder()
                                                .code(201)
                                                .message("Variant created successfully")
                                                .result(result)
                                                .timestamp(Instant.now())
                                                .path(request.getRequestURI())
                                                .build());
        }

        @PutMapping("/{productId}/variants/{variantId}")
        @Operation(summary = "Update a variant (replaces ingredient list)")
        public ResponseEntity<ApiResponse<ProductVariantResponse>> updateVariant(
                        HttpServletRequest request,
                        @PathVariable("productId") UUID productId,
                        @PathVariable("variantId") UUID variantId,
                        @Valid @RequestBody ProductVariantRequest body) {

                ProductVariantResponse result = productVariantService.updateVariant(productId, variantId, body);
                return ResponseEntity.ok(ApiResponse.<ProductVariantResponse>builder()
                                .code(200)
                                .message("Variant updated successfully")
                                .result(result)
                                .timestamp(Instant.now())
                                .path(request.getRequestURI())
                                .build());
        }

        @DeleteMapping("/{productId}/variants/{variantId}")
        @Operation(summary = "Soft-delete a variant")
        public ResponseEntity<Void> deleteVariant(
                        @PathVariable("productId") UUID productId,
                        @PathVariable("variantId") UUID variantId) {

                productVariantService.deleteVariant(productId, variantId);
                return ResponseEntity.noContent().build();
        }
}
