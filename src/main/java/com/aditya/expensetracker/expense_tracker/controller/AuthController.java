package com.aditya.expensetracker.expense_tracker.controller;

import java.time.Duration;

import com.aditya.expensetracker.expense_tracker.dto.AuthResponse;
import com.aditya.expensetracker.expense_tracker.dto.AuthTokens;
import com.aditya.expensetracker.expense_tracker.dto.ForgotPasswordRequest;
import com.aditya.expensetracker.expense_tracker.dto.LoginRequest;
import com.aditya.expensetracker.expense_tracker.dto.RegisterRequest;
import com.aditya.expensetracker.expense_tracker.dto.ResetPasswordRequest;
import com.aditya.expensetracker.expense_tracker.exception.InvalidRefreshTokenException;
import com.aditya.expensetracker.expense_tracker.service.AuthService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Authentication APIs")
@RequiredArgsConstructor
public class AuthController {

    private static final String REFRESH_COOKIE_NAME = "refresh_token";

    private final AuthService authService;

    @Value("${app.refresh-token.expiration-days}")
    private long refreshTokenExpirationDays;

    @Value("${app.auth.refresh-cookie-secure:false}")
    private boolean refreshCookieSecure;

    @Value("${app.auth.refresh-cookie-same-site:Lax}")
    private String refreshCookieSameSite;

    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Register a new user")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "User registered successfully"),
        @ApiResponse(responseCode = "400", description = "Validation failed")
    })
    @PostMapping("/register")
    public void register(
            @Valid @RequestBody RegisterRequest request
    ) {
        authService.register(request);
    }

    @Operation(summary = "Verify email")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Email verified"),
        @ApiResponse(responseCode = "400", description = "Invalid verification token")
    })
    @GetMapping("/verify")
    public ResponseEntity<Void> verifyEmail(
            @RequestParam String token) {

        authService.verifyEmail(token);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Resend email verification")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Request accepted")
    })
    @PostMapping("/resend-verification")
    public ResponseEntity<Void> resendVerificationEmail(
            @Valid @RequestBody ForgotPasswordRequest request) {

        authService.resendVerificationEmail(request);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Login")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Login successful"),
        @ApiResponse(responseCode = "401", description = "Invalid credentials")
    })
    @PostMapping("/login")
    public AuthResponse login(
            @Valid @RequestBody LoginRequest request,
            HttpServletResponse response
    ) {
        AuthTokens tokens = authService.login(request);
        addRefreshCookie(response, tokens.refreshToken());
        return new AuthResponse(tokens.accessToken());
    }

    @Operation(summary = "Refresh access token")
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(
            @CookieValue(name = REFRESH_COOKIE_NAME, required = false)
            String refreshToken,
            HttpServletResponse response) {

        if (refreshToken == null || refreshToken.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }



        AuthTokens tokens = authService.refresh(refreshToken);
        addRefreshCookie(response, tokens.refreshToken());

        return ResponseEntity.ok(
                new AuthResponse(tokens.accessToken()));
    }

    @Operation(summary = "Send password reset email")
    @PostMapping("/forgot-password")
    public ResponseEntity<Void> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {

        authService.forgotPassword(request);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Reset password")
    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {

        authService.resetPassword(request);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Logout")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @CookieValue(name = REFRESH_COOKIE_NAME, required = false)
            String refreshToken,
            HttpServletResponse response) {

        clearRefreshCookie(response);

        if (refreshToken == null || refreshToken.isBlank()) {
            return ResponseEntity.noContent().build();
        }

        try {
            authService.logout(refreshToken);
        } catch (InvalidRefreshTokenException ignored) {
        }

        return ResponseEntity.noContent().build();
    }

    private void addRefreshCookie(
            HttpServletResponse response,
            String refreshToken) {

        ResponseCookie cookie = ResponseCookie.from(
                        REFRESH_COOKIE_NAME,
                        refreshToken)
                .httpOnly(true)
                .secure(refreshCookieSecure)
                .sameSite(refreshCookieSameSite)
                .path("/api/auth")
                .maxAge(Duration.ofDays(refreshTokenExpirationDays))
                .build();

        response.addHeader(
                HttpHeaders.SET_COOKIE,
                cookie.toString());
    }

    private void clearRefreshCookie(
            HttpServletResponse response) {

        ResponseCookie cookie = ResponseCookie.from(
                        REFRESH_COOKIE_NAME,
                        "")
                .httpOnly(true)
                .secure(refreshCookieSecure)
                .sameSite(refreshCookieSameSite)
                .path("/api/auth")
                .maxAge(Duration.ZERO)
                .build();

        response.addHeader(
                HttpHeaders.SET_COOKIE,
                cookie.toString());
    }
}
