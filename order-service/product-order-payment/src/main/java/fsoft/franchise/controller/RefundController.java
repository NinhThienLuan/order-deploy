package fsoft.franchise.controller;

import fsoft.franchise.common.response.ApiResponse;
import fsoft.franchise.exception.CommonErrorCode;
import fsoft.franchise.dto.payments.DeclineRefundRequest;
import fsoft.franchise.dto.payments.RefundResponse;
import fsoft.franchise.service.RefundService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/refunds")
@Validated
@RequiredArgsConstructor
@Tag(name = "Refunds", description = "Refund request management — FRANCHISE_ADMIN and STORE_MANAGER only")
public class RefundController {

    private final RefundService refundService;

    /**
     * GET /v1/refunds/pending — Get all pending refund requests
     * Only accessible by FRANCHISE_ADMIN and STORE_MANAGER
     */
    @GetMapping("/pending")
    @Operation(summary = "Get pending refunds", description = "Returns all refund requests with PENDING status.")
    @PreAuthorize("hasAnyRole('FRANCHISE_ADMIN', 'STORE_MANAGER')")
    public ResponseEntity<ApiResponse<List<RefundResponse>>> getAllPendingRefunds(
            HttpServletRequest request) {
        List<RefundResponse> result = refundService.getAllPendingRefunds();

        return ResponseEntity.ok()
                .body(ApiResponse.<List<RefundResponse>>builder()
                        .code(200)
                        .message(CommonErrorCode.SUCCESS.getMessage())
                        .result(result)
                        .timestamp(Instant.now())
                        .path(request.getRequestURI())
                        .build());
    }

    /**
     * PATCH /v1/refunds/{id}/approve — Approve a refund request
     * Only accessible by FRANCHISE_ADMIN and STORE_MANAGER
     */
    @PatchMapping("/{id}/approve")
    @Operation(summary = "Approve refund", description = "Approve a pending refund request and trigger the refund process.")
    @PreAuthorize("hasAnyRole('FRANCHISE_ADMIN', 'STORE_MANAGER')")
    public ResponseEntity<ApiResponse<RefundResponse>> approveRefund(
            HttpServletRequest request,
            @PathVariable("id") UUID id) {
        RefundResponse result = refundService.approveRefund(id);

        return ResponseEntity.ok()
                .body(ApiResponse.<RefundResponse>builder()
                        .code(200)
                        .message("Refund approved successfully")
                        .result(result)
                        .timestamp(Instant.now())
                        .path(request.getRequestURI())
                        .build());
    }

    /**
     * PATCH /v1/refunds/{id}/decline — Decline a refund request
     * Only accessible by FRANCHISE_ADMIN and STORE_MANAGER
     */
    @PatchMapping("/{id}/decline")
    @Operation(summary = "Decline refund", description = "Decline a pending refund request with a reason.")
    @PreAuthorize("hasAnyRole('FRANCHISE_ADMIN', 'STORE_MANAGER')")
    public ResponseEntity<ApiResponse<RefundResponse>> declineRefund(
            HttpServletRequest request,
            @PathVariable("id") UUID id,
            @Valid @RequestBody DeclineRefundRequest requestDTO) {
        RefundResponse result = refundService.declineRefund(id, requestDTO.declineReason());

        return ResponseEntity.ok()
                .body(ApiResponse.<RefundResponse>builder()
                        .code(200)
                        .message("Refund declined successfully")
                        .result(result)
                        .timestamp(Instant.now())
                        .path(request.getRequestURI())
                        .build());
    }
}

