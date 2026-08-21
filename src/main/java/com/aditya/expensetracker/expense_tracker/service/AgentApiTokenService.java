package com.aditya.expensetracker.expense_tracker.service;

import com.aditya.expensetracker.expense_tracker.dto.CreateAgentTokenRequest;
import com.aditya.expensetracker.expense_tracker.dto.CreateAgentTokenResponse;
import com.aditya.expensetracker.expense_tracker.entity.AgentApiToken;
import com.aditya.expensetracker.expense_tracker.entity.User;
import com.aditya.expensetracker.expense_tracker.exception.ResourceNotFoundException;
import com.aditya.expensetracker.expense_tracker.repository.AgentApiTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class AgentApiTokenService {
    private final AgentApiTokenRepository repository;
    private final CurrentUserService currentUserService;

    @Transactional
    public CreateAgentTokenResponse create(CreateAgentTokenRequest request) {
        User user = currentUserService.getCurrentUser();

        byte[] random = new byte[32];
        new SecureRandom().nextBytes(random);

        String rawToken = "et_" + Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(random);

        LocalDateTime now = LocalDateTime.now();
        AgentApiToken token = AgentApiToken.builder()
                .user(user)
                .name(request.getName().trim())
                .tokenHash(hash(rawToken))
                .tokenPrefix(rawToken.substring(0, Math.min(12, rawToken.length())))
                .expiresAt(now.plusDays(request.getExpirationDays()))
                .revoked(false)
                .createdAt(now)
                .build();

        AgentApiToken saved = repository.save(token);

        return CreateAgentTokenResponse.builder()
                .id(saved.getId())
                .name(saved.getName())
                .token(rawToken)
                .expiresAt(saved.getExpiresAt())
                .warning("Store this token securely. It is shown only once and should never be committed to source control.")
                .build();
    }

    @Transactional
    public void revoke(Long id) {
        User user = currentUserService.getCurrentUser();

        AgentApiToken token = repository.findById(id)
                .filter(value -> value.getUser().getId().equals(user.getId()))
                .orElseThrow(() -> new ResourceNotFoundException("Agent token not found"));

        token.setRevoked(true);
        repository.save(token);
    }

    @Transactional
    public User authenticate(String rawToken) {
        AgentApiToken token = repository
                .findActive(hash(rawToken), LocalDateTime.now())
                .orElse(null);

        if (token == null || !token.getUser().isEnabled()) {
            return null;
        }

        token.setLastUsedAt(LocalDateTime.now());
        return token.getUser();
    }

    private String hash(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is unavailable.", ex);
        }
    }
}
