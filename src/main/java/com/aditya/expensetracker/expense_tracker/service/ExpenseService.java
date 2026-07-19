package com.aditya.expensetracker.expense_tracker.service;


import org.springframework.data.domain.Pageable;

import com.aditya.expensetracker.expense_tracker.dto.CreateExpenseRequest;
import com.aditya.expensetracker.expense_tracker.dto.ExpenseFilterRequest;
import com.aditya.expensetracker.expense_tracker.dto.ExpenseResponse;
import com.aditya.expensetracker.expense_tracker.dto.PagedResponse;

public interface ExpenseService {

    ExpenseResponse createExpense(CreateExpenseRequest request);

    PagedResponse<ExpenseResponse> getMyExpenses(
            ExpenseFilterRequest filter,
            Pageable pageable
    );

    ExpenseResponse getExpense(Long id);

    ExpenseResponse updateExpense(Long id, CreateExpenseRequest request);

    void deleteExpense(Long id);
}