package com.issuetracker.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Configuration for asynchronous processing.
 * Optimizes thread pool settings for better performance in production.
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    @Value("${performance.async.core-pool-size:4}")
    private int corePoolSize;

    @Value("${performance.async.max-pool-size:16}")
    private int maxPoolSize;

    @Value("${performance.async.queue-capacity:100}")
    private int queueCapacity;

    @Value("${performance.async.thread-name-prefix:async-exec-}")
    private String threadNamePrefix;

    @Value("${performance.async.keep-alive-seconds:60}")
    private int keepAliveSeconds;

    /**
     * Configures the task executor for asynchronous operations.
     * Optimized for handling dashboard calculations and audit logging.
     */
    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        // Core thread pool settings
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setKeepAliveSeconds(keepAliveSeconds);
        
        // Thread naming for easier debugging
        executor.setThreadNamePrefix(threadNamePrefix);
        
        // Graceful shutdown configuration
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        
        // Rejection policy - caller runs to prevent task loss
        executor.setRejectedExecutionHandler(new java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy());
        
        executor.initialize();
        return executor;
    }

    /**
     * Dedicated executor for audit logging operations.
     * Separate thread pool to prevent audit operations from blocking main operations.
     */
    @Bean(name = "auditExecutor")
    public Executor auditExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        // Smaller pool for audit operations
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(50);
        executor.setKeepAliveSeconds(60);
        
        executor.setThreadNamePrefix("audit-exec-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        
        // For audit operations, we can afford to wait
        executor.setRejectedExecutionHandler(new java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy());
        
        executor.initialize();
        return executor;
    }
}