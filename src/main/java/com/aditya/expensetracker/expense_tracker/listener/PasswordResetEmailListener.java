package com.aditya.expensetracker.expense_tracker.listener;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.aditya.expensetracker.expense_tracker.config.AsyncExecutors;
import com.aditya.expensetracker.expense_tracker.entity.PasswordResetToken;
import com.aditya.expensetracker.expense_tracker.entity.User;
import com.aditya.expensetracker.expense_tracker.event.ForgotPasswordRequestedEvent;
import com.aditya.expensetracker.expense_tracker.exception.ResourceNotFoundException;
import com.aditya.expensetracker.expense_tracker.repository.PasswordResetTokenRepository;
import com.aditya.expensetracker.expense_tracker.repository.UserRepository;
import com.aditya.expensetracker.expense_tracker.service.EmailService;
import com.aditya.expensetracker.expense_tracker.service.PasswordResetTokenService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class PasswordResetEmailListener {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordResetTokenService passwordResetTokenService;
    private final EmailService emailService;

    @Async(AsyncExecutors.EMAIL)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleForgotPassword(ForgotPasswordRequestedEvent event) {

        User user = userRepository.findById(event.userId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        tokenRepository.findByUser(user)
                .ifPresent(passwordResetTokenService::deletePasswordResetToken);

        PasswordResetToken token =
                passwordResetTokenService.createPasswordResetToken(user);

        log.info(
                "Sending password reset email to {}",
                user.getEmail());

        emailService.sendPasswordResetEmail(
                user,
                token.getToken());

        log.info(
                "Password reset email sent to {}",
                user.getEmail());
    }
}