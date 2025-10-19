package com.auth.framework.core.service.impl;

import com.auth.framework.core.dto.AuthenticationRequest;
import com.auth.framework.core.dto.AuthenticationResult;
import com.auth.framework.core.exception.AuthenticationException;
import com.auth.framework.core.service.AuthenticationManager;
import com.auth.framework.core.strategy.AuthenticationStrategy;
import com.auth.framework.core.strategy.AuthenticationType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementation of authentication manager service.
 * Follows Single Responsibility Principle - only handles authentication management.
 * Follows Open/Closed Principle - easy to add new authentication strategies.
 * Follows Dependency Inversion Principle - depends on abstractions.
 */
@Service
public class AuthenticationManagerImpl implements AuthenticationManager {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthenticationManagerImpl.class);
    
    private final Map<AuthenticationType, AuthenticationStrategy> strategies = new ConcurrentHashMap<>();
    
    @Autowired
    public AuthenticationManagerImpl(List<AuthenticationStrategy> strategyList) {
        // Register all available strategies
        for (AuthenticationStrategy strategy : strategyList) {
            registerStrategy(strategy);
        }
        logger.info("Registered {} authentication strategies", strategies.size());
    }

    @Override
    public AuthenticationResult authenticate(AuthenticationRequest request) throws AuthenticationException {
        if (request == null) {
            throw new AuthenticationException("INVALID_REQUEST", "Authentication request cannot be null");
        }
        
        AuthenticationType authType = request.getAuthenticationType();
        AuthenticationStrategy strategy = strategies.get(authType);
        
        if (strategy == null) {
            throw new AuthenticationException("UNSUPPORTED_AUTH_TYPE", 
                "Authentication type not supported: " + authType);
        }
        
        logger.debug("Authenticating user using strategy: {}", authType);
        
        try {
            return strategy.authenticate(request);
        } catch (Exception e) {
            logger.error("Authentication failed for type {}: {}", authType, e.getMessage());
            if (e instanceof AuthenticationException) {
                throw e;
            }
            throw new AuthenticationException("AUTHENTICATION_FAILED", "Authentication failed: " + e.getMessage(), e);
        }
    }

    @Override
    public void registerStrategy(AuthenticationStrategy strategy) {
        if (strategy == null) {
            throw new IllegalArgumentException("Strategy cannot be null");
        }
        
        AuthenticationType authType = strategy.getAuthenticationType();
        strategies.put(authType, strategy);
        logger.info("Registered authentication strategy for type: {}", authType);
    }

    @Override
    public AuthenticationType[] getSupportedAuthenticationTypes() {
        return strategies.keySet().toArray(new AuthenticationType[0]);
    }
}