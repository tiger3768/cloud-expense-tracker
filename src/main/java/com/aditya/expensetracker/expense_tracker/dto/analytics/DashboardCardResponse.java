package com.aditya.expensetracker.expense_tracker.dto.analytics;

import java.math.BigDecimal;

public record DashboardCardResponse(

        BigDecimal income,

        BigDecimal expense,

        BigDecimal balance

) {
}