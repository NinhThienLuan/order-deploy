package fsoft.franchise.controller;

import fsoft.franchise.common.response.ApiResponse;
import fsoft.franchise.exception.CommonErrorCode;
import fsoft.franchise.dto.products.CategoryResponse;
import fsoft.franchise.dto.products.ProductDetailResponse;
import fsoft.franchise.dto.products.ProductSummaryResponse;
import fsoft.franchise.service.CategoryService;
import fsoft.franchise.service.ProductService;
import jakarta.servlet.http.HttpServletRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Public product catalogue endpoints — no authentication required.
 *
 * GET /v1/products — paginated + filtered product list
 * GET /v1/products/{id} — full product detail
 * GET /v1/products/categories — all active categories
 */
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Tag(name = "Products", description = "Public product catalogue — no authentication required")
public class ProductController {

        private final ProductService productService;
        private final CategoryService categoryService;

        /**
         * GET /v1/products
         * Returns a paginated, filterable list of active products.
         *
         * @param page       0-based page index (default 0)
         * @param size       page size (default 12, max 100)
         * @param categoryId optional UUID to filter by category
         * @param search     optional keyword (matched against name and description)
         * @param type       optional product type filter: MASTER or SIGNATURE
         */
        @GetMapping
        @Operation(summary = "List products", description = "Paginated, filterable list of active products. Supports filtering by category, keyword, and type.")
        public ResponseEntity<ApiResponse<Page<ProductSummaryResponse>>> getProducts(
                        HttpServletRequest request,
                        @RequestParam(name = "page", defaultValue = "0") int page,
                        @RequestParam(name = "size", defaultValue = "12") int size,
                        @RequestParam(required = false) UUID categoryId,
                        @RequestParam(required = false) String search,
                        @RequestParam(required = false) String type) {

                int safeSize = Math.min(size, 100);
                Page<ProductSummaryResponse> result = productService.getProducts(page, safeSize, categoryId, search,
                                type, true);

                return ResponseEntity.ok(
                                ApiResponse.<Page<ProductSummaryResponse>>builder()
                                                .code(200)
                                                .message(CommonErrorCode.SUCCESS.getMessage())
                                                .result(result)
                                                .timestamp(Instant.now())
                                                .path(request.getRequestURI())
                                                .build());
        }

        /**
         * GET /v1/products/categories
         * Returns all active, non-deleted categories ordered by name.
         */
        @GetMapping("/categories")
        @Operation(summary = "List categories", description = "Returns all active, non-deleted categories ordered by name.")
        public ResponseEntity<ApiResponse<List<CategoryResponse>>> getCategories(HttpServletRequest request) {
                List<CategoryResponse> result = categoryService.getCategories();

                return ResponseEntity.ok(
                                ApiResponse.<List<CategoryResponse>>builder()
                                                .code(200)
                                                .message(CommonErrorCode.SUCCESS.getMessage())
                                                .result(result)
                                                .timestamp(Instant.now())
                                                .path(request.getRequestURI())
                                                .build());
        }

        /**
         * GET /v1/products/{id}
         * Returns full product detail including all active variants and images.
         */
        @GetMapping("/{id}")
        @Operation(summary = "Get product detail", description = "Returns full product detail including all active variants and images.")
        public ResponseEntity<ApiResponse<ProductDetailResponse>> getProductById(
                        HttpServletRequest request,
                        @PathVariable("id") UUID id) {

                ProductDetailResponse result = productService.getProductById(id);

                return ResponseEntity.ok(
                                ApiResponse.<ProductDetailResponse>builder()
                                                .code(200)
                                                .message(CommonErrorCode.SUCCESS.getMessage())
                                                .result(result)
                                                .timestamp(Instant.now())
                                                .path(request.getRequestURI())
                                                .build());
        }
}
