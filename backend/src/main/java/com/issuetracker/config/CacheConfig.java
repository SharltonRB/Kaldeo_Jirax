package com.issuetracker.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Configuration class for caching support.
 * Provides both Redis-based distributed caching for production and in-memory caching for development.
 * Optimized for dashboard metrics and frequently accessed data.
 */
@Configuration
@EnableCaching
public class CacheConfig {

    @Value("${cache.redis.time-to-live:300}")
    private long defaultTtl;

    @Value("${cache.redis.dashboard-metrics-ttl:180}")
    private long dashboardMetricsTtl;

    @Value("${cache.redis.project-statistics-ttl:300}")
    private long projectStatisticsTtl;

    @Value("${cache.redis.sprint-statistics-ttl:120}")
    private long sprintStatisticsTtl;

    @Value("${cache.redis.user-data-ttl:600}")
    private long userDataTtl;

    @Value("${cache.redis.key-prefix:issuetracker}")
    private String keyPrefix;

    /**
     * Redis-based cache manager for production environments.
     * Provides distributed caching with optimized serialization and TTL configurations.
     */
    @Bean
    @Primary
    @Profile("prod")
    @ConditionalOnProperty(name = "spring.data.redis.host")
    public CacheManager redisCacheManager(RedisConnectionFactory redisConnectionFactory) {
        // Configure JSON serialization for cache values
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.activateDefaultTyping(
            objectMapper.getPolymorphicTypeValidator(),
            ObjectMapper.DefaultTyping.NON_FINAL,
            JsonTypeInfo.As.PROPERTY
        );

        GenericJackson2JsonRedisSerializer jsonSerializer = 
            new GenericJackson2JsonRedisSerializer(objectMapper);

        // Default cache configuration
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofSeconds(defaultTtl))
            .prefixCacheNameWith(keyPrefix + ":")
            .serializeKeysWith(RedisSerializationContext.SerializationPair
                .fromSerializer(new StringRedisSerializer()))
            .serializeValuesWith(RedisSerializationContext.SerializationPair
                .fromSerializer(jsonSerializer))
            .disableCachingNullValues();

        // Specific cache configurations with different TTLs
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
        
        cacheConfigurations.put("dashboardMetrics", defaultConfig
            .entryTtl(Duration.ofSeconds(dashboardMetricsTtl)));
        
        cacheConfigurations.put("dashboardSummary", defaultConfig
            .entryTtl(Duration.ofSeconds(dashboardMetricsTtl)));
        
        cacheConfigurations.put("projectStatistics", defaultConfig
            .entryTtl(Duration.ofSeconds(projectStatisticsTtl)));
        
        cacheConfigurations.put("sprintStatistics", defaultConfig
            .entryTtl(Duration.ofSeconds(sprintStatisticsTtl)));
        
        cacheConfigurations.put("userProjects", defaultConfig
            .entryTtl(Duration.ofSeconds(userDataTtl)));
        
        cacheConfigurations.put("userIssues", defaultConfig
            .entryTtl(Duration.ofSeconds(300))); // 5 minutes for issue data
        
        cacheConfigurations.put("sprintIssues", defaultConfig
            .entryTtl(Duration.ofSeconds(180))); // 3 minutes for sprint issues

        return RedisCacheManager.builder(redisConnectionFactory)
            .cacheDefaults(defaultConfig)
            .withInitialCacheConfigurations(cacheConfigurations)
            .transactionAware()
            .build();
    }

    /**
     * In-memory cache manager for development and testing environments.
     * Uses concurrent map cache for fast local development.
     */
    @Bean
    @Profile({"dev", "test"})
    public CacheManager concurrentMapCacheManager() {
        ConcurrentMapCacheManager cacheManager = new ConcurrentMapCacheManager();
        
        // Pre-configure cache names for dashboard metrics
        cacheManager.setCacheNames(java.util.List.of(
            "dashboardMetrics",
            "dashboardSummary", 
            "projectStatistics",
            "sprintStatistics",
            "userProjects",
            "userIssues",
            "sprintIssues"
        ));
        
        // Allow dynamic cache creation for future cache names
        cacheManager.setAllowNullValues(false);
        
        return cacheManager;
    }
}