package com.aditya.expensetracker.expense_tracker.exception;

public class InvalidPasswordResetTokenException
        extends RuntimeException {

    private static final long serialVersionUID = 1L;

	public InvalidPasswordResetTokenException(String message) {
        super(message);
    }
}