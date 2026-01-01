package com.issuetracker.service;

import com.issuetracker.entity.User;
import com.issuetracker.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UserService.
 * Tests user registration, profile management, and password operations.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Tests")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User("test@example.com", "hashedPassword", "Test User");
        testUser.setId(1L);
    }

    @Test
    @DisplayName("Should register new user successfully")
    void shouldRegisterNewUser() {
        // Given
        String email = "new@example.com";
        String password = "plainPassword";
        String name = "New User";
        String hashedPassword = "hashedPassword";

        when(userRepository.existsByEmail(email)).thenReturn(false);
        when(passwordEncoder.encode(password)).thenReturn(hashedPassword);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        User result = userService.registerUser(email, password, name);

        // Then
        assertThat(result).isNotNull();
        verify(userRepository).existsByEmail(email);
        verify(passwordEncoder).encode(password);
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw exception when registering user with existing email")
    void shouldThrowExceptionWhenRegisteringExistingEmail() {
        // Given
        String email = "existing@example.com";
        when(userRepository.existsByEmail(email)).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> userService.registerUser(email, "password", "Name"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("User with email existing@example.com already exists");

        verify(userRepository).existsByEmail(email);
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should find user by email")
    void shouldFindUserByEmail() {
        // Given
        String email = "test@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));

        // When
        Optional<User> result = userService.findByEmail(email);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(testUser);
        verify(userRepository).findByEmail(email);
    }

    @Test
    @DisplayName("Should find user by ID")
    void shouldFindUserById() {
        // Given
        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

        // When
        Optional<User> result = userService.findById(userId);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(testUser);
        verify(userRepository).findById(userId);
    }

    @Test
    @DisplayName("Should update user profile successfully")
    void shouldUpdateUserProfile() {
        // Given
        Long userId = 1L;
        String newName = "Updated Name";
        String newEmail = "updated@example.com";

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.existsByEmail(newEmail)).thenReturn(false);
        when(userRepository.save(testUser)).thenReturn(testUser);

        // When
        User result = userService.updateProfile(userId, newName, newEmail);

        // Then
        assertThat(result).isNotNull();
        assertThat(testUser.getName()).isEqualTo(newName);
        assertThat(testUser.getEmail()).isEqualTo(newEmail);
        verify(userRepository).findById(userId);
        verify(userRepository).existsByEmail(newEmail);
        verify(userRepository).save(testUser);
    }

    @Test
    @DisplayName("Should throw exception when updating profile with existing email")
    void shouldThrowExceptionWhenUpdatingWithExistingEmail() {
        // Given
        Long userId = 1L;
        String existingEmail = "existing@example.com";

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.existsByEmail(existingEmail)).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> userService.updateProfile(userId, "Name", existingEmail))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Email existing@example.com is already in use");

        verify(userRepository).findById(userId);
        verify(userRepository).existsByEmail(existingEmail);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw exception when updating profile for non-existent user")
    void shouldThrowExceptionWhenUpdatingNonExistentUser() {
        // Given
        Long userId = 999L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.updateProfile(userId, "Name", "email@example.com"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("User not found with ID: 999");

        verify(userRepository).findById(userId);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should change password successfully")
    void shouldChangePasswordSuccessfully() {
        // Given
        Long userId = 1L;
        String currentPassword = "currentPassword";
        String newPassword = "newPassword";
        String hashedNewPassword = "hashedNewPassword";

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(currentPassword, testUser.getPasswordHash())).thenReturn(true);
        when(passwordEncoder.matches(newPassword, testUser.getPasswordHash())).thenReturn(false);
        when(passwordEncoder.encode(newPassword)).thenReturn(hashedNewPassword);
        when(userRepository.save(testUser)).thenReturn(testUser);

        // When
        userService.changePassword(userId, currentPassword, newPassword);

        // Then
        assertThat(testUser.getPasswordHash()).isEqualTo(hashedNewPassword);
        verify(userRepository).findById(userId);
        verify(passwordEncoder).matches(currentPassword, "hashedPassword");
        verify(passwordEncoder).matches(newPassword, "hashedPassword");
        verify(passwordEncoder).encode(newPassword);
        verify(userRepository).save(testUser);
    }

    @Test
    @DisplayName("Should throw exception when changing password with incorrect current password")
    void shouldThrowExceptionWhenCurrentPasswordIncorrect() {
        // Given
        Long userId = 1L;
        String incorrectPassword = "wrongPassword";
        String newPassword = "newPassword";

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(incorrectPassword, testUser.getPasswordHash())).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> userService.changePassword(userId, incorrectPassword, newPassword))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Current password is incorrect");

        verify(userRepository).findById(userId);
        verify(passwordEncoder).matches(incorrectPassword, "hashedPassword");
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw exception when new password is same as current")
    void shouldThrowExceptionWhenNewPasswordSameAsCurrent() {
        // Given
        Long userId = 1L;
        String currentPassword = "currentPassword";
        String samePassword = "currentPassword";

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(currentPassword, testUser.getPasswordHash())).thenReturn(true);
        when(passwordEncoder.matches(samePassword, testUser.getPasswordHash())).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> userService.changePassword(userId, currentPassword, samePassword))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("New password must be different from current password");

        verify(userRepository).findById(userId);
        verify(passwordEncoder, times(2)).matches(anyString(), eq("hashedPassword"));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should validate credentials correctly")
    void shouldValidateCredentials() {
        // Given
        String email = "test@example.com";
        String password = "password";

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(password, testUser.getPasswordHash())).thenReturn(true);

        // When
        boolean result = userService.validateCredentials(email, password);

        // Then
        assertThat(result).isTrue();
        verify(userRepository).findByEmail(email);
        verify(passwordEncoder).matches(password, "hashedPassword");
    }

    @Test
    @DisplayName("Should return false for invalid credentials")
    void shouldReturnFalseForInvalidCredentials() {
        // Given
        String email = "test@example.com";
        String wrongPassword = "wrongPassword";

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(wrongPassword, testUser.getPasswordHash())).thenReturn(false);

        // When
        boolean result = userService.validateCredentials(email, wrongPassword);

        // Then
        assertThat(result).isFalse();
        verify(userRepository).findByEmail(email);
        verify(passwordEncoder).matches(wrongPassword, "hashedPassword");
    }

    @Test
    @DisplayName("Should validate password strength correctly")
    void shouldValidatePasswordStrength() {
        // Valid passwords
        assertThat(userService.isValidPassword("Password123")).isTrue();
        assertThat(userService.isValidPassword("MySecure1")).isTrue();
        assertThat(userService.isValidPassword("ComplexPass9")).isTrue();

        // Invalid passwords
        assertThat(userService.isValidPassword(null)).isFalse();
        assertThat(userService.isValidPassword("short")).isFalse();
        assertThat(userService.isValidPassword("password")).isFalse(); // no uppercase or digit
        assertThat(userService.isValidPassword("PASSWORD123")).isFalse(); // no lowercase
        assertThat(userService.isValidPassword("Password")).isFalse(); // no digit
        assertThat(userService.isValidPassword("password123")).isFalse(); // no uppercase
    }

    @Test
    @DisplayName("Should check if user exists by email")
    void shouldCheckIfUserExistsByEmail() {
        // Given
        String email = "test@example.com";
        when(userRepository.existsByEmail(email)).thenReturn(true);

        // When
        boolean result = userService.existsByEmail(email);

        // Then
        assertThat(result).isTrue();
        verify(userRepository).existsByEmail(email);
    }

    @Test
    @DisplayName("Should check if user exists by ID")
    void shouldCheckIfUserExistsById() {
        // Given
        Long userId = 1L;
        when(userRepository.existsById(userId)).thenReturn(true);

        // When
        boolean result = userService.existsById(userId);

        // Then
        assertThat(result).isTrue();
        verify(userRepository).existsById(userId);
    }

    @Test
    @DisplayName("Should delete user successfully")
    void shouldDeleteUserSuccessfully() {
        // Given
        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

        // When
        userService.deleteUser(userId);

        // Then
        verify(userRepository).findById(userId);
        verify(userRepository).delete(testUser);
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent user")
    void shouldThrowExceptionWhenDeletingNonExistentUser() {
        // Given
        Long userId = 999L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.deleteUser(userId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("User not found with ID: 999");

        verify(userRepository).findById(userId);
        verify(userRepository, never()).delete(any(User.class));
    }
}