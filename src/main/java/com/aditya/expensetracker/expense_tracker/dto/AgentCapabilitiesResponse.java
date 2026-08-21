package com.aditya.expensetracker.expense_tracker.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class AgentCapabilitiesResponse {
    private String apiVersion;
    private String description;
    private String authentication;
    private String validationContract;
    private String idempotency;
    private List<String> transactionTypes;
    private List<String> categories;
    private List<Map<String, String>> operations;
}
