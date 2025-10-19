package com.auth.framework.core.strategy;

import com.auth.framework.core.domain.User;
import com.auth.framework.core.dto.AuthenticationRequest;
import com.auth.framework.core.dto.AuthenticationResult;

/**
 * Strategy interface for different authentication methods.
 * Follows Strategy Pattern and Open/Closed Principle - easy to add new auth types.
 * Follows Interface Segregation Principle - focused interface for authentication.
 */
public interface AuthenticationStrategy {
    
    /**
     * Authenticates a user using the specific strategy.
     * 
     * @param request the authentication request
     * @return authentication result containing user and tokens
     * @throws AuthenticationException if authentication fails
     */
    AuthenticationResult authenticate(AuthenticationRequest request) throws AuthenticationException;
    
    /**
     * Checks if this strategy supports the given authentication type.
     * 
     * @param authType the authentication type
     * @return true if supported, false otherwise
     */
    boolean supports(AuthenticationType authType);
    
    /**
     * Gets the authentication type this strategy handles.
     * 
     * @return the authentication type
     */
    AuthenticationType getAuthenticationType();
    
    /**
     * Validates the authentication request for this strategy.
     * 
     * @param request the authentication request
     * @throws ValidationException if validation fails
     */
    void validateRequest(AuthenticationRequest request) throws ValidationException;
}