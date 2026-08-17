package com.aditya.expensetracker.expense_tracker.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.aditya.expensetracker.expense_tracker.entity.PasswordResetToken;
import com.aditya.expensetracker.expense_tracker.entity.User;
import com.aditya.expensetracker.expense_tracker.exception.InvalidPasswordResetTokenException;
import com.aditya.expensetracker.expense_tracker.repository.PasswordResetTokenRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordResetTokenServiceImpl implements PasswordResetTokenService {
	
	private final PasswordResetTokenRepository passwordResetTokenRepository;

	@Value("${app.password-reset.expiration-hours}")
	private long passwordResetExpirationHours;

	@Override
	public PasswordResetToken createPasswordResetToken(User user) {

	    PasswordResetToken resetToken =
	            PasswordResetToken.builder()
	                    .token(UUID.randomUUID().toString())
	                    .expiresAt(
	                            LocalDateTime.now()
	                                    .plusHours(passwordResetExpirationHours))
	                    .user(user)
	                    .build();

	    return passwordResetTokenRepository.save(resetToken);
	}

	@Override
	public PasswordResetToken validatePasswordResetToken(String token) {

	    PasswordResetToken resetToken =
	            passwordResetTokenRepository.findByTokenForUpdate(token)
	                    .orElseThrow(() -> {
	                        log.warn("Password reset attempted with unknown token");
	                        return new InvalidPasswordResetTokenException(
	                                "Invalid password reset token");
	                    });

	    if (resetToken.getExpiresAt().isBefore(LocalDateTime.now())) {

	        log.warn(
	                "Password reset attempted with expired token for {}",
	                resetToken.getUser().getEmail());

	        throw new InvalidPasswordResetTokenException(
	                "Password reset token has expired");
	    }

	    return resetToken;
	}

	@Override
	public void deletePasswordResetToken(
	        PasswordResetToken token) {

	    passwordResetTokenRepository.delete(token);
	}
}