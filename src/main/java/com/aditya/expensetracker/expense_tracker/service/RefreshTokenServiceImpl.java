package com.aditya.expensetracker.expense_tracker.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.aditya.expensetracker.expense_tracker.entity.RefreshToken;
import com.aditya.expensetracker.expense_tracker.entity.User;
import com.aditya.expensetracker.expense_tracker.exception.InvalidRefreshTokenException;
import com.aditya.expensetracker.expense_tracker.repository.RefreshTokenRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    
    @Value("${app.refresh-token.expiration-days}")
    private long refreshTokenExpirationDays;

    @Override
    public RefreshToken createRefreshToken(User user) {

        RefreshToken refreshToken = RefreshToken.builder()
                .token(UUID.randomUUID().toString())
                .expiresAt(LocalDateTime.now().plusDays(refreshTokenExpirationDays))
                .revoked(false)
                .user(user)
                .build();

        return refreshTokenRepository.save(refreshToken);
    }
    
    @Override
    public RefreshToken validateRefreshToken(String token) {

        RefreshToken refreshToken = refreshTokenRepository
                .findByToken(token)
                .orElseThrow(() ->
                	new InvalidRefreshTokenException(
                        "Invalid refresh token"));

        if (refreshToken.isRevoked()) {
            throw new InvalidRefreshTokenException(
                    "Refresh token has been revoked");
        }

        if (refreshToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new InvalidRefreshTokenException(
                    "Refresh token has expired");
        }

        return refreshToken;
    }
    
    @Override
    public void revokeRefreshToken(RefreshToken refreshToken) {

    	if (!refreshToken.isRevoked()) {
    	    refreshToken.setRevoked(true);
    	    refreshTokenRepository.save(refreshToken);
    	}
    }
}