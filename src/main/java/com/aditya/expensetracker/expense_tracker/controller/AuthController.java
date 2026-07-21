package com.aditya.expensetracker.expense_tracker.controller;

import com.aditya.expensetracker.expense_tracker.dto.AuthResponse;
import com.aditya.expensetracker.expense_tracker.dto.LoginRequest;
import com.aditya.expensetracker.expense_tracker.dto.RefreshTokenRequest;
import com.aditya.expensetracker.expense_tracker.dto.RegisterRequest;
import com.aditya.expensetracker.expense_tracker.service.AuthService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor

public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)

    public void register(@RequestBody RegisterRequest request) {
        authService.register(request);
    }
    
    @PostMapping("/login")

    public AuthResponse login(@RequestBody LoginRequest request) {
        return authService.login(request);
    }
    
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(
            @Valid @RequestBody RefreshTokenRequest request) {

        return ResponseEntity.ok(
                authService.refresh(request));
    }
    
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @Valid @RequestBody RefreshTokenRequest request) {

        authService.logout(request);

        return ResponseEntity.noContent().build();
    }
}