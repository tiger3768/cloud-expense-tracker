package com.aditya.expensetracker.expense_tracker.controller;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.aditya.expensetracker.expense_tracker.dto.analytics.DashboardCardResponse;
import com.aditya.expensetracker.expense_tracker.exception.InvalidAnalyticsRequestException;
import com.aditya.expensetracker.expense_tracker.security.JwtAuthenticationFilter;
import com.aditya.expensetracker.expense_tracker.security.RateLimitFilter;
import com.aditya.expensetracker.expense_tracker.service.AgentApiTokenService;
import com.aditya.expensetracker.expense_tracker.service.AnalyticsService;
import com.aditya.expensetracker.expense_tracker.service.CurrentUserService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AnalyticsController.class)
@AutoConfigureMockMvc(addFilters = false)
class AnalyticsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AnalyticsService analyticsService;

    @MockitoBean
    private CurrentUserService currentUserService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private RateLimitFilter rateLimitFilter;
    
    @MockitoBean
    private AgentApiTokenService tokenService;

    @Test
    void getSummary_authenticated_returns200() throws Exception {

        when(currentUserService.getCurrentUserId())
                .thenReturn(1L);

        when(analyticsService.getSummary(eq(1L), any()))
                .thenReturn(new DashboardCardResponse(
                        new BigDecimal("5000.00"),
                        new BigDecimal("2000.00"),
                        new BigDecimal("3000.00")
                ));

        mockMvc.perform(
                get("/api/analytics/summary")
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.income").value(5000.00))
                .andExpect(jsonPath("$.expense").value(2000.00))
                .andExpect(jsonPath("$.balance").value(3000.00));
    }

    @Test
    void getCategorySummary_authenticated_returns200() throws Exception {

        when(currentUserService.getCurrentUserId())
                .thenReturn(1L);

        when(analyticsService.getCategorySummary(eq(1L), any()))
                .thenReturn(List.of());

        mockMvc.perform(
                get("/api/analytics/categories")
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void getSummary_invalidDateRange_returns400() throws Exception {

        when(currentUserService.getCurrentUserId())
                .thenReturn(1L);

        when(analyticsService.getSummary(eq(1L), any()))
                .thenThrow(new InvalidAnalyticsRequestException(
                        "From date cannot be after To date."
                ));

        mockMvc.perform(
                get("/api/analytics/summary")
                        .param("from", "2026-08-10")
                        .param("to", "2026-08-01")
        )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error")
                        .value("From date cannot be after To date."));
    }
}