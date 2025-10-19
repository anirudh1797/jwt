package com.auth.framework.core.dto;

import com.auth.framework.core.domain.User;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Result of token validation containing user information and validation status.
 * Follows Single Responsibility Principle - only handles token validation result data.
 */
public class TokenValidationResult {
    
    private boolean valid;
    private User user;
    private String username;
    private List<String> roles;
    private LocalDateTime issuedAt;
    private LocalDateTime expiresAt;
    private String errorMessage;
    private String errorCode;

    public TokenValidationResult() {}

    public TokenValidationResult(boolean valid) {
        this.valid = valid;
    }

    public TokenValidationResult(boolean valid, String errorMessage) {
        this.valid = valid;
        this.errorMessage = errorMessage;
    }

    public TokenValidationResult(boolean valid, String errorCode, String errorMessage) {
        this.valid = valid;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public LocalDateTime getIssuedAt() {
        return issuedAt;
    }

    public void setIssuedAt(LocalDateTime issuedAt) {
        this.issuedAt = issuedAt;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }
}