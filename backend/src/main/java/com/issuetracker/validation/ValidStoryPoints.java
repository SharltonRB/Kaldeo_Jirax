package com.issuetracker.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Custom validation annotation for story points validation.
 * Ensures story points follow Fibonacci sequence or common estimation values.
 */
@Documented
@Constraint(validatedBy = StoryPointsValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidStoryPoints {
    
    String message() default "Story points must be a valid estimation value";
    
    Class<?>[] groups() default {};
    
    Class<? extends Payload>[] payload() default {};
}