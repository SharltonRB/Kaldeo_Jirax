package com.issuetracker.service;

import com.issuetracker.base.BasePostgreSQLTest;
import com.issuetracker.entity.User;
import org.junit.jupiter.api.Test;
import org.quicktheories.QuickTheory;
import org.quicktheories.core.Gen;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.quicktheories.generators.SourceDSL.*;

/**
 * Property-based tests for authentication functionality.
 * Tests Properties 2 and 3 from the design document.
 * Uses Testcontainers with PostgreSQL for production parity and full isolation.
 */
class AuthenticationPropertyTest extends BasePostgreSQLTest {

    // QuickTheory instance for property testing
    private static final QuickTheory qt = QuickTheory.qt();

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserService userService;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // Counter to ensure unique emails across test iterations
    private static final AtomicInteger emailCounter = new AtomicInteger(0);

    /**
     * Property 2: Authentication Token Management
     * For any valid user credentials, the authentication service should generate a valid JWT token,
     * and for any invalid credentials, authentication should be rejected with appropriate error messages.
     * 
     * Feature: personal-issue-tracker, Property 2: Authentication Token Management
     * Validates: Requirements 1.2, 1.4
     */
    @Test
    @Transactional
    void authenticationTokenManagementProperty() {
        qt.withFixedSeed(12345L) // Use fixed seed for reproducible tests
                .withExamples(5) // Small number for fast execution
                .forAll(
                        uniqueEmails(),
                        validPasswords(),
                        validNames()
                )
                .checkAssert((email, password, name) -> {
                    try {
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
                        
                    } catch (Exception e) {
                        // If any unexpected exception occurs, fail the test with details
                        throw new AssertionError("Unexpected exception during authentication test: " + e.getMessage(), e);
                    }
                });
    }

    /**
     * Property 3: User Registration with Encryption
     * For any valid email and password combination, user registration should create a new account
     * with properly encrypted password that cannot be retrieved in plain text.
     * 
     * Feature: personal-issue-tracker, Property 3: User Registration with Encryption
     * Validates: Requirements 1.1, 9.2
     */
    @Test
    @Transactional
    void userRegistrationWithEncryptionProperty() {
        qt.withFixedSeed(54321L) // Use fixed seed for reproducible tests
                .withExamples(5) // Small number for fast execution
                .forAll(
                        uniqueEmails(),
                        validPasswords(),
                        validNames()
                )
                .checkAssert((email, password, name) -> {
                    try {
                        // Register user
                        User user = userService.registerUser(email, password, name);
                        
                        // User should be created successfully
                        assertThat(user).isNotNull();
                        assertThat(user.getId()).isNotNull();
                        assertThat(user.getEmail()).isEqualTo(email);
                        assertThat(user.getName()).isEqualTo(name);
                        
                        // Password should be encrypted (not plain text)
                        assertThat(user.getPasswordHash()).isNotEqualTo(password);
                        assertThat(user.getPasswordHash()).isNotNull();
                        assertThat(user.getPasswordHash().length()).isGreaterThan(password.length());
                        
                        // Encrypted password should be verifiable
                        assertThat(passwordEncoder.matches(password, user.getPasswordHash())).isTrue();
                        
                        // Wrong password should not match
                        assertThat(passwordEncoder.matches(password + "wrong", user.getPasswordHash())).isFalse();
                        
                        // User should exist in database
                        assertThat(userService.existsByEmail(email)).isTrue();
                        
                    } catch (Exception e) {
                        // If any unexpected exception occurs, fail the test with details
                        throw new AssertionError("Unexpected exception during registration test: " + e.getMessage(), e);
                    }
                });
    }

    /**
     * Test duplicate registration behavior separately to avoid transaction conflicts
     */
    @Test
    @Transactional
    void duplicateRegistrationShouldFail() {
        String email = "duplicate-test-" + UUID.randomUUID() + "@example.com";
        String password = "testPassword123";
        String name = "Test User";
        
        // First registration should succeed
        User user1 = userService.registerUser(email, password, name);
        assertThat(user1).isNotNull();
        
        // Second registration with same email should fail
        assertThatThrownBy(() -> userService.registerUser(email, password + "different", name + "different"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already exists");
    }

    // Generators for property testing - optimized for speed

    /**
     * Generates unique email addresses to avoid conflicts between test iterations.
     * Uses UUID and counter to ensure uniqueness.
     */
    private Gen<String> uniqueEmails() {
        return strings().ascii()
                .ofLengthBetween(3, 6) // Very short for fast generation
                .map(s -> s.replaceAll("[^a-zA-Z0-9]", "a")) // Replace special chars with 'a'
                .map(s -> s.isEmpty() ? "user" : s) // Ensure not empty
                .map(s -> "u" + emailCounter.incrementAndGet() + s + "@test.com");
    }

    private Gen<String> validPasswords() {
        return strings().ascii()
                .ofLengthBetween(8, 10) // Short for fast generation
                .map(s -> s.replaceAll("[^a-zA-Z0-9]", "a")) // Replace special chars with 'a'
                .map(s -> s.isEmpty() ? "password123" : s); // Ensure not empty
    }

    private Gen<String> validNames() {
        return strings().ascii()
                .ofLengthBetween(3, 6) // Short for fast generation
                .map(s -> s.replaceAll("[^a-zA-Z0-9]", "a")) // Replace special chars with 'a'
                .map(s -> s.isEmpty() ? "user" : s); // Ensure not empty
    }
}