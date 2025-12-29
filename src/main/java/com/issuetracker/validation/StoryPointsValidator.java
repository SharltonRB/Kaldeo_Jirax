package com.issuetracker.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Set;

/**
 * Validator for story points validation.
 * Validates that story points follow common estimation values (Fibonacci sequence).
 */
public class StoryPointsValidator implements ConstraintValidator<ValidStoryPoints, Integer> {

    private static final Set<Integer> VALID_STORY_POINTS = Set.of(
        0, 1, 2, 3, 5, 8, 13, 21, 34, 55, 89
    );

    @Override
    public void initialize(ValidStoryPoints constraintAnnotation) {
        // No initialization needed
    }

    @Override
    public boolean isValid(Integer value, ConstraintValidatorContext context) {
        if (value == null) {
            return true; // Allow null values (optional field)
        }

        if (!VALID_STORY_POINTS.contains(value)) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                "Story points must be one of: " + VALID_STORY_POINTS.toString()
            ).addConstraintViolation();
            return false;
        }

        return true;
    }
}