package com.aditya.expensetracker.expense_tracker.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aditya.expensetracker.expense_tracker.dto.analytics.AnalyticsDashboardResponse;
import com.aditya.expensetracker.expense_tracker.dto.analytics.AnalyticsFilterRequest;
import com.aditya.expensetracker.expense_tracker.dto.analytics.CategorySummaryResponse;
import com.aditya.expensetracker.expense_tracker.dto.analytics.DashboardCardResponse;
import com.aditya.expensetracker.expense_tracker.dto.analytics.MonthlySummaryResponse;
import com.aditya.expensetracker.expense_tracker.dto.analytics.RecentExpenseResponse;
import com.aditya.expensetracker.expense_tracker.dto.analytics.SpendingTrendResponse;
import com.aditya.expensetracker.expense_tracker.dto.analytics.TrendPointResponse;
import com.aditya.expensetracker.expense_tracker.exception.InvalidAnalyticsRequestException;
import com.aditya.expensetracker.expense_tracker.repository.AnalyticsRepository;
import com.aditya.expensetracker.expense_tracker.repository.projection.CategorySummaryProjection;
import com.aditya.expensetracker.expense_tracker.repository.projection.DashboardSummaryProjection;
import com.aditya.expensetracker.expense_tracker.repository.projection.MonthlySummaryProjection;
import com.aditya.expensetracker.expense_tracker.repository.projection.RecentExpenseProjection;
import com.aditya.expensetracker.expense_tracker.repository.projection.TrendProjection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AnalyticsServiceImpl implements AnalyticsService {

    private static final int DEFAULT_LIMIT = 5;
    private static final int MAX_LIMIT = 50;

    private final AnalyticsRepository analyticsRepository;

    // Self-injected proxy so calls made from within this class (e.g. from
    // getDashboard) still go through the Spring AOP proxy and hit the
    // per-method @Cacheable caches instead of silently bypassing them.
    //
    // NOTE: this is deliberately field-injected via @Autowired, not added to
    // the Lombok-generated constructor. @Lazy on a constructor parameter only
    // works if it's present on the actual constructor, and Lombok's
    // @RequiredArgsConstructor does not copy @Lazy onto generated parameters
    // by default -- so putting `final AnalyticsService self` in the
    // constructor recreates the circular dependency this is meant to fix.
    @Autowired
    @Lazy
    private AnalyticsService self;

    @Override
    @Cacheable(
            value = "analytics-summary",
            key = "#userId + ':' + (#request == null ? 'default' : #request)"
    )
    public DashboardCardResponse getSummary(
            Long userId,
            AnalyticsFilterRequest request
    ) {

        validateRequest(request);

        LocalDate from = resolveFrom(request);
        LocalDate to = resolveTo(request);

        String category = normalizeCategory(request);
        String type = normalizeType(request);

        DashboardSummaryProjection projection =
                analyticsRepository.getDashboardSummary(
                        userId,
                        from,
                        to,
                        category,
                        type
                );

        BigDecimal income = BigDecimal.ZERO;
        BigDecimal expense = BigDecimal.ZERO;

        if (projection != null) {

            if (projection.getIncome() != null) {
                income = projection.getIncome();
            }

            if (projection.getExpense() != null) {
                expense = projection.getExpense();
            }

        }

        return new DashboardCardResponse(
                income,
                expense,
                income.subtract(expense)
        );
    }

    @Override
    @Cacheable(
            value = "analytics-categories",
            key = "#userId + ':' + (#request == null ? 'default' : #request)"
    )
    public List<CategorySummaryResponse> getCategorySummary(
            Long userId,
            AnalyticsFilterRequest request
    ) {

        validateRequest(request);

        LocalDate from = resolveFrom(request);
        LocalDate to = resolveTo(request);

        String category = normalizeCategory(request);
        String type = normalizeType(request);

        List<CategorySummaryProjection> rows =
                analyticsRepository.getCategorySummary(
                        userId,
                        from,
                        to,
                        category,
                        type
                );

        if (rows.isEmpty()) {
            return new ArrayList<>();
        }

        BigDecimal total = rows.stream()
                .map(CategorySummaryProjection::getAmount)
                .filter(amount -> amount != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return rows.stream()
                .map(row -> {

                    BigDecimal amount =
                            row.getAmount() == null
                                    ? BigDecimal.ZERO
                                    : row.getAmount();

                    double percentage = 0;

                    if (total.compareTo(BigDecimal.ZERO) > 0) {
                        percentage = amount
                                .multiply(BigDecimal.valueOf(100))
                                .divide(
                                        total,
                                        2,
                                        RoundingMode.HALF_UP)
                                .doubleValue();
                    }

                    return new CategorySummaryResponse(
                            row.getCategory(),
                            amount,
                            percentage);
                })
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    @Cacheable(
            value = "analytics-monthly",
            key = "#userId + ':' + (#request == null ? 'default' : #request)"
    )
    public List<MonthlySummaryResponse> getMonthlySummary(
            Long userId,
            AnalyticsFilterRequest request
    ) {

        validateRequest(request);

        LocalDate from = resolveFrom(request);
        LocalDate to = resolveTo(request);

        String category = normalizeCategory(request);
        String type = normalizeType(request);

        List<MonthlySummaryProjection> rows =
                analyticsRepository.getMonthlySummary(
                        userId,
                        from,
                        to,
                        category,
                        type
                );

        if (rows.isEmpty()) {
            return new ArrayList<>();
        }

        return rows.stream()
                .map(row -> {

                    BigDecimal income =
                            row.getIncome() == null
                                    ? BigDecimal.ZERO
                                    : row.getIncome();

                    BigDecimal expense =
                            row.getExpense() == null
                                    ? BigDecimal.ZERO
                                    : row.getExpense();

                    return new MonthlySummaryResponse(
                            YearMonth.from(row.getMonth()),
                            income,
                            expense,
                            income.subtract(expense)
                    );

                })
                .collect(Collectors.toCollection(ArrayList::new));
    }
    @Override
    @Cacheable(
            value = "analytics-trend",
            key = "#userId + ':' + (#request == null ? 'default' : #request)"
    )
    public SpendingTrendResponse getTrend(
            Long userId,
            AnalyticsFilterRequest request
    ) {

        validateRequest(request);

        LocalDate from = resolveFrom(request);
        LocalDate to = resolveTo(request);

        String category = normalizeCategory(request);
        String type = normalizeType(request);

        List<TrendProjection> rows =
                analyticsRepository.getTrend(
                        userId,
                        from,
                        to,
                        category,
                        type
                );

        if (rows.isEmpty()) {
            return new SpendingTrendResponse(
                    new ArrayList<>()
            );
        }

        List<TrendPointResponse> trend =
                rows.stream()
                        .map(row ->
                                new TrendPointResponse(
                                        row.getDate(),
                                        row.getIncome() == null
                                                ? BigDecimal.ZERO
                                                : row.getIncome(),
                                        row.getExpense() == null
                                                ? BigDecimal.ZERO
                                                : row.getExpense()
                                )
                        )
                        .collect(
                                Collectors.toCollection(
                                        ArrayList::new
                                )
                        );

        return new SpendingTrendResponse(trend);
    }

    @Override
    @Cacheable(
            value = "analytics-recent",
            key = "#userId + ':' + (#request == null ? 'default' : #request)"
    )
    public List<RecentExpenseResponse> getRecentExpenses(
            Long userId,
            AnalyticsFilterRequest request
    ) {

        validateRequest(request);

        LocalDate from = resolveFrom(request);
        LocalDate to = resolveTo(request);

        String category = normalizeCategory(request);
        String type = normalizeType(request);

        Pageable pageable =
                PageRequest.of(
                        0,
                        resolveLimit(request)
                );

        List<RecentExpenseProjection> rows =
                analyticsRepository.getRecentExpenses(
                        userId,
                        from,
                        to,
                        category,
                        type,
                        pageable
                );

        if (rows.isEmpty()) {
            return new ArrayList<>();
        }

        return rows.stream()
                .map(row ->
                        new RecentExpenseResponse(
                                row.getId(),
                                row.getTitle(),
                                row.getCategory(),
                                row.getAmount(),
                                row.getExpenseDate(),
                                row.getType()
                        )
                )
                .collect(
                        Collectors.toCollection(
                                ArrayList::new
                        )
                );
    }

    @Override
    @Cacheable(
            value = "analytics-dashboard",
            key = "#userId + ':' + (#request == null ? 'default' : #request)"
    )
    public AnalyticsDashboardResponse getDashboard(
            Long userId,
            AnalyticsFilterRequest request
    ) {

        validateRequest(request);

        DashboardCardResponse summary =
                self.getSummary(userId, request);

        List<CategorySummaryResponse> categories =
                self.getCategorySummary(userId, request);

        List<MonthlySummaryResponse> monthlySummary =
                self.getMonthlySummary(userId, request);

        SpendingTrendResponse trend =
                self.getTrend(userId, request);

        List<RecentExpenseResponse> recentExpenses =
                self.getRecentExpenses(userId, request);

        return new AnalyticsDashboardResponse(
                summary,
                categories,
                monthlySummary,
                trend,
                recentExpenses
        );
    }
    private void validateRequest(
            AnalyticsFilterRequest request
    ) {

        LocalDate from = resolveFrom(request);
        LocalDate to = resolveTo(request);

        if (from.isAfter(to)) {
            throw new InvalidAnalyticsRequestException(
                    "From date cannot be after To date."
            );
        }

        if (request != null
                && request.limit() != null
                && request.limit() <= 0) {

            throw new InvalidAnalyticsRequestException(
                    "Limit must be greater than zero."
            );
        }
    }

    private LocalDate resolveFrom(
            AnalyticsFilterRequest request
    ) {

        if (request == null || request.from() == null) {
            return LocalDate.now().withDayOfMonth(1);
        }

        return request.from();
    }

    private LocalDate resolveTo(
            AnalyticsFilterRequest request
    ) {

        if (request == null || request.to() == null) {
            return LocalDate.now();
        }

        return request.to();
    }

    private int resolveLimit(
            AnalyticsFilterRequest request
    ) {

        if (request == null || request.limit() == null) {
            return DEFAULT_LIMIT;
        }

        if (request.limit() <= 0) {
            return DEFAULT_LIMIT;
        }

        return Math.min(
                request.limit(),
                MAX_LIMIT
        );
    }
    private String normalizeCategory(
            AnalyticsFilterRequest request
    ) {

        if (request == null || request.category() == null) {
            return null;
        }

        String value = request.category().trim();

        return value.isBlank()
                ? null
                : value.toUpperCase();
    }

    private String normalizeType(
            AnalyticsFilterRequest request
    ) {

        if (request == null || request.type() == null) {
            return null;
        }

        String value = request.type().trim();

        if (value.isBlank()) {
            return null;
        }

        return value.toUpperCase();
    }
}