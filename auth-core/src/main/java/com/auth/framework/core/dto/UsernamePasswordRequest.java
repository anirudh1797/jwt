package com.auth.framework.core.dto;

import jakarta.validation.constraints.NotBlank;
import com.auth.framework.core.strategy.AuthenticationType;

/**
 * Authentication request for username/password authentication.
 * Follows Single Responsibility Principle - only handles username/password auth data.
 */
public class UsernamePasswordRequest extends AuthenticationRequest {
    
    @NotBlank(message = "Username is required")
    private String username;
    
    @NotBlank(message = "Password is required")
    private String password;

    public UsernamePasswordRequest() {
        super(AuthenticationType.USERNAME_PASSWORD);
    }

    public UsernamePasswordRequest(String username, String password) {
        super(AuthenticationType.USERNAME_PASSWORD);
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}