package com.aditya.expensetracker.expense_tracker.controller;

import com.aditya.expensetracker.expense_tracker.dto.CreateExpenseRequest;
import com.aditya.expensetracker.expense_tracker.dto.ExpenseFilterRequest;
import com.aditya.expensetracker.expense_tracker.dto.ExpenseResponse;
import com.aditya.expensetracker.expense_tracker.dto.PagedResponse;
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
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/expenses")
@RequiredArgsConstructor
@Tag(name = "Expenses", description = "Expense management APIs")
@SecurityRequirement(name = "Bearer Authentication")
public class ExpenseController{

    private final ExpenseService expenseService;
    
    @Operation(summary = "Create expense")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Expense created"),
        @ApiResponse(responseCode = "400", description = "Validation failed"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ExpenseResponse createExpense(
            @Valid @RequestBody CreateExpenseRequest request) {

        return expenseService.createExpense(request);
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
    public ExpenseResponse getExpense(
            @PathVariable Long id) {

        return expenseService.getExpense(id);
    }
    
    @Operation(summary = "Update expense")
    @PutMapping("/{id}")
    public ExpenseResponse updateExpense(
            @PathVariable Long id,
            @Valid @RequestBody CreateExpenseRequest request) {

        return expenseService.updateExpense(id, request);
    }
    
    @Operation(summary = "Delete expense")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteExpense(@PathVariable Long id) {

        expenseService.deleteExpense(id);
    }
}