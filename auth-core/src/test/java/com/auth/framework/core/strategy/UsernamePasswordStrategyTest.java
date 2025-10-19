package com.auth.framework.core.strategy;

import com.auth.framework.core.domain.ERole;
import com.auth.framework.core.domain.Role;
import com.auth.framework.core.domain.User;
import com.auth.framework.core.dto.AuthenticationResult;
import com.auth.framework.core.dto.UsernamePasswordRequest;
import com.auth.framework.core.exception.AuthenticationException;
import com.auth.framework.core.exception.ValidationException;
import com.auth.framework.core.repository.UserRepository;
import com.auth.framework.core.service.JwtService;
import com.auth.framework.core.service.PasswordService;
import com.auth.framework.core.strategy.impl.UsernamePasswordStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Test class for UsernamePasswordStrategy.
 * Demonstrates comprehensive testing of authentication strategy.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Username Password Strategy Tests")
class UsernamePasswordStrategyTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordService passwordService;

    @Mock
    private JwtService jwtService;

    private UsernamePasswordStrategy strategy;
    private User testUser;
    private UsernamePasswordRequest validRequest;

    @BeforeEach
    void setUp() {
        strategy = new UsernamePasswordStrategy(userRepository, passwordService, jwtService);
        
        // Create test user
        testUser = new User("testuser", "test@example.com", "encodedPassword");
        testUser.setId(1L);
        testUser.setEnabled(true);
        testUser.setAccountNonExpired(true);
        testUser.setAccountNonLocked(true);
        testUser.setCredentialsNonExpired(true);
        testUser.setFailedLoginAttempts(0);
        
        // Add role
        Role userRole = new Role(ERole.ROLE_USER);
        userRole.setId(1L);
        testUser.setRoles(new HashSet<>());
        testUser.getRoles().add(userRole);
        
        // Create valid request
        validRequest = new UsernamePasswordRequest("testuser", "password123");
    }

    @Test
    @DisplayName("Should authenticate valid user successfully")
    void shouldAuthenticateValidUserSuccessfully() throws Exception {
        // Given
        when(userRepository.findActiveUserByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(passwordService.matches("password123", "encodedPassword")).thenReturn(true);
        
        AuthenticationResult expectedResult = new AuthenticationResult();
        expectedResult.setUser(testUser);
        when(jwtService.generateTokens(testUser)).thenReturn(expectedResult);

        // When
        AuthenticationResult result = strategy.authenticate(validRequest);

        // Then
        assertNotNull(result);
        assertEquals(testUser, result.getUser());
        verify(userRepository).save(testUser);
        verify(jwtService).generateTokens(testUser);
    }

    @Test
    @DisplayName("Should throw exception for invalid username")
    void shouldThrowExceptionForInvalidUsername() {
        // Given
        when(userRepository.findActiveUserByUsername("testuser")).thenReturn(Optional.empty());

        // When & Then
        AuthenticationException exception = assertThrows(AuthenticationException.class,
                () -> strategy.authenticate(validRequest));
        assertEquals("INVALID_CREDENTIALS", exception.getErrorCode());
    }

    @Test
    @DisplayName("Should throw exception for invalid password")
    void shouldThrowExceptionForInvalidPassword() {
        // Given
        when(userRepository.findActiveUserByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(passwordService.matches("password123", "encodedPassword")).thenReturn(false);

        // When & Then
        AuthenticationException exception = assertThrows(AuthenticationException.class,
                () -> strategy.authenticate(validRequest));
        assertEquals("INVALID_CREDENTIALS", exception.getErrorCode());
        verify(userRepository).save(testUser); // Should save user with incremented failed attempts
    }

    @Test
    @DisplayName("Should throw exception for disabled account")
    void shouldThrowExceptionForDisabledAccount() {
        // Given
        testUser.setEnabled(false);
        when(userRepository.findActiveUserByUsername("testuser")).thenReturn(Optional.of(testUser));

        // When & Then
        AuthenticationException exception = assertThrows(AuthenticationException.class,
                () -> strategy.authenticate(validRequest));
        assertEquals("ACCOUNT_DISABLED", exception.getErrorCode());
    }

    @Test
    @DisplayName("Should throw exception for locked account")
    void shouldThrowExceptionForLockedAccount() {
        // Given
        testUser.setLockedUntil(LocalDateTime.now().plusMinutes(30));
        when(userRepository.findActiveUserByUsername("testuser")).thenReturn(Optional.of(testUser));

        // When & Then
        AuthenticationException exception = assertThrows(AuthenticationException.class,
                () -> strategy.authenticate(validRequest));
        assertEquals("ACCOUNT_LOCKED", exception.getErrorCode());
    }

    @Test
    @DisplayName("Should lock account after max failed attempts")
    void shouldLockAccountAfterMaxFailedAttempts() {
        // Given
        testUser.setFailedLoginAttempts(4); // One attempt away from lockout
        when(userRepository.findActiveUserByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(passwordService.matches("password123", "encodedPassword")).thenReturn(false);

        // When
        assertThrows(AuthenticationException.class, () -> strategy.authenticate(validRequest));

        // Then
        verify(userRepository, atLeastOnce()).save(testUser);
        assertTrue(testUser.getFailedLoginAttempts() >= 5);
    }

    @Test
    @DisplayName("Should reset failed attempts on successful login")
    void shouldResetFailedAttemptsOnSuccessfulLogin() throws Exception {
        // Given
        testUser.setFailedLoginAttempts(3);
        when(userRepository.findActiveUserByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(passwordService.matches("password123", "encodedPassword")).thenReturn(true);
        when(jwtService.generateTokens(testUser)).thenReturn(new AuthenticationResult());

        // When
        strategy.authenticate(validRequest);

        // Then
        verify(userRepository).save(testUser);
        assertEquals(0, testUser.getFailedLoginAttempts());
    }

    @Test
    @DisplayName("Should support username password authentication type")
    void shouldSupportUsernamePasswordAuthenticationType() {
        // When & Then
        assertTrue(strategy.supports(AuthenticationType.USERNAME_PASSWORD));
        assertFalse(strategy.supports(AuthenticationType.EMAIL_PASSWORD));
        assertFalse(strategy.supports(AuthenticationType.API_KEY));
    }

    @Test
    @DisplayName("Should return correct authentication type")
    void shouldReturnCorrectAuthenticationType() {
        // When & Then
        assertEquals(AuthenticationType.USERNAME_PASSWORD, strategy.getAuthenticationType());
    }

    @Test
    @DisplayName("Should validate request successfully")
    void shouldValidateRequestSuccessfully() {
        // When & Then
        assertDoesNotThrow(() -> strategy.validateRequest(validRequest));
    }

    @Test
    @DisplayName("Should throw validation exception for null username")
    void shouldThrowValidationExceptionForNullUsername() {
        // Given
        UsernamePasswordRequest request = new UsernamePasswordRequest(null, "password123");

        // When & Then
        ValidationException exception = assertThrows(ValidationException.class,
                () -> strategy.validateRequest(request));
        assertEquals("USERNAME_REQUIRED", exception.getErrorCode());
    }

    @Test
    @DisplayName("Should throw validation exception for empty username")
    void shouldThrowValidationExceptionForEmptyUsername() {
        // Given
        UsernamePasswordRequest request = new UsernamePasswordRequest("", "password123");

        // When & Then
        ValidationException exception = assertThrows(ValidationException.class,
                () -> strategy.validateRequest(request));
        assertEquals("USERNAME_REQUIRED", exception.getErrorCode());
    }

    @Test
    @DisplayName("Should throw validation exception for null password")
    void shouldThrowValidationExceptionForNullPassword() {
        // Given
        UsernamePasswordRequest request = new UsernamePasswordRequest("testuser", null);

        // When & Then
        ValidationException exception = assertThrows(ValidationException.class,
                () -> strategy.validateRequest(request));
        assertEquals("PASSWORD_REQUIRED", exception.getErrorCode());
    }

    @Test
    @DisplayName("Should throw validation exception for too long username")
    void shouldThrowValidationExceptionForTooLongUsername() {
        // Given
        String longUsername = "a".repeat(51);
        UsernamePasswordRequest request = new UsernamePasswordRequest(longUsername, "password123");

        // When & Then
        ValidationException exception = assertThrows(ValidationException.class,
                () -> strategy.validateRequest(request));
        assertEquals("USERNAME_TOO_LONG", exception.getErrorCode());
    }

    @Test
    @DisplayName("Should throw validation exception for too long password")
    void shouldThrowValidationExceptionForTooLongPassword() {
        // Given
        String longPassword = "a".repeat(129);
        UsernamePasswordRequest request = new UsernamePasswordRequest("testuser", longPassword);

        // When & Then
        ValidationException exception = assertThrows(ValidationException.class,
                () -> strategy.validateRequest(request));
        assertEquals("PASSWORD_TOO_LONG", exception.getErrorCode());
    }

    @Test
    @DisplayName("Should throw exception for wrong request type")
    void shouldThrowExceptionForWrongRequestType() {
        // Given
        com.auth.framework.core.dto.EmailPasswordRequest wrongRequest = 
            new com.auth.framework.core.dto.EmailPasswordRequest("test@example.com", "password123");

        // When & Then
        AuthenticationException exception = assertThrows(AuthenticationException.class,
                () -> strategy.authenticate(wrongRequest));
        assertEquals("INVALID_REQUEST_TYPE", exception.getErrorCode());
    }
}