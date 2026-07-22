package com.aditya.expensetracker.expense_tracker.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.aditya.expensetracker.expense_tracker.entity.EmailVerificationToken;
import com.aditya.expensetracker.expense_tracker.entity.User;
import com.aditya.expensetracker.expense_tracker.exception.InvalidVerificationTokenException;
import com.aditya.expensetracker.expense_tracker.repository.EmailVerificationTokenRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmailVerificationTokenServiceImpl
        implements EmailVerificationTokenService {

    private final EmailVerificationTokenRepository verificationTokenRepository;

    @Value("${app.email-verification.expiration-hours}")
    private long verificationExpirationHours;

    @Override
    public EmailVerificationToken createVerificationToken(User user) {

        EmailVerificationToken verificationToken =
                EmailVerificationToken.builder()
                        .token(UUID.randomUUID().toString())
                        .expiresAt(LocalDateTime.now()
                                .plusHours(verificationExpirationHours))
                        .user(user)
                        .build();

        return verificationTokenRepository.save(verificationToken);
    }

    @Override
    public EmailVerificationToken validateVerificationToken(String token) {

        EmailVerificationToken verificationToken =
                verificationTokenRepository.findByToken(token)
                        .orElseThrow(() ->
                                new InvalidVerificationTokenException(
                                        "Invalid verification token"));

        if (verificationToken.getExpiresAt()
                .isBefore(LocalDateTime.now())) {

            throw new InvalidVerificationTokenException(
                    "Verification token has expired");
        }

        return verificationToken;
    }

    @Override
    public void deleteVerificationToken(
            EmailVerificationToken verificationToken) {

        verificationTokenRepository.delete(verificationToken);
    }
}