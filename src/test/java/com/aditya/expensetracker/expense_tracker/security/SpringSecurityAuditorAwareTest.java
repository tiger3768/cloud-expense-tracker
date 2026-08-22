package com.aditya.expensetracker.expense_tracker.security;

import com.aditya.expensetracker.expense_tracker.entity.Role;
import com.aditya.expensetracker.expense_tracker.entity.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SpringSecurityAuditorAwareTest {

    private final SpringSecurityAuditorAware auditorAware =
            new SpringSecurityAuditorAware();

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void returnsUserIdWhenAuthenticatedPrincipalIsUser() {
        User user = User.builder()
                .id(42L)
                .email("user@example.com")
                .password("password")
                .role(Role.USER)
                .enabled(true)
                .build();

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        user,
                        null,
                        user.getAuthorities()));

        assertEquals(42L, auditorAware.getCurrentAuditor().orElseThrow());
    }

    @Test
    void returnsEmptyWhenThereIsNoAuthenticatedPrincipal() {
        assertTrue(auditorAware.getCurrentAuditor().isEmpty());
    }
}
