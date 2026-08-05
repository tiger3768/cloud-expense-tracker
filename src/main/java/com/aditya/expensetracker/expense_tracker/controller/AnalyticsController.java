package com.aditya.expensetracker.expense_tracker.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aditya.expensetracker.expense_tracker.dto.analytics.AnalyticsDashboardResponse;
import com.aditya.expensetracker.expense_tracker.dto.analytics.AnalyticsFilterRequest;
import com.aditya.expensetracker.expense_tracker.dto.analytics.CategorySummaryResponse;
import com.aditya.expensetracker.expense_tracker.dto.analytics.DashboardCardResponse;
import com.aditya.expensetracker.expense_tracker.dto.analytics.MonthlySummaryResponse;
import com.aditya.expensetracker.expense_tracker.dto.analytics.RecentExpenseResponse;
import com.aditya.expensetracker.expense_tracker.dto.analytics.SpendingTrendResponse;
import com.aditya.expensetracker.expense_tracker.entity.User;
import com.aditya.expensetracker.expense_tracker.service.AnalyticsService;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
@Validated
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @Operation(
            summary = "Analytics Dashboard",
            description = "Returns the complete analytics dashboard."
    )
    @GetMapping("/dashboard")
    public ResponseEntity<AnalyticsDashboardResponse> getDashboard(
            @Valid AnalyticsFilterRequest request
    ) {

        return ResponseEntity.ok(
                analyticsService.getDashboard(
                        getCurrentUserId(),
                        request
                )
        );

    }

    @Operation(
            summary = "Summary",
            description = "Returns total income, expense and balance."
    )
    @GetMapping("/summary")
    public ResponseEntity<DashboardCardResponse> getSummary(
            @Valid AnalyticsFilterRequest request
    ) {

        return ResponseEntity.ok(
                analyticsService.getSummary(
                        getCurrentUserId(),
                        request
                )
        );

    }

    @Operation(
            summary = "Category Summary",
            description = "Returns expense grouped by category."
    )
    @GetMapping("/categories")
    public ResponseEntity<List<CategorySummaryResponse>> getCategorySummary(
            @Valid AnalyticsFilterRequest request
    ) {

        return ResponseEntity.ok(
                analyticsService.getCategorySummary(
                        getCurrentUserId(),
                        request
                )
        );

    }

    @Operation(
            summary = "Monthly Summary",
            description = "Returns monthly analytics."
    )
    @GetMapping("/monthly")
    public ResponseEntity<List<MonthlySummaryResponse>> getMonthlySummary(
            @Valid AnalyticsFilterRequest request
    ) {

        return ResponseEntity.ok(
                analyticsService.getMonthlySummary(
                        getCurrentUserId(),
                        request
                )
        );

    }

    @Operation(
            summary = "Trend",
            description = "Returns income and expense trend."
    )
    @GetMapping("/trend")
    public ResponseEntity<SpendingTrendResponse> getTrend(
            @Valid AnalyticsFilterRequest request
    ) {

        return ResponseEntity.ok(
                analyticsService.getTrend(
                        getCurrentUserId(),
                        request
                )
        );

    }

    @Operation(
            summary = "Recent Transactions",
            description = "Returns recent transactions."
    )
    @GetMapping("/recent")
    public ResponseEntity<List<RecentExpenseResponse>> getRecentExpenses(
            @Valid AnalyticsFilterRequest request
    ) {

        return ResponseEntity.ok(
                analyticsService.getRecentExpenses(
                        getCurrentUserId(),
                        request
                )
        );

    }

    private Long getCurrentUserId() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        User user = (User) authentication.getPrincipal();

        return user.getId();

    }

}