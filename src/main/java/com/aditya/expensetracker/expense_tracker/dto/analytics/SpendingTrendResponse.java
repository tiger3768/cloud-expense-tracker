package com.aditya.expensetracker.expense_tracker.dto.analytics;

import java.util.List;

public record SpendingTrendResponse(

        List<TrendPointResponse> trend

) {
}