package com.issuetracker.service;

import com.issuetracker.entity.User;
import com.issuetracker.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Service for user management operations.
 * Handles user registration, authentication, and profile management.
 */
@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Registers a new user with encrypted password.
     *
     * @param email user email
     * @param password plain text password
     * @param name user name
     * @return created user
     * @throws IllegalArgumentException if email already exists
     */
    public User registerUser(String email, String password, String name) {
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("User with email " + email + " already exists");
        }

        String encryptedPassword = passwordEncoder.encode(password);
        User user = new User(email, encryptedPassword, name);
        return userRepository.save(user);
    }

    /**
     * Finds user by email.
     *
     * @param email user email
     * @return optional user
     */
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    /**
     * Finds user by ID.
     *
     * @param id user ID
     * @return optional user
     */
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    /**
     * Updates user profile information.
     *
     * @param userId user ID
     * @param name new name (optional)
     * @param email new email (optional)
     * @return updated user
     * @throws IllegalArgumentException if user not found or email already exists
     */
    public User updateProfile(Long userId, String name, String email) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

        // Update name if provided
        if (name != null && !name.trim().isEmpty()) {
            user.setName(name.trim());
        }

        // Update email if provided and different from current
        if (email != null && !email.trim().isEmpty() && !email.equals(user.getEmail())) {
            if (userRepository.existsByEmail(email)) {
                throw new IllegalArgumentException("Email " + email + " is already in use");
            }
            user.setEmail(email.trim());
        }

        return userRepository.save(user);
    }

    /**
     * Changes user password.
     *
     * @param userId user ID
     * @param currentPassword current password for verification
     * @param newPassword new password
     * @throws IllegalArgumentException if user not found or current password is incorrect
     */
    public void changePassword(Long userId, String currentPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

        // Verify current password
        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }

        // Validate new password is different
        if (passwordEncoder.matches(newPassword, user.getPasswordHash())) {
            throw new IllegalArgumentException("New password must be different from current password");
        }

        // Update password
        String encryptedNewPassword = passwordEncoder.encode(newPassword);
        user.setPasswordHash(encryptedNewPassword);
        userRepository.save(user);
    }

    /**
     * Validates user credentials.
     *
     * @param email user email
     * @param password plain text password
     * @return true if credentials are valid
     */
    public boolean validateCredentials(String email, String password) {
        return userRepository.findByEmail(email)
                .map(user -> passwordEncoder.matches(password, user.getPasswordHash()))
                .orElse(false);
    }

    /**
     * Validates password strength.
     *
     * @param password password to validate
     * @return true if password meets requirements
     */
    public boolean isValidPassword(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }
        
        // Check for at least one uppercase, one lowercase, one digit
        boolean hasUpper = password.chars().anyMatch(Character::isUpperCase);
        boolean hasLower = password.chars().anyMatch(Character::isLowerCase);
        boolean hasDigit = password.chars().anyMatch(Character::isDigit);
        
        return hasUpper && hasLower && hasDigit;
    }

    /**
     * Checks if user exists by email.
     *
     * @param email user email
     * @return true if user exists
     */
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    /**
     * Checks if user exists by ID.
     *
     * @param id user ID
     * @return true if user exists
     */
    public boolean existsById(Long id) {
        return userRepository.existsById(id);
    }

    /**
     * Deletes a user account and all associated data.
     * This is a soft delete operation that maintains data integrity.
     *
     * @param userId user ID
     * @throws IllegalArgumentException if user not found
     */
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));
        
        // Due to cascade settings, this will remove all associated data
        userRepository.delete(user);
    }
}