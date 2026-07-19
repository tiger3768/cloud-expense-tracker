package com.aditya.expensetracker.expense_tracker.specification;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.springframework.data.jpa.domain.Specification;

import com.aditya.expensetracker.expense_tracker.entity.Category;
import com.aditya.expensetracker.expense_tracker.entity.Expense;
import com.aditya.expensetracker.expense_tracker.entity.User;

public class ExpenseSpecification {

    public static Specification<Expense> belongsToUser(User user) {

        return (root, query, cb) ->
                cb.equal(root.get("user"), user);
    }
    
    public static Specification<Expense> hasCategory(Category category) {

        return (root, query, cb) ->
                cb.equal(root.get("category"), category);
    }
    
    public static Specification<Expense> minAmount(BigDecimal amount) {

        return (root, query, cb) ->
                cb.greaterThanOrEqualTo(
                        root.get("amount"),
                        amount
                );
    }
    
    public static Specification<Expense> maxAmount(BigDecimal amount) {

        return (root, query, cb) ->
                cb.lessThanOrEqualTo(
                        root.get("amount"),
                        amount
                );
    }
    
    public static Specification<Expense> startDate(LocalDate startDate) {

        return (root, query, cb) ->
                cb.greaterThanOrEqualTo(
                        root.get("expenseDate"),
                        startDate
                );
    }
    
    public static Specification<Expense> endDate(LocalDate endDate) {

        return (root, query, cb) ->
                cb.lessThanOrEqualTo(
                        root.get("expenseDate"),
                        endDate
                );
    }
}