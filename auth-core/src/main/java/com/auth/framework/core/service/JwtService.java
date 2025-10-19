package com.auth.framework.core.service;

import com.auth.framework.core.domain.User;
import com.auth.framework.core.dto.AuthenticationResult;
import com.auth.framework.core.dto.TokenValidationResult;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service interface for JWT token operations.
 * Follows Interface Segregation Principle - focused interface for JWT operations.
 * Follows Dependency Inversion Principle - depends on abstraction, not concrete implementation.
 */
public interface JwtService {
    
    /**
     * Generates an access token for the given user.
     * 
     * @param user the user to generate token for
     * @return the access token
     */
    String generateAccessToken(User user);
    
    /**
     * Generates a refresh token for the given user.
     * 
     * @param user the user to generate token for
     * @return the refresh token
     */
    String generateRefreshToken(User user);
    
    /**
     * Generates both access and refresh tokens for the given user.
     * 
     * @param user the user to generate tokens for
     * @return authentication result with both tokens
     */
    AuthenticationResult generateTokens(User user);
    
    /**
     * Validates an access token.
     * 
     * @param token the token to validate
     * @return validation result
     */
    TokenValidationResult validateAccessToken(String token);
    
    /**
     * Validates a refresh token.
     * 
     * @param token the token to validate
     * @return validation result
     */
    TokenValidationResult validateRefreshToken(String token);
    
    /**
     * Extracts username from a token.
     * 
     * @param token the token to extract username from
     * @return the username
     */
    String extractUsername(String token);
    
    /**
     * Extracts roles from a token.
     * 
     * @param token the token to extract roles from
     * @return list of roles
     */
    List<String> extractRoles(String token);
    
    /**
     * Checks if a token is expired.
     * 
     * @param token the token to check
     * @return true if expired, false otherwise
     */
    boolean isTokenExpired(String token);
    
    /**
     * Gets the expiration time of a token.
     * 
     * @param token the token to check
     * @return the expiration time
     */
    LocalDateTime getTokenExpiration(String token);
    
    /**
     * Revokes a token by adding it to the blacklist.
     * 
     * @param token the token to revoke
     */
    void revokeToken(String token);
    
    /**
     * Checks if a token is blacklisted.
     * 
     * @param token the token to check
     * @return true if blacklisted, false otherwise
     */
    boolean isTokenBlacklisted(String token);
}