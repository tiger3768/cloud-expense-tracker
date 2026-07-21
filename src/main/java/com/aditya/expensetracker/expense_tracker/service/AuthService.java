package com.aditya.expensetracker.expense_tracker.service;

import java.time.LocalDateTime;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.aditya.expensetracker.expense_tracker.dto.AuthResponse;
import com.aditya.expensetracker.expense_tracker.dto.LoginRequest;
import com.aditya.expensetracker.expense_tracker.dto.RefreshTokenRequest;
import com.aditya.expensetracker.expense_tracker.dto.RegisterRequest;
import com.aditya.expensetracker.expense_tracker.entity.RefreshToken;
import com.aditya.expensetracker.expense_tracker.entity.Role;
import com.aditya.expensetracker.expense_tracker.entity.User;
import com.aditya.expensetracker.expense_tracker.exception.InvalidCredentialsException;
import com.aditya.expensetracker.expense_tracker.mapper.UserMapper;
import com.aditya.expensetracker.expense_tracker.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final JwtService jwtService;

    private final UserMapper userMapper;
    
    private final RefreshTokenService refreshTokenService;

    public void register(RegisterRequest request) {

        User user = userMapper.toEntity(request);

        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.USER);
        user.setCreatedAt(LocalDateTime.now());

        userRepository.save(user);
    }

    public AuthResponse login(LoginRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        boolean matches = passwordEncoder.matches(
                request.getPassword(),
                user.getPassword());

        if (!matches) {
        	throw new InvalidCredentialsException("Invalid email or password");
        }

        String accessToken = jwtService.generateToken(user);

        RefreshToken refreshToken =
                refreshTokenService.createRefreshToken(user);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .build();
    }
    
    public AuthResponse refresh(RefreshTokenRequest request) {

    	RefreshToken oldToken =
    	        refreshTokenService.validateRefreshToken(
    	                request.getRefreshToken());

    	User user = oldToken.getUser();

    	refreshTokenService.revokeRefreshToken(oldToken);

    	RefreshToken newRefreshToken =
    	        refreshTokenService.createRefreshToken(user);

    	String accessToken =
    	        jwtService.generateToken(user);

    	return AuthResponse.builder()
    	        .accessToken(accessToken)
    	        .refreshToken(newRefreshToken.getToken())
    	        .build();
    }
    
    public void logout(RefreshTokenRequest request) {

        RefreshToken refreshToken =
                refreshTokenService.validateRefreshToken(
                        request.getRefreshToken());

        refreshTokenService.revokeRefreshToken(refreshToken);
    }
}