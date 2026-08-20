package com.aditya.expensetracker.expense_tracker.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.aditya.expensetracker.expense_tracker.entity.User;
import com.aditya.expensetracker.expense_tracker.exception.EmailDeliveryException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.frontend-url}")
    private String baseUrl;

    @Override
    public void sendVerificationEmail(User user, String token) {
        try {

            String verificationLink =
                    normalizedBaseUrl() + "/verify-email?token=" + token;

            SimpleMailMessage message = new SimpleMailMessage();

            message.setFrom(fromEmail);
            message.setTo(user.getEmail());
            message.setSubject("Verify your Expense Tracker account");
            message.setText(
                    "Hello " + user.getName() + ",\n\n"
                    + "Please verify your email by clicking the link below:\n\n"
                    + verificationLink
                    + "\n\nThis link expires in 24 hours.");

            mailSender.send(message);

            log.info(
                    "Verification email delivered to {}",
                    user.getEmail()
            );

        } catch (MailException ex) {

            log.error(
                    "Failed to send verification email to {}",
                    user.getEmail(),
                    ex
            );

            throw new EmailDeliveryException(
                    "Failed to send verification email",
                    ex
            );
        }
    }

    @Override
    public void sendPasswordResetEmail(User user, String token) {
        try {

            String resetLink =
                    normalizedBaseUrl() + "/reset-password?token=" + token;

            SimpleMailMessage message = new SimpleMailMessage();

            message.setFrom(fromEmail);
            message.setTo(user.getEmail());
            message.setSubject("Expense Tracker password reset");
            message.setText(
                    "Hello " + user.getName() + ",\n\n"
                    + "Reset your password using the link below:\n\n"
                    + resetLink
                    + "\n\nThis link expires in 1 hour.");

            mailSender.send(message);

            log.info(
                    "Password reset email delivered to {}",
                    user.getEmail()
            );

        } catch (MailException ex) {

            log.error(
                    "Failed to send password reset email to {}",
                    user.getEmail(),
                    ex
            );

            throw new EmailDeliveryException(
                    "Failed to send password reset email",
                    ex
            );
        }
    }

    private String normalizedBaseUrl() {
        return baseUrl.endsWith("/")
                ? baseUrl.substring(0, baseUrl.length() - 1)
                : baseUrl;
    }
}
