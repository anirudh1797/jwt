package com.auth.framework.core.strategy.impl;

import com.auth.framework.core.domain.User;
import com.auth.framework.core.dto.AuthenticationResult;
import com.auth.framework.core.dto.EmailPasswordRequest;
import com.auth.framework.core.exception.AuthenticationException;
import com.auth.framework.core.exception.ValidationException;
import com.auth.framework.core.repository.UserRepository;
import com.auth.framework.core.service.JwtService;
import com.auth.framework.core.service.PasswordService;
import com.auth.framework.core.strategy.AuthenticationStrategy;
import com.auth.framework.core.strategy.AuthenticationType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

/**
 * Email/password authentication strategy implementation.
 * Follows Single Responsibility Principle - only handles email/password authentication.
 * Follows Liskov Substitution Principle - can be substituted for AuthenticationStrategy.
 */
@Component
public class EmailPasswordStrategy implements AuthenticationStrategy {
    
    private final UserRepository userRepository;
    private final PasswordService passwordService;
    private final JwtService jwtService;
    
    @Autowired
    public EmailPasswordStrategy(UserRepository userRepository, 
                                PasswordService passwordService, 
                                JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordService = passwordService;
        this.jwtService = jwtService;
    }

    @Override
    public AuthenticationResult authenticate(com.auth.framework.core.dto.AuthenticationRequest request) throws AuthenticationException {
        if (!(request instanceof EmailPasswordRequest)) {
            throw new AuthenticationException("INVALID_REQUEST_TYPE", "Invalid request type for email/password authentication");
        }
        
        EmailPasswordRequest emailPasswordRequest = (EmailPasswordRequest) request;
        validateRequest(emailPasswordRequest);
        
        User user = userRepository.findActiveUserByEmail(emailPasswordRequest.getEmail())
                .orElseThrow(() -> new AuthenticationException("INVALID_CREDENTIALS", "Invalid email or password"));
        
        // Check if account is locked
        if (user.isAccountLocked()) {
            throw new AuthenticationException("ACCOUNT_LOCKED", "Account is locked due to too many failed login attempts");
        }
        
        // Check if account is enabled
        if (!user.getEnabled()) {
            throw new AuthenticationException("ACCOUNT_DISABLED", "Account is disabled");
        }
        
        // Check if account is expired
        if (!user.getAccountNonExpired()) {
            throw new AuthenticationException("ACCOUNT_EXPIRED", "Account has expired");
        }
        
        // Check if credentials are expired
        if (!user.getCredentialsNonExpired()) {
            throw new AuthenticationException("CREDENTIALS_EXPIRED", "Credentials have expired");
        }
        
        // Verify password
        if (!passwordService.matches(emailPasswordRequest.getPassword(), user.getPassword())) {
            // Increment failed login attempts
            user.incrementFailedLoginAttempts();
            userRepository.save(user);
            
            // Lock account after 5 failed attempts
            if (user.getFailedLoginAttempts() >= 5) {
                user.setLockedUntil(LocalDateTime.now().plusMinutes(30));
                userRepository.save(user);
            }
            
            throw new AuthenticationException("INVALID_CREDENTIALS", "Invalid email or password");
        }
        
        // Reset failed login attempts on successful login
        if (user.getFailedLoginAttempts() > 0) {
            user.resetFailedLoginAttempts();
        }
        
        // Update last login
        user.updateLastLogin();
        userRepository.save(user);
        
        // Generate tokens
        AuthenticationResult result = jwtService.generateTokens(user);
        
        // Add additional metadata
        result.setSessionId(generateSessionId());
        result.setLastLogin(user.getLastLogin());
        
        return result;
    }

    @Override
    public boolean supports(AuthenticationType authType) {
        return AuthenticationType.EMAIL_PASSWORD.equals(authType);
    }

    @Override
    public AuthenticationType getAuthenticationType() {
        return AuthenticationType.EMAIL_PASSWORD;
    }

    @Override
    public void validateRequest(com.auth.framework.core.dto.AuthenticationRequest request) throws ValidationException {
        if (!(request instanceof EmailPasswordRequest)) {
            throw new ValidationException("request", "INVALID_REQUEST_TYPE", "Invalid request type for email/password authentication");
        }
        
        EmailPasswordRequest emailPasswordRequest = (EmailPasswordRequest) request;
        
        if (emailPasswordRequest == null) {
            throw new ValidationException("request", "REQUEST_REQUIRED", "Authentication request is required");
        }
        
        if (emailPasswordRequest.getEmail() == null || emailPasswordRequest.getEmail().trim().isEmpty()) {
            throw new ValidationException("email", "EMAIL_REQUIRED", "Email is required");
        }
        
        if (emailPasswordRequest.getPassword() == null || emailPasswordRequest.getPassword().isEmpty()) {
            throw new ValidationException("password", "PASSWORD_REQUIRED", "Password is required");
        }
        
        if (emailPasswordRequest.getEmail().length() > 100) {
            throw new ValidationException("email", "EMAIL_TOO_LONG", "Email must be no more than 100 characters");
        }
        
        if (emailPasswordRequest.getPassword().length() > 128) {
            throw new ValidationException("password", "PASSWORD_TOO_LONG", "Password must be no more than 128 characters");
        }
        
        // Basic email format validation
        if (!emailPasswordRequest.getEmail().contains("@") || !emailPasswordRequest.getEmail().contains(".")) {
            throw new ValidationException("email", "INVALID_EMAIL_FORMAT", "Invalid email format");
        }
    }
    
    private String generateSessionId() {
        return "sess_" + System.currentTimeMillis() + "_" + (int)(Math.random() * 1000);
    }
}