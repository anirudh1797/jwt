package com.auth.framework.core.service;

/**
 * Service interface for password operations.
 * Follows Interface Segregation Principle - focused interface for password operations.
 * Follows Dependency Inversion Principle - depends on abstraction, not concrete implementation.
 */
public interface PasswordService {
    
    /**
     * Encodes a raw password using the configured password encoder.
     * 
     * @param rawPassword the raw password to encode
     * @return the encoded password
     */
    String encode(String rawPassword);
    
    /**
     * Verifies a raw password against an encoded password.
     * 
     * @param rawPassword the raw password to verify
     * @param encodedPassword the encoded password to verify against
     * @return true if the password matches, false otherwise
     */
    boolean matches(String rawPassword, String encodedPassword);
    
    /**
     * Checks if a password meets the security requirements.
     * 
     * @param password the password to validate
     * @throws ValidationException if the password doesn't meet requirements
     */
    void validatePassword(String password) throws ValidationException;
    
    /**
     * Generates a secure random password.
     * 
     * @param length the desired length of the password
     * @return a secure random password
     */
    String generateSecurePassword(int length);
}