package com.aditya.expensetracker.expense_tracker.repository.projection;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface RecentExpenseProjection {

    Long getId();

    String getTitle();

    String getCategory();

    BigDecimal getAmount();

    LocalDate getExpenseDate();

    String getType();

}