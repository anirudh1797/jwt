package com.auth.framework.core.exception;

/**
 * Exception for validation-related errors.
 * Follows Single Responsibility Principle - only handles validation exceptions.
 */
public class ValidationException extends RuntimeException {
    
    private final String field;
    private final String errorCode;

    public ValidationException(String message) {
        super(message);
        this.field = null;
        this.errorCode = "VALIDATION_ERROR";
    }

    public ValidationException(String field, String message) {
        super(message);
        this.field = field;
        this.errorCode = "VALIDATION_ERROR";
    }

    public ValidationException(String field, String errorCode, String message) {
        super(message);
        this.field = field;
        this.errorCode = errorCode;
    }

    public String getField() {
        return field;
    }

    public String getErrorCode() {
        return errorCode;
    }
}