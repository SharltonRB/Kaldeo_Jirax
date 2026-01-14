package com.issuetracker.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Enhanced rate limiting configuration for all API endpoints.
 * Implements different rate limits for authentication and general API endpoints.
 */
@Configuration
public class RateLimitingConfig {

    /**
     * Enhanced rate limiting filter for all API endpoints.
     * Implements different rate limits for authentication vs general API endpoints.
     * Only active in non-test profiles.
     */
    @Component
    @Order(1)
    @Profile("!test")
    public static class ApiRateLimitingFilter extends OncePerRequestFilter {

        private final ConcurrentHashMap<String, Bucket> authBuckets = new ConcurrentHashMap<>();
        private final ConcurrentHashMap<String, Bucket> apiBuckets = new ConcurrentHashMap<>();
        
        @Value("${rate-limit.auth.requests-per-minute:10}")
        private int authRequestsPerMinute;
        
        @Value("${rate-limit.auth.burst-capacity:20}")
        private int authBurstCapacity;
        
        @Value("${rate-limit.api.requests-per-minute:100}")
        private int apiRequestsPerMinute;
        
        @Value("${rate-limit.api.burst-capacity:200}")
        private int apiBurstCapacity;

        @Override
        protected void doFilterInternal(
                HttpServletRequest request,
                HttpServletResponse response,
                FilterChain filterChain
        ) throws ServletException, IOException {

            String requestURI = request.getRequestURI();
            String clientIp = getClientIpAddress(request);
            
            // Skip rate limiting for health checks and actuator endpoints
            if (requestURI.startsWith("/api/actuator/") || 
                requestURI.equals("/api/actuator/health") ||
                requestURI.equals("/api/actuator/info")) {
                filterChain.doFilter(request, response);
                return;
            }
            
            Bucket bucket;
            String rateLimitType;
            
            // Apply stricter rate limiting to authentication endpoints
            if (requestURI.startsWith("/api/auth/")) {
                bucket = getAuthBucket(clientIp);
                rateLimitType = "authentication";
            } else if (requestURI.startsWith("/api/")) {
                // Apply general rate limiting to all other API endpoints
                bucket = getApiBucket(clientIp);
                rateLimitType = "api";
            } else {
                // Skip rate limiting for non-API endpoints
                filterChain.doFilter(request, response);
                return;
            }

            if (!bucket.tryConsume(1)) {
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.setContentType("application/json");
                response.setHeader("X-RateLimit-Limit", getRateLimitHeader(rateLimitType));
                response.setHeader("X-RateLimit-Remaining", "0");
                response.setHeader("X-RateLimit-Reset", String.valueOf(System.currentTimeMillis() + 60000));
                response.setHeader("Retry-After", "60");
                
                response.getWriter().write(String.format(
                    "{\"error\":\"Too many requests\",\"message\":\"Rate limit exceeded for %s endpoints\",\"code\":\"RATE_LIMIT_EXCEEDED\"}",
                    rateLimitType
                ));
                return;
            }
            
            // Add rate limit headers to successful responses
            long availableTokens = bucket.getAvailableTokens();
            response.setHeader("X-RateLimit-Limit", getRateLimitHeader(rateLimitType));
            response.setHeader("X-RateLimit-Remaining", String.valueOf(availableTokens));

            filterChain.doFilter(request, response);
        }

        /**
         * Gets or creates a rate limiting bucket for authentication endpoints.
         *
         * @param ip client IP address
         * @return rate limiting bucket for auth endpoints
         */
        private Bucket getAuthBucket(String ip) {
            return authBuckets.computeIfAbsent(ip, this::createAuthBucket);
        }

        /**
         * Gets or creates a rate limiting bucket for general API endpoints.
         *
         * @param ip client IP address
         * @return rate limiting bucket for API endpoints
         */
        private Bucket getApiBucket(String ip) {
            return apiBuckets.computeIfAbsent(ip, this::createApiBucket);
        }

        /**
         * Creates a new rate limiting bucket for authentication endpoints.
         * More restrictive limits to prevent brute force attacks.
         *
         * @param ip client IP address
         * @return new auth bucket
         */
        private Bucket createAuthBucket(String ip) {
            Bandwidth limit = Bandwidth.classic(
                authBurstCapacity, 
                Refill.intervally(authRequestsPerMinute, Duration.ofMinutes(1))
            );
            return Bucket.builder()
                    .addLimit(limit)
                    .build();
        }

        /**
         * Creates a new rate limiting bucket for general API endpoints.
         * More permissive limits for normal API usage.
         *
         * @param ip client IP address
         * @return new API bucket
         */
        private Bucket createApiBucket(String ip) {
            Bandwidth limit = Bandwidth.classic(
                apiBurstCapacity, 
                Refill.intervally(apiRequestsPerMinute, Duration.ofMinutes(1))
            );
            return Bucket.builder()
                    .addLimit(limit)
                    .build();
        }

        /**
         * Gets the rate limit header value for the given type.
         *
         * @param type rate limit type (auth or api)
         * @return rate limit header value
         */
        private String getRateLimitHeader(String type) {
            if ("authentication".equals(type)) {
                return String.valueOf(authRequestsPerMinute);
            } else {
                return String.valueOf(apiRequestsPerMinute);
            }
        }

        /**
         * Extracts client IP address from request, considering proxy headers.
         *
         * @param request HTTP request
         * @return client IP address
         */
        private String getClientIpAddress(HttpServletRequest request) {
            // Check for X-Forwarded-For header (common in load balancers)
            String xForwardedFor = request.getHeader("X-Forwarded-For");
            if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
                // Take the first IP in the chain
                return xForwardedFor.split(",")[0].trim();
            }

            // Check for X-Real-IP header (nginx)
            String xRealIp = request.getHeader("X-Real-IP");
            if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
                return xRealIp;
            }

            // Check for X-Forwarded header
            String xForwarded = request.getHeader("X-Forwarded");
            if (xForwarded != null && !xForwarded.isEmpty() && !"unknown".equalsIgnoreCase(xForwarded)) {
                return xForwarded;
            }

            // Check for Forwarded-For header
            String forwardedFor = request.getHeader("Forwarded-For");
            if (forwardedFor != null && !forwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(forwardedFor)) {
                return forwardedFor;
            }

            // Check for Forwarded header
            String forwarded = request.getHeader("Forwarded");
            if (forwarded != null && !forwarded.isEmpty() && !"unknown".equalsIgnoreCase(forwarded)) {
                return forwarded;
            }

            // Fall back to remote address
            return request.getRemoteAddr();
        }
    }
}