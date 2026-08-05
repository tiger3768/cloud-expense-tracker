package com.aditya.expensetracker.expense_tracker.dto.analytics;

import java.math.BigDecimal;

public record IncomeExpenseSummaryResponse(

        BigDecimal totalIncome,

        BigDecimal totalExpense,

        BigDecimal balance

) {
}