package com.issuetracker.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

/**
 * Validator for safe project names.
 * Prevents path traversal attacks and other potentially dangerous characters.
 */
public class SafeProjectNameValidator implements ConstraintValidator<SafeProjectName, String> {
    
    // Pattern to detect potentially dangerous characters
    private static final Pattern DANGEROUS_PATTERN = Pattern.compile(
        ".*(\\.\\./|\\.\\.\\\\|<|>|\\||&|;|\\$|`|\\\\|/|\\*|\\?|\\[|\\]|\\{|\\}|\\(|\\)|!|#|%|\\^|~|'|\"|=|\\+).*"
    );
    
    @Override
    public void initialize(SafeProjectName constraintAnnotation) {
        // No initialization needed
    }
    
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true; // Let @NotBlank handle null values
        }
        
        // Check for dangerous patterns
        if (DANGEROUS_PATTERN.matcher(value).matches()) {
            return false;
        }
        
        // Additional checks for common attack patterns
        String lowerValue = value.toLowerCase();
        if (lowerValue.contains("etc/passwd") || 
            lowerValue.contains("etc\\passwd") ||
            lowerValue.contains("windows/system32") ||
            lowerValue.contains("windows\\system32") ||
            lowerValue.contains("proc/") ||
            lowerValue.contains("dev/") ||
            lowerValue.contains("var/") ||
            lowerValue.contains("tmp/") ||
            lowerValue.contains("boot/")) {
            return false;
        }
        
        return true;
    }
}