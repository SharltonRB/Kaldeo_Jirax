package com.issuetracker.monitoring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Service for tracking and alerting on application errors
 */
@Service
public class ErrorTrackingService {

    private static final Logger logger = LoggerFactory.getLogger("com.issuetracker.errors");
    
    private final Map<String, ErrorStats> errorStats = new ConcurrentHashMap<>();
    private final PerformanceMonitoringService performanceMonitoring;
    
    // Thresholds for alerting
    private static final int ERROR_THRESHOLD_PER_MINUTE = 10;
    private static final int CRITICAL_ERROR_THRESHOLD = 5;
    
    public ErrorTrackingService(PerformanceMonitoringService performanceMonitoring) {
        this.performanceMonitoring = performanceMonitoring;
    }
    
    /**
     * Track an error occurrence
     */
    public void trackError(String errorType, String errorMessage, Throwable throwable) {
        ErrorStats stats = errorStats.computeIfAbsent(errorType, k -> new ErrorStats());
        stats.increment();
        
        // Log the error
        if (throwable != null) {
            logger.error("Error tracked - Type: {}, Message: {}", errorType, errorMessage, throwable);
        } else {
            logger.error("Error tracked - Type: {}, Message: {}", errorType, errorMessage);
        }
        
        // Record in performance monitoring
        performanceMonitoring.recordError(errorType, errorMessage);
        
        // Check if we need to alert
        checkErrorThresholds(errorType, stats);
    }
    
    /**
     * Track a critical error that requires immediate attention
     */
    public void trackCriticalError(String errorType, String errorMessage, Throwable throwable) {
        trackError(errorType, errorMessage, throwable);
        
        // Log as critical
        logger.error("CRITICAL ERROR - Type: {}, Message: {}", errorType, errorMessage, throwable);
        
        // In production, this would trigger alerts (email, Slack, PagerDuty, etc.)
        alertCriticalError(errorType, errorMessage);
    }
    
    /**
     * Track authentication failures
     */
    public void trackAuthenticationFailure(String username, String reason) {
        String errorType = "AUTH_FAILURE";
        trackError(errorType, String.format("User: %s, Reason: %s", username, reason), null);
    }
    
    /**
     * Track validation errors
     */
    public void trackValidationError(String field, String message) {
        String errorType = "VALIDATION_ERROR";
        trackError(errorType, String.format("Field: %s, Message: %s", field, message), null);
    }
    
    /**
     * Track database errors
     */
    public void trackDatabaseError(String operation, Throwable throwable) {
        String errorType = "DATABASE_ERROR";
        trackError(errorType, String.format("Operation: %s", operation), throwable);
    }
    
    /**
     * Track external service errors
     */
    public void trackExternalServiceError(String serviceName, Throwable throwable) {
        String errorType = "EXTERNAL_SERVICE_ERROR";
        trackError(errorType, String.format("Service: %s", serviceName), throwable);
    }
    
    /**
     * Get error statistics
     */
    public Map<String, ErrorStats> getErrorStats() {
        return Map.copyOf(errorStats);
    }
    
    /**
     * Reset error statistics
     */
    public void resetStats() {
        errorStats.clear();
        logger.info("Error statistics reset");
    }
    
    /**
     * Check if error thresholds are exceeded
     */
    private void checkErrorThresholds(String errorType, ErrorStats stats) {
        long recentErrors = stats.getRecentCount();
        
        if (recentErrors > ERROR_THRESHOLD_PER_MINUTE) {
            logger.warn("ERROR THRESHOLD EXCEEDED - Type: {}, Count: {} errors in last minute", 
                errorType, recentErrors);
            // In production, trigger alert
        }
    }
    
    /**
     * Alert on critical errors
     */
    private void alertCriticalError(String errorType, String errorMessage) {
        // In production, this would:
        // 1. Send email to on-call team
        // 2. Post to Slack/Teams channel
        // 3. Create PagerDuty incident
        // 4. Log to external monitoring service (Sentry, Datadog, etc.)
        
        logger.error("ALERT: Critical error requires immediate attention - Type: {}, Message: {}", 
            errorType, errorMessage);
    }
    
    /**
     * Statistics for a specific error type
     */
    public static class ErrorStats {
        private final AtomicLong totalCount = new AtomicLong(0);
        private final AtomicLong recentCount = new AtomicLong(0);
        private volatile Instant lastOccurrence = Instant.now();
        private volatile Instant windowStart = Instant.now();
        
        public void increment() {
            totalCount.incrementAndGet();
            lastOccurrence = Instant.now();
            
            // Reset window if more than 1 minute has passed
            Instant now = Instant.now();
            if (now.isAfter(windowStart.plusSeconds(60))) {
                recentCount.set(1);
                windowStart = now;
            } else {
                recentCount.incrementAndGet();
            }
        }
        
        public long getTotalCount() {
            return totalCount.get();
        }
        
        public long getRecentCount() {
            // Reset if window expired
            Instant now = Instant.now();
            if (now.isAfter(windowStart.plusSeconds(60))) {
                recentCount.set(0);
                windowStart = now;
            }
            return recentCount.get();
        }
        
        public Instant getLastOccurrence() {
            return lastOccurrence;
        }
    }
}
