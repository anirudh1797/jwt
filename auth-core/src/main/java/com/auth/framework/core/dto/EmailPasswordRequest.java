package com.auth.framework.core.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import com.auth.framework.core.strategy.AuthenticationType;

/**
 * Authentication request for email/password authentication.
 * Follows Single Responsibility Principle - only handles email/password auth data.
 */
public class EmailPasswordRequest extends AuthenticationRequest {
    
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;
    
    @NotBlank(message = "Password is required")
    private String password;

    public EmailPasswordRequest() {
        super(AuthenticationType.EMAIL_PASSWORD);
    }

    public EmailPasswordRequest(String email, String password) {
        super(AuthenticationType.EMAIL_PASSWORD);
        this.email = email;
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}