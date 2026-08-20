package com.aditya.expensetracker.expense_tracker.service;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.github.bucket4j.Bucket;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RateLimitService {

    private final ProxyManager<String> rateLimitProxyManager;

    @Value("${app.rate-limit.auth.capacity}")
    private int authCapacity;

    @Value("${app.rate-limit.auth.period}")
    private Duration authPeriod;

    @Value("${app.rate-limit.email-action.capacity}")
    private int emailActionCapacity;

    @Value("${app.rate-limit.email-action.period}")
    private Duration emailActionPeriod;

    @Value("${app.rate-limit.api.capacity}")
    private int apiCapacity;

    @Value("${app.rate-limit.api.period}")
    private Duration apiPeriod;

    @Value("${app.rate-limit.upload.capacity}")
    private int uploadCapacity;

    @Value("${app.rate-limit.upload.period}")
    private Duration uploadPeriod;

    @Value("${app.rate-limit.analytics.capacity}")
    private int analyticsCapacity;

    @Value("${app.rate-limit.analytics.period}")
    private Duration analyticsPeriod;

    public Bucket resolveAuthBucket(String clientKey) {
        return resolveBucket(
                "rate-limit:auth:" + clientKey,
                authCapacity,
                authPeriod);
    }

    public Bucket resolveEmailActionBucket(String clientKey) {
        return resolveBucket(
                "rate-limit:email-action:" + clientKey,
                emailActionCapacity,
                emailActionPeriod);
    }

    public Bucket resolveApiBucket(String clientKey) {
        return resolveBucket(
                "rate-limit:api:" + clientKey,
                apiCapacity,
                apiPeriod);
    }

    public Bucket resolveUploadBucket(String clientKey) {
        return resolveBucket(
                "rate-limit:upload:" + clientKey,
                uploadCapacity,
                uploadPeriod);
    }

    public Bucket resolveAnalyticsBucket(String clientKey) {
        return resolveBucket(
                "rate-limit:analytics:" + clientKey,
                analyticsCapacity,
                analyticsPeriod);
    }

    private Bucket resolveBucket(
            String key,
            int capacity,
            Duration period) {

        BucketConfiguration configuration =
                BucketConfiguration.builder()
                        .addLimit(limit -> limit
                                .capacity(capacity)
                                .refillGreedy(capacity, period))
                        .build();

        return rateLimitProxyManager.getProxy(
                key,
                () -> configuration);
    }
}