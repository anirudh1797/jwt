package com.auth.framework.demo.controller;

import com.auth.framework.core.domain.User;
import com.auth.framework.core.repository.UserRepository;
import com.auth.framework.core.service.PasswordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * REST controller for user management operations.
 * Demonstrates role-based access control using the authentication framework.
 */
@RestController
@RequestMapping("/api/v1/users")
@CrossOrigin(origins = "*", maxAge = 3600)
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordService passwordService;

    /**
     * Get all users (Admin only).
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllUsers() {
        List<User> users = userRepository.findAll();
        return ResponseEntity.ok(Map.of("users", users));
    }

    /**
     * Get user by ID (Admin or own profile).
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @userSecurityService.isOwner(authentication.name, #id)")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        Optional<User> user = userRepository.findById(id);
        if (user.isPresent()) {
            return ResponseEntity.ok(user.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get current user profile.
     */
    @GetMapping("/profile")
    public ResponseEntity<?> getCurrentUserProfile() {
        // This would typically get the current user from SecurityContext
        // For demo purposes, we'll return a placeholder
        Map<String, Object> profile = new HashMap<>();
        profile.put("message", "User profile endpoint - implement based on your needs");
        profile.put("note", "Use SecurityContextHolder.getContext().getAuthentication() to get current user");
        return ResponseEntity.ok(profile);
    }

    /**
     * Update user profile (own profile only).
     */
    @PutMapping("/{id}/profile")
    @PreAuthorize("@userSecurityService.isOwner(authentication.name, #id)")
    public ResponseEntity<?> updateUserProfile(@PathVariable Long id, @RequestBody Map<String, Object> updates) {
        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        User user = userOpt.get();
        
        // Update allowed fields
        if (updates.containsKey("firstName")) {
            user.setFirstName((String) updates.get("firstName"));
        }
        if (updates.containsKey("lastName")) {
            user.setLastName((String) updates.get("lastName"));
        }
        if (updates.containsKey("email")) {
            user.setEmail((String) updates.get("email"));
        }

        userRepository.save(user);
        return ResponseEntity.ok(Map.of("message", "Profile updated successfully"));
    }

    /**
     * Change password (own profile only).
     */
    @PostMapping("/{id}/change-password")
    @PreAuthorize("@userSecurityService.isOwner(authentication.name, #id)")
    public ResponseEntity<?> changePassword(@PathVariable Long id, @RequestBody Map<String, String> request) {
        String currentPassword = request.get("currentPassword");
        String newPassword = request.get("newPassword");

        if (currentPassword == null || newPassword == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Current password and new password are required"));
        }

        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        User user = userOpt.get();

        // Verify current password
        if (!passwordService.matches(currentPassword, user.getPassword())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Current password is incorrect"));
        }

        // Validate new password
        try {
            passwordService.validatePassword(newPassword);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }

        // Update password
        user.setPassword(passwordService.encode(newPassword));
        userRepository.save(user);

        return ResponseEntity.ok(Map.of("message", "Password changed successfully"));
    }

    /**
     * Disable user account (Admin only).
     */
    @PostMapping("/{id}/disable")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> disableUser(@PathVariable Long id) {
        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        User user = userOpt.get();
        user.setEnabled(false);
        userRepository.save(user);

        return ResponseEntity.ok(Map.of("message", "User disabled successfully"));
    }

    /**
     * Enable user account (Admin only).
     */
    @PostMapping("/{id}/enable")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> enableUser(@PathVariable Long id) {
        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        User user = userOpt.get();
        user.setEnabled(true);
        userRepository.save(user);

        return ResponseEntity.ok(Map.of("message", "User enabled successfully"));
    }
}