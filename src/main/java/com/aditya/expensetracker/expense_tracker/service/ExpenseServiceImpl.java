package com.aditya.expensetracker.expense_tracker.service;

import com.aditya.expensetracker.expense_tracker.dto.CreateExpenseRequest;
import com.aditya.expensetracker.expense_tracker.dto.ExpenseFilterRequest;
import com.aditya.expensetracker.expense_tracker.dto.ExpenseResponse;
import com.aditya.expensetracker.expense_tracker.dto.PagedResponse;
import com.aditya.expensetracker.expense_tracker.dto.UpdateExpenseRequest;
import com.aditya.expensetracker.expense_tracker.exception.OptimisticLockConflictException;
import com.aditya.expensetracker.expense_tracker.entity.Expense;
import com.aditya.expensetracker.expense_tracker.entity.User;
import com.aditya.expensetracker.expense_tracker.exception.ResourceNotFoundException;
import com.aditya.expensetracker.expense_tracker.mapper.ExpenseMapper;
import com.aditya.expensetracker.expense_tracker.repository.ExpenseRepository;
import com.aditya.expensetracker.expense_tracker.repository.IdempotencyRecordRepository;
import com.aditya.expensetracker.expense_tracker.entity.IdempotencyRecord;
import com.aditya.expensetracker.expense_tracker.exception.IdempotencyConflictException;
import com.aditya.expensetracker.expense_tracker.specification.ExpenseSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.springframework.cache.annotation.Caching;

import java.util.List;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExpenseServiceImpl implements ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final CurrentUserService currentUserService;
    private final ExpenseMapper expenseMapper;
    private final FileStorageService fileStorageService;
    private final IdempotencyRecordRepository idempotencyRecordRepository;
    private final ObjectMapper objectMapper;

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
        return createExpenseInternal(request, receipt, null);
    }

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
            MultipartFile receipt,
            String idempotencyKey) {

        if (receipt != null && !receipt.isEmpty()) {
            throw new IdempotencyConflictException(
                    "Idempotency-Key is supported for JSON transactions without receipt uploads.");
        }

        return createExpenseInternal(request, null, idempotencyKey);
    }

    private ExpenseResponse createExpenseInternal(
            CreateExpenseRequest request,
            MultipartFile receipt,
            String idempotencyKey) {

        User currentUser = currentUserService.getCurrentUser();

        IdempotencyRecord idempotencyRecord = null;

        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            String normalizedKey = idempotencyKey.trim();

            if (normalizedKey.length() > 128) {
                throw new IdempotencyConflictException(
                        "Idempotency-Key cannot exceed 128 characters.");
            }

            String requestHash = hashRequest(request);

            idempotencyRecordRepository.reserve(
                    currentUser.getId(),
                    normalizedKey,
                    requestHash,
                    java.time.LocalDateTime.now());

            idempotencyRecord = idempotencyRecordRepository
                    .findByUserAndIdempotencyKey(currentUser, normalizedKey)
                    .orElseThrow(() ->
                            new IllegalStateException("Unable to reserve idempotency key."));

            if (!idempotencyRecord.getRequestHash().equals(requestHash)) {
                throw new IdempotencyConflictException(
                        "This Idempotency-Key was already used with a different request.");
            }

            if (idempotencyRecord.getExpenseId() != null) {
                return getExpense(idempotencyRecord.getExpenseId());
            }
        }

        Expense expense = expenseMapper.toEntity(request);


        expense.setUser(currentUser);

        if (receipt != null && !receipt.isEmpty()) {
            String receiptKey = fileStorageService.uploadFile(receipt);
            expense.setReceiptKey(receiptKey);
        }

        Expense savedExpense = expenseRepository.save(expense);

        if (idempotencyRecord != null) {
            idempotencyRecord.setExpenseId(savedExpense.getId());
            idempotencyRecordRepository.save(idempotencyRecord);
        }

        log.info(
                "Expense {} created by {}",
                savedExpense.getId(),
                currentUser.getEmail());

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

        if (filter.getCategory() != null && !filter.getCategory().isEmpty()) {
            specification = specification.and(
                    ExpenseSpecification.hasCategories(filter.getCategory()));
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
            key = "#id + ':' + @currentUserService.getCurrentUserId()")
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
            @CacheEvict(value = "expenses", key = "#id + ':' + @currentUserService.getCurrentUserId()")
    })
    public ExpenseResponse updateExpense(
            Long id,
            UpdateExpenseRequest request,
            MultipartFile receipt) {
        return updateExpenseInternal(id, request, receipt, null);
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
            @CacheEvict(value = "expenses", key = "#id + ':' + @currentUserService.getCurrentUserId()")
    })
    public ExpenseResponse updateExpense(
            Long id,
            UpdateExpenseRequest request,
            MultipartFile receipt,
            String idempotencyKey) {

        if (receipt != null && !receipt.isEmpty()) {
            throw new IdempotencyConflictException(
                    "Idempotency-Key is supported for JSON transactions without receipt uploads.");
        }

        return updateExpenseInternal(id, request, null, idempotencyKey);
    }

    private ExpenseResponse updateExpenseInternal(
            Long id,
            UpdateExpenseRequest request,
            MultipartFile receipt,
            String idempotencyKey) {

        User currentUser = currentUserService.getCurrentUser();

        Expense expense = expenseRepository
                .findByIdAndUser(id, currentUser)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Expense not found"));

        IdempotencyRecord idempotencyRecord = null;

        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            String normalizedKey = idempotencyKey.trim();

            if (normalizedKey.length() > 128) {
                throw new IdempotencyConflictException(
                        "Idempotency-Key cannot exceed 128 characters.");
            }

            String requestHash = hashUpdateRequest(id, request);
            idempotencyRecordRepository.reserve(
                    currentUser.getId(),
                    normalizedKey,
                    requestHash,
                    java.time.LocalDateTime.now());

            idempotencyRecord = idempotencyRecordRepository
                    .findByUserAndIdempotencyKey(currentUser, normalizedKey)
                    .orElseThrow(() ->
                            new IllegalStateException("Unable to reserve idempotency key."));

            if (!idempotencyRecord.getRequestHash().equals(requestHash)) {
                throw new IdempotencyConflictException(
                        "This Idempotency-Key was already used with a different request.");
            }

            if (idempotencyRecord.getExpenseId() != null) {
                return getExpense(idempotencyRecord.getExpenseId());
            }
        }

        if (!request.getVersion().equals(expense.getVersion())) {

            log.warn(
                    "Optimistic locking conflict for expense {}: expected version {}, actual version {}",
                    id,
                    request.getVersion(),
                    expense.getVersion());

            throw new OptimisticLockConflictException();
        }

        expenseMapper.updateExpenseFromRequest(request, expense);

        String oldReceiptKey = expense.getReceiptKey();

        if (receipt != null && !receipt.isEmpty()) {

            String newReceiptKey =
                    fileStorageService.uploadFile(receipt);

            expense.setReceiptKey(newReceiptKey);
        }

        Expense updatedExpense =
                expenseRepository.saveAndFlush(expense);

        if (idempotencyRecord != null) {
            idempotencyRecord.setExpenseId(updatedExpense.getId());
            idempotencyRecordRepository.save(idempotencyRecord);
        }

        if (receipt != null
                && !receipt.isEmpty()
                && oldReceiptKey != null) {

            fileStorageService.deleteFile(oldReceiptKey);
        }

        log.info(
                "Expense {} updated by {}",
                updatedExpense.getId(),
                currentUser.getEmail());

        ExpenseResponse response =
                expenseMapper.toResponse(updatedExpense);

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
    	    @CacheEvict(value = "expenses", key = "#id + ':' + @currentUserService.getCurrentUserId()")
    })
    public void deleteExpense(Long id) {

        User currentUser = currentUserService.getCurrentUser();

        Expense expense = expenseRepository
                .findByIdAndUser(id, currentUser)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Expense not found"));

        // @SoftDelete changes the deleted flag instead of physically deleting the row.
        // Keep the timestamp separately so scheduled retention cleanup can identify
        // rows that have been soft-deleted for more than the retention period.
        expense.setDeletedAt(LocalDateTime.now());
        expenseRepository.delete(expense);

        log.info(
                "Expense {} deleted by {}",
                id,
                currentUser.getEmail());
    }
    private String hashRequest(CreateExpenseRequest request) {
        try {
            byte[] json = objectMapper.writeValueAsBytes(request);
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(json);
            StringBuilder hex = new StringBuilder(digest.length * 2);
            for (byte value : digest) {
                hex.append(String.format("%02x", value));
            }
            return hex.toString();
        } catch (JsonProcessingException | NoSuchAlgorithmException ex) {
            throw new IllegalStateException(
                    "Unable to calculate idempotency request hash.", ex);
        }
    }

    private String hashUpdateRequest(Long id, UpdateExpenseRequest request) {
        try {
            byte[] json = objectMapper.writeValueAsBytes(request);
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(String.valueOf(id).getBytes(java.nio.charset.StandardCharsets.UTF_8));
            digest.update((byte) ':');
            byte[] hash = digest.digest(json);
            StringBuilder hex = new StringBuilder(hash.length * 2);
            for (byte value : hash) {
                hex.append(String.format("%02x", value));
            }
            return hex.toString();
        } catch (JsonProcessingException | NoSuchAlgorithmException ex) {
            throw new IllegalStateException(
                    "Unable to calculate idempotency request hash.", ex);
        }
    }

}