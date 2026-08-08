package com.aditya.expensetracker.expense_tracker.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.aditya.expensetracker.expense_tracker.entity.Expense;
import com.aditya.expensetracker.expense_tracker.repository.projection.CategorySummaryProjection;
import com.aditya.expensetracker.expense_tracker.repository.projection.DashboardSummaryProjection;
import com.aditya.expensetracker.expense_tracker.repository.projection.MonthlySummaryProjection;
import com.aditya.expensetracker.expense_tracker.repository.projection.RecentExpenseProjection;
import com.aditya.expensetracker.expense_tracker.repository.projection.TrendProjection;

public interface AnalyticsRepository extends JpaRepository<Expense, Long> {

    @Query("""
            SELECT
                COALESCE(
                    SUM(
                        CASE
                            WHEN e.type = 'INCOME'
                            THEN e.amount
                            ELSE 0
                        END
                    ),
                    0
                ) AS income,

                COALESCE(
                    SUM(
                        CASE
                            WHEN e.type = 'EXPENSE'
                            THEN e.amount
                            ELSE 0
                        END
                    ),
                    0
                ) AS expense

            FROM Expense e

            WHERE
                e.user.id = :userId
                AND e.expenseDate BETWEEN :from AND :to
                AND (:category IS NULL OR e.category = :category)
                AND (:type IS NULL OR e.type = :type)
            """)
    DashboardSummaryProjection getDashboardSummary(
            @Param("userId") Long userId,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to,
            @Param("category") String category,
            @Param("type") String type
    );

    @Query("""
            SELECT
                e.category AS category,
                SUM(e.amount) AS amount

            FROM Expense e

            WHERE
			    e.user.id = :userId
			    AND e.expenseDate BETWEEN :from AND :to
			    AND (:category IS NULL OR e.category = :category)
			    AND (:type IS NULL OR e.type = :type)

            GROUP BY e.category

            ORDER BY SUM(e.amount) DESC
            """)
    List<CategorySummaryProjection> getCategorySummary(
            @Param("userId") Long userId,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to,
            @Param("category") String category,
            @Param("type") String type
    );

    @Query(value = """
            SELECT
                CAST(DATE_TRUNC('month', expense_date) AS DATE) AS month,

                SUM(
                    CASE
                        WHEN type = 'INCOME'
                        THEN amount
                        ELSE 0
                    END
                ) AS income,

                SUM(
                    CASE
                        WHEN type = 'EXPENSE'
                        THEN amount
                        ELSE 0
                    END
                ) AS expense

            FROM expenses

            WHERE
                user_id = :userId
                AND deleted = false
                AND expense_date BETWEEN :from AND :to
                AND (:category IS NULL OR category = :category)
                AND (:type IS NULL OR type = :type)

            GROUP BY DATE_TRUNC('month', expense_date)

            ORDER BY month
            """, nativeQuery = true)
    List<MonthlySummaryProjection> getMonthlySummary(
            @Param("userId") Long userId,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to,
            @Param("category") String category,
            @Param("type") String type
    );

    @Query(value = """
            SELECT
                expense_date AS date,

                SUM(
                    CASE
                        WHEN type = 'INCOME'
                        THEN amount
                        ELSE 0
                    END
                ) AS income,

                SUM(
                    CASE
                        WHEN type = 'EXPENSE'
                        THEN amount
                        ELSE 0
                    END
                ) AS expense

            FROM expenses

            WHERE
                user_id = :userId
                AND deleted = false
                AND expense_date BETWEEN :from AND :to
                AND (:category IS NULL OR category = :category)
                AND (:type IS NULL OR type = :type)

            GROUP BY expense_date

            ORDER BY expense_date
            """, nativeQuery = true)
    List<TrendProjection> getTrend(
            @Param("userId") Long userId,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to,
            @Param("category") String category,
            @Param("type") String type
    );

    @Query("""
            SELECT
                e.id AS id,
                e.title AS title,
                e.category AS category,
                e.amount AS amount,
                e.expenseDate AS expenseDate,
                e.type AS type

            FROM Expense e

            WHERE
                e.user.id = :userId
                AND e.expenseDate BETWEEN :from AND :to
                AND (:category IS NULL OR e.category = :category)
                AND (:type IS NULL OR e.type = :type)

            ORDER BY e.expenseDate DESC
            """)
    List<RecentExpenseProjection> getRecentExpenses(
            @Param("userId") Long userId,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to,
            @Param("category") String category,
            @Param("type") String type,
            Pageable pageable
    );
}