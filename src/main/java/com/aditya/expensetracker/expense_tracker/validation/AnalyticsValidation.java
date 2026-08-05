package com.aditya.expensetracker.expense_tracker.validation;

import java.time.LocalDate;

import org.springframework.stereotype.Component;

import com.aditya.expensetracker.expense_tracker.dto.analytics.AnalyticsFilterRequest;

@Component
public class AnalyticsValidation {

    public void validate(AnalyticsFilterRequest request) {

        if (request == null) {
            return;
        }

        LocalDate from = request.from();
        LocalDate to = request.to();

        if (from != null && to != null && from.isAfter(to)) {
            throw new IllegalArgumentException(
                    "From date cannot be after To date."
            );
        }

        if (request.limit() != null && request.limit() <= 0) {
            throw new IllegalArgumentException(
                    "Limit must be greater than zero."
            );
        }

    }

}