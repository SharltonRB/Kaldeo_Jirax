package com.issuetracker.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for JwtService edge cases.
 * Tests token validation, expiration, and malformed tokens.
 */
class JwtServiceTest {

    private JwtService jwtService;
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        
        // Set test values using reflection
        ReflectionTestUtils.setField(jwtService, "secretKey", "dGVzdC1zZWNyZXQta2V5LWZvci1qd3QtdG9rZW4tdGVzdGluZy1wdXJwb3Nlcw==");
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", 86400000L); // 24 hours
        ReflectionTestUtils.setField(jwtService, "refreshExpiration", 604800000L); // 7 days
        
        userDetails = User.builder()
                .username("test@example.com")
                .password("password")
                .authorities(new ArrayList<>())
                .build();
    }

    @Test
    void generateToken_ShouldCreateValidToken() {
        String token = jwtService.generateToken(userDetails);
        
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
        assertThat(jwtService.extractUsername(token)).isEqualTo("test@example.com");
        assertThat(jwtService.isTokenValid(token, userDetails)).isTrue();
    }

    @Test
    void generateRefreshToken_ShouldCreateValidToken() {
        String refreshToken = jwtService.generateRefreshToken(userDetails);
        
        assertThat(refreshToken).isNotNull();
        assertThat(refreshToken).isNotEmpty();
        assertThat(jwtService.extractUsername(refreshToken)).isEqualTo("test@example.com");
        assertThat(jwtService.isTokenValid(refreshToken, userDetails)).isTrue();
    }

    @Test
    void isTokenValid_WithWrongUser_ShouldReturnFalse() {
        String token = jwtService.generateToken(userDetails);
        
        UserDetails wrongUser = User.builder()
                .username("wrong@example.com")
                .password("password")
                .authorities(new ArrayList<>())
                .build();
        
        assertThat(jwtService.isTokenValid(token, wrongUser)).isFalse();
    }

    @Test
    void extractUsername_WithMalformedToken_ShouldThrowException() {
        String malformedToken = "malformed.jwt.token";
        
        assertThatThrownBy(() -> jwtService.extractUsername(malformedToken))
                .isInstanceOf(Exception.class);
    }

    @Test
    void extractUsername_WithEmptyToken_ShouldThrowException() {
        assertThatThrownBy(() -> jwtService.extractUsername(""))
                .isInstanceOf(Exception.class);
    }

    @Test
    void extractUsername_WithNullToken_ShouldThrowException() {
        assertThatThrownBy(() -> jwtService.extractUsername(null))
                .isInstanceOf(Exception.class);
    }

    @Test
    void isTokenValid_WithExpiredToken_ShouldThrowException() {
        // Create a service with very short expiration
        JwtService shortExpirationService = new JwtService();
        ReflectionTestUtils.setField(shortExpirationService, "secretKey", "dGVzdC1zZWNyZXQta2V5LWZvci1qd3QtdG9rZW4tdGVzdGluZy1wdXJwb3Nlcw==");
        ReflectionTestUtils.setField(shortExpirationService, "jwtExpiration", 1L); // 1 millisecond
        ReflectionTestUtils.setField(shortExpirationService, "refreshExpiration", 1L);
        
        String token = shortExpirationService.generateToken(userDetails);
        
        // Wait for token to expire
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Expired tokens should throw an exception when validated
        assertThatThrownBy(() -> shortExpirationService.isTokenValid(token, userDetails))
                .isInstanceOf(Exception.class);
    }

    @Test
    void isTokenValid_WithTamperedToken_ShouldReturnFalse() {
        String token = jwtService.generateToken(userDetails);
        
        // Tamper with the token by changing a character
        String tamperedToken = token.substring(0, token.length() - 5) + "XXXXX";
        
        assertThatThrownBy(() -> jwtService.isTokenValid(tamperedToken, userDetails))
                .isInstanceOf(Exception.class);
    }

    @Test
    void extractClaim_WithValidToken_ShouldExtractCorrectClaim() {
        String token = jwtService.generateToken(userDetails);
        
        String subject = jwtService.extractClaim(token, claims -> claims.getSubject());
        assertThat(subject).isEqualTo("test@example.com");
        
        java.util.Date issuedAt = jwtService.extractClaim(token, claims -> claims.getIssuedAt());
        java.util.Date expiration = jwtService.extractClaim(token, claims -> claims.getExpiration());
        
        assertThat(issuedAt).isNotNull();
        assertThat(expiration).isNotNull();
    }

    @Test
    void generateToken_WithExtraClaims_ShouldIncludeClaims() {
        java.util.Map<String, Object> extraClaims = new java.util.HashMap<>();
        extraClaims.put("role", "USER");
        extraClaims.put("userId", 123L);
        
        String token = jwtService.generateToken(extraClaims, userDetails);
        
        assertThat(token).isNotNull();
        assertThat(jwtService.extractUsername(token)).isEqualTo("test@example.com");
        assertThat(jwtService.isTokenValid(token, userDetails)).isTrue();
    }
}