package com.aditya.expensetracker.expense_tracker.service;

import com.aditya.expensetracker.expense_tracker.dto.CreateExpenseRequest;
import com.aditya.expensetracker.expense_tracker.dto.ExpenseFilterRequest;
import com.aditya.expensetracker.expense_tracker.dto.ExpenseResponse;
import com.aditya.expensetracker.expense_tracker.dto.PagedResponse;
import com.aditya.expensetracker.expense_tracker.dto.UpdateExpenseRequest;

import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

public interface ExpenseService {

    ExpenseResponse createExpense(
            CreateExpenseRequest request,
            MultipartFile receipt);

    ExpenseResponse createExpense(
            CreateExpenseRequest request,
            MultipartFile receipt,
            String idempotencyKey);

    PagedResponse<ExpenseResponse> getMyExpenses(
            ExpenseFilterRequest filter,
            Pageable pageable
    );

    ExpenseResponse getExpense(Long id);

    ExpenseResponse updateExpense(
            Long id,
            UpdateExpenseRequest request,
            MultipartFile receipt);

    ExpenseResponse updateExpense(
            Long id,
            UpdateExpenseRequest request,
            MultipartFile receipt,
            String idempotencyKey);

    void deleteExpense(Long id);
}