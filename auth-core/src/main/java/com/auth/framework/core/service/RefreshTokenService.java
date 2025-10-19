package com.auth.framework.core.service;

import com.auth.framework.core.domain.RefreshToken;
import com.auth.framework.core.domain.User;
import com.auth.framework.core.dto.AuthenticationResult;
import com.auth.framework.core.exception.TokenRefreshException;

/**
 * Service interface for refresh token operations.
 * Follows Interface Segregation Principle - focused interface for refresh token operations.
 * Follows Dependency Inversion Principle - depends on abstraction, not concrete implementation.
 */
public interface RefreshTokenService {
    
    /**
     * Creates a new refresh token for the given user.
     * 
     * @param user the user to create refresh token for
     * @return the created refresh token
     */
    RefreshToken createRefreshToken(User user);
    
    /**
     * Creates a new refresh token for the given user with additional metadata.
     * 
     * @param user the user to create refresh token for
     * @param ipAddress the IP address of the request
     * @param userAgent the user agent of the request
     * @return the created refresh token
     */
    RefreshToken createRefreshToken(User user, String ipAddress, String userAgent);
    
    /**
     * Refreshes an access token using a refresh token.
     * 
     * @param refreshToken the refresh token
     * @return authentication result with new tokens
     * @throws TokenRefreshException if refresh fails
     */
    AuthenticationResult refreshAccessToken(String refreshToken) throws TokenRefreshException;
    
    /**
     * Verifies if a refresh token is valid and not expired.
     * 
     * @param token the refresh token to verify
     * @return the refresh token if valid
     * @throws TokenRefreshException if token is invalid or expired
     */
    RefreshToken verifyExpiration(RefreshToken token) throws TokenRefreshException;
    
    /**
     * Deletes all refresh tokens for a user.
     * 
     * @param user the user to delete tokens for
     */
    void deleteByUser(User user);
    
    /**
     * Deletes all refresh tokens for a user by ID.
     * 
     * @param userId the user ID to delete tokens for
     */
    void deleteByUserId(Long userId);
    
    /**
     * Revokes a specific refresh token.
     * 
     * @param token the token to revoke
     */
    void revokeToken(String token);
    
    /**
     * Revokes all refresh tokens for a user.
     * 
     * @param user the user to revoke tokens for
     */
    void revokeByUser(User user);
    
    /**
     * Revokes all refresh tokens for a user by ID.
     * 
     * @param userId the user ID to revoke tokens for
     */
    void revokeByUserId(Long userId);
    
    /**
     * Cleans up expired tokens.
     */
    void cleanupExpiredTokens();
    
    /**
     * Gets the number of active refresh tokens for a user.
     * 
     * @param user the user to check
     * @return the number of active tokens
     */
    long getActiveTokenCount(User user);
}