package com.aditya.expensetracker.expense_tracker.controller;

import com.aditya.expensetracker.expense_tracker.dto.AgentCapabilitiesResponse;
import com.aditya.expensetracker.expense_tracker.entity.Category;
import com.aditya.expensetracker.expense_tracker.entity.ExpenseType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@Tag(name = "Agent Discovery", description = "Public metadata describing how external agents can consume the API")
public class AgentCapabilitiesController {

    @Operation(
            summary = "Discover agent-consumable capabilities",
            description = "Returns stable metadata for external AI agents and integrations. "
                    + "No user data is returned. User operations still require JWT authentication.")
    @GetMapping("/api/agent/capabilities")
    public AgentCapabilitiesResponse capabilities() {

        List<Map<String, String>> operations = List.of(
                Map.of(
                        "method", "POST",
                        "path", "/api/agent-tokens",
                        "description", "Create a user-managed, expiring X-API-Key for an external agent. "
                                + "Requires a normal user JWT and returns the plaintext key once."),
                Map.of(
                        "method", "DELETE",
                        "path", "/api/agent-tokens/{id}",
                        "description", "Revoke a user-owned agent API key."),
                Map.of(
                        "method", "POST",
                        "path", "/api/expenses",
                        "description", "Create an expense or income transaction using JSON. "
                                + "Validation errors include missing fields and allowed enum values. "
                                + "Use Idempotency-Key for safe retries."),
                Map.of(
                        "method", "GET",
                        "path", "/api/expenses",
                        "description", "List the authenticated user's transactions with filtering and pagination."),
                Map.of(
                        "method", "GET",
                        "path", "/api/expenses/{id}",
                        "description", "Get one transaction belonging to the authenticated user."),
                Map.of(
                        "method", "PUT",
                        "path", "/api/expenses/{id}",
                        "description", "Update a transaction using JSON. Supply the current version for optimistic locking "
                                + "and Idempotency-Key for safe retries."),
                Map.of(
                        "method", "DELETE",
                        "path", "/api/expenses/{id}",
                        "description", "Soft-delete a transaction belonging to the authenticated user. "
                                + "Agents should obtain user confirmation before invoking destructive operations."),
                Map.of(
                        "method", "GET",
                        "path", "/api/analytics/dashboard",
                        "description", "Get the authenticated user's complete analytics dashboard."),
                Map.of(
                        "method", "GET",
                        "path", "/api/analytics/summary",
                        "description", "Get income, expense, and balance totals."),
                Map.of(
                        "method", "GET",
                        "path", "/api/analytics/categories",
                        "description", "Get spending grouped by category."),
                Map.of(
                        "method", "GET",
                        "path", "/api/analytics/monthly",
                        "description", "Get monthly income and expense summaries."),
                Map.of(
                        "method", "GET",
                        "path", "/api/analytics/trend",
                        "description", "Get spending/income trend data."),
                Map.of(
                        "method", "GET",
                        "path", "/api/analytics/recent",
                        "description", "Get recent transactions."),
                Map.of(
                        "method", "GET",
                        "path", "/v3/api-docs",
                        "description", "OpenAPI document describing the complete API contract.")
        );

        return AgentCapabilitiesResponse.builder()
                .apiVersion("v1")
                .description("Expense Tracker is a JWT-protected personal finance REST API. "
                        + "External agents can use the same business API as the human React client.")
                .authentication("User JWT for normal clients. External agents can use a user-created X-API-Key from /api/agent-tokens.")
                .validationContract("400 VALIDATION_ERROR responses preserve the legacy errors map and add structured fields metadata.")
                .idempotency("For JSON POST/PUT mutations, send a unique Idempotency-Key (max 128 characters). "
                        + "Reusing a key with a different payload returns 409.")
                .transactionTypes(Arrays.stream(ExpenseType.values()).map(Enum::name).toList())
                .categories(Arrays.stream(Category.values()).map(Enum::name).toList())
                .operations(operations)
                .build();
    }
}
