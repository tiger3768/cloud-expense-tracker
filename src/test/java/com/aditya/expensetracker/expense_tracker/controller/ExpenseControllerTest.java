package com.aditya.expensetracker.expense_tracker.controller;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.aditya.expensetracker.expense_tracker.dto.CreateExpenseRequest;
import com.aditya.expensetracker.expense_tracker.dto.ExpenseResponse;
import com.aditya.expensetracker.expense_tracker.entity.Category;
import com.aditya.expensetracker.expense_tracker.entity.ExpenseType;
import com.aditya.expensetracker.expense_tracker.exception.ResourceNotFoundException;
import com.aditya.expensetracker.expense_tracker.security.JwtAuthenticationFilter;
import com.aditya.expensetracker.expense_tracker.security.RateLimitFilter;
import com.aditya.expensetracker.expense_tracker.service.ExpenseService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ExpenseController.class)
@AutoConfigureMockMvc(addFilters = false)
class ExpenseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ExpenseService expenseService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private RateLimitFilter rateLimitFilter;

    private final ObjectMapper objectMapper =
            new ObjectMapper().registerModule(new JavaTimeModule());

    private MockMultipartFile expensePart(CreateExpenseRequest request)
            throws Exception {

        return new MockMultipartFile(
                "expense",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(request)
        );
    }

    private CreateExpenseRequest validRequest() {

        CreateExpenseRequest request = new CreateExpenseRequest();
        request.setTitle("Groceries");
        request.setAmount(new BigDecimal("42.50"));
        request.setType(ExpenseType.EXPENSE);
        request.setCategory(Category.FOOD);
        request.setExpenseDate(LocalDate.of(2026, 8, 1));
        request.setVersion(0L);

        return request;
    }

    @Test
    void createExpense_valid_returns201() throws Exception {

        ExpenseResponse response = ExpenseResponse.builder()
                .id(1L)
                .title("Groceries")
                .amount(new BigDecimal("42.50"))
                .type(ExpenseType.EXPENSE)
                .category(Category.FOOD)
                .expenseDate(LocalDate.of(2026, 8, 1))
                .build();

        when(expenseService.createExpense(any(), isNull()))
                .thenReturn(response);

        mockMvc.perform(
                multipart("/api/expenses")
                        .file(expensePart(validRequest()))
        )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Groceries"));
    }

    @Test
    void createExpense_missingTitle_returns400() throws Exception {

        CreateExpenseRequest request = validRequest();
        request.setTitle(null);

        mockMvc.perform(
                multipart("/api/expenses")
                        .file(expensePart(request))
        )
                .andExpect(status().isBadRequest());
    }

    @Test
    void createExpense_negativeAmount_returns400() throws Exception {

        CreateExpenseRequest request = validRequest();
        request.setAmount(new BigDecimal("-5"));

        mockMvc.perform(
                multipart("/api/expenses")
                        .file(expensePart(request))
        )
                .andExpect(status().isBadRequest());
    }

    @Test
    void getExpense_found_returns200() throws Exception {

        ExpenseResponse response = ExpenseResponse.builder()
                .id(5L)
                .title("Fuel")
                .amount(new BigDecimal("20.00"))
                .build();

        when(expenseService.getExpense(5L))
                .thenReturn(response);

        mockMvc.perform(get("/api/expenses/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.title").value("Fuel"));
    }

    @Test
    void getExpense_notFound_returns404() throws Exception {

        when(expenseService.getExpense(999L))
                .thenThrow(new ResourceNotFoundException("Expense not found"));

        mockMvc.perform(get("/api/expenses/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error")
                        .value("Expense not found"));
    }

    @Test
    void deleteExpense_returns204() throws Exception {

        mockMvc.perform(delete("/api/expenses/1"))
                .andExpect(status().isNoContent());

        verify(expenseService).deleteExpense(1L);
    }

    @Test
    void deleteExpense_notFound_returns404() throws Exception {

        doThrow(new ResourceNotFoundException("Expense not found"))
                .when(expenseService)
                .deleteExpense(999L);

        mockMvc.perform(delete("/api/expenses/999"))
                .andExpect(status().isNotFound());
    }
}