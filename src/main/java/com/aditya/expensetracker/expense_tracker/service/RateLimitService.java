package com.aditya.expensetracker.expense_tracker.service;

import java.time.Duration;

import org.springframework.stereotype.Service;

import io.github.bucket4j.Bucket;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.distributed.proxy.ProxyManager;

import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor
public class RateLimitService {

    private static final int AUTH_CAPACITY = 5;
    private static final Duration AUTH_PERIOD = Duration.ofMinutes(1);

    private static final int API_CAPACITY = 100;
    private static final Duration API_PERIOD = Duration.ofMinutes(1);

    private final ProxyManager<String> rateLimitProxyManager;

    public Bucket resolveAuthBucket(String clientKey) {
        return resolveBucket(
                "rate-limit:auth:" + clientKey,
                AUTH_CAPACITY,
                AUTH_PERIOD);
    }

    public Bucket resolveApiBucket(String clientKey) {
        return resolveBucket(
                "rate-limit:api:" + clientKey,
                API_CAPACITY,
                API_PERIOD);
    }

    private Bucket resolveBucket(String key, int capacity, Duration period) {

        BucketConfiguration configuration =
                BucketConfiguration.builder()
                        .addLimit(limit -> limit
                                .capacity(capacity)
                                .refillGreedy(capacity, period))
                        .build();

        return rateLimitProxyManager.getProxy(key, () -> configuration);
    }
}