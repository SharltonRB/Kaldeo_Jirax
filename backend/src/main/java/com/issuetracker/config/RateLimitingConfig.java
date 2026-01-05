package com.issuetracker.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
 * Rate limiting configuration for authentication endpoints.
 * Implements rate limiting to prevent brute force attacks.
 */
@Configuration
public class RateLimitingConfig {

    /**
     * Rate limiting filter for authentication endpoints.
     * Limits authentication attempts per IP address.
     * Only active in non-test profiles.
     */
    @Component
    @Order(1)
    @Profile("!test")
    public static class AuthRateLimitingFilter extends OncePerRequestFilter {

        private final ConcurrentHashMap<String, Bucket> buckets = new ConcurrentHashMap<>();

        @Override
        protected void doFilterInternal(
                HttpServletRequest request,
                HttpServletResponse response,
                FilterChain filterChain
        ) throws ServletException, IOException {

            String requestURI = request.getRequestURI();
            
            // Apply rate limiting only to authentication endpoints
            if (requestURI.startsWith("/api/auth/")) {
                String clientIp = getClientIpAddress(request);
                Bucket bucket = getBucket(clientIp);

                if (!bucket.tryConsume(1)) {
                    response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                    response.setContentType("application/json");
                    response.getWriter().write(
                            "{\"error\":\"Too many requests\",\"message\":\"Rate limit exceeded for authentication endpoints\"}"
                    );
                    return;
                }
            }

            filterChain.doFilter(request, response);
        }

        /**
         * Gets or creates a rate limiting bucket for the given IP address.
         *
         * @param ip client IP address
         * @return rate limiting bucket
         */
        private Bucket getBucket(String ip) {
            return buckets.computeIfAbsent(ip, this::createBucket);
        }

        /**
         * Creates a new rate limiting bucket.
         * Allows 100 requests per minute for authentication endpoints (increased for development).
         *
         * @param ip client IP address
         * @return new bucket
         */
        private Bucket createBucket(String ip) {
            Bandwidth limit = Bandwidth.classic(100, Refill.intervally(100, Duration.ofMinutes(1)));
            return Bucket.builder()
                    .addLimit(limit)
                    .build();
        }

        /**
         * Extracts client IP address from request.
         *
         * @param request HTTP request
         * @return client IP address
         */
        private String getClientIpAddress(HttpServletRequest request) {
            String xForwardedFor = request.getHeader("X-Forwarded-For");
            if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
                return xForwardedFor.split(",")[0].trim();
            }

            String xRealIp = request.getHeader("X-Real-IP");
            if (xRealIp != null && !xRealIp.isEmpty()) {
                return xRealIp;
            }

            return request.getRemoteAddr();
        }
    }
}