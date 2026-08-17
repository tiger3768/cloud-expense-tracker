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
@SecurityRequirement(name = "Bearer Authentication")
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

    @Operation(summary = "Get all expenses")
    @GetMapping
    public PagedResponse<ExpenseResponse> getMyExpenses(
            @ModelAttribute ExpenseFilterRequest filter,
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

    @Operation(summary = "Delete expense")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteExpense(@PathVariable Long id) {

        expenseService.deleteExpense(id);
    }
}