package com.aditya.expensetracker.expense_tracker.dto.analytics;

import java.math.BigDecimal;
import java.time.YearMonth;

public record MonthlySummaryResponse(

        YearMonth month,

        BigDecimal income,

        BigDecimal expense,

        BigDecimal balance

) {
}