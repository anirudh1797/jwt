package com.auth.framework.core.dto;

import java.time.LocalDateTime;

/**
 * Standard error response format for API errors.
 * Follows Single Responsibility Principle - only handles error response data.
 */
public class ErrorResponse {
    
    private String errorCode;
    private String message;
    private String details;
    private LocalDateTime timestamp;
    private String path;

    public ErrorResponse() {}

    public ErrorResponse(String errorCode, String message, String details, LocalDateTime timestamp, String path) {
        this.errorCode = errorCode;
        this.message = message;
        this.details = details;
        this.timestamp = timestamp;
        this.path = path;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}