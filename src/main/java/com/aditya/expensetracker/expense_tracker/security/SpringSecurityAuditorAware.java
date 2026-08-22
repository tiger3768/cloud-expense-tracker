package com.aditya.expensetracker.expense_tracker.security;

import com.aditya.expensetracker.expense_tracker.entity.User;
import java.util.Optional;

import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SpringSecurityAuditorAware
        implements AuditorAware<Long> {

    @Override
    public Optional<Long> getCurrentAuditor() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null ||
                !authentication.isAuthenticated()) {
            return Optional.empty();
        }

        Object principal = authentication.getPrincipal();

        // JWT and X-API-Key authentication both place the authenticated
        // application User entity in the SecurityContext.
        if (principal instanceof User user && user.getId() != null) {
            return Optional.of(user.getId());
        }

        // Keep support for tests/custom authentication implementations
        // that may expose the user ID directly.
        if (principal instanceof Long userId) {
            return Optional.of(userId);
        }

        return Optional.empty();
    }
}
