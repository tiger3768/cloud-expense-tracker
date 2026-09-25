package com.aditya.expensetracker.expense_tracker.dto;

public record AuthTokens(
        String accessToken,
        String refreshToken
) {
}
