package com.aditya.expensetracker.expense_tracker.service;

import java.time.LocalDateTime;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aditya.expensetracker.expense_tracker.dto.AuthResponse;
import com.aditya.expensetracker.expense_tracker.dto.ForgotPasswordRequest;
import com.aditya.expensetracker.expense_tracker.dto.LoginRequest;
import com.aditya.expensetracker.expense_tracker.dto.RefreshTokenRequest;
import com.aditya.expensetracker.expense_tracker.dto.RegisterRequest;
import com.aditya.expensetracker.expense_tracker.dto.ResetPasswordRequest;
import com.aditya.expensetracker.expense_tracker.entity.EmailVerificationToken;
import com.aditya.expensetracker.expense_tracker.entity.PasswordResetToken;
import com.aditya.expensetracker.expense_tracker.entity.RefreshToken;
import com.aditya.expensetracker.expense_tracker.entity.Role;
import com.aditya.expensetracker.expense_tracker.entity.User;
import com.aditya.expensetracker.expense_tracker.exception.DuplicateEmailException;
import com.aditya.expensetracker.expense_tracker.exception.InvalidCredentialsException;
import com.aditya.expensetracker.expense_tracker.mapper.UserMapper;
import com.aditya.expensetracker.expense_tracker.repository.PasswordResetTokenRepository;
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
    
    private final EmailVerificationTokenService emailVerificationTokenService;
    
    private final PasswordResetTokenService passwordResetTokenService;
    
    private final PasswordResetTokenRepository passwordResetTokenRepository;

    public void register(RegisterRequest request) {

        User user = userMapper.toEntity(request);

        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.USER);
        user.setCreatedAt(LocalDateTime.now());
        user.setEnabled(false);
        
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new DuplicateEmailException("Email already exists");
        }

        userRepository.save(user);

        EmailVerificationToken verificationToken =
                emailVerificationTokenService.createVerificationToken(user);

        System.out.println(
                "Verification Link: http://localhost:8080/api/auth/verify?token="
                        + verificationToken.getToken());
    }
    
    public void verifyEmail(String token) {

        EmailVerificationToken verificationToken =
                emailVerificationTokenService
                        .validateVerificationToken(token);

        User user = verificationToken.getUser();

        user.setEnabled(true);

        userRepository.save(user);

        emailVerificationTokenService
                .deleteVerificationToken(verificationToken);
    }

    public AuthResponse login(LoginRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        boolean matches = passwordEncoder.matches(
                request.getPassword(),
                user.getPassword());
        
        if (!user.isEnabled()) {
            throw new InvalidCredentialsException(
                    "Please verify your email before logging in.");
        }

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
    
    public void forgotPassword(ForgotPasswordRequest request) {

        userRepository.findByEmail(request.getEmail())
                .ifPresent(user -> {

                    passwordResetTokenRepository
                            .findByUser(user)
                            .ifPresent(
                                    passwordResetTokenService::deletePasswordResetToken);

                    PasswordResetToken resetToken =
                            passwordResetTokenService
                                    .createPasswordResetToken(user);

                    System.out.println(
                            "Reset Password Link: "
                            + "http://localhost:8080/api/auth/reset-password?token="
                            + resetToken.getToken());
                });
    }
    
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {

        PasswordResetToken resetToken =
                passwordResetTokenService
                        .validatePasswordResetToken(request.getToken());

        User user = resetToken.getUser();

        user.setPassword(
                passwordEncoder.encode(request.getNewPassword()));

        userRepository.save(user);

        passwordResetTokenService
                .deletePasswordResetToken(resetToken);

        refreshTokenService.revokeAllRefreshTokens(user);
    }
    
    public void logout(RefreshTokenRequest request) {

        RefreshToken refreshToken =
                refreshTokenService.validateRefreshToken(
                        request.getRefreshToken());

        refreshTokenService.revokeRefreshToken(refreshToken);
    }
}