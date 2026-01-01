package com.issuetracker.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for caching support.
 * Enables caching for dashboard metrics and statistics to improve performance.
 */
@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * Configures the cache manager for dashboard metrics caching.
     * Uses in-memory concurrent map cache for development and testing.
     * In production, this should be replaced with Redis or another distributed cache.
     *
     * @return configured cache manager
     */
    @Bean
    public CacheManager cacheManager() {
        ConcurrentMapCacheManager cacheManager = new ConcurrentMapCacheManager();
        
        // Pre-configure cache names for dashboard metrics
        cacheManager.setCacheNames(java.util.List.of(
            "dashboardMetrics",
            "dashboardSummary", 
            "projectStatistics",
            "sprintStatistics"
        ));
        
        // Allow dynamic cache creation for future cache names
        cacheManager.setAllowNullValues(false);
        
        return cacheManager;
    }
}