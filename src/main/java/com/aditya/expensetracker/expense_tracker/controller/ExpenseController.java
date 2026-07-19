package com.aditya.expensetracker.expense_tracker.controller;

import com.aditya.expensetracker.expense_tracker.dto.CreateExpenseRequest;
import com.aditya.expensetracker.expense_tracker.dto.ExpenseFilterRequest;
import com.aditya.expensetracker.expense_tracker.dto.ExpenseResponse;
import com.aditya.expensetracker.expense_tracker.dto.PagedResponse;
import com.aditya.expensetracker.expense_tracker.service.ExpenseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/expenses")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService expenseService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ExpenseResponse createExpense(
            @Valid @RequestBody CreateExpenseRequest request) {

        return expenseService.createExpense(request);
    }
    
    @GetMapping
    public PagedResponse<ExpenseResponse> getMyExpenses(
            @ModelAttribute ExpenseFilterRequest filter,
            Pageable pageable) {

        return expenseService.getMyExpenses(filter, pageable);
    }
    
    @GetMapping("/{id}")
    public ExpenseResponse getExpense(
            @PathVariable Long id) {

        return expenseService.getExpense(id);
    }
    
    @PutMapping("/{id}")
    public ExpenseResponse updateExpense(
            @PathVariable Long id,
            @Valid @RequestBody CreateExpenseRequest request) {

        return expenseService.updateExpense(id, request);
    }
    
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteExpense(@PathVariable Long id) {

        expenseService.deleteExpense(id);
    }
}