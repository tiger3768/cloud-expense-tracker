package com.aditya.expensetracker.expense_tracker.service;

import com.aditya.expensetracker.expense_tracker.entity.User;

public interface EmailService {

    void sendVerificationEmail(User user, String token);

    void sendPasswordResetEmail(User user, String token);

}