package com.aditya.expensetracker.expense_tracker.dto.analytics;

import java.math.BigDecimal;
import java.time.LocalDate;

public record RecentExpenseResponse(

        Long id,

        String title,

        String category,

        BigDecimal amount,

        LocalDate expenseDate,

        String type

) {
}