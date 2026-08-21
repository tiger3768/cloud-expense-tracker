package com.aditya.expensetracker.expense_tracker.repository;

import com.aditya.expensetracker.expense_tracker.entity.AgentApiToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface AgentApiTokenRepository extends JpaRepository<AgentApiToken, Long> {
    @Query("""
            select t from AgentApiToken t
            join fetch t.user
            where t.tokenHash = :tokenHash
              and t.revoked = false
              and t.expiresAt > :now
            """)
    Optional<AgentApiToken> findActive(
            @Param("tokenHash") String tokenHash,
            @Param("now") LocalDateTime now);
}
