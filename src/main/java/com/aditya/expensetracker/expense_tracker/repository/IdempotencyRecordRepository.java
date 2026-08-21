package com.aditya.expensetracker.expense_tracker.repository;

import com.aditya.expensetracker.expense_tracker.entity.IdempotencyRecord;
import com.aditya.expensetracker.expense_tracker.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface IdempotencyRecordRepository extends JpaRepository<IdempotencyRecord, Long> {

    Optional<IdempotencyRecord> findByUserAndIdempotencyKey(User user, String idempotencyKey);

    @Modifying
    @Query(value = """
            INSERT INTO idempotency_records
                (user_id, idempotency_key, request_hash, created_at)
            VALUES
                (:userId, :key, :requestHash, :createdAt)
            ON CONFLICT (user_id, idempotency_key) DO NOTHING
            """, nativeQuery = true)
    int reserve(
            @Param("userId") Long userId,
            @Param("key") String key,
            @Param("requestHash") String requestHash,
            @Param("createdAt") LocalDateTime createdAt);
}
