package com.aditya.expensetracker.expense_tracker.dto;

import com.aditya.expensetracker.expense_tracker.entity.Category;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.PastOrPresent;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class ExpenseFilterRequest {

    private List<Category> category;

    @DecimalMin(
            value = "0.00",
            message = "Minimum amount cannot be negative")
    private BigDecimal minAmount;

    @DecimalMin(
            value = "0.00",
            message = "Maximum amount cannot be negative")
    private BigDecimal maxAmount;

    @PastOrPresent(
            message = "Start date cannot be in the future")
    private LocalDate startDate;

    @PastOrPresent(
            message = "End date cannot be in the future")
    private LocalDate endDate;

    @AssertTrue(message = "Minimum amount cannot be greater than maximum amount")
    public boolean isAmountRangeValid() {

        if (minAmount == null || maxAmount == null) {
            return true;
        }

        return minAmount.compareTo(maxAmount) <= 0;
    }

    @AssertTrue(message = "Start date cannot be after end date")
    public boolean isDateRangeValid() {

        if (startDate == null || endDate == null) {
            return true;
        }

        return !startDate.isAfter(endDate);
    }
}
