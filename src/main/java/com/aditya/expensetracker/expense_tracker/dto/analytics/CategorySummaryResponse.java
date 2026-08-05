package com.aditya.expensetracker.expense_tracker.dto.analytics;

import java.math.BigDecimal;

public record CategorySummaryResponse(

        String category,

        BigDecimal amount,

        Double percentage

) {
}