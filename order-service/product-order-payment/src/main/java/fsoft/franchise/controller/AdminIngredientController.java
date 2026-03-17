package fsoft.franchise.controller;

import fsoft.franchise.common.response.ApiResponse;
import fsoft.franchise.dto.products.IngredientResponse;
import fsoft.franchise.service.IngredientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/ingredients")
@RequiredArgsConstructor
@Tag(name = "Admin — Ingredients", description = "Ingredient management for product formulas")
public class AdminIngredientController {

    private final IngredientService ingredientService;

    @GetMapping
    @Operation(summary = "Get all ingredients", description = "Fetch all non-deleted ingredients for selection in formulas. Permission: ADMIN, MANAGER.")
    @PreAuthorize("hasAnyRole('FRANCHISE_ADMIN', 'STORE_MANAGER')")
    public ResponseEntity<ApiResponse<List<IngredientResponse>>> getAllIngredients(HttpServletRequest request) {
        List<IngredientResponse> result = ingredientService.getAllIngredients();
        return ResponseEntity.ok(
                ApiResponse.<List<IngredientResponse>>builder()
                        .code(200)
                        .message("Get ingredient list successfully")
                        .result(result)
                        .timestamp(Instant.now())
                        .path(request.getRequestURI())
                        .build());
    }
}
