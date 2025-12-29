package com.issuetracker.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.stream.Collectors;

/**
 * Filter to log HTTP requests and responses for audit and debugging purposes.
 * Logs request method, URI, headers, and response status with execution time.
 */
@Component
@Order(2)
public class RequestLoggingFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(RequestLoggingFilter.class);
    
    private static final int MAX_PAYLOAD_LENGTH = 1000;

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                  HttpServletResponse response, 
                                  FilterChain filterChain) throws ServletException, IOException {
        
        // Skip logging for health check and static resources
        if (shouldSkipLogging(request)) {
            filterChain.doFilter(request, response);
            return;
        }
        
        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);
        
        long startTime = System.currentTimeMillis();
        
        try {
            // Log incoming request
            logRequest(wrappedRequest);
            
            // Continue with the filter chain
            filterChain.doFilter(wrappedRequest, wrappedResponse);
            
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            
            // Log outgoing response
            logResponse(wrappedRequest, wrappedResponse, duration);
            
            // Copy response content back to original response
            wrappedResponse.copyBodyToResponse();
        }
    }

    private void logRequest(ContentCachingRequestWrapper request) {
        String headers = Collections.list(request.getHeaderNames())
                .stream()
                .filter(this::shouldLogHeader)
                .map(name -> name + "=" + request.getHeader(name))
                .collect(Collectors.joining(", "));
        
        logger.info("HTTP Request: {} {} | Headers: [{}] | Remote: {}", 
                   request.getMethod(), 
                   request.getRequestURI(), 
                   headers,
                   request.getRemoteAddr());
        
        // Log request body for POST/PUT requests (excluding sensitive endpoints)
        if (shouldLogRequestBody(request)) {
            String payload = getRequestPayload(request);
            if (!payload.isEmpty()) {
                logger.debug("Request Body: {}", payload);
            }
        }
    }

    private void logResponse(ContentCachingRequestWrapper request, 
                           ContentCachingResponseWrapper response, 
                           long duration) {
        
        logger.info("HTTP Response: {} {} | Status: {} | Duration: {}ms | Size: {} bytes", 
                   request.getMethod(), 
                   request.getRequestURI(), 
                   response.getStatus(),
                   duration,
                   response.getContentSize());
        
        // Log response body for error responses
        if (response.getStatus() >= 400 && shouldLogResponseBody(request)) {
            String payload = getResponsePayload(response);
            if (!payload.isEmpty()) {
                logger.debug("Response Body: {}", payload);
            }
        }
    }

    private boolean shouldSkipLogging(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return uri.contains("/actuator/") || 
               uri.contains("/health") || 
               uri.contains("/favicon.ico") ||
               uri.contains("/static/");
    }

    private boolean shouldLogHeader(String headerName) {
        String lowerName = headerName.toLowerCase();
        return !lowerName.contains("authorization") && 
               !lowerName.contains("cookie") && 
               !lowerName.contains("password");
    }

    private boolean shouldLogRequestBody(ContentCachingRequestWrapper request) {
        String method = request.getMethod();
        String uri = request.getRequestURI();
        
        return ("POST".equals(method) || "PUT".equals(method) || "PATCH".equals(method)) &&
               !uri.contains("/auth/") && // Skip auth endpoints for security
               !uri.contains("/login") &&
               !uri.contains("/register");
    }

    private boolean shouldLogResponseBody(ContentCachingRequestWrapper request) {
        String uri = request.getRequestURI();
        return !uri.contains("/auth/") && 
               !uri.contains("/login") &&
               !uri.contains("/register");
    }

    private String getRequestPayload(ContentCachingRequestWrapper request) {
        byte[] content = request.getContentAsByteArray();
        if (content.length > 0) {
            String payload = new String(content, StandardCharsets.UTF_8);
            return payload.length() > MAX_PAYLOAD_LENGTH ? 
                   payload.substring(0, MAX_PAYLOAD_LENGTH) + "..." : payload;
        }
        return "";
    }

    private String getResponsePayload(ContentCachingResponseWrapper response) {
        byte[] content = response.getContentAsByteArray();
        if (content.length > 0) {
            String payload = new String(content, StandardCharsets.UTF_8);
            return payload.length() > MAX_PAYLOAD_LENGTH ? 
                   payload.substring(0, MAX_PAYLOAD_LENGTH) + "..." : payload;
        }
        return "";
    }
}