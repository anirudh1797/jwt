package com.auth.framework.core.service.impl;

import com.auth.framework.core.exception.ValidationException;
import com.auth.framework.core.service.PasswordService;
import org.bouncycastle.crypto.generators.Argon2BytesGenerator;
import org.bouncycastle.crypto.params.Argon2Parameters;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.regex.Pattern;

/**
 * Argon2 password service implementation for secure password hashing.
 * Follows Single Responsibility Principle - only handles password operations.
 * Uses Argon2id for modern password hashing with resistance to side-channel attacks.
 */
@Service
public class Argon2PasswordService implements PasswordService {
    
    private static final int SALT_LENGTH = 16;
    private static final int HASH_LENGTH = 32;
    private static final int MEMORY = 65536; // 64 MB
    private static final int ITERATIONS = 3;
    private static final int PARALLELISM = 1;
    
    // Password validation patterns
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
        "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$"
    );
    
    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    public String encode(String rawPassword) {
        if (rawPassword == null || rawPassword.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }
        
        byte[] salt = generateSalt();
        byte[] hash = generateHash(rawPassword, salt);
        
        // Combine salt and hash for storage
        byte[] combined = new byte[salt.length + hash.length];
        System.arraycopy(salt, 0, combined, 0, salt.length);
        System.arraycopy(hash, 0, combined, salt.length, hash.length);
        
        return Base64.getEncoder().encodeToString(combined);
    }

    @Override
    public boolean matches(String rawPassword, String encodedPassword) {
        if (rawPassword == null || encodedPassword == null) {
            return false;
        }
        
        try {
            byte[] combined = Base64.getDecoder().decode(encodedPassword);
            byte[] salt = new byte[SALT_LENGTH];
            byte[] hash = new byte[HASH_LENGTH];
            
            System.arraycopy(combined, 0, salt, 0, SALT_LENGTH);
            System.arraycopy(combined, SALT_LENGTH, hash, 0, HASH_LENGTH);
            
            byte[] testHash = generateHash(rawPassword, salt);
            return constantTimeEquals(hash, testHash);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void validatePassword(String password) throws ValidationException {
        if (password == null || password.isEmpty()) {
            throw new ValidationException("password", "PASSWORD_REQUIRED", "Password is required");
        }
        
        if (password.length() < 8) {
            throw new ValidationException("password", "PASSWORD_TOO_SHORT", 
                "Password must be at least 8 characters long");
        }
        
        if (password.length() > 128) {
            throw new ValidationException("password", "PASSWORD_TOO_LONG", 
                "Password must be no more than 128 characters long");
        }
        
        if (!PASSWORD_PATTERN.matcher(password).matches()) {
            throw new ValidationException("password", "PASSWORD_WEAK", 
                "Password must contain at least one lowercase letter, one uppercase letter, " +
                "one digit, and one special character (@$!%*?&)");
        }
    }

    @Override
    public String generateSecurePassword(int length) {
        if (length < 8 || length > 128) {
            throw new IllegalArgumentException("Password length must be between 8 and 128 characters");
        }
        
        String lowercase = "abcdefghijklmnopqrstuvwxyz";
        String uppercase = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String digits = "0123456789";
        String special = "@$!%*?&";
        String allChars = lowercase + uppercase + digits + special;
        
        StringBuilder password = new StringBuilder(length);
        
        // Ensure at least one character from each category
        password.append(lowercase.charAt(secureRandom.nextInt(lowercase.length())));
        password.append(uppercase.charAt(secureRandom.nextInt(uppercase.length())));
        password.append(digits.charAt(secureRandom.nextInt(digits.length())));
        password.append(special.charAt(secureRandom.nextInt(special.length())));
        
        // Fill the rest randomly
        for (int i = 4; i < length; i++) {
            password.append(allChars.charAt(secureRandom.nextInt(allChars.length())));
        }
        
        // Shuffle the password
        for (int i = 0; i < password.length(); i++) {
            int randomIndex = secureRandom.nextInt(password.length());
            char temp = password.charAt(i);
            password.setCharAt(i, password.charAt(randomIndex));
            password.setCharAt(randomIndex, temp);
        }
        
        return password.toString();
    }
    
    private byte[] generateSalt() {
        byte[] salt = new byte[SALT_LENGTH];
        secureRandom.nextBytes(salt);
        return salt;
    }
    
    private byte[] generateHash(String password, byte[] salt) {
        Argon2BytesGenerator generator = new Argon2BytesGenerator();
        Argon2Parameters.Builder builder = new Argon2Parameters.Builder(Argon2Parameters.ARGON2_id)
                .withSalt(salt)
                .withMemoryAsKB(MEMORY / 1024)
                .withIterations(ITERATIONS)
                .withParallelism(PARALLELISM);
        
        generator.init(builder.build());
        byte[] hash = new byte[HASH_LENGTH];
        generator.generateBytes(password.toCharArray(), hash);
        return hash;
    }
    
    private boolean constantTimeEquals(byte[] a, byte[] b) {
        if (a.length != b.length) {
            return false;
        }
        
        int result = 0;
        for (int i = 0; i < a.length; i++) {
            result |= a[i] ^ b[i];
        }
        return result == 0;
    }
}