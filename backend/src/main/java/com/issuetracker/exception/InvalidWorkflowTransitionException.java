package com.issuetracker.exception;

import com.issuetracker.entity.IssueStatus;

/**
 * Exception thrown when an invalid workflow transition is attempted.
 */
public class InvalidWorkflowTransitionException extends RuntimeException {

    public InvalidWorkflowTransitionException(String message) {
        super(message);
    }

    public InvalidWorkflowTransitionException(String message, Throwable cause) {
        super(message, cause);
    }

    public static InvalidWorkflowTransitionException transition(IssueStatus from, IssueStatus to) {
        return new InvalidWorkflowTransitionException(
            String.format("Invalid workflow transition from %s to %s", from, to)
        );
    }
}