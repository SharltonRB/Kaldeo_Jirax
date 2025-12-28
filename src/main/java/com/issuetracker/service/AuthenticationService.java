package com.issuetracker.service;

import com.issuetracker.dto.AuthResponse;
import com.issuetracker.dto.LoginRequest;
import com.issuetracker.dto.RegisterRequest;
import com.issuetracker.dto.UserDto;
import com.issuetracker.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for authentication operations.
 * Handles user registration, login, and token refresh.
 */
@Service
@Transactional
public class AuthenticationService {

    private final UserService userService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsServiceImpl userDetailsService;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    @Autowired
    public AuthenticationService(
            UserService userService,
            JwtService jwtService,
            AuthenticationManager authenticationManager,
            UserDetailsServiceImpl userDetailsService
    ) {
        this.userService = userService;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
    }

    /**
     * Registers a new user.
     *
     * @param request registration request
     * @return authentication response with tokens
     */
    public AuthResponse register(RegisterRequest request) {
        // Register user
        User user = userService.registerUser(request.getEmail(), request.getPassword(), request.getName());
        
        // Load user details for token generation
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        
        // Generate tokens
        String accessToken = jwtService.generateToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);
        
        // Create response
        UserDto userDto = UserDto.fromEntity(user);
        return new AuthResponse(accessToken, refreshToken, jwtExpiration / 1000, userDto);
    }

    /**
     * Authenticates a user and returns tokens.
     *
     * @param request login request
     * @return authentication response with tokens
     */
    public AuthResponse login(LoginRequest request) {
        // Authenticate user
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        
        // Load user details
        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getEmail());
        User user = userService.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Generate tokens
        String accessToken = jwtService.generateToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);
        
        // Create response
        UserDto userDto = UserDto.fromEntity(user);
        return new AuthResponse(accessToken, refreshToken, jwtExpiration / 1000, userDto);
    }

    /**
     * Refreshes access token using refresh token.
     *
     * @param refreshToken refresh token
     * @return authentication response with new tokens
     */
    public AuthResponse refreshToken(String refreshToken) {
        // Extract username from refresh token
        String userEmail = jwtService.extractUsername(refreshToken);
        
        if (userEmail != null) {
            // Load user details
            UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);
            
            // Validate refresh token
            if (jwtService.isTokenValid(refreshToken, userDetails)) {
                User user = userService.findByEmail(userEmail)
                        .orElseThrow(() -> new RuntimeException("User not found"));
                
                // Generate new tokens
                String accessToken = jwtService.generateToken(userDetails);
                String newRefreshToken = jwtService.generateRefreshToken(userDetails);
                
                // Create response
                UserDto userDto = UserDto.fromEntity(user);
                return new AuthResponse(accessToken, newRefreshToken, jwtExpiration / 1000, userDto);
            }
        }
        
        throw new RuntimeException("Invalid refresh token");
    }
}