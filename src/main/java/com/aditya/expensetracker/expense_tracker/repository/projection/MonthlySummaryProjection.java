package com.aditya.expensetracker.expense_tracker.repository.projection;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface MonthlySummaryProjection {

    LocalDate getMonth();

    BigDecimal getIncome();

    BigDecimal getExpense();

}