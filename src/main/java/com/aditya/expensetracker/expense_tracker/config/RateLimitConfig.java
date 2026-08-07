package com.aditya.expensetracker.expense_tracker.config;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.github.bucket4j.distributed.ExpirationAfterWriteStrategy;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.github.bucket4j.redis.lettuce.Bucket4jLettuce;
import io.github.bucket4j.redis.lettuce.cas.LettuceBasedProxyManager;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.codec.ByteArrayCodec;
import io.lettuce.core.codec.RedisCodec;
import io.lettuce.core.codec.StringCodec;

@Configuration
public class RateLimitConfig {

    @Value("${spring.data.redis.host}")
    private String redisHost;

    @Value("${spring.data.redis.port}")
    private int redisPort;

    @Bean
    public ProxyManager<String> rateLimitProxyManager() {

        RedisClient redisClient = RedisClient.create(
                RedisURI.builder()
                        .withHost(redisHost)
                        .withPort(redisPort)
                        .build());

        StatefulRedisConnection<String, byte[]> connection =
                redisClient.connect(
                        RedisCodec.of(StringCodec.UTF8, ByteArrayCodec.INSTANCE));

        LettuceBasedProxyManager<String> proxyManager =
                Bucket4jLettuce.casBasedBuilder(connection)
                        .expirationAfterWrite(
                                ExpirationAfterWriteStrategy
                                        .basedOnTimeForRefillingBucketUpToMax(
                                                Duration.ofMinutes(10)))
                        .build();

        return proxyManager;
    }
}