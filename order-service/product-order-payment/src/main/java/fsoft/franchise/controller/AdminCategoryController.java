package fsoft.franchise.controller;

import fsoft.franchise.common.response.ApiResponse;
import fsoft.franchise.dto.products.CategoryRequest;
import fsoft.franchise.dto.products.CategoryResponse;
import fsoft.franchise.exception.CommonErrorCode;
import fsoft.franchise.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Admin category management endpoints.
 *
 * GET /api/v1/admin/categories — list all categories
 * POST /api/v1/admin/categories — create category
 * PUT /api/v1/admin/categories/{id} — update category
 * DELETE /api/v1/admin/categories/{id} — soft-delete category
 */
@RestController
@RequestMapping("/api/v1/admin/categories")
@RequiredArgsConstructor
@Tag(name = "Admin — Category", description = "Admin category management API")
// @PreAuthorize("hasRole('FRANCHISE_ADMIN')")
public class AdminCategoryController {

        private final CategoryService categoryService;

        @GetMapping
        @Operation(summary = "List all categories")
        public ResponseEntity<ApiResponse<List<CategoryResponse>>> getAll(HttpServletRequest request) {
                return ResponseEntity.ok(ApiResponse.<List<CategoryResponse>>builder()
                                .code(200)
                                .message(CommonErrorCode.SUCCESS.getMessage())
                                .result(categoryService.getCategories())
                                .timestamp(Instant.now())
                                .path(request.getRequestURI())
                                .build());
        }

        @PostMapping
        @Operation(summary = "Create a new category")
        public ResponseEntity<ApiResponse<CategoryResponse>> create(
                        HttpServletRequest request,
                        @Valid @RequestBody CategoryRequest body) {

                CategoryResponse created = categoryService.createCategory(body);
                return ResponseEntity.status(HttpStatus.CREATED).body(
                                ApiResponse.<CategoryResponse>builder()
                                                .code(201)
                                                .message("Category created successfully.")
                                                .result(created)
                                                .timestamp(Instant.now())
                                                .path(request.getRequestURI())
                                                .build());
        }

        @PutMapping("/{id}")
        @Operation(summary = "Update an existing category")
        public ResponseEntity<ApiResponse<CategoryResponse>> update(
                        HttpServletRequest request,
                        @PathVariable("id") UUID id,
                        @Valid @RequestBody CategoryRequest body) {

                CategoryResponse updated = categoryService.updateCategory(id, body);
                return ResponseEntity.ok(ApiResponse.<CategoryResponse>builder()
                                .code(200)
                                .message("Category updated successfully.")
                                .result(updated)
                                .timestamp(Instant.now())
                                .path(request.getRequestURI())
                                .build());
        }

        @DeleteMapping("/{id}")
        @Operation(summary = "Soft-delete a category")
        public ResponseEntity<ApiResponse<Void>> delete(
                        HttpServletRequest request,
                        @PathVariable("id") UUID id) {

                categoryService.deleteCategory(id);
                return ResponseEntity.ok(ApiResponse.<Void>builder()
                                .code(200)
                                .message("Category deleted successfully.")
                                .result(null)
                                .timestamp(Instant.now())
                                .path(request.getRequestURI())
                                .build());
        }
}
