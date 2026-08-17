package com.aditya.expensetracker.expense_tracker.security;

import java.io.IOException;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.aditya.expensetracker.expense_tracker.service.RateLimitService;

import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;

import org.springframework.http.HttpStatus;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

    private static final String AUTH_PATH_PREFIX = "/api/auth/";

    private final RateLimitService rateLimitService;

    @Value("${app.rate-limit.trusted-proxies:}")
    private String trustedProxiesProperty;

    private Set<String> trustedProxies;

    @PostConstruct
    private void init() {
        trustedProxies = Arrays.stream(trustedProxiesProperty.split(","))
                .map(String::trim)
                .filter(ip -> !ip.isBlank())
                .collect(Collectors.toSet());
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String clientKey = resolveClientKey(request);

        String requestUri = request.getRequestURI();
        boolean isAuthEndpoint = requestUri.startsWith(AUTH_PATH_PREFIX);
        boolean isEmailAction =
                "/api/auth/forgot-password".equals(requestUri)
                        || "/api/auth/resend-verification".equals(requestUri);

        try {
            Bucket bucket;
            if (isEmailAction) {
                bucket = rateLimitService.resolveEmailActionBucket(clientKey);
            } else if (isAuthEndpoint) {
                bucket = rateLimitService.resolveAuthBucket(clientKey);
            } else {
                bucket = rateLimitService.resolveApiBucket(clientKey);
            }

            ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

            response.setHeader(
                    "X-RateLimit-Remaining",
                    String.valueOf(probe.getRemainingTokens()));

            if (!probe.isConsumed()) {

                logger.warn(
                        "Rate limit exceeded for " + clientKey
                                + " on " + request.getMethod() + " "
                                + request.getRequestURI()
                                + " (tier=" + (isEmailAction ? "email-action"
                                : (isAuthEndpoint ? "auth" : "api")) + ")");

                rejectWithTooManyRequests(response, probe);
                return;
            }

        } catch (Exception ex) {

            logger.warn(
                    "Rate limiting unavailable, allowing request through: "
                            + ex.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    private void rejectWithTooManyRequests(
            HttpServletResponse response,
            ConsumptionProbe probe
    ) throws IOException {

        long retryAfterSeconds =
                Math.max(1, probe.getNanosToWaitForRefill() / 1_000_000_000);

        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setHeader("Retry-After", String.valueOf(retryAfterSeconds));
        response.setContentType("application/json");
        response.getWriter().write(
                "{\"status\":429,\"error\":\"Too many requests. Please try again in "
                        + retryAfterSeconds + " seconds.\"}");
    }


    private String resolveClientKey(HttpServletRequest request) {

        String remoteAddr = request.getRemoteAddr();

        if (!trustedProxies.contains(remoteAddr)) {
            return remoteAddr;
        }

        String forwardedFor = request.getHeader("X-Forwarded-For");

        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }

        return remoteAddr;
    }
}