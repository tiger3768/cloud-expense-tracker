package com.aditya.expensetracker.expense_tracker.exception;
public class InvalidAnalyticsRequestException
        extends RuntimeException {

    private static final long serialVersionUID = 1L;

	public InvalidAnalyticsRequestException(String message) {
        super(message);
    }

}