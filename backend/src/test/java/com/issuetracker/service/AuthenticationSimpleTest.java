package com.issuetracker.service;

import com.issuetracker.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Simplified authentication test that works with H2.
 * This test demonstrates that authentication logic works correctly.
 * 
 * To run: mvn test -Dtest="AuthenticationSimpleTest"
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AuthenticationSimpleTest {

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserService userService;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void authenticationWorkflow_ShouldWorkCorrectly() {
        // Test data
        String email = "test@example.com";
        String password = "password123";
        String name = "Test User";
        
        // 1. Register user
        User user = userService.registerUser(email, password, name);
        assertThat(user).isNotNull();
        assertThat(user.getId()).isNotNull();
        assertThat(user.getEmail()).isEqualTo(email);
        assertThat(user.getName()).isEqualTo(name);
        
        // 2. Load user details
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);
        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUsername()).isEqualTo(email);
        
        // 3. Generate JWT token
        String token = jwtService.generateToken(userDetails);
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
        
        // 4. Validate token
        assertThat(jwtService.isTokenValid(token, userDetails)).isTrue();
        
        // 5. Extract token information
        assertThat(jwtService.extractUsername(token)).isEqualTo(email);
        
        // 6. Validate credentials
        assertThat(userService.validateCredentials(email, password)).isTrue();
        assertThat(userService.validateCredentials(email, "wrongpassword")).isFalse();
        
        System.out.println("✅ Authentication test completed successfully");
        System.out.println("📧 User: " + email);
        System.out.println("🔑 Generated token: " + token.substring(0, 20) + "...");
    }

    @Test
    void multipleUsers_ShouldWorkIndependently() {
        // User 1
        String email1 = "user1@test.com";
        String password1 = "password123";
        User user1 = userService.registerUser(email1, password1, "User One");
        
        // User 2
        String email2 = "user2@test.com";
        String password2 = "password456";
        User user2 = userService.registerUser(email2, password2, "User Two");
        
        // Verify they are different
        assertThat(user1.getId()).isNotEqualTo(user2.getId());
        assertThat(user1.getEmail()).isNotEqualTo(user2.getEmail());
        
        // Generate independent tokens
        UserDetails userDetails1 = userDetailsService.loadUserByUsername(email1);
        UserDetails userDetails2 = userDetailsService.loadUserByUsername(email2);
        
        String token1 = jwtService.generateToken(userDetails1);
        String token2 = jwtService.generateToken(userDetails2);
        
        // Tokens must be different
        assertThat(token1).isNotEqualTo(token2);
        
        // Each token should be valid only for its user
        assertThat(jwtService.isTokenValid(token1, userDetails1)).isTrue();
        assertThat(jwtService.isTokenValid(token2, userDetails2)).isTrue();
        assertThat(jwtService.isTokenValid(token1, userDetails2)).isFalse();
        assertThat(jwtService.isTokenValid(token2, userDetails1)).isFalse();
        
        System.out.println("✅ Multiple users test completed successfully");
    }

    @Test
    void passwordEncryption_ShouldWorkCorrectly() {
        String plainPassword = "mySecretPassword123";
        String email = "encryption@test.com";
        
        // Register user
        User user = userService.registerUser(email, plainPassword, "Encryption Test");
        
        // Stored password should be encrypted
        assertThat(user.getPasswordHash()).isNotEqualTo(plainPassword);
        assertThat(passwordEncoder.matches(plainPassword, user.getPasswordHash())).isTrue();
        assertThat(passwordEncoder.matches("wrongPassword", user.getPasswordHash())).isFalse();
        
        System.out.println("✅ Password encryption test completed successfully");
    }
}