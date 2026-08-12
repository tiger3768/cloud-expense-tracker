package com.aditya.expensetracker.expense_tracker.security;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.aditya.expensetracker.expense_tracker.service.RateLimitService;

import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;

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
    
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {

        return request.getRequestURI().startsWith("/actuator");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String clientKey = resolveClientKey(request);

        boolean isAuthEndpoint =
                request.getRequestURI().startsWith(AUTH_PATH_PREFIX);

        try {
            Bucket bucket = isAuthEndpoint
                    ? rateLimitService.resolveAuthBucket(clientKey)
                    : rateLimitService.resolveApiBucket(clientKey);

            ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

            response.setHeader(
                    "X-RateLimit-Remaining",
                    String.valueOf(probe.getRemainingTokens()));

            if (!probe.isConsumed()) {
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

        String forwardedFor = request.getHeader("X-Forwarded-For");

        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }

        return request.getRemoteAddr();
    }
}