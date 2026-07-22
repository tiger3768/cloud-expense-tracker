package com.aditya.expensetracker.expense_tracker.service;

import com.aditya.expensetracker.expense_tracker.entity.EmailVerificationToken;
import com.aditya.expensetracker.expense_tracker.entity.User;

public interface EmailVerificationTokenService {
	EmailVerificationToken createVerificationToken(User user);

	EmailVerificationToken validateVerificationToken(String token);

	void deleteVerificationToken(EmailVerificationToken token);
}
