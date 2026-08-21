package com.aditya.expensetracker.expense_tracker.dto;

import com.aditya.expensetracker.expense_tracker.entity.Category;
import com.aditya.expensetracker.expense_tracker.entity.ExpenseType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class UpdateExpenseRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title cannot exceed 255 characters")
    @Schema(description = "Human-readable transaction title", example = "Dinner")
    private String title;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    @Digits(
            integer = 10,
            fraction = 2,
            message = "Amount must have at most 10 integer digits and 2 decimal places")
    @Schema(description = "Positive transaction amount", example = "850.00")
    private BigDecimal amount;

    @NotNull(message = "Expense type is required")
    @Schema(description = "Whether this is money received or spent", example = "EXPENSE", allowableValues = {"EXPENSE", "INCOME"})
    private ExpenseType type;

    @NotNull(message = "Category is required")
    @Schema(description = "Transaction category", example = "FOOD")
    private Category category;

    @Size(max = 2000, message = "Description cannot exceed 2000 characters")
    @Schema(description = "Optional free-form description", example = "Dinner with friends")
    private String description;

    @NotNull(message = "Expense date is required")
    @PastOrPresent(message = "Expense date cannot be in the future")
    @Schema(description = "Date the transaction occurred; cannot be in the future", example = "2026-08-21")
    private LocalDate expenseDate;

    @NotNull(message = "Version is required")
    @Min(value = 0, message = "Version cannot be negative")
    @Schema(description = "Current optimistic-lock version returned by the API", example = "0", minimum = "0")
    private Long version;
}
