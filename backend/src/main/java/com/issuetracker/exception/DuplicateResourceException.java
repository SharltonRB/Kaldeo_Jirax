package com.issuetracker.exception;

/**
 * Exception thrown when attempting to create a resource that already exists.
 */
public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String message) {
        super(message);
    }

    public DuplicateResourceException(String message, Throwable cause) {
        super(message, cause);
    }

    public static DuplicateResourceException projectKey(String key) {
        return new DuplicateResourceException("Project key already exists: " + key);
    }

    public static DuplicateResourceException labelName(String name) {
        return new DuplicateResourceException("Label name already exists: " + name);
    }

    public static DuplicateResourceException userEmail(String email) {
        return new DuplicateResourceException("User email already exists: " + email);
    }

    public static DuplicateResourceException issueTypeName(String name) {
        return new DuplicateResourceException("Issue type name already exists: " + name);
    }
}