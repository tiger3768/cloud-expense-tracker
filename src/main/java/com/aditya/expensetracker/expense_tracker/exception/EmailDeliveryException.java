package com.aditya.expensetracker.expense_tracker.exception;

public class EmailDeliveryException extends RuntimeException {

    private static final long serialVersionUID = 1L;

	public EmailDeliveryException(String message, Throwable cause) {
        super(message, cause);
    }
}