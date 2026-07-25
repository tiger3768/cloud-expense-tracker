package com.aditya.expensetracker.expense_tracker.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.aditya.expensetracker.expense_tracker.dto.CreateExpenseRequest;
import com.aditya.expensetracker.expense_tracker.dto.ExpenseResponse;
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
    Expense toEntity(CreateExpenseRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "receiptKey", ignore = true)
    void updateExpenseFromRequest(
            CreateExpenseRequest request,
            @MappingTarget Expense expense);
}