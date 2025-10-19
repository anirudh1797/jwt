package com.auth.framework.core.strategy;

/**
 * Enumeration of supported authentication types.
 * Follows Open/Closed Principle - easy to add new authentication types.
 */
public enum AuthenticationType {
    USERNAME_PASSWORD("username_password"),
    EMAIL_PASSWORD("email_password"),
    OAUTH2_GOOGLE("oauth2_google"),
    OAUTH2_AZURE("oauth2_azure"),
    API_KEY("api_key"),
    LDAP("ldap"),
    SAML("saml"),
    CUSTOM("custom");
    
    private final String value;
    
    AuthenticationType(String value) {
        this.value = value;
    }
    
    public String getValue() {
        return value;
    }
    
    public static AuthenticationType fromString(String value) {
        for (AuthenticationType type : AuthenticationType.values()) {
            if (type.value.equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown authentication type: " + value);
    }
}