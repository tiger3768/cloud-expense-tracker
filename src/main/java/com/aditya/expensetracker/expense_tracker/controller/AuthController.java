package com.aditya.expensetracker.expense_tracker.controller;

import com.aditya.expensetracker.expense_tracker.dto.AuthResponse;
import com.aditya.expensetracker.expense_tracker.dto.ForgotPasswordRequest;
import com.aditya.expensetracker.expense_tracker.dto.LoginRequest;
import com.aditya.expensetracker.expense_tracker.dto.RefreshTokenRequest;
import com.aditya.expensetracker.expense_tracker.dto.RegisterRequest;
import com.aditya.expensetracker.expense_tracker.dto.ResetPasswordRequest;
import com.aditya.expensetracker.expense_tracker.service.AuthService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Authentication APIs")
@RequiredArgsConstructor

public class AuthController {

    private final AuthService authService;

    
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
            @Valid @RequestBody LoginRequest request
    ) {
        return authService.login(request);
    }
    
    @Operation(summary = "Refresh access token")
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(
            @Valid @RequestBody RefreshTokenRequest request) {

        return ResponseEntity.ok(
                authService.refresh(request));
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
            @Valid @RequestBody RefreshTokenRequest request) {

        authService.logout(request);

        return ResponseEntity.noContent().build();
    }
}