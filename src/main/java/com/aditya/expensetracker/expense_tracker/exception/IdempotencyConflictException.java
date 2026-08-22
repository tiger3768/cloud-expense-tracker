package com.aditya.expensetracker.expense_tracker.exception;

public class IdempotencyConflictException extends RuntimeException {
    private static final long serialVersionUID = 1L;

	public IdempotencyConflictException(String message) {
        super(message);
    }
}
