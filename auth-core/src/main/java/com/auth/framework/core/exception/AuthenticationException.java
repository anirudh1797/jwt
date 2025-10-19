package com.auth.framework.core.exception;

/**
 * Base exception for authentication-related errors.
 * Follows Single Responsibility Principle - only handles authentication exceptions.
 */
public class AuthenticationException extends RuntimeException {
    
    private final String errorCode;
    private final String errorMessage;

    public AuthenticationException(String message) {
        super(message);
        this.errorCode = "AUTH_ERROR";
        this.errorMessage = message;
    }

    public AuthenticationException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.errorMessage = message;
    }

    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "AUTH_ERROR";
        this.errorMessage = message;
    }

    public AuthenticationException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.errorMessage = message;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}