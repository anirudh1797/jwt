package com.auth.framework.core.service;

import com.auth.framework.core.dto.AuthenticationRequest;
import com.auth.framework.core.dto.AuthenticationResult;
import com.auth.framework.core.exception.AuthenticationException;

/**
 * Service interface for managing authentication operations.
 * Follows Interface Segregation Principle - focused interface for authentication management.
 * Follows Dependency Inversion Principle - depends on abstraction, not concrete implementation.
 */
public interface AuthenticationManager {
    
    /**
     * Authenticates a user using the appropriate strategy.
     * 
     * @param request the authentication request
     * @return authentication result containing user and tokens
     * @throws AuthenticationException if authentication fails
     */
    AuthenticationResult authenticate(AuthenticationRequest request) throws AuthenticationException;
    
    /**
     * Registers a new authentication strategy.
     * 
     * @param strategy the authentication strategy to register
     */
    void registerStrategy(com.auth.framework.core.strategy.AuthenticationStrategy strategy);
    
    /**
     * Gets the list of supported authentication types.
     * 
     * @return array of supported authentication types
     */
    com.auth.framework.core.strategy.AuthenticationType[] getSupportedAuthenticationTypes();
}