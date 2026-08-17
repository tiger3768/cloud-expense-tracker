package com.aditya.expensetracker.expense_tracker.exception;

public class OptimisticLockConflictException extends RuntimeException {

    public OptimisticLockConflictException() {
        super("This expense was changed by another request in the meantime. Please reload it and try again.");
    }
}
