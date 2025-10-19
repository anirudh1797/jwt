package com.auth.framework.core.strategy.impl;

import com.auth.framework.core.domain.User;
import com.auth.framework.core.dto.AuthenticationResult;
import com.auth.framework.core.dto.AuthenticationRequest;
import com.auth.framework.core.exception.AuthenticationException;
import com.auth.framework.core.exception.ValidationException;
import com.auth.framework.core.repository.UserRepository;
import com.auth.framework.core.service.JwtService;
import com.auth.framework.core.strategy.AuthenticationStrategy;
import com.auth.framework.core.strategy.AuthenticationType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

/**
 * API Key authentication strategy implementation.
 * Follows Single Responsibility Principle - only handles API key authentication.
 * Follows Liskov Substitution Principle - can be substituted for AuthenticationStrategy.
 */
@Component
public class ApiKeyStrategy implements AuthenticationStrategy {
    
    private final UserRepository userRepository;
    private final JwtService jwtService;
    
    @Autowired
    public ApiKeyStrategy(UserRepository userRepository, JwtService jwtService) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
    }

    @Override
    public AuthenticationResult authenticate(AuthenticationRequest request) throws AuthenticationException {
        validateRequest(request);
        
        String apiKey = extractApiKey(request);
        
        // In a real implementation, you would have an API key repository
        // For demo purposes, we'll simulate API key validation
        User user = validateApiKey(apiKey);
        
        if (user == null) {
            throw new AuthenticationException("INVALID_API_KEY", "Invalid API key");
        }
        
        // Check if account is enabled
        if (!user.getEnabled()) {
            throw new AuthenticationException("ACCOUNT_DISABLED", "Account is disabled");
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
        return AuthenticationType.API_KEY.equals(authType);
    }

    @Override
    public AuthenticationType getAuthenticationType() {
        return AuthenticationType.API_KEY;
    }

    @Override
    public void validateRequest(AuthenticationRequest request) throws ValidationException {
        if (request == null) {
            throw new ValidationException("request", "REQUEST_REQUIRED", "Authentication request is required");
        }
        
        String apiKey = extractApiKey(request);
        
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new ValidationException("apiKey", "API_KEY_REQUIRED", "API key is required");
        }
        
        if (apiKey.length() < 32) {
            throw new ValidationException("apiKey", "API_KEY_TOO_SHORT", "API key must be at least 32 characters");
        }
        
        if (apiKey.length() > 128) {
            throw new ValidationException("apiKey", "API_KEY_TOO_LONG", "API key must be no more than 128 characters");
        }
    }
    
    private String extractApiKey(AuthenticationRequest request) {
        // In a real implementation, you would extract the API key from headers or request parameters
        // For demo purposes, we'll use a custom field or header
        return request.getClientId(); // Using clientId as API key for demo
    }
    
    private User validateApiKey(String apiKey) {
        // In a real implementation, you would:
        // 1. Look up the API key in a dedicated API key table
        // 2. Check if the API key is active and not expired
        // 3. Return the associated user
        
        // For demo purposes, we'll simulate this by looking for a user with a specific pattern
        // In production, you would have a proper API key entity and repository
        if (apiKey != null && apiKey.startsWith("ak_")) {
            // Simulate finding a user by API key
            // In real implementation, you'd query an API key repository
            return userRepository.findActiveUserByUsername("api_user")
                    .orElse(null);
        }
        
        return null;
    }
    
    private String generateSessionId() {
        return "sess_" + System.currentTimeMillis() + "_" + (int)(Math.random() * 1000);
    }
}