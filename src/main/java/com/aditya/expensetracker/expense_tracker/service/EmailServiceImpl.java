package com.aditya.expensetracker.expense_tracker.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.aditya.expensetracker.expense_tracker.entity.User;
import com.aditya.expensetracker.expense_tracker.exception.EmailDeliveryException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Override
    public void sendVerificationEmail(User user, String token) {
    	try {

	        String verificationLink =
	                "http://localhost:8080/api/auth/verify?token=" + token;
	
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
    	}
    	catch (MailException ex) {

            throw new EmailDeliveryException(
                    "Failed to send verification email",
                    ex);
        }
    }

    @Override
    public void sendPasswordResetEmail(User user, String token) {
    	
    	try {

	        String resetLink =
	                "http://localhost:8080/api/auth/reset-password?token=" + token;
	
	        SimpleMailMessage message = new SimpleMailMessage();
	
	        message.setFrom(fromEmail);
	        message.setTo(user.getEmail());
	        message.setSubject("Reset your Expense Tracker password");
	        message.setText(
	                "Hello " + user.getName() + ",\n\n"
	                + "Click the link below to reset your password:\n\n"
	                + resetLink
	                + "\n\nThis link expires in 1 hour.");
	
	        mailSender.send(message);
    	}
    	catch (MailException ex) {

            throw new EmailDeliveryException(
                    "Failed to send password reset email",
                    ex);
        }
    }
}