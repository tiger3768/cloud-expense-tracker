package com.aditya.expensetracker.expense_tracker.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.aditya.expensetracker.expense_tracker.entity.PasswordResetToken;
import com.aditya.expensetracker.expense_tracker.entity.User;

import jakarta.persistence.LockModeType;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findByToken(String token);
    Optional<PasswordResetToken> findByUser(User user);

    void deleteByUser(User user);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from PasswordResetToken t where t.token = :token")
    Optional<PasswordResetToken> findByTokenForUpdate(@Param("token") String token);
}
