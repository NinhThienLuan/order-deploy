package fsoft.franchise.controller;

import fsoft.franchise.common.response.ApiResponse;
import fsoft.franchise.dto.payments.AdminTransactionListResponse;
import fsoft.franchise.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;

/**
 * Admin APIs for internal use (đối soát dòng tiền, báo cáo).
 */
@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@Tag(name = "Admin — Transactions", description = "Internal reconciliation and reporting APIs. Permission: ADMIN, MANAGER only.")
public class AdminController {

        private final PaymentService paymentService;

        /**
         * GET /v1/admin/transactions
         * List all payment transactions for reconciliation. Request params giống GET
         * /v1/orders.
         * Permission: ADMIN or MANAGER only.
         */
        @GetMapping("/transactions")
        @Operation(summary = "List all transactions", description = "Paginated, filterable payment transaction list for reconciliation. Permission: ADMIN, MANAGER.")
        @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
        public ResponseEntity<ApiResponse<AdminTransactionListResponse>> getTransactions(
                        HttpServletRequest request,
                        @RequestParam(name = "page", defaultValue = "1") int page,
                        @RequestParam(name = "size", defaultValue = "10") int size,
                        @RequestParam(required = false) String status,
                        @RequestParam(required = false) String paymentMethod,
                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
                AdminTransactionListResponse result = paymentService.getAdminTransactions(
                                page, size,
                                Optional.ofNullable(status).filter(s -> s != null && !s.isBlank()),
                                Optional.ofNullable(paymentMethod).filter(s -> s != null && !s.isBlank()),
                                Optional.ofNullable(fromDate),
                                Optional.ofNullable(toDate));
                return ResponseEntity.ok(
                                ApiResponse.<AdminTransactionListResponse>builder()
                                                .code(200)
                                                .message("Get transaction list successfully")
                                                .result(result)
                                                .timestamp(Instant.now())
                                                .path(request.getRequestURI())
                                                .build());
        }
}
