package com.aditya.expensetracker.expense_tracker.service;

import com.aditya.expensetracker.expense_tracker.dto.CreateExpenseRequest;
import com.aditya.expensetracker.expense_tracker.dto.ExpenseFilterRequest;
import com.aditya.expensetracker.expense_tracker.dto.ExpenseResponse;
import com.aditya.expensetracker.expense_tracker.dto.PagedResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

public interface ExpenseService {

    ExpenseResponse createExpense(
            CreateExpenseRequest request,
            MultipartFile receipt);

    PagedResponse<ExpenseResponse> getMyExpenses(
            ExpenseFilterRequest filter,
            Pageable pageable
    );

    ExpenseResponse getExpense(Long id);

    ExpenseResponse updateExpense(
            Long id,
            CreateExpenseRequest request,
            MultipartFile receipt);

    void deleteExpense(Long id);
}