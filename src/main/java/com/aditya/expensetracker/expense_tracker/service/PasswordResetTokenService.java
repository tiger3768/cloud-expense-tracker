package com.aditya.expensetracker.expense_tracker.service;

import com.aditya.expensetracker.expense_tracker.entity.PasswordResetToken;
import com.aditya.expensetracker.expense_tracker.entity.User;

public interface PasswordResetTokenService {
	PasswordResetToken createPasswordResetToken(User user);

	PasswordResetToken validatePasswordResetToken(String token);

	void deletePasswordResetToken(PasswordResetToken token);
}
