package com.aditya.expensetracker.expense_tracker.config;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import com.aditya.expensetracker.expense_tracker.dto.ExpenseResponse;
import com.aditya.expensetracker.expense_tracker.dto.analytics.AnalyticsDashboardResponse;
import com.aditya.expensetracker.expense_tracker.dto.analytics.CategorySummaryResponse;
import com.aditya.expensetracker.expense_tracker.dto.analytics.DashboardCardResponse;
import com.aditya.expensetracker.expense_tracker.dto.analytics.MonthlySummaryResponse;
import com.aditya.expensetracker.expense_tracker.dto.analytics.RecentExpenseResponse;
import com.aditya.expensetracker.expense_tracker.dto.analytics.SpendingTrendResponse;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public RedisCacheManager cacheManager(
            RedisConnectionFactory connectionFactory,
            ObjectMapper objectMapper) {

        ObjectMapper redisObjectMapper = objectMapper.copy();

        RedisCacheConfiguration baseConfig =
                RedisCacheConfiguration.defaultCacheConfig()
                        .disableCachingNullValues();

        Map<String, RedisCacheConfiguration> cacheConfigurations =
                new HashMap<>();

        cacheConfigurations.put(
                "analytics-dashboard",
                typed(baseConfig, redisObjectMapper, AnalyticsDashboardResponse.class)
                        .entryTtl(Duration.ofMinutes(5)));

        cacheConfigurations.put(
                "analytics-summary",
                typed(baseConfig, redisObjectMapper, DashboardCardResponse.class)
                        .entryTtl(Duration.ofMinutes(10)));

        cacheConfigurations.put(
                "analytics-categories",
                typedList(baseConfig, redisObjectMapper, CategorySummaryResponse.class)
                        .entryTtl(Duration.ofMinutes(30)));

        cacheConfigurations.put(
                "analytics-monthly",
                typedList(baseConfig, redisObjectMapper, MonthlySummaryResponse.class)
                        .entryTtl(Duration.ofHours(1)));

        cacheConfigurations.put(
                "analytics-trend",
                typed(baseConfig, redisObjectMapper, SpendingTrendResponse.class)
                        .entryTtl(Duration.ofMinutes(15)));

        cacheConfigurations.put(
                "analytics-recent",
                typedList(baseConfig, redisObjectMapper, RecentExpenseResponse.class)
                        .entryTtl(Duration.ofMinutes(2)));

        cacheConfigurations.put(
                "expenses",
                typed(baseConfig, redisObjectMapper, ExpenseResponse.class)
                        .entryTtl(Duration.ofMinutes(5)));

        
        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(baseConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .build();
    }

    /**
     * Binds a cache to a single concrete type, e.g. {@code ExpenseResponse}.
     */
    private RedisCacheConfiguration typed(
            RedisCacheConfiguration base,
            ObjectMapper mapper,
            Class<?> type) {

        JavaType javaType = mapper.getTypeFactory().constructType(type);

        Jackson2JsonRedisSerializer<Object> serializer =
                new Jackson2JsonRedisSerializer<>(mapper, javaType);

        return base.serializeValuesWith(
                RedisSerializationContext.SerializationPair
                        .fromSerializer(serializer));
    }

    /**
     * Binds a cache to a {@code List<elementType>}, e.g. the
     * {@code List<CategorySummaryResponse>} returned by getCategorySummary.
     */
    private RedisCacheConfiguration typedList(
            RedisCacheConfiguration base,
            ObjectMapper mapper,
            Class<?> elementType) {

        JavaType javaType = mapper.getTypeFactory()
                .constructCollectionType(List.class, elementType);

        Jackson2JsonRedisSerializer<Object> serializer =
                new Jackson2JsonRedisSerializer<>(mapper, javaType);

        return base.serializeValuesWith(
                RedisSerializationContext.SerializationPair
                        .fromSerializer(serializer));
    }

}