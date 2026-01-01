package com.issuetracker.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Set;

/**
 * Validator for project key validation.
 * Validates project key format and ensures it doesn't contain reserved words.
 */
public class ProjectKeyValidator implements ConstraintValidator<ValidProjectKey, String> {

    private static final Set<String> RESERVED_KEYS = Set.of(
        "API", "ADMIN", "ROOT", "SYSTEM", "TEST", "DEMO", "SAMPLE", 
        "NULL", "VOID", "TEMP", "TMP", "DELETE", "REMOVE", "DROP"
    );

    @Override
    public void initialize(ValidProjectKey constraintAnnotation) {
        // No initialization needed
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.trim().isEmpty()) {
            return true; // Let @NotBlank handle empty validation
        }

        String key = value.trim().toUpperCase();

        // Disable default constraint violation
        context.disableDefaultConstraintViolation();

        boolean isValid = true;

        // Check if key contains reserved words
        if (RESERVED_KEYS.contains(key)) {
            context.buildConstraintViolationWithTemplate("Project key cannot be a reserved word: " + key)
                    .addConstraintViolation();
            isValid = false;
        }

        // Check if key starts with a number
        if (!key.isEmpty() && Character.isDigit(key.charAt(0))) {
            context.buildConstraintViolationWithTemplate("Project key cannot start with a number")
                    .addConstraintViolation();
            isValid = false;
        }

        // Check if key contains only valid characters (letters, numbers, underscore, hyphen)
        if (!key.matches("^[A-Z][A-Z0-9_-]*$")) {
            context.buildConstraintViolationWithTemplate("Project key must start with a letter and contain only uppercase letters, numbers, underscores, and hyphens")
                    .addConstraintViolation();
            isValid = false;
        }

        // Check minimum meaningful length
        if (key.length() < 2) {
            context.buildConstraintViolationWithTemplate("Project key must be at least 2 characters long")
                    .addConstraintViolation();
            isValid = false;
        }

        return isValid;
    }
}