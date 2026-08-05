package com.aditya.expensetracker.expense_tracker.dto.analytics;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TrendPointResponse(

        LocalDate date,

        BigDecimal income,

        BigDecimal expense

) {
}