package com.issuetracker.service;

import com.issuetracker.base.BaseTestcontainersTest;
import com.issuetracker.entity.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.quicktheories.QuickTheory;
import org.quicktheories.core.Gen;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.quicktheories.generators.SourceDSL.*;

/**
 * Property-based tests for authentication functionality using Testcontainers PostgreSQL.
 * 
 * This test class provides production parity by using PostgreSQL via Testcontainers.
 * Use this for:
 * - CI/CD pipelines (production parity)
 * - Testing PostgreSQL-specific features
 * - Final validation before deployment
 * 
 * To run: mvn test -Dtest="AuthenticationTestcontainersTest"
 * Note: Requires Docker to be available
 */
// @EnabledIfSystemProperty(named = "testcontainers.enabled", matches = "true") // COMENTADO - Test habilitado por defecto
class AuthenticationTestcontainersTest extends BaseTestcontainersTest {

    private static final QuickTheory qt = QuickTheory.qt();

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserService userService;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final AtomicInteger emailCounter = new AtomicInteger(0);

    /**
     * Property 2: Authentication Token Management (PostgreSQL version)
     * Tests with production database for full compatibility validation.
     */
    @Test
    @Transactional
    void authenticationTokenManagementPropertyWithPostgreSQL() {
        qt.withFixedSeed(12345L)
                .withExamples(3) // Fewer examples for Testcontainers (slower)
                .forAll(
                        uniqueEmails(),
                        validPasswords(),
                        validNames()
                )
                .checkAssert((email, password, name) -> {
                    // Register user with valid credentials
                    User user = userService.registerUser(email, password, name);
                    assertThat(user).isNotNull();
                    assertThat(user.getId()).isNotNull();
                    
                    // Load user details for token generation
                    UserDetails userDetails = userDetailsService.loadUserByUsername(email);
                    assertThat(userDetails).isNotNull();
                    
                    // Generate JWT token
                    String token = jwtService.generateToken(userDetails);
                    assertThat(token).isNotNull();
                    assertThat(token).isNotEmpty();
                    
                    // Token should be valid for the user
                    assertThat(jwtService.isTokenValid(token, userDetails)).isTrue();
                    
                    // Token should contain correct username
                    assertThat(jwtService.extractUsername(token)).isEqualTo(email);
                    
                    // Validate credentials should work
                    assertThat(userService.validateCredentials(email, password)).isTrue();
                    
                    // Invalid password should fail validation
                    assertThat(userService.validateCredentials(email, password + "invalid")).isFalse();
                });
    }

    // Generators (same as H2 version but optimized for PostgreSQL)
    private Gen<String> uniqueEmails() {
        return strings().ascii()
                .ofLengthBetween(3, 6)
                .map(s -> s.replaceAll("[^a-zA-Z0-9]", "a"))
                .map(s -> s.isEmpty() ? "user" : s)
                .map(s -> "tc" + emailCounter.incrementAndGet() + s + "@test.com");
    }

    private Gen<String> validPasswords() {
        return strings().ascii()
                .ofLengthBetween(8, 10)
                .map(s -> s.replaceAll("[^a-zA-Z0-9]", "a"))
                .map(s -> s.isEmpty() ? "password123" : s);
    }

    private Gen<String> validNames() {
        return strings().ascii()
                .ofLengthBetween(3, 6)
                .map(s -> s.replaceAll("[^a-zA-Z0-9]", "a"))
                .map(s -> s.isEmpty() ? "user" : s);
    }
}