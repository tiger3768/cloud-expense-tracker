package com.aditya.expensetracker.expense_tracker.repository;

import com.aditya.expensetracker.expense_tracker.entity.Expense;
import com.aditya.expensetracker.expense_tracker.entity.User;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.math.BigDecimal;
import java.util.Optional;

public interface ExpenseRepository
        extends JpaRepository<Expense, Long>,
        JpaSpecificationExecutor<Expense> {
	
	interface MonthlySummaryProjection {

	    Integer getMonth();

	    BigDecimal getIncome();

	    BigDecimal getExpense();

	}

    Page<Expense> findByUser(
            User user,
            Pageable pageable
    );

    Optional<Expense> findByIdAndUser(
            Long id,
            User user
    );
    
}