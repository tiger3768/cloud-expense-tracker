package com.aditya.expensetracker.expense_tracker.dto;

public record PasswordResetTokenValidationResponse(
        boolean valid,
        String message
) {
}
