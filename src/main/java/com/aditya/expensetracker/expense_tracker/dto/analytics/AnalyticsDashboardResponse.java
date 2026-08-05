package com.aditya.expensetracker.expense_tracker.dto.analytics;

import java.util.List;

public record AnalyticsDashboardResponse(

        DashboardCardResponse summary,

        List<CategorySummaryResponse> categories,

        List<MonthlySummaryResponse> monthlySummary,

        SpendingTrendResponse trend,

        List<RecentExpenseResponse> recentExpenses

) {
}