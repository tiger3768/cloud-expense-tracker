package com.aditya.expensetracker.expense_tracker.specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.aditya.expensetracker.expense_tracker.entity.Category;
import com.aditya.expensetracker.expense_tracker.entity.Expense;
import com.aditya.expensetracker.expense_tracker.entity.User;

public class ExpenseSpecification {

    public static Specification<Expense> belongsToUser(User user) {

        return (root, query, cb) ->
                cb.equal(root.get("user"), user);
    }
    
    public static Specification<Expense> hasCategories(List<Category> categories) {

        return (root, query, cb) ->
                root.get("category").in(categories);
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