package com.aditya.expensetracker.expense_tracker.controller;

import com.aditya.expensetracker.expense_tracker.dto.CreateAgentTokenRequest;
import com.aditya.expensetracker.expense_tracker.dto.CreateAgentTokenResponse;
import com.aditya.expensetracker.expense_tracker.service.AgentApiTokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/agent-tokens")
@RequiredArgsConstructor
@Tag(name = "Agent Access", description = "User-managed API credentials for external agents")
@SecurityRequirement(name = "Bearer Authentication")
@PreAuthorize("hasAuthority('USER') or hasAuthority('ADMIN')")
public class AgentTokenController {

    private final AgentApiTokenService tokenService;

    @Operation(
            summary = "Create an agent API key",
            description = "Creates a user-scoped API key. The plaintext key is returned once and is never stored.")
    @ApiResponse(responseCode = "201", description = "API key created")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CreateAgentTokenResponse create(
            @Valid @RequestBody CreateAgentTokenRequest request) {
        return tokenService.create(request);
    }

    @Operation(
            summary = "Revoke an agent API key",
            description = "Revokes a user-owned agent API key.")
    @ApiResponse(responseCode = "204", description = "API key revoked")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void revoke(@PathVariable Long id) {
        tokenService.revoke(id);
    }
}
