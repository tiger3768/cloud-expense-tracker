package com.aditya.expensetracker.expense_tracker.service;

import com.aditya.expensetracker.expense_tracker.dto.CreateExpenseRequest;
import com.aditya.expensetracker.expense_tracker.dto.ExpenseFilterRequest;
import com.aditya.expensetracker.expense_tracker.dto.ExpenseResponse;
import com.aditya.expensetracker.expense_tracker.dto.PagedResponse;
import com.aditya.expensetracker.expense_tracker.entity.Expense;
import com.aditya.expensetracker.expense_tracker.entity.User;
import com.aditya.expensetracker.expense_tracker.exception.ResourceNotFoundException;
import com.aditya.expensetracker.expense_tracker.repository.ExpenseRepository;
import com.aditya.expensetracker.expense_tracker.specification.ExpenseSpecification;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ExpenseServiceImpl implements ExpenseService {

    private final ExpenseRepository expenseRepository;

    private final CurrentUserService currentUserService;

    @Override
    public ExpenseResponse createExpense(CreateExpenseRequest request) {

        User currentUser = currentUserService.getCurrentUser();

        Expense expense = Expense.builder()
                .title(request.getTitle())
                .amount(request.getAmount())
                .category(request.getCategory())
                .description(request.getDescription())
                .expenseDate(request.getExpenseDate())
                .createdAt(LocalDateTime.now())
                .user(currentUser)
                .build();

        Expense savedExpense = expenseRepository.save(expense);

        return mapToResponse(savedExpense);
    }

    private ExpenseResponse mapToResponse(Expense expense) {

        return ExpenseResponse.builder()
                .id(expense.getId())
                .title(expense.getTitle())
                .amount(expense.getAmount())
                .category(expense.getCategory())
                .description(expense.getDescription())
                .expenseDate(expense.getExpenseDate())
                .receiptUrl(expense.getReceiptUrl())
                .build();
    }
    
    @Override
    public PagedResponse<ExpenseResponse> getMyExpenses(
            ExpenseFilterRequest filter,
            Pageable pageable) {

        User currentUser = currentUserService.getCurrentUser();

        Specification<Expense> specification = Specification.unrestricted();

        specification = specification.and(
                ExpenseSpecification.belongsToUser(currentUser)
        );

        if (filter.getCategory() != null) {
            specification = specification.and(
                    ExpenseSpecification.hasCategory(filter.getCategory())
            );
        }

        if (filter.getMinAmount() != null) {
            specification = specification.and(
                    ExpenseSpecification.minAmount(filter.getMinAmount())
            );
        }

        if (filter.getMaxAmount() != null) {
            specification = specification.and(
                    ExpenseSpecification.maxAmount(filter.getMaxAmount())
            );
        }

        if (filter.getStartDate() != null) {
            specification = specification.and(
                    ExpenseSpecification.startDate(filter.getStartDate())
            );
        }

        if (filter.getEndDate() != null) {
            specification = specification.and(
                    ExpenseSpecification.endDate(filter.getEndDate())
            );
        }

        Page<Expense> expenses =
                expenseRepository.findAll(specification, pageable);

        return PagedResponse.<ExpenseResponse>builder()
                .items(expenses.getContent().stream()
                        .map(this::mapToResponse)
                        .toList())
                .page(expenses.getNumber())
                .size(expenses.getSize())
                .totalElements(expenses.getTotalElements())
                .totalPages(expenses.getTotalPages())
                .hasNext(expenses.hasNext())
                .hasPrevious(expenses.hasPrevious())
                .build();
    }

    @Override
    public ExpenseResponse getExpense(Long id) {

        User currentUser = currentUserService.getCurrentUser();

        Expense expense = expenseRepository
                .findByIdAndUser(id, currentUser)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Expense not found"));

        return mapToResponse(expense);
    }

    @Override
    public ExpenseResponse updateExpense(
            Long id,
            CreateExpenseRequest request) {

        User currentUser = currentUserService.getCurrentUser();

        Expense expense = expenseRepository
                .findByIdAndUser(id, currentUser)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Expense not found"));

        expense.setTitle(request.getTitle());
        expense.setAmount(request.getAmount());
        expense.setCategory(request.getCategory());
        expense.setDescription(request.getDescription());
        expense.setExpenseDate(request.getExpenseDate());

        Expense updatedExpense = expenseRepository.save(expense);

        return mapToResponse(updatedExpense);
    }

    @Override
    public void deleteExpense(Long id) {

        User currentUser = currentUserService.getCurrentUser();

        Expense expense = expenseRepository
                .findByIdAndUser(id, currentUser)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Expense not found"));

        expenseRepository.delete(expense);
    }
}