package com.aditya.expensetracker.expense_tracker.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ValidationField {
    private String message;
    private boolean required;
    private List<String> allowedValues;
}
