package com.issuetracker.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Filter to add correlation IDs to all HTTP requests for request tracing.
 * The correlation ID is added to the MDC (Mapped Diagnostic Context) and 
 * included in all log messages for the request.
 */
@Component
@Order(1)
public class CorrelationIdFilter extends OncePerRequestFilter {

    private static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
    private static final String CORRELATION_ID_MDC_KEY = "correlationId";

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                  HttpServletResponse response, 
                                  FilterChain filterChain) throws ServletException, IOException {
        
        // Get correlation ID from header or generate new one
        String correlationId = request.getHeader(CORRELATION_ID_HEADER);
        if (correlationId == null || correlationId.trim().isEmpty()) {
            correlationId = UUID.randomUUID().toString();
        }
        
        try {
            // Add correlation ID to MDC for logging
            MDC.put(CORRELATION_ID_MDC_KEY, correlationId);
            
            // Add correlation ID to response header
            response.setHeader(CORRELATION_ID_HEADER, correlationId);
            
            // Continue with the filter chain
            filterChain.doFilter(request, response);
            
        } finally {
            // Clean up MDC to prevent memory leaks
            MDC.remove(CORRELATION_ID_MDC_KEY);
        }
    }
}