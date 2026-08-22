package com.aditya.expensetracker.expense_tracker.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.aditya.expensetracker.expense_tracker.dto.CreateExpenseRequest;
import com.aditya.expensetracker.expense_tracker.dto.ExpenseResponse;
import com.aditya.expensetracker.expense_tracker.dto.UpdateExpenseRequest;
import com.aditya.expensetracker.expense_tracker.entity.Expense;

@Mapper(componentModel = "spring")
public interface ExpenseMapper {

    @Mapping(target = "receiptUrl", ignore = true)
    ExpenseResponse toResponse(Expense expense);

    List<ExpenseResponse> toResponseList(List<Expense> expenses);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "receiptKey", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    Expense toEntity(CreateExpenseRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "receiptKey", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    void updateExpenseFromRequest(
            UpdateExpenseRequest request,
            @MappingTarget Expense expense);
}
