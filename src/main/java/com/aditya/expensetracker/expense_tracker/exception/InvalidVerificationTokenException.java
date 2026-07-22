package com.aditya.expensetracker.expense_tracker.exception;

public class InvalidVerificationTokenException
        extends RuntimeException {

    private static final long serialVersionUID = 1L;

	public InvalidVerificationTokenException(String message) {
        super(message);
    }
}