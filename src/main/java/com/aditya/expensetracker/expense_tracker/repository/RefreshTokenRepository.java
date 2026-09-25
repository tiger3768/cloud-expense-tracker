package com.aditya.expensetracker.expense_tracker.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.aditya.expensetracker.expense_tracker.entity.RefreshToken;
import com.aditya.expensetracker.expense_tracker.entity.User;

public interface RefreshTokenRepository
        extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);
    
    List<RefreshToken> findByUser(User user);
    
    @Modifying
    @Query("""
        UPDATE RefreshToken r
        SET r.revoked = true
        WHERE r.token = :token
          AND r.revoked = false
    """)
    int revokeIfActive(@Param("token") String token);

}