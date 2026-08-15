package com.aditya.expensetracker.expense_tracker.security;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import com.aditya.expensetracker.expense_tracker.dto.LoginRequest;
import com.aditya.expensetracker.expense_tracker.entity.Role;
import com.aditya.expensetracker.expense_tracker.entity.User;
import com.aditya.expensetracker.expense_tracker.repository.UserRepository;
import com.aditya.expensetracker.expense_tracker.service.JwtService;
import com.aditya.expensetracker.expense_tracker.support.AbstractIntegrationTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class AuthAndRateLimitIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void protectedEndpoint_withoutToken_returns401() {

        ResponseEntity<String> response = restTemplate.getForEntity(
                "/api/expenses", String.class);

        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void protectedEndpoint_withValidToken_isNotRejectedByAuth() {

        User user = userRepository.save(
                User.builder()
                        .name("JWT Test User")
                        .email("jwt-test-" + System.nanoTime() + "@example.com")
                        .password(passwordEncoder.encode("irrelevant"))
                        .role(Role.USER)
                        .enabled(true)
                        .createdAt(LocalDateTime.now())
                        .build());

        String token = jwtService.generateToken(user);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/expenses",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class);

        assertThat(response.getStatusCode())
                .isNotEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void authEndpoint_exceedingLimit_returns429WithRetryAfter() {

        LoginRequest badLogin = new LoginRequest();
        badLogin.setEmail("nonexistent-" + System.nanoTime() + "@example.com");
        badLogin.setPassword("wrong-password");

        for (int attempt = 1; attempt <= 6; attempt++) {

            ResponseEntity<String> response =
                    restTemplate.postForEntity(
                            "/api/auth/login",
                            badLogin,
                            String.class
                    );

            System.out.println(
                    "Attempt " + attempt
                            + " -> status=" + response.getStatusCode()
                            + ", remaining="
                            + response.getHeaders()
                                    .getFirst("X-RateLimit-Remaining")
                            + ", retryAfter="
                            + response.getHeaders()
                                    .getFirst("Retry-After")
            );

            if (attempt == 6) {
                assertThat(response.getStatusCode())
                        .isEqualTo(HttpStatus.TOO_MANY_REQUESTS);

                assertThat(response.getHeaders()
                        .containsKey("Retry-After"))
                        .isTrue();
            }
        }
    }
}