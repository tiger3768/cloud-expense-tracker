package com.aditya.expensetracker.expense_tracker.service;

import com.aditya.expensetracker.expense_tracker.dto.analytics.*;

public interface AnalyticsService {

    AnalyticsDashboardResponse getDashboard(
            Long userId,
            AnalyticsFilterRequest request
    );

    DashboardCardResponse getSummary(
            Long userId,
            AnalyticsFilterRequest request
    );

    SpendingTrendResponse getTrend(
            Long userId,
            AnalyticsFilterRequest request
    );

    java.util.List<CategorySummaryResponse> getCategorySummary(
            Long userId,
            AnalyticsFilterRequest request
    );

    java.util.List<MonthlySummaryResponse> getMonthlySummary(
            Long userId,
            AnalyticsFilterRequest request
    );

    java.util.List<RecentExpenseResponse> getRecentExpenses(
            Long userId,
            AnalyticsFilterRequest request
    );

}