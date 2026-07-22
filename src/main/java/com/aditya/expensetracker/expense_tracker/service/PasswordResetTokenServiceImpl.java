package com.aditya.expensetracker.expense_tracker.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.aditya.expensetracker.expense_tracker.entity.PasswordResetToken;
import com.aditya.expensetracker.expense_tracker.entity.RefreshToken;
import com.aditya.expensetracker.expense_tracker.entity.User;
import com.aditya.expensetracker.expense_tracker.exception.InvalidPasswordResetTokenException;
import com.aditya.expensetracker.expense_tracker.repository.PasswordResetTokenRepository;
import com.aditya.expensetracker.expense_tracker.repository.RefreshTokenRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PasswordResetTokenServiceImpl implements PasswordResetTokenService {
	
	private final PasswordResetTokenRepository passwordResetTokenRepository;
	
	private final RefreshTokenRepository refreshTokenRepository;

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
	            passwordResetTokenRepository.findByToken(token)
	                    .orElseThrow(() ->
	                            new InvalidPasswordResetTokenException(
	                                    "Invalid password reset token"));

	    if (resetToken.getExpiresAt().isBefore(LocalDateTime.now())) {

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
	
	@Override
	public void revokeAllRefreshTokens(User user) {

	    List<RefreshToken> refreshTokens =
	            refreshTokenRepository.findByUser(user);

	    refreshTokens.forEach(token -> token.setRevoked(true));

	    refreshTokenRepository.saveAll(refreshTokens);
	}

}
