package com.issuetracker.config;

import com.issuetracker.dto.ErrorResponse;
import com.issuetracker.exception.DuplicateResourceException;
import com.issuetracker.exception.InvalidSprintOperationException;
import com.issuetracker.exception.InvalidWorkflowTransitionException;
import com.issuetracker.exception.ResourceNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler for consistent error responses across the application.
 * Handles all types of exceptions and converts them to standardized error responses.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handle validation errors from @Valid annotations
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        
        logger.warn("Validation error on {}: {}", request.getRequestURI(), ex.getMessage());
        
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        ErrorResponse errorResponse = ErrorResponse.builder()
                .code("VALIDATION_ERROR")
                .message("Input validation failed")
                .details(errors)
                .path(request.getRequestURI())
                .timestamp(Instant.now())
                .build();

        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * Handle constraint violation errors
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(
            ConstraintViolationException ex, HttpServletRequest request) {
        
        logger.warn("Constraint violation on {}: {}", request.getRequestURI(), ex.getMessage());
        
        Map<String, String> errors = new HashMap<>();
        for (ConstraintViolation<?> violation : ex.getConstraintViolations()) {
            String fieldName = violation.getPropertyPath().toString();
            String errorMessage = violation.getMessage();
            errors.put(fieldName, errorMessage);
        }

        ErrorResponse errorResponse = ErrorResponse.builder()
                .code("VALIDATION_ERROR")
                .message("Constraint validation failed")
                .details(errors)
                .path(request.getRequestURI())
                .timestamp(Instant.now())
                .build();

        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * Handle resource not found errors
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(
            ResourceNotFoundException ex, HttpServletRequest request) {
        
        logger.warn("Resource not found on {}: {}", request.getRequestURI(), ex.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .code("RESOURCE_NOT_FOUND")
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .timestamp(Instant.now())
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    /**
     * Handle duplicate resource errors
     */
    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateResource(
            DuplicateResourceException ex, HttpServletRequest request) {
        
        logger.warn("Duplicate resource on {}: {}", request.getRequestURI(), ex.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .code("DUPLICATE_RESOURCE")
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .timestamp(Instant.now())
                .build();

        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    /**
     * Handle invalid workflow transition errors
     */
    @ExceptionHandler(InvalidWorkflowTransitionException.class)
    public ResponseEntity<ErrorResponse> handleInvalidWorkflowTransition(
            InvalidWorkflowTransitionException ex, HttpServletRequest request) {
        
        logger.warn("Invalid workflow transition on {}: {}", request.getRequestURI(), ex.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .code("INVALID_WORKFLOW_TRANSITION")
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .timestamp(Instant.now())
                .build();

        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * Handle invalid sprint operation errors
     */
    @ExceptionHandler(InvalidSprintOperationException.class)
    public ResponseEntity<ErrorResponse> handleInvalidSprintOperation(
            InvalidSprintOperationException ex, HttpServletRequest request) {
        
        logger.warn("Invalid sprint operation on {}: {}", request.getRequestURI(), ex.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .code("INVALID_SPRINT_OPERATION")
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .timestamp(Instant.now())
                .build();

        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * Handle authentication errors
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthentication(
            AuthenticationException ex, HttpServletRequest request) {
        
        logger.warn("Authentication error on {}: {}", request.getRequestURI(), ex.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .code("AUTHENTICATION_ERROR")
                .message("Authentication failed")
                .path(request.getRequestURI())
                .timestamp(Instant.now())
                .build();

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    /**
     * Handle bad credentials errors
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(
            BadCredentialsException ex, HttpServletRequest request) {
        
        logger.warn("Bad credentials on {}: {}", request.getRequestURI(), ex.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .code("INVALID_CREDENTIALS")
                .message("Invalid username or password")
                .path(request.getRequestURI())
                .timestamp(Instant.now())
                .build();

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    /**
     * Handle access denied errors
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(
            AccessDeniedException ex, HttpServletRequest request) {
        
        logger.warn("Access denied on {}: {}", request.getRequestURI(), ex.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .code("ACCESS_DENIED")
                .message("Access denied")
                .path(request.getRequestURI())
                .timestamp(Instant.now())
                .build();

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
    }

    /**
     * Handle data integrity violation errors (SQL constraints)
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(
            DataIntegrityViolationException ex, HttpServletRequest request) {
        
        logger.error("Data integrity violation on {}: {}", request.getRequestURI(), ex.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .code("DATA_INTEGRITY_ERROR")
                .message("Data integrity constraint violation")
                .path(request.getRequestURI())
                .timestamp(Instant.now())
                .build();

        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    /**
     * Handle malformed JSON requests
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex, HttpServletRequest request) {
        
        logger.warn("Malformed request on {}: {}", request.getRequestURI(), ex.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .code("MALFORMED_REQUEST")
                .message("Malformed JSON request")
                .path(request.getRequestURI())
                .timestamp(Instant.now())
                .build();

        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * Handle method argument type mismatch
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatch(
            MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        
        logger.warn("Type mismatch on {}: {}", request.getRequestURI(), ex.getMessage());
        
        Map<String, String> details = new HashMap<>();
        details.put(ex.getName(), "Invalid value: " + ex.getValue());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .code("TYPE_MISMATCH")
                .message("Invalid parameter type")
                .details(details)
                .path(request.getRequestURI())
                .timestamp(Instant.now())
                .build();

        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * Handle unsupported media type errors
     */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleUnsupportedMediaType(
            HttpMediaTypeNotSupportedException ex, HttpServletRequest request) {
        
        logger.warn("Unsupported media type on {}: {}", request.getRequestURI(), ex.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .code("UNSUPPORTED_MEDIA_TYPE")
                .message("Unsupported media type: " + ex.getContentType())
                .path(request.getRequestURI())
                .timestamp(Instant.now())
                .build();

        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body(errorResponse);
    }

    /**
     * Handle unsupported HTTP methods
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotSupported(
            HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {
        
        logger.warn("Method not supported on {}: {}", request.getRequestURI(), ex.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .code("METHOD_NOT_SUPPORTED")
                .message("HTTP method not supported: " + ex.getMethod())
                .path(request.getRequestURI())
                .timestamp(Instant.now())
                .build();

        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(errorResponse);
    }

    /**
     * Handle 404 errors
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoHandlerFound(
            NoHandlerFoundException ex, HttpServletRequest request) {
        
        logger.warn("No handler found for {}: {}", request.getRequestURI(), ex.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .code("ENDPOINT_NOT_FOUND")
                .message("Endpoint not found")
                .path(request.getRequestURI())
                .timestamp(Instant.now())
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    /**
     * Handle all other unexpected errors
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex, HttpServletRequest request) {
        
        // Log full error details for debugging (server-side only)
        logger.error("Unexpected error on {}: {}", request.getRequestURI(), ex.getMessage(), ex);
        
        // Return generic error message to prevent information leakage
        ErrorResponse errorResponse = ErrorResponse.builder()
                .code("INTERNAL_SERVER_ERROR")
                .message("An unexpected error occurred. Please try again later.")
                .path(request.getRequestURI())
                .timestamp(Instant.now())
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    /**
     * Handle SQL-related exceptions to prevent information leakage
     */
    @ExceptionHandler({
        org.springframework.dao.DataAccessException.class,
        java.sql.SQLException.class,
        org.hibernate.exception.SQLGrammarException.class,
        org.hibernate.exception.ConstraintViolationException.class
    })
    public ResponseEntity<ErrorResponse> handleDatabaseException(
            Exception ex, HttpServletRequest request) {
        
        // Log full error details for debugging (server-side only)
        logger.error("Database error on {}: {}", request.getRequestURI(), ex.getMessage(), ex);
        
        // Return generic error message to prevent SQL injection information leakage
        ErrorResponse errorResponse = ErrorResponse.builder()
                .code("DATABASE_ERROR")
                .message("A database error occurred. Please try again later.")
                .path(request.getRequestURI())
                .timestamp(Instant.now())
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    /**
     * Handle security-related exceptions
     */
    @ExceptionHandler({
        org.springframework.security.core.userdetails.UsernameNotFoundException.class,
        org.springframework.security.authentication.DisabledException.class,
        org.springframework.security.authentication.LockedException.class,
        org.springframework.security.authentication.AccountExpiredException.class,
        org.springframework.security.authentication.CredentialsExpiredException.class
    })
    public ResponseEntity<ErrorResponse> handleSecurityException(
            Exception ex, HttpServletRequest request) {
        
        // Log security events for monitoring
        logger.warn("Security exception on {}: {}", request.getRequestURI(), ex.getClass().getSimpleName());
        
        // Return generic authentication error to prevent user enumeration
        ErrorResponse errorResponse = ErrorResponse.builder()
                .code("AUTHENTICATION_ERROR")
                .message("Authentication failed. Please check your credentials.")
                .path(request.getRequestURI())
                .timestamp(Instant.now())
                .build();

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }
}