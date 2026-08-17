package com.aditya.expensetracker.expense_tracker.listener;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.aditya.expensetracker.expense_tracker.config.AsyncExecutors;
import com.aditya.expensetracker.expense_tracker.entity.EmailVerificationToken;
import com.aditya.expensetracker.expense_tracker.entity.User;
import com.aditya.expensetracker.expense_tracker.event.ResendVerificationRequestedEvent;
import com.aditya.expensetracker.expense_tracker.event.UserRegisteredEvent;
import com.aditya.expensetracker.expense_tracker.exception.ResourceNotFoundException;
import com.aditya.expensetracker.expense_tracker.repository.EmailVerificationTokenRepository;
import com.aditya.expensetracker.expense_tracker.repository.UserRepository;
import com.aditya.expensetracker.expense_tracker.service.EmailService;
import com.aditya.expensetracker.expense_tracker.service.EmailVerificationTokenService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class VerificationEmailListener {

    private final UserRepository userRepository;
    private final EmailVerificationTokenRepository tokenRepository;
    private final EmailVerificationTokenService emailVerificationTokenService;
    private final EmailService emailService;

    @Async(AsyncExecutors.EMAIL)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleUserRegistered(UserRegisteredEvent event) {
        sendVerificationEmail(event.userId(), "registration");
    }

    @Async(AsyncExecutors.EMAIL)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleResendVerification(
            ResendVerificationRequestedEvent event) {

        sendVerificationEmail(event.userId(), "resend");
    }

    private void sendVerificationEmail(Long userId, String reason) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        if (user.isEnabled()) {

            log.info(
                    "Skipping verification email for already verified user {}",
                    user.getEmail());

            return;
        }

        tokenRepository.findByUser(user)
                .ifPresent(
                        emailVerificationTokenService::deleteVerificationToken);

        EmailVerificationToken token =
                emailVerificationTokenService.createVerificationToken(user);

        log.info(
                "Sending verification email to {} (reason={})",
                user.getEmail(),
                reason);

        emailService.sendVerificationEmail(
                user,
                token.getToken());

        log.info(
                "Verification email sent to {} (reason={})",
                user.getEmail(),
                reason);
    }
}