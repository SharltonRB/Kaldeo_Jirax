package com.issuetracker.monitoring;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * Service for tracking application performance metrics
 */
@Service
public class PerformanceMonitoringService {

    private static final Logger logger = LoggerFactory.getLogger("com.issuetracker.performance");
    
    private final MeterRegistry meterRegistry;
    
    // Counters
    private final Counter authSuccessCounter;
    private final Counter authFailureCounter;
    private final Counter issueCreatedCounter;
    private final Counter projectCreatedCounter;
    private final Counter sprintCreatedCounter;
    
    // Timers
    private final Timer authenticationTimer;
    private final Timer issueCreationTimer;
    private final Timer projectCreationTimer;
    private final Timer dashboardLoadTimer;
    
    public PerformanceMonitoringService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        
        // Initialize counters
        this.authSuccessCounter = Counter.builder("auth.success")
            .description("Number of successful authentication attempts")
            .register(meterRegistry);
            
        this.authFailureCounter = Counter.builder("auth.failure")
            .description("Number of failed authentication attempts")
            .register(meterRegistry);
            
        this.issueCreatedCounter = Counter.builder("issue.created")
            .description("Number of issues created")
            .register(meterRegistry);
            
        this.projectCreatedCounter = Counter.builder("project.created")
            .description("Number of projects created")
            .register(meterRegistry);
            
        this.sprintCreatedCounter = Counter.builder("sprint.created")
            .description("Number of sprints created")
            .register(meterRegistry);
        
        // Initialize timers
        this.authenticationTimer = Timer.builder("auth.duration")
            .description("Time taken for authentication")
            .register(meterRegistry);
            
        this.issueCreationTimer = Timer.builder("issue.creation.duration")
            .description("Time taken to create an issue")
            .register(meterRegistry);
            
        this.projectCreationTimer = Timer.builder("project.creation.duration")
            .description("Time taken to create a project")
            .register(meterRegistry);
            
        this.dashboardLoadTimer = Timer.builder("dashboard.load.duration")
            .description("Time taken to load dashboard")
            .register(meterRegistry);
    }
    
    // Authentication metrics
    public void recordAuthSuccess() {
        authSuccessCounter.increment();
        logger.info("Authentication successful");
    }
    
    public void recordAuthFailure(String reason) {
        authFailureCounter.increment();
        logger.warn("Authentication failed: {}", reason);
    }
    
    public void recordAuthenticationTime(long durationMs) {
        authenticationTimer.record(durationMs, TimeUnit.MILLISECONDS);
        if (durationMs > 1000) {
            logger.warn("Slow authentication detected: {}ms", durationMs);
        }
    }
    
    // Issue metrics
    public void recordIssueCreated() {
        issueCreatedCounter.increment();
        logger.info("Issue created");
    }
    
    public void recordIssueCreationTime(long durationMs) {
        issueCreationTimer.record(durationMs, TimeUnit.MILLISECONDS);
        if (durationMs > 500) {
            logger.warn("Slow issue creation detected: {}ms", durationMs);
        }
    }
    
    // Project metrics
    public void recordProjectCreated() {
        projectCreatedCounter.increment();
        logger.info("Project created");
    }
    
    public void recordProjectCreationTime(long durationMs) {
        projectCreationTimer.record(durationMs, TimeUnit.MILLISECONDS);
        if (durationMs > 500) {
            logger.warn("Slow project creation detected: {}ms", durationMs);
        }
    }
    
    // Sprint metrics
    public void recordSprintCreated() {
        sprintCreatedCounter.increment();
        logger.info("Sprint created");
    }
    
    // Dashboard metrics
    public void recordDashboardLoadTime(long durationMs) {
        dashboardLoadTimer.record(durationMs, TimeUnit.MILLISECONDS);
        if (durationMs > 2000) {
            logger.warn("Slow dashboard load detected: {}ms", durationMs);
        }
    }
    
    // Generic operation timing
    public void recordOperationTime(String operationName, long durationMs) {
        Timer.builder("operation.duration")
            .tag("operation", operationName)
            .description("Time taken for operation: " + operationName)
            .register(meterRegistry)
            .record(durationMs, TimeUnit.MILLISECONDS);
            
        if (durationMs > 1000) {
            logger.warn("Slow operation detected: {} took {}ms", operationName, durationMs);
        }
    }
    
    // Error tracking
    public void recordError(String errorType, String errorMessage) {
        Counter.builder("error.count")
            .tag("type", errorType)
            .description("Number of errors by type")
            .register(meterRegistry)
            .increment();
            
        logger.error("Error recorded - Type: {}, Message: {}", errorType, errorMessage);
    }
    
    // Database query metrics
    public void recordSlowQuery(String queryType, long durationMs) {
        Counter.builder("database.slow.query")
            .tag("type", queryType)
            .description("Number of slow database queries")
            .register(meterRegistry)
            .increment();
            
        logger.warn("Slow database query detected - Type: {}, Duration: {}ms", queryType, durationMs);
    }
}
