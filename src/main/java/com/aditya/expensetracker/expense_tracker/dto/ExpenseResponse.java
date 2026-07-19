package com.aditya.expensetracker.expense_tracker.dto;

import com.aditya.expensetracker.expense_tracker.entity.Category;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class ExpenseResponse {

    private Long id;

    private String title;

    private BigDecimal amount;

    private Category category;

    private String description;

    private LocalDate expenseDate;

    private String receiptUrl;
}