package com.aditya.expensetracker.expense_tracker.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.aditya.expensetracker.expense_tracker.entity.RefreshToken;

public interface RefreshTokenRepository
        extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

}