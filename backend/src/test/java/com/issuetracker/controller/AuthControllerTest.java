package com.issuetracker.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.issuetracker.dto.LoginRequest;
import com.issuetracker.dto.RefreshRequest;
import com.issuetracker.dto.RegisterRequest;
import com.issuetracker.service.AuthenticationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for AuthController edge cases.
 * Tests invalid credentials, expired tokens, and malformed requests.
 */
@WebMvcTest(controllers = AuthController.class)
@ContextConfiguration(classes = {AuthController.class, AuthControllerTest.TestConfig.class})
@ActiveProfiles("test")
class AuthControllerTest {

    @Configuration
    @EnableWebSecurity
    static class TestConfig {
        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
            http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(authz -> authz.anyRequest().permitAll());
            return http.build();
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthenticationService authenticationService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void register_WithInvalidEmail_ShouldReturnBadRequest() throws Exception {
        RegisterRequest request = new RegisterRequest("invalid-email", "password123", "Test User");

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_WithShortPassword_ShouldReturnBadRequest() throws Exception {
        RegisterRequest request = new RegisterRequest("test@example.com", "short", "Test User");

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_WithEmptyName_ShouldReturnBadRequest() throws Exception {
        RegisterRequest request = new RegisterRequest("test@example.com", "password123", "");

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_WithDuplicateEmail_ShouldReturnConflict() throws Exception {
        RegisterRequest request = new RegisterRequest("test@example.com", "password123", "Test User");
        
        when(authenticationService.register(any(RegisterRequest.class)))
                .thenThrow(new IllegalArgumentException("User already exists"));

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void login_WithInvalidCredentials_ShouldReturnUnauthorized() throws Exception {
        LoginRequest request = new LoginRequest("test@example.com", "wrongpassword");
        
        when(authenticationService.login(any(LoginRequest.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void login_WithMalformedRequest_ShouldReturnBadRequest() throws Exception {
        String malformedJson = "{\"email\":\"test@example.com\"}"; // Missing password

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(malformedJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_WithInvalidEmailFormat_ShouldReturnBadRequest() throws Exception {
        LoginRequest request = new LoginRequest("invalid-email", "password123");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void refresh_WithExpiredToken_ShouldReturnUnauthorized() throws Exception {
        RefreshRequest request = new RefreshRequest("expired.jwt.token");
        
        when(authenticationService.refreshToken(any(String.class)))
                .thenThrow(new RuntimeException("Invalid refresh token"));

        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void refresh_WithMalformedToken_ShouldReturnUnauthorized() throws Exception {
        RefreshRequest request = new RefreshRequest("malformed-token");
        
        when(authenticationService.refreshToken(any(String.class)))
                .thenThrow(new RuntimeException("Invalid refresh token"));

        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void refresh_WithEmptyToken_ShouldReturnBadRequest() throws Exception {
        RefreshRequest request = new RefreshRequest("");

        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void allEndpoints_WithInvalidContentType_ShouldReturnUnsupportedMediaType() throws Exception {
        RegisterRequest request = new RegisterRequest("test@example.com", "password123", "Test User");

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnsupportedMediaType());
    }

    @Test
    void allEndpoints_WithEmptyBody_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(""))
                .andExpect(status().isBadRequest());
    }
}