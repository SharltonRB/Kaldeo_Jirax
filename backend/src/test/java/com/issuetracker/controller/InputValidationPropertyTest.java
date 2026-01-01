package com.issuetracker.controller;

import com.issuetracker.base.BasePostgreSQLTest;
import com.issuetracker.dto.*;
import com.issuetracker.entity.*;
import com.issuetracker.service.*;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.quicktheories.QuickTheory;
import org.quicktheories.core.Gen;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.quicktheories.generators.SourceDSL.*;

/**
 * Property-based tests for input validation consistency across all entities.
 * Tests Property 6 from the design document.
 * Uses Jakarta Bean Validation to test DTO validation without Hibernate session issues.
 */
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class InputValidationPropertyTest extends BasePostgreSQLTest {

    // QuickTheory instance for property testing
    private static final QuickTheory qt = QuickTheory.qt();

    @Autowired
    private Validator validator;

    // Counter to ensure unique values across test iterations
    private static final AtomicInteger counter = new AtomicInteger(0);

    /**
     * Property 6: Input Validation Consistency
     * For any user input across all entities (projects, issues, sprints, labels, comments),
     * the system should validate data according to defined constraints and reject invalid inputs
     * with appropriate error messages.
     * 
     * Feature: personal-issue-tracker, Property 6: Input Validation Consistency
     * Validates: Requirements 2.2, 3.2, 3.5, 4.1, 5.2, 6.5, 9.1
     */
    @Test
    void projectInputValidationProperty() {
        qt.withFixedSeed(12345L)
                .withExamples(100) // Test with 100 iterations as specified
                .forAll(
                        projectNames(),
                        projectKeys(),
                        projectDescriptions()
                )
                .checkAssert((name, key, description) -> {
                    CreateProjectRequest request = new CreateProjectRequest(name, key, description);
                    
                    // Use Jakarta Bean Validation to check constraints
                    Set<ConstraintViolation<CreateProjectRequest>> violations = validator.validate(request);
                    
                    // Determine if input should be valid based on business rules
                    boolean shouldBeValid = isValidProjectInput(name, key, description);
                    
                    if (shouldBeValid) {
                        // If input should be valid, there should be no validation violations
                        assertThat(violations)
                                .as("Valid input should not have validation violations: name='%s', key='%s', desc='%s'", 
                                    name, key, description)
                                .isEmpty();
                    } else {
                        // If input should be invalid, there should be validation violations
                        assertThat(violations)
                                .as("Invalid input should have validation violations: name='%s', key='%s', desc='%s'", 
                                    name, key, description)
                                .isNotEmpty();
                    }
                });
    }

    @Test
    void issueInputValidationProperty() {
        qt.withFixedSeed(23456L)
                .withExamples(100)
                .forAll(
                        issueTitles(),
                        issueDescriptions(),
                        priorities(),
                        storyPoints()
                )
                .checkAssert((title, description, priority, storyPoints) -> {
                    CreateIssueRequest request = new CreateIssueRequest(title, description, priority, 1L, 1L);
                    request.setStoryPoints(storyPoints);
                    
                    // Use Jakarta Bean Validation to check constraints
                    Set<ConstraintViolation<CreateIssueRequest>> violations = validator.validate(request);
                    
                    // Determine if input should be valid based on business rules
                    boolean shouldBeValid = isValidIssueInput(title, description, priority, storyPoints);
                    
                    if (shouldBeValid) {
                        // If input should be valid, there should be no validation violations
                        assertThat(violations)
                                .as("Valid input should not have validation violations: title='%s', desc='%s', priority='%s', points='%s'", 
                                    title, description, priority, storyPoints)
                                .isEmpty();
                    } else {
                        // If input should be invalid, there should be validation violations
                        assertThat(violations)
                                .as("Invalid input should have validation violations: title='%s', desc='%s', priority='%s', points='%s'", 
                                    title, description, priority, storyPoints)
                                .isNotEmpty();
                    }
                });
    }

    @Test
    void labelInputValidationProperty() {
        qt.withFixedSeed(45678L)
                .withExamples(100)
                .forAll(
                        labelNames(),
                        labelColors()
                )
                .checkAssert((name, color) -> {
                    CreateLabelRequest request = new CreateLabelRequest(name, color);
                    
                    // Use Jakarta Bean Validation to check constraints
                    Set<ConstraintViolation<CreateLabelRequest>> violations = validator.validate(request);
                    
                    // Determine if input should be valid based on business rules
                    boolean shouldBeValid = isValidLabelInput(name, color);
                    
                    if (shouldBeValid) {
                        // If input should be valid, there should be no validation violations
                        assertThat(violations)
                                .as("Valid input should not have validation violations: name='%s', color='%s'", 
                                    name, color)
                                .isEmpty();
                    } else {
                        // If input should be invalid, there should be validation violations
                        assertThat(violations)
                                .as("Invalid input should have validation violations: name='%s', color='%s'", 
                                    name, color)
                                .isNotEmpty();
                    }
                });
    }

    // Validation logic methods

    private boolean isValidProjectInput(String name, String key, String description) {
        // Project name validation: required, 1-100 characters
        if (name == null || name.trim().isEmpty() || name.length() > 100) {
            return false;
        }
        
        // Project key validation: required, 2-10 characters, uppercase letters and numbers, starts with letter
        if (key == null || key.trim().isEmpty() || key.length() < 2 || key.length() > 10) {
            return false;
        }
        if (!key.matches("^[A-Z][A-Z0-9]*$")) {
            return false;
        }
        
        // Description validation: optional, max 1000 characters
        if (description != null && description.length() > 1000) {
            return false;
        }
        
        return true;
    }

    private boolean isValidIssueInput(String title, String description, Priority priority, Integer storyPoints) {
        // Title validation: required, 1-255 characters
        if (title == null || title.trim().isEmpty() || title.length() > 255) {
            return false;
        }
        
        // Description validation: optional, max 5000 characters
        if (description != null && description.length() > 5000) {
            return false;
        }
        
        // Priority validation: required
        if (priority == null) {
            return false;
        }
        
        // Story points validation: optional, must be valid Fibonacci values
        if (storyPoints != null && !isValidStoryPoints(storyPoints)) {
            return false;
        }
        
        return true;
    }

    private boolean isValidStoryPoints(Integer storyPoints) {
        // Must match the same validation as StoryPointsValidator
        Set<Integer> validStoryPoints = Set.of(0, 1, 2, 3, 5, 8, 13, 21, 34, 55, 89);
        return validStoryPoints.contains(storyPoints);
    }

    private boolean isValidLabelInput(String name, String color) {
        // Name validation: required, 1-50 characters
        if (name == null || name.trim().isEmpty() || name.length() > 50) {
            return false;
        }
        
        // Color validation: optional, valid hex color if provided
        if (color != null && !color.matches("^#[0-9A-Fa-f]{6}$")) {
            return false;
        }
        
        return true;
    }

    // Generators for property testing

    private Gen<String> projectNames() {
        return strings().ascii().ofLengthBetween(0, 120)
                .map(s -> {
                    // Generate mix of valid and invalid names
                    int variant = s.length() % 5;
                    switch (variant) {
                        case 0: return s.length() <= 100 ? s : s.substring(0, 100); // Valid names
                        case 1: return s.length() > 100 ? s : s + "x".repeat(101 - s.length()); // Too long
                        case 2: return ""; // Empty string
                        case 3: return "   "; // Whitespace only
                        case 4: return null; // null values
                        default: return s;
                    }
                });
    }

    private Gen<String> projectKeys() {
        return strings().ascii().ofLengthBetween(0, 15)
                .map(s -> {
                    // Generate mix of valid and invalid keys
                    int variant = s.length() % 7;
                    switch (variant) {
                        case 0: // Valid keys
                            String valid = s.toUpperCase().replaceAll("[^A-Z0-9]", "A");
                            return valid.length() >= 2 && valid.length() <= 10 ? 
                                   (valid.matches("^[A-Z].*") ? valid : "A" + valid.substring(1)) : 
                                   "TEST";
                        case 1: return s.length() > 10 ? s.toUpperCase() : "TOOLONGKEY"; // Too long
                        case 2: return "a"; // Too short
                        case 3: return "lowercase"; // Invalid format
                        case 4: return "123INVALID"; // Starts with number
                        case 5: return ""; // Empty string
                        case 6: return null; // null values
                        default: return "TEST";
                    }
                });
    }

    private Gen<String> projectDescriptions() {
        return strings().ascii().ofLengthBetween(0, 1100)
                .map(s -> {
                    // Generate mix of valid and invalid descriptions
                    int variant = s.length() % 3;
                    switch (variant) {
                        case 0: return s.length() <= 1000 ? s : s.substring(0, 1000); // Valid descriptions
                        case 1: return s.length() > 1000 ? s : s + "x".repeat(1001 - s.length()); // Too long
                        case 2: return null; // null values
                        default: return s;
                    }
                });
    }

    private Gen<String> issueTitles() {
        return strings().ascii().ofLengthBetween(0, 270)
                .map(s -> {
                    // Generate mix of valid and invalid titles
                    int variant = s.length() % 4;
                    switch (variant) {
                        case 0: return s.length() <= 255 && !s.trim().isEmpty() ? s : "Valid Title"; // Valid titles
                        case 1: return s.length() > 255 ? s : s + "x".repeat(256 - s.length()); // Too long
                        case 2: return ""; // Empty string
                        case 3: return "   "; // Whitespace only
                        default: return null; // null values
                    }
                });
    }

    private Gen<String> issueDescriptions() {
        return strings().ascii().ofLengthBetween(0, 5100)
                .map(s -> {
                    // Generate mix of valid and invalid descriptions
                    int variant = s.length() % 3;
                    switch (variant) {
                        case 0: return s.length() <= 5000 ? s : s.substring(0, 5000); // Valid descriptions
                        case 1: return s.length() > 5000 ? s : s + "x".repeat(5001 - s.length()); // Too long
                        case 2: return null; // null values
                        default: return s;
                    }
                });
    }

    private Gen<Priority> priorities() {
        return integers().between(0, 4)
                .map(i -> {
                    switch (i) {
                        case 0: return Priority.LOW;
                        case 1: return Priority.MEDIUM;
                        case 2: return Priority.HIGH;
                        case 3: return Priority.CRITICAL;
                        case 4: return null; // null values
                        default: return Priority.MEDIUM;
                    }
                });
    }

    private Gen<Integer> storyPoints() {
        return integers().between(0, 10)
                .map(i -> {
                    // Generate mix of valid Fibonacci values and invalid values
                    int variant = i % 5;
                    switch (variant) {
                        case 0: // Valid Fibonacci values
                            Integer[] validValues = {0, 1, 2, 3, 5, 8, 13, 21, 34, 55, 89};
                            return validValues[i % validValues.length];
                        case 1: // Invalid positive values (not Fibonacci)
                            Integer[] invalidValues = {4, 6, 7, 9, 10, 11, 12, 14, 15, 16, 17, 18, 19, 20, 22, 100};
                            return invalidValues[i % invalidValues.length];
                        case 2: // Invalid negative values
                            return -Math.abs(i + 1);
                        case 3: // null values
                            return null;
                        case 4: // More valid Fibonacci values
                            Integer[] moreValidValues = {1, 3, 8, 21, 55};
                            return moreValidValues[i % moreValidValues.length];
                        default: 
                            return i;
                    }
                });
    }

    private Gen<String> labelNames() {
        return strings().ascii().ofLengthBetween(0, 60)
                .map(s -> {
                    // Generate mix of valid and invalid names
                    int variant = s.length() % 4;
                    switch (variant) {
                        case 0: return s.length() <= 50 && !s.trim().isEmpty() ? s : "ValidLabel"; // Valid names
                        case 1: return s.length() > 50 ? s : s + "x".repeat(51 - s.length()); // Too long
                        case 2: return ""; // Empty string
                        case 3: return "   "; // Whitespace only
                        default: return null; // null values
                    }
                });
    }

    private Gen<String> labelColors() {
        return strings().ascii().ofLengthBetween(0, 15)
                .map(s -> {
                    // Generate mix of valid and invalid colors
                    int variant = s.length() % 8;
                    switch (variant) {
                        case 0: return "#FF0000"; // Valid red
                        case 1: return "#00FF00"; // Valid green
                        case 2: return "#0000FF"; // Valid blue
                        case 3: return "#123456"; // Valid hex
                        case 4: return "FF0000"; // Missing #
                        case 5: return "#GG0000"; // Invalid hex
                        case 6: return "#FF00"; // Too short
                        case 7: return "#FF000000"; // Too long
                        default: return null; // null values
                    }
                });
    }
}