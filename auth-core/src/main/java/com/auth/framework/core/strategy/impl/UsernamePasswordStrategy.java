package com.auth.framework.core.strategy.impl;

import com.auth.framework.core.domain.User;
import com.auth.framework.core.dto.AuthenticationResult;
import com.auth.framework.core.dto.UsernamePasswordRequest;
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
 * Username/password authentication strategy implementation.
 * Follows Single Responsibility Principle - only handles username/password authentication.
 * Follows Liskov Substitution Principle - can be substituted for AuthenticationStrategy.
 */
@Component
public class UsernamePasswordStrategy implements AuthenticationStrategy {
    
    private final UserRepository userRepository;
    private final PasswordService passwordService;
    private final JwtService jwtService;
    
    @Autowired
    public UsernamePasswordStrategy(UserRepository userRepository, 
                                   PasswordService passwordService, 
                                   JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordService = passwordService;
        this.jwtService = jwtService;
    }

    @Override
    public AuthenticationResult authenticate(AuthenticationRequest request) throws AuthenticationException {
        if (!(request instanceof UsernamePasswordRequest)) {
            throw new AuthenticationException("INVALID_REQUEST_TYPE", "Invalid request type for username/password authentication");
        }
        
        UsernamePasswordRequest usernamePasswordRequest = (UsernamePasswordRequest) request;
        validateRequest(usernamePasswordRequest);
        
        User user = userRepository.findActiveUserByUsername(usernamePasswordRequest.getUsername())
                .orElseThrow(() -> new AuthenticationException("INVALID_CREDENTIALS", "Invalid username or password"));
        
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
        if (!passwordService.matches(usernamePasswordRequest.getPassword(), user.getPassword())) {
            // Increment failed login attempts
            user.incrementFailedLoginAttempts();
            userRepository.save(user);
            
            // Lock account after 5 failed attempts
            if (user.getFailedLoginAttempts() >= 5) {
                user.setLockedUntil(LocalDateTime.now().plusMinutes(30));
                userRepository.save(user);
            }
            
            throw new AuthenticationException("INVALID_CREDENTIALS", "Invalid username or password");
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
        return AuthenticationType.USERNAME_PASSWORD.equals(authType);
    }

    @Override
    public AuthenticationType getAuthenticationType() {
        return AuthenticationType.USERNAME_PASSWORD;
    }

    @Override
    public void validateRequest(AuthenticationRequest request) throws ValidationException {
        if (!(request instanceof UsernamePasswordRequest)) {
            throw new ValidationException("request", "INVALID_REQUEST_TYPE", "Invalid request type for username/password authentication");
        }
        
        UsernamePasswordRequest usernamePasswordRequest = (UsernamePasswordRequest) request;
        if (usernamePasswordRequest == null) {
            throw new ValidationException("request", "REQUEST_REQUIRED", "Authentication request is required");
        }
        
        if (usernamePasswordRequest.getUsername() == null || usernamePasswordRequest.getUsername().trim().isEmpty()) {
            throw new ValidationException("username", "USERNAME_REQUIRED", "Username is required");
        }
        
        if (usernamePasswordRequest.getPassword() == null || usernamePasswordRequest.getPassword().isEmpty()) {
            throw new ValidationException("password", "PASSWORD_REQUIRED", "Password is required");
        }
        
        if (usernamePasswordRequest.getUsername().length() > 50) {
            throw new ValidationException("username", "USERNAME_TOO_LONG", "Username must be no more than 50 characters");
        }
        
        if (usernamePasswordRequest.getPassword().length() > 128) {
            throw new ValidationException("password", "PASSWORD_TOO_LONG", "Password must be no more than 128 characters");
        }
    }
    
    private String generateSessionId() {
        return "sess_" + System.currentTimeMillis() + "_" + (int)(Math.random() * 1000);
    }
}