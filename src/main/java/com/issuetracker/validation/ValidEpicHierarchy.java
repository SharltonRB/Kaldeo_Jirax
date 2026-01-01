package com.issuetracker.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Validation annotation for epic hierarchy rules.
 * Ensures that:
 * - Epic issues cannot have a parent
 * - Non-epic issues must have a parent epic
 */
@Documented
@Constraint(validatedBy = EpicHierarchyValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidEpicHierarchy {
    
    String message() default "Invalid epic hierarchy: Epic issues cannot have a parent, non-epic issues must have a parent epic";
    
    Class<?>[] groups() default {};
    
    Class<? extends Payload>[] payload() default {};
}