package com.aditya.expensetracker.expense_tracker.service;

import com.aditya.expensetracker.expense_tracker.entity.RefreshToken;
import com.aditya.expensetracker.expense_tracker.entity.User;

public interface RefreshTokenService {

    RefreshToken createRefreshToken(User user);
    RefreshToken validateRefreshToken(String token);
    void revokeRefreshToken(RefreshToken refreshToken);
}