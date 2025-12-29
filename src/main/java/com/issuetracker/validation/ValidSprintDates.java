package com.issuetracker.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Custom validation annotation for sprint date validation.
 * Ensures that end date is after start date and dates are not in the past.
 */
@Documented
@Constraint(validatedBy = SprintDatesValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidSprintDates {
    
    String message() default "Sprint dates are invalid";
    
    Class<?>[] groups() default {};
    
    Class<? extends Payload>[] payload() default {};
}