package com.aditya.expensetracker.expense_tracker.repository.projection;

import java.math.BigDecimal;

public interface CategorySummaryProjection {

    String getCategory();

    BigDecimal getAmount();

}