package com.issuetracker.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Security headers configuration filter.
 * Adds comprehensive security headers to all HTTP responses.
 */
@Component
@Order(2)
public class SecurityHeadersConfig extends OncePerRequestFilter {

    @Value("${security.csp.policy:default-src 'self'; script-src 'self' 'unsafe-inline' 'unsafe-eval'; style-src 'self' 'unsafe-inline'; img-src 'self' data: https:; font-src 'self' data:; connect-src 'self'; frame-ancestors 'none'; base-uri 'self'; form-action 'self'}")
    private String cspPolicy;

    @Value("${security.hsts.max-age:31536000}")
    private long hstsMaxAge;

    @Value("${security.hsts.include-subdomains:true}")
    private boolean hstsIncludeSubdomains;

    @Value("${security.hsts.preload:true}")
    private boolean hstsPreload;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        // Content Security Policy
        response.setHeader("Content-Security-Policy", cspPolicy);
        
        // Strict Transport Security (HSTS) - only for HTTPS
        if (request.isSecure()) {
            StringBuilder hstsValue = new StringBuilder("max-age=" + hstsMaxAge);
            if (hstsIncludeSubdomains) {
                hstsValue.append("; includeSubDomains");
            }
            if (hstsPreload) {
                hstsValue.append("; preload");
            }
            response.setHeader("Strict-Transport-Security", hstsValue.toString());
        }
        
        // X-Frame-Options (prevent clickjacking)
        response.setHeader("X-Frame-Options", "DENY");
        
        // X-Content-Type-Options (prevent MIME sniffing)
        response.setHeader("X-Content-Type-Options", "nosniff");
        
        // X-XSS-Protection (legacy XSS protection)
        response.setHeader("X-XSS-Protection", "1; mode=block");
        
        // Referrer Policy
        response.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");
        
        // Permissions Policy (formerly Feature Policy)
        response.setHeader("Permissions-Policy", 
            "camera=(), microphone=(), geolocation=(), payment=(), usb=(), " +
            "accelerometer=(), gyroscope=(), magnetometer=(), " +
            "fullscreen=(self), picture-in-picture=(self)");
        
        // Cross-Origin Embedder Policy
        response.setHeader("Cross-Origin-Embedder-Policy", "require-corp");
        
        // Cross-Origin Opener Policy
        response.setHeader("Cross-Origin-Opener-Policy", "same-origin");
        
        // Cross-Origin Resource Policy
        response.setHeader("Cross-Origin-Resource-Policy", "same-origin");
        
        // Cache Control for sensitive endpoints
        String requestURI = request.getRequestURI();
        if (requestURI.startsWith("/api/auth/") || 
            requestURI.contains("/user") || 
            requestURI.contains("/profile")) {
            response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate, private");
            response.setHeader("Pragma", "no-cache");
            response.setHeader("Expires", "0");
        }
        
        // Remove server information to prevent fingerprinting
        response.setHeader("Server", "");
        
        // Add security-focused headers for API responses
        if (requestURI.startsWith("/api/")) {
            response.setHeader("X-Robots-Tag", "noindex, nofollow, nosnippet, noarchive");
        }

        filterChain.doFilter(request, response);
    }
}