package com.aditya.expensetracker.expense_tracker.service;

import java.util.Optional;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.aditya.expensetracker.expense_tracker.entity.User;
import com.aditya.expensetracker.expense_tracker.exception.ResourceNotFoundException;
import com.aditya.expensetracker.expense_tracker.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CurrentUserService {

    private final UserRepository userRepository;

    public User getCurrentUser() {

        log.debug("Fetching current user");

        return getCurrentUserOptional()
                .orElseThrow(() -> {
                    log.warn("No authenticated user");
                    return new ResourceNotFoundException("No authenticated user");
                });
    }

    public Optional<User> getCurrentUserOptional() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {

            log.debug("User is not authenticated");
            return Optional.empty();
        }

        return userRepository.findByEmail(authentication.getName());
    }

    public Long getCurrentUserId() {

        return getCurrentUser().getId();
    }
}