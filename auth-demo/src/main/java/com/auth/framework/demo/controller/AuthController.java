package com.auth.framework.demo.controller;

import com.auth.framework.core.dto.AuthenticationResult;
import com.auth.framework.core.dto.EmailPasswordRequest;
import com.auth.framework.core.dto.UsernamePasswordRequest;
import com.auth.framework.core.exception.AuthenticationException;
import com.auth.framework.core.exception.ValidationException;
import com.auth.framework.core.service.AuthenticationManager;
import com.auth.framework.core.service.RefreshTokenService;
import com.auth.framework.core.strategy.AuthenticationType;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * REST controller for authentication operations.
 * Demonstrates how to use the authentication framework in a real application.
 */
@RestController
@RequestMapping("/api/v1/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private RefreshTokenService refreshTokenService;

    /**
     * Authenticate user with username and password.
     */
    @PostMapping("/login/username")
    public ResponseEntity<?> authenticateWithUsername(@Valid @RequestBody UsernamePasswordRequest request) {
        try {
            AuthenticationResult result = authenticationManager.authenticate(request);
            return ResponseEntity.ok(result);
        } catch (AuthenticationException e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getErrorCode(), e.getMessage()));
        } catch (ValidationException e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getErrorCode(), e.getMessage()));
        }
    }

    /**
     * Authenticate user with email and password.
     */
    @PostMapping("/login/email")
    public ResponseEntity<?> authenticateWithEmail(@Valid @RequestBody EmailPasswordRequest request) {
        try {
            AuthenticationResult result = authenticationManager.authenticate(request);
            return ResponseEntity.ok(result);
        } catch (AuthenticationException e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getErrorCode(), e.getMessage()));
        } catch (ValidationException e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getErrorCode(), e.getMessage()));
        }
    }

    /**
     * Refresh access token using refresh token.
     */
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody Map<String, String> request) {
        try {
            String refreshToken = request.get("refreshToken");
            if (refreshToken == null || refreshToken.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("REFRESH_TOKEN_REQUIRED", "Refresh token is required"));
            }

            AuthenticationResult result = refreshTokenService.refreshAccessToken(refreshToken);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse("REFRESH_FAILED", e.getMessage()));
        }
    }

    /**
     * Logout user by revoking refresh tokens.
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestBody Map<String, Long> request) {
        try {
            Long userId = request.get("userId");
            if (userId == null) {
                return ResponseEntity.badRequest().body(createErrorResponse("USER_ID_REQUIRED", "User ID is required"));
            }

            refreshTokenService.revokeByUserId(userId);
            return ResponseEntity.ok(createSuccessResponse("Logout successful"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse("LOGOUT_FAILED", e.getMessage()));
        }
    }

    /**
     * Get supported authentication types.
     */
    @GetMapping("/types")
    public ResponseEntity<?> getSupportedAuthTypes() {
        AuthenticationType[] types = authenticationManager.getSupportedAuthenticationTypes();
        return ResponseEntity.ok(Map.of("supportedTypes", types));
    }

    /**
     * Health check endpoint.
     */
    @GetMapping("/health")
    public ResponseEntity<?> health() {
        return ResponseEntity.ok(Map.of("status", "UP", "service", "auth-framework-demo"));
    }

    private Map<String, Object> createErrorResponse(String errorCode, String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("error", true);
        response.put("errorCode", errorCode);
        response.put("message", message);
        response.put("timestamp", System.currentTimeMillis());
        return response;
    }

    private Map<String, Object> createSuccessResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", message);
        response.put("timestamp", System.currentTimeMillis());
        return response;
    }
}