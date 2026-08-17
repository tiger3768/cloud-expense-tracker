package com.aditya.expensetracker.expense_tracker.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import com.aditya.expensetracker.expense_tracker.entity.EmailVerificationToken;
import jakarta.persistence.LockModeType;

public interface EmailVerificationTokenRepository
        extends JpaRepository<EmailVerificationToken, Long> {

    Optional<EmailVerificationToken> findByToken(String token);

    Optional<EmailVerificationToken> findByUser(
            com.aditya.expensetracker.expense_tracker.entity.User user);

    void deleteByUser(
            com.aditya.expensetracker.expense_tracker.entity.User user);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query(
            "select t from EmailVerificationToken t where t.token = :token")
    Optional<EmailVerificationToken> findByTokenForUpdate(
            @org.springframework.data.repository.query.Param("token") String token);
}
