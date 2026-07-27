package com.aditya.expensetracker.expense_tracker.event;

public record ForgotPasswordRequestedEvent(
        Long userId
) {
}