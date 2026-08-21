package com.aditya.expensetracker.expense_tracker.controller;

import com.aditya.expensetracker.expense_tracker.dto.CreateAgentTokenResponse;
import com.aditya.expensetracker.expense_tracker.security.JwtAuthenticationFilter;
import com.aditya.expensetracker.expense_tracker.security.RateLimitFilter;
import com.aditya.expensetracker.expense_tracker.service.AgentApiTokenService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AgentTokenController.class)
@AutoConfigureMockMvc(addFilters = false)

class AgentTokenControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AgentApiTokenService tokenService;
    
    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;
    
    @MockitoBean
    private RateLimitFilter rateLimitFilter;

    @Test
    void createAgentToken_returnsPlaintextOnce() throws Exception {
        when(tokenService.create(any()))
                .thenReturn(CreateAgentTokenResponse.builder()
                        .id(1L)
                        .name("My AI")
                        .token("et_test-token")
                        .expiresAt(LocalDateTime.of(2026, 9, 20, 0, 0))
                        .warning("Store this token securely.")
                        .build());

        mockMvc.perform(
                post("/api/agent-tokens")
                        .with(user("user@example.com").authorities(() -> "USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "My AI",
                                  "expirationDays": 30
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value("et_test-token"))
                .andExpect(jsonPath("$.name").value("My AI"));
    }
}
