package com.aditya.expensetracker.expense_tracker.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.aditya.expensetracker.expense_tracker.security.JwtAuthenticationFilter;
import com.aditya.expensetracker.expense_tracker.security.RateLimitFilter;
import com.aditya.expensetracker.expense_tracker.service.AgentApiTokenService;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AgentCapabilitiesController.class)
@AutoConfigureMockMvc(addFilters = false)

class AgentCapabilitiesControllerTest {

    @Autowired
    private MockMvc mockMvc;
    
    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;
    
    @MockitoBean
    private RateLimitFilter rateLimitFilter;
    
    @MockitoBean
    private AgentApiTokenService tokenService;

    @Test
    void capabilities_returnsAgentContractMetadata() throws Exception {
        mockMvc.perform(get("/api/agent/capabilities"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.apiVersion").value("v1"))
                .andExpect(jsonPath("$.transactionTypes").isArray())
                .andExpect(jsonPath("$.categories").isArray())
                .andExpect(jsonPath("$.operations").isArray());
    }
}
