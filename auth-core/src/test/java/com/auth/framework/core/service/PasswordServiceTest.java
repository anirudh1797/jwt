package com.auth.framework.core.service;

import com.auth.framework.core.exception.ValidationException;
import com.auth.framework.core.service.impl.Argon2PasswordService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for PasswordService implementation.
 * Demonstrates comprehensive testing of password operations.
 */
@DisplayName("Password Service Tests")
class PasswordServiceTest {

    private PasswordService passwordService;

    @BeforeEach
    void setUp() {
        passwordService = new Argon2PasswordService();
    }

    @Test
    @DisplayName("Should encode password successfully")
    void shouldEncodePasswordSuccessfully() {
        // Given
        String rawPassword = "TestPassword123!";

        // When
        String encodedPassword = passwordService.encode(rawPassword);

        // Then
        assertNotNull(encodedPassword);
        assertNotEquals(rawPassword, encodedPassword);
        assertTrue(encodedPassword.length() > 0);
    }

    @Test
    @DisplayName("Should match correct password")
    void shouldMatchCorrectPassword() {
        // Given
        String rawPassword = "TestPassword123!";
        String encodedPassword = passwordService.encode(rawPassword);

        // When
        boolean matches = passwordService.matches(rawPassword, encodedPassword);

        // Then
        assertTrue(matches);
    }

    @Test
    @DisplayName("Should not match incorrect password")
    void shouldNotMatchIncorrectPassword() {
        // Given
        String rawPassword = "TestPassword123!";
        String wrongPassword = "WrongPassword123!";
        String encodedPassword = passwordService.encode(rawPassword);

        // When
        boolean matches = passwordService.matches(wrongPassword, encodedPassword);

        // Then
        assertFalse(matches);
    }

    @Test
    @DisplayName("Should validate strong password")
    void shouldValidateStrongPassword() {
        // Given
        String strongPassword = "StrongPassword123!";

        // When & Then
        assertDoesNotThrow(() -> passwordService.validatePassword(strongPassword));
    }

    @Test
    @DisplayName("Should reject weak password - too short")
    void shouldRejectWeakPasswordTooShort() {
        // Given
        String weakPassword = "Weak1!";

        // When & Then
        ValidationException exception = assertThrows(ValidationException.class,
                () -> passwordService.validatePassword(weakPassword));
        assertEquals("PASSWORD_TOO_SHORT", exception.getErrorCode());
    }

    @Test
    @DisplayName("Should reject weak password - no uppercase")
    void shouldRejectWeakPasswordNoUppercase() {
        // Given
        String weakPassword = "weakpassword123!";

        // When & Then
        ValidationException exception = assertThrows(ValidationException.class,
                () -> passwordService.validatePassword(weakPassword));
        assertEquals("PASSWORD_WEAK", exception.getErrorCode());
    }

    @Test
    @DisplayName("Should reject weak password - no lowercase")
    void shouldRejectWeakPasswordNoLowercase() {
        // Given
        String weakPassword = "WEAKPASSWORD123!";

        // When & Then
        ValidationException exception = assertThrows(ValidationException.class,
                () -> passwordService.validatePassword(weakPassword));
        assertEquals("PASSWORD_WEAK", exception.getErrorCode());
    }

    @Test
    @DisplayName("Should reject weak password - no digit")
    void shouldRejectWeakPasswordNoDigit() {
        // Given
        String weakPassword = "WeakPassword!";

        // When & Then
        ValidationException exception = assertThrows(ValidationException.class,
                () -> passwordService.validatePassword(weakPassword));
        assertEquals("PASSWORD_WEAK", exception.getErrorCode());
    }

    @Test
    @DisplayName("Should reject weak password - no special character")
    void shouldRejectWeakPasswordNoSpecialChar() {
        // Given
        String weakPassword = "WeakPassword123";

        // When & Then
        ValidationException exception = assertThrows(ValidationException.class,
                () -> passwordService.validatePassword(weakPassword));
        assertEquals("PASSWORD_WEAK", exception.getErrorCode());
    }

    @Test
    @DisplayName("Should generate secure password")
    void shouldGenerateSecurePassword() {
        // Given
        int length = 12;

        // When
        String generatedPassword = passwordService.generateSecurePassword(length);

        // Then
        assertNotNull(generatedPassword);
        assertEquals(length, generatedPassword.length());
        assertDoesNotThrow(() -> passwordService.validatePassword(generatedPassword));
    }

    @Test
    @DisplayName("Should generate different passwords each time")
    void shouldGenerateDifferentPasswordsEachTime() {
        // Given
        int length = 12;

        // When
        String password1 = passwordService.generateSecurePassword(length);
        String password2 = passwordService.generateSecurePassword(length);

        // Then
        assertNotEquals(password1, password2);
    }

    @Test
    @DisplayName("Should handle null password gracefully")
    void shouldHandleNullPasswordGracefully() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> passwordService.encode(null));
        assertFalse(passwordService.matches(null, "encoded"));
        assertFalse(passwordService.matches("password", null));
    }

    @Test
    @DisplayName("Should handle empty password gracefully")
    void shouldHandleEmptyPasswordGracefully() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> passwordService.encode(""));
        assertFalse(passwordService.matches("", "encoded"));
    }
}