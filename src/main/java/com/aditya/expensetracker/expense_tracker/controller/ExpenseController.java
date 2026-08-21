package com.aditya.expensetracker.expense_tracker.controller;

import com.aditya.expensetracker.expense_tracker.dto.CreateExpenseRequest;
import com.aditya.expensetracker.expense_tracker.dto.ExpenseFilterRequest;
import com.aditya.expensetracker.expense_tracker.dto.ExpenseResponse;
import com.aditya.expensetracker.expense_tracker.dto.PagedResponse;
import com.aditya.expensetracker.expense_tracker.dto.UpdateExpenseRequest;
import com.aditya.expensetracker.expense_tracker.service.ExpenseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/expenses")
@RequiredArgsConstructor
@Tag(name = "Expenses", description = "Expense management APIs")
@SecurityRequirements({
        @SecurityRequirement(name = "Bearer Authentication"),
        @SecurityRequirement(name = "Agent API Key")
})
public class ExpenseController {

    private final ExpenseService expenseService;

    @Operation(summary = "Create expense")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Expense created"),
            @ApiResponse(responseCode = "400", description = "Validation failed"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public ExpenseResponse createExpense(
            @Valid @RequestPart("expense") CreateExpenseRequest request,
            @RequestPart(value = "receipt", required = false) MultipartFile receipt) {

        return expenseService.createExpense(request, receipt);
    }

    @Operation(
            summary = "Create transaction from JSON",
            description = "Agent- and integration-friendly JSON variant of transaction creation. "
                    + "Use Idempotency-Key for safe retries. The existing multipart endpoint remains "
                    + "available for the human UI and receipt uploads.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Transaction created"),
            @ApiResponse(responseCode = "400", description = "Validation failed with machine-readable field metadata"),
            @ApiResponse(responseCode = "409", description = "Idempotency key conflict"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public ExpenseResponse createTransactionFromJson(
            @Valid @RequestBody CreateExpenseRequest request,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {

        return expenseService.createExpense(request, null, idempotencyKey);
    }

    @Operation(summary = "Get all expenses")
    @GetMapping
    public PagedResponse<ExpenseResponse> getMyExpenses(
            @Valid @ModelAttribute ExpenseFilterRequest filter,
            Pageable pageable) {

        return expenseService.getMyExpenses(filter, pageable);
    }

    @Operation(summary = "Get expense by ID")
    @GetMapping("/{id}")
    public ExpenseResponse getExpense(@PathVariable Long id) {

        return expenseService.getExpense(id);
    }

    @Operation(summary = "Update expense")
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ExpenseResponse updateExpense(
            @PathVariable Long id,
            @Valid @RequestPart("expense") UpdateExpenseRequest request,
            @RequestPart(value = "receipt", required = false) MultipartFile receipt) {

        return expenseService.updateExpense(id, request, receipt);
    }

    @Operation(
            summary = "Update transaction from JSON",
            description = "Agent- and integration-friendly JSON variant of transaction update. "
                    + "Use Idempotency-Key for safe retries. Optimistic locking still applies via version.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Transaction updated"),
            @ApiResponse(responseCode = "400", description = "Validation failed"),
            @ApiResponse(responseCode = "409", description = "Version or idempotency conflict"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ExpenseResponse updateTransactionFromJson(
            @PathVariable Long id,
            @Valid @RequestBody UpdateExpenseRequest request,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {

        return expenseService.updateExpense(id, request, null, idempotencyKey);
    }

    @Operation(summary = "Delete expense")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteExpense(@PathVariable Long id) {

        expenseService.deleteExpense(id);
    }
}