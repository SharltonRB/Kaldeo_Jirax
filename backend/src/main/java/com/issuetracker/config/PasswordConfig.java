package com.issuetracker.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Configuration for password encoding.
 * Provides BCrypt password encoder for secure password hashing.
 */
@Configuration
public class PasswordConfig {

    /**
     * BCrypt password encoder bean.
     * Uses BCrypt hashing algorithm with default strength (10 rounds).
     *
     * @return BCrypt password encoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}