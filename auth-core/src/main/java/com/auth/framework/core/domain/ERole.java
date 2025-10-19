package com.auth.framework.core.domain;

/**
 * Enumeration of available user roles in the system.
 * Follows Open/Closed Principle - easy to add new roles without modifying existing code.
 */
public enum ERole {
    ROLE_USER("User"),
    ROLE_ADMIN("Administrator"),
    ROLE_MODERATOR("Moderator"),
    ROLE_STUDENT("Student"),
    ROLE_PROFESSOR("Professor"),
    ROLE_GUEST("Guest");
    
    private final String displayName;
    
    ERole(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public static ERole fromString(String role) {
        for (ERole eRole : ERole.values()) {
            if (eRole.name().equalsIgnoreCase(role) || 
                eRole.displayName.equalsIgnoreCase(role)) {
                return eRole;
            }
        }
        throw new IllegalArgumentException("Unknown role: " + role);
    }
}