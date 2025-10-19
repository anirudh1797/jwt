package com.auth.framework.core.exception;

/**
 * Exception for token refresh-related errors.
 * Follows Single Responsibility Principle - only handles token refresh exceptions.
 */
public class TokenRefreshException extends AuthenticationException {
    
    private final String token;

    public TokenRefreshException(String token, String message) {
        super("TOKEN_REFRESH_ERROR", message);
        this.token = token;
    }

    public TokenRefreshException(String token, String message, Throwable cause) {
        super("TOKEN_REFRESH_ERROR", message, cause);
        this.token = token;
    }

    public String getToken() {
        return token;
    }
}