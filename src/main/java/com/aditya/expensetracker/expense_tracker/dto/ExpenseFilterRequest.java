package com.aditya.expensetracker.expense_tracker.dto;

import com.aditya.expensetracker.expense_tracker.entity.Category;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ExpenseFilterRequest {

    private Category category;

    private BigDecimal minAmount;

    private BigDecimal maxAmount;

    private LocalDate startDate;

    private LocalDate endDate;
}