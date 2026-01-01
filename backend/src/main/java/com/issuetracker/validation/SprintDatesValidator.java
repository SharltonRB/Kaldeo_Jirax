package com.issuetracker.validation;

import com.issuetracker.dto.CreateSprintRequest;
import com.issuetracker.dto.UpdateSprintRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDate;

/**
 * Validator for sprint date validation.
 * Validates that end date is after start date and dates are not in the past.
 */
public class SprintDatesValidator implements ConstraintValidator<ValidSprintDates, Object> {

    @Override
    public void initialize(ValidSprintDates constraintAnnotation) {
        // No initialization needed
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value == null) {
            return true; // Let @NotNull handle null validation
        }

        LocalDate startDate = null;
        LocalDate endDate = null;

        if (value instanceof CreateSprintRequest request) {
            startDate = request.getStartDate();
            endDate = request.getEndDate();
        } else if (value instanceof UpdateSprintRequest request) {
            startDate = request.getStartDate();
            endDate = request.getEndDate();
        } else {
            return true; // Not applicable to this type
        }

        if (startDate == null || endDate == null) {
            return true; // Let @NotNull handle null validation
        }

        // Disable default constraint violation
        context.disableDefaultConstraintViolation();

        boolean isValid = true;

        // Check if end date is after start date
        if (!endDate.isAfter(startDate)) {
            context.buildConstraintViolationWithTemplate("End date must be after start date")
                    .addPropertyNode("endDate")
                    .addConstraintViolation();
            isValid = false;
        }

        // Check if start date is not in the past (allow today)
        if (startDate.isBefore(LocalDate.now())) {
            context.buildConstraintViolationWithTemplate("Start date cannot be in the past")
                    .addPropertyNode("startDate")
                    .addConstraintViolation();
            isValid = false;
        }

        // Check if sprint duration is reasonable (not more than 6 months)
        if (startDate.plusMonths(6).isBefore(endDate)) {
            context.buildConstraintViolationWithTemplate("Sprint duration cannot exceed 6 months")
                    .addPropertyNode("endDate")
                    .addConstraintViolation();
            isValid = false;
        }

        return isValid;
    }
}