package com.issuetracker.controller;

import com.issuetracker.dto.AuthResponse;
import com.issuetracker.dto.LoginRequest;
import com.issuetracker.dto.RefreshRequest;
import com.issuetracker.dto.RegisterRequest;
import com.issuetracker.dto.UserDto;
import com.issuetracker.entity.User;
import com.issuetracker.service.AuthenticationService;
import com.issuetracker.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.logging.Logger;

/**
 * REST controller for authentication operations.
 * Handles user registration, login, and token refresh.
 */
@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {

    private static final Logger logger = Logger.getLogger(AuthController.class.getName());
    private final AuthenticationService authenticationService;
    private final UserService userService;

    @Autowired
    public AuthController(AuthenticationService authenticationService, UserService userService) {
        this.authenticationService = authenticationService;
        this.userService = userService;
    }

    /**
     * Registers a new user.
     *
     * @param request registration request
     * @return authentication response with tokens
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        try {
            AuthResponse response = authenticationService.register(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            logger.warning("Registration failed - user already exists: " + request.getEmail());
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (Exception e) {
            logger.severe("Registration failed for email: " + request.getEmail() + " - " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Authenticates a user and returns tokens.
     *
     * @param request login request
     * @return authentication response with tokens
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        try {
            AuthResponse response = authenticationService.login(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.warning("Login failed for email: " + request.getEmail());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    /**
     * Refreshes access token using refresh token.
     *
     * @param request refresh request
     * @return authentication response with new tokens
     */
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        try {
            AuthResponse response = authenticationService.refreshToken(request.getRefreshToken());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    /**
     * Gets the current authenticated user's information.
     *
     * @return current user DTO
     */
    @GetMapping("/me")
    public ResponseEntity<UserDto> getCurrentUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            String email = authentication.getName();
            User user = userService.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found: " + email));
            UserDto userDto = UserDto.fromEntity(user);
            
            return ResponseEntity.ok(userDto);
        } catch (Exception e) {
            logger.warning("Failed to get current user: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}