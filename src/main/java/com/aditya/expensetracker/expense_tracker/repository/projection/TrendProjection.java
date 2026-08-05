package com.aditya.expensetracker.expense_tracker.repository.projection;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface TrendProjection {

    LocalDate getDate();

    BigDecimal getIncome();

    BigDecimal getExpense();

}