package com.aditya.expensetracker.expense_tracker.dto.analytics;

import java.time.LocalDate;

import jakarta.validation.constraints.PastOrPresent;

public record AnalyticsFilterRequest(

        @PastOrPresent
        LocalDate from,

        @PastOrPresent
        LocalDate to,

        String category,

        String type,

        Integer limit

) {
}