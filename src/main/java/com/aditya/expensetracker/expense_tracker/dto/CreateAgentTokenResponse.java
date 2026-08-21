package com.aditya.expensetracker.expense_tracker.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class CreateAgentTokenResponse {
    private Long id;
    private String name;
    private String token;
    private LocalDateTime expiresAt;
    private String warning;
}
