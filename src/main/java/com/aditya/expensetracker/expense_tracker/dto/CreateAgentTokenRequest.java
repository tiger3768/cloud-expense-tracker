package com.aditya.expensetracker.expense_tracker.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateAgentTokenRequest {
    @NotBlank(message = "Token name is required")
    @Size(max = 100, message = "Token name cannot exceed 100 characters")
    private String name;

    @Min(value = 1, message = "Expiration must be at least 1 day")
    @Max(value = 365, message = "Expiration cannot exceed 365 days")
    private int expirationDays = 30;
}
