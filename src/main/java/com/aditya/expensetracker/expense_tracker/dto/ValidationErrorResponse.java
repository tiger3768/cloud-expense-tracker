package com.aditya.expensetracker.expense_tracker.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
public class ValidationErrorResponse {
    private LocalDateTime timestamp;
    private int status;

    // Kept for backward compatibility with the existing React client.
    private Map<String, String> errors;

    // Machine-readable contract for external agents/integrations.
    private String type;
    private String message;
    private Map<String, ValidationField> fields;
}
