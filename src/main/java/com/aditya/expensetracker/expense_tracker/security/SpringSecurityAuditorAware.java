package com.aditya.expensetracker.expense_tracker.security;

import java.util.Optional;

import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Component;

import com.aditya.expensetracker.expense_tracker.service.CurrentUserService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SpringSecurityAuditorAware
        implements AuditorAware<Long> {

    private final CurrentUserService currentUserService;

    @Override
    public Optional<Long> getCurrentAuditor() {

        return currentUserService
                .getCurrentUserOptional()
                .map(user -> user.getId());
    }
}