package com.aditya.expensetracker.expense_tracker.service;

import java.util.Optional;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.aditya.expensetracker.expense_tracker.entity.User;
import com.aditya.expensetracker.expense_tracker.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CurrentUserService {

    private final UserRepository userRepository;

    public User getCurrentUser() {

        return getCurrentUserOptional()
                .orElseThrow(() ->
                        new RuntimeException("No authenticated user"));
    }

    public Optional<User> getCurrentUserOptional() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {

            return Optional.empty();
        }

        return userRepository.findByEmail(authentication.getName());
    }

    public Long getCurrentUserId() {

        return getCurrentUser().getId();
    }
}