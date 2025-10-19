package com.auth.framework.demo.controller;

import com.auth.framework.core.domain.ERole;
import com.auth.framework.core.domain.Role;
import com.auth.framework.core.domain.User;
import com.auth.framework.core.repository.RoleRepository;
import com.auth.framework.core.repository.UserRepository;
import com.auth.framework.core.service.PasswordService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.util.HashSet;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for AuthController.
 * Demonstrates comprehensive API testing with real Spring context.
 */
@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("Auth Controller Integration Tests")
class AuthControllerIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordService passwordService;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;
    private User testUser;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        
        // Create test user
        testUser = new User("testuser", "test@example.com", passwordService.encode("password123"));
        testUser.setEnabled(true);
        testUser.setAccountNonExpired(true);
        testUser.setAccountNonLocked(true);
        testUser.setCredentialsNonExpired(true);
        
        // Create and assign role
        Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                .orElseGet(() -> {
                    Role role = new Role(ERole.ROLE_USER);
                    return roleRepository.save(role);
                });
        
        testUser.setRoles(new HashSet<>());
        testUser.getRoles().add(userRole);
        userRepository.save(testUser);
    }

    @Test
    @DisplayName("Should authenticate user with username and password")
    void shouldAuthenticateUserWithUsernameAndPassword() throws Exception {
        // Given
        String requestBody = objectMapper.writeValueAsString(new com.auth.framework.core.dto.UsernamePasswordRequest("testuser", "password123"));

        // When & Then
        mockMvc.perform(post("/api/v1/auth/login/username")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user").exists())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.roles").isArray());
    }

    @Test
    @DisplayName("Should authenticate user with email and password")
    void shouldAuthenticateUserWithEmailAndPassword() throws Exception {
        // Given
        String requestBody = objectMapper.writeValueAsString(new com.auth.framework.core.dto.EmailPasswordRequest("test@example.com", "password123"));

        // When & Then
        mockMvc.perform(post("/api/v1/auth/login/email")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user").exists())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.roles").isArray());
    }

    @Test
    @DisplayName("Should return error for invalid credentials")
    void shouldReturnErrorForInvalidCredentials() throws Exception {
        // Given
        String requestBody = objectMapper.writeValueAsString(new com.auth.framework.core.dto.UsernamePasswordRequest("testuser", "wrongpassword"));

        // When & Then
        mockMvc.perform(post("/api/v1/auth/login/username")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(true))
                .andExpect(jsonPath("$.errorCode").value("INVALID_CREDENTIALS"));
    }

    @Test
    @DisplayName("Should return error for non-existent user")
    void shouldReturnErrorForNonExistentUser() throws Exception {
        // Given
        String requestBody = objectMapper.writeValueAsString(new com.auth.framework.core.dto.UsernamePasswordRequest("nonexistent", "password123"));

        // When & Then
        mockMvc.perform(post("/api/v1/auth/login/username")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(true))
                .andExpect(jsonPath("$.errorCode").value("INVALID_CREDENTIALS"));
    }

    @Test
    @DisplayName("Should return error for missing username")
    void shouldReturnErrorForMissingUsername() throws Exception {
        // Given
        String requestBody = "{\"password\": \"password123\"}";

        // When & Then
        mockMvc.perform(post("/api/v1/auth/login/username")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return error for missing password")
    void shouldReturnErrorForMissingPassword() throws Exception {
        // Given
        String requestBody = "{\"username\": \"testuser\"}";

        // When & Then
        mockMvc.perform(post("/api/v1/auth/login/username")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return supported authentication types")
    void shouldReturnSupportedAuthenticationTypes() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/auth/types"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.supportedTypes").isArray())
                .andExpect(jsonPath("$.supportedTypes").isNotEmpty());
    }

    @Test
    @DisplayName("Should return health status")
    void shouldReturnHealthStatus() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/auth/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.service").value("auth-framework-demo"));
    }

    @Test
    @DisplayName("Should handle malformed JSON gracefully")
    void shouldHandleMalformedJsonGracefully() throws Exception {
        // Given
        String malformedJson = "{\"username\": \"testuser\", \"password\": \"password123\""; // Missing closing brace

        // When & Then
        mockMvc.perform(post("/api/v1/auth/login/username")
                .contentType(MediaType.APPLICATION_JSON)
                .content(malformedJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should handle empty request body")
    void shouldHandleEmptyRequestBody() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/v1/auth/login/username")
                .contentType(MediaType.APPLICATION_JSON)
                .content(""))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should handle unsupported content type")
    void shouldHandleUnsupportedContentType() throws Exception {
        // Given
        String requestBody = "username=testuser&password=password123";

        // When & Then
        mockMvc.perform(post("/api/v1/auth/login/username")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .content(requestBody))
                .andExpect(status().isUnsupportedMediaType());
    }
}