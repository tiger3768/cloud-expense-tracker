package com.aditya.expensetracker.expense_tracker.config;

import java.time.Duration;

import org.springframework.boot.autoconfigure.data.redis.RedisConnectionDetails;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.github.bucket4j.distributed.ExpirationAfterWriteStrategy;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.github.bucket4j.redis.lettuce.Bucket4jLettuce;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.codec.ByteArrayCodec;
import io.lettuce.core.codec.RedisCodec;
import io.lettuce.core.codec.StringCodec;

@Configuration
public class RateLimitConfig {

    @Bean
    public ProxyManager<String> rateLimitProxyManager(
            RedisConnectionDetails redisConnectionDetails) {

        RedisConnectionDetails.Standalone standalone =
                redisConnectionDetails.getStandalone();

        RedisClient redisClient = RedisClient.create(
                RedisURI.builder()
                        .withHost(standalone.getHost())
                        .withPort(standalone.getPort())
                        .build()
        );

        StatefulRedisConnection<String, byte[]> connection =
                redisClient.connect(
                        RedisCodec.of(
                                StringCodec.UTF8,
                                ByteArrayCodec.INSTANCE
                        )
                );

        return Bucket4jLettuce
                .casBasedBuilder(connection)
                .expirationAfterWrite(
                        ExpirationAfterWriteStrategy
                                .basedOnTimeForRefillingBucketUpToMax(
                                        Duration.ofMinutes(10)
                                )
                )
                .build();
    }
}