package com.aditya.expensetracker.expense_tracker.service;

import com.aditya.expensetracker.expense_tracker.dto.CreateExpenseRequest;
import com.aditya.expensetracker.expense_tracker.dto.ExpenseFilterRequest;
import com.aditya.expensetracker.expense_tracker.dto.ExpenseResponse;
import com.aditya.expensetracker.expense_tracker.dto.PagedResponse;
import com.aditya.expensetracker.expense_tracker.entity.Expense;
import com.aditya.expensetracker.expense_tracker.entity.User;
import com.aditya.expensetracker.expense_tracker.exception.ResourceNotFoundException;
import com.aditya.expensetracker.expense_tracker.mapper.ExpenseMapper;
import com.aditya.expensetracker.expense_tracker.repository.ExpenseRepository;
import com.aditya.expensetracker.expense_tracker.specification.ExpenseSpecification;
import lombok.RequiredArgsConstructor;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.cache.annotation.Caching;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ExpenseServiceImpl implements ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final CurrentUserService currentUserService;
    private final ExpenseMapper expenseMapper;
    private final FileStorageService fileStorageService;

    @Transactional
    @Override
    @Caching(evict = {
    	    @CacheEvict(value = "analytics-dashboard", allEntries = true),
    	    @CacheEvict(value = "analytics-summary", allEntries = true),
    	    @CacheEvict(value = "analytics-categories", allEntries = true),
    	    @CacheEvict(value = "analytics-monthly", allEntries = true),
    	    @CacheEvict(value = "analytics-trend", allEntries = true),
    	    @CacheEvict(value = "analytics-recent", allEntries = true)
    	})
    public ExpenseResponse createExpense(
            CreateExpenseRequest request,
            MultipartFile receipt) {

        User currentUser = currentUserService.getCurrentUser();

        Expense expense = expenseMapper.toEntity(request);

        // createdAt/createdBy are now populated automatically by JPA
        // auditing (see BaseAuditableEntity) -- no manual timestamp needed.
        expense.setUser(currentUser);

        if (receipt != null && !receipt.isEmpty()) {
            String receiptKey = fileStorageService.uploadFile(receipt);
            expense.setReceiptKey(receiptKey);
        }

        Expense savedExpense = expenseRepository.save(expense);

        ExpenseResponse response = expenseMapper.toResponse(savedExpense);

        if (savedExpense.getReceiptKey() != null) {
            response.setReceiptUrl(
                    fileStorageService.generatePresignedUrl(
                            savedExpense.getReceiptKey()));
        }

        return response;
    }
    @Override
    public PagedResponse<ExpenseResponse> getMyExpenses(
            ExpenseFilterRequest filter,
            Pageable pageable) {

        User currentUser = currentUserService.getCurrentUser();

        Specification<Expense> specification = Specification.unrestricted();

        specification = specification.and(
                ExpenseSpecification.belongsToUser(currentUser));

        if (filter.getCategory() != null) {
            specification = specification.and(
                    ExpenseSpecification.hasCategory(filter.getCategory()));
        }

        if (filter.getMinAmount() != null) {
            specification = specification.and(
                    ExpenseSpecification.minAmount(filter.getMinAmount()));
        }

        if (filter.getMaxAmount() != null) {
            specification = specification.and(
                    ExpenseSpecification.maxAmount(filter.getMaxAmount()));
        }

        if (filter.getStartDate() != null) {
            specification = specification.and(
                    ExpenseSpecification.startDate(filter.getStartDate()));
        }

        if (filter.getEndDate() != null) {
            specification = specification.and(
                    ExpenseSpecification.endDate(filter.getEndDate()));
        }

        Page<Expense> expenses =
                expenseRepository.findAll(specification, pageable);

        List<Expense> expenseEntities = expenses.getContent();
        List<ExpenseResponse> responses =
                expenseMapper.toResponseList(expenseEntities);

        for (int i = 0; i < expenseEntities.size(); i++) {
            String receiptKey = expenseEntities.get(i).getReceiptKey();

            if (receiptKey != null) {
                responses.get(i).setReceiptUrl(
                        fileStorageService.generatePresignedUrl(receiptKey));
            }
        }

        return PagedResponse.<ExpenseResponse>builder()
                .items(responses)
                .page(expenses.getNumber())
                .size(expenses.getSize())
                .totalElements(expenses.getTotalElements())
                .totalPages(expenses.getTotalPages())
                .hasNext(expenses.hasNext())
                .hasPrevious(expenses.hasPrevious())
                .build();
    }

    @Override
    @Cacheable(
            value = "expenses",
            key = "#id")
    public ExpenseResponse getExpense(Long id) {

        User currentUser = currentUserService.getCurrentUser();

        Expense expense = expenseRepository
                .findByIdAndUser(id, currentUser)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Expense not found"));

        ExpenseResponse response = expenseMapper.toResponse(expense);

        if (expense.getReceiptKey() != null) {
            response.setReceiptUrl(
                    fileStorageService.generatePresignedUrl(
                            expense.getReceiptKey()));
        }

        return response;
    }

    @Transactional
    @Override
    @Caching(evict = {
    	    @CacheEvict(value = "analytics-dashboard", allEntries = true),
    	    @CacheEvict(value = "analytics-summary", allEntries = true),
    	    @CacheEvict(value = "analytics-categories", allEntries = true),
    	    @CacheEvict(value = "analytics-monthly", allEntries = true),
    	    @CacheEvict(value = "analytics-trend", allEntries = true),
    	    @CacheEvict(value = "analytics-recent", allEntries = true),
    	    @CacheEvict(value = "expenses", key = "#id")
    })
    public ExpenseResponse updateExpense(
            Long id,
            CreateExpenseRequest request,
            MultipartFile receipt) {

        User currentUser = currentUserService.getCurrentUser();

        Expense expense = expenseRepository
                .findByIdAndUser(id, currentUser)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Expense not found"));

        expenseMapper.updateExpenseFromRequest(request, expense);

        if (receipt != null && !receipt.isEmpty()) {

            if (expense.getReceiptKey() != null) {
                fileStorageService.deleteFile(expense.getReceiptKey());
            }

            String receiptKey = fileStorageService.uploadFile(receipt);

            expense.setReceiptKey(receiptKey);
        }

        Expense updatedExpense = expenseRepository.save(expense);

        ExpenseResponse response = expenseMapper.toResponse(updatedExpense);

        if (updatedExpense.getReceiptKey() != null) {
            response.setReceiptUrl(
                    fileStorageService.generatePresignedUrl(
                            updatedExpense.getReceiptKey()));
        }

        return response;
    }

    @Transactional
    @Override
    @Caching(evict = {
    	    @CacheEvict(value = "analytics-dashboard", allEntries = true),
    	    @CacheEvict(value = "analytics-summary", allEntries = true),
    	    @CacheEvict(value = "analytics-categories", allEntries = true),
    	    @CacheEvict(value = "analytics-monthly", allEntries = true),
    	    @CacheEvict(value = "analytics-trend", allEntries = true),
    	    @CacheEvict(value = "analytics-recent", allEntries = true),
    	    @CacheEvict(value = "expenses", key = "#id")
    })
    public void deleteExpense(Long id) {

        User currentUser = currentUserService.getCurrentUser();

        Expense expense = expenseRepository
                .findByIdAndUser(id, currentUser)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Expense not found"));

        expenseRepository.delete(expense);
    }
}