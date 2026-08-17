package com.aditya.expensetracker.expense_tracker.dto;

import com.aditya.expensetracker.expense_tracker.entity.Category;
import com.aditya.expensetracker.expense_tracker.entity.ExpenseType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class UpdateExpenseRequest {

    @NotBlank
    private String title;

    @NotNull
    @DecimalMin("0.01")
    private BigDecimal amount;

    @NotNull
    private ExpenseType type;

    @NotNull
    private Category category;

    private String description;

    @NotNull
    private LocalDate expenseDate;

    @NotNull
    @Min(0)
    private Long version;
}
