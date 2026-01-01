package com.issuetracker.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Validation annotation for safe project names.
 * Prevents path traversal and other potentially dangerous characters.
 */
@Documented
@Constraint(validatedBy = SafeProjectNameValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface SafeProjectName {
    String message() default "Project name contains invalid characters";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}