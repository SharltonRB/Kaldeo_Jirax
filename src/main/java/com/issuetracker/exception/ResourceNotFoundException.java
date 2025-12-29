package com.issuetracker.exception;

/**
 * Exception thrown when a requested resource is not found.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public static ResourceNotFoundException project(Long id) {
        return new ResourceNotFoundException("Project not found with id: " + id);
    }

    public static ResourceNotFoundException projectByKey(String key) {
        return new ResourceNotFoundException("Project not found with key: " + key);
    }

    public static ResourceNotFoundException issue(Long id) {
        return new ResourceNotFoundException("Issue not found with id: " + id);
    }

    public static ResourceNotFoundException sprint(Long id) {
        return new ResourceNotFoundException("Sprint not found with id: " + id);
    }

    public static ResourceNotFoundException label(Long id) {
        return new ResourceNotFoundException("Label not found with id: " + id);
    }

    public static ResourceNotFoundException comment(Long id) {
        return new ResourceNotFoundException("Comment not found with id: " + id);
    }

    public static ResourceNotFoundException user(Long id) {
        return new ResourceNotFoundException("User not found with id: " + id);
    }
}