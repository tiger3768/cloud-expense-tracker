package com.aditya.expensetracker.expense_tracker.exception;

public class OptimisticLockConflictException extends RuntimeException {

    private static final long serialVersionUID = 1L;

	public OptimisticLockConflictException() {
        super("This expense was changed by another request in the meantime. Please reload it and try again.");
    }
}
