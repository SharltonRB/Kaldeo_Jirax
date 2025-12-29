package com.issuetracker.exception;

/**
 * Exception thrown when an invalid sprint operation is attempted.
 */
public class InvalidSprintOperationException extends RuntimeException {

    public InvalidSprintOperationException(String message) {
        super(message);
    }

    public InvalidSprintOperationException(String message, Throwable cause) {
        super(message, cause);
    }

    public static InvalidSprintOperationException dateValidation(String message) {
        return new InvalidSprintOperationException("Invalid sprint dates: " + message);
    }

    public static InvalidSprintOperationException activeSprintExists() {
        return new InvalidSprintOperationException("Cannot activate sprint: another sprint is already active");
    }

    public static InvalidSprintOperationException sprintNotActive() {
        return new InvalidSprintOperationException("Sprint is not active and cannot be completed");
    }

    public static InvalidSprintOperationException overlappingSprints() {
        return new InvalidSprintOperationException("Sprint dates overlap with existing sprint");
    }
}