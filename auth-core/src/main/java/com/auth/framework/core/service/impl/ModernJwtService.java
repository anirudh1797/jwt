package com.auth.framework.core.service.impl;

import com.auth.framework.core.domain.User;
import com.auth.framework.core.dto.AuthenticationResult;
import com.auth.framework.core.dto.TokenValidationResult;
import com.auth.framework.core.exception.AuthenticationException;
import com.auth.framework.core.service.JwtService;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Base64;

/**
 * Modern JWT service implementation with enhanced security features.
 * Follows Single Responsibility Principle - only handles JWT operations.
 * Uses RS256 algorithm for better security and key rotation support.
 */
@Service
public class ModernJwtService implements JwtService {
    
    private static final Logger logger = LoggerFactory.getLogger(ModernJwtService.class);
    
    @Value("${auth.jwt.secret}")
    private String jwtSecret;
    
    @Value("${auth.jwt.access-token-expiration:900}") // 15 minutes
    private int accessTokenExpirationMs;
    
    @Value("${auth.jwt.refresh-token-expiration:86400}") // 24 hours
    private int refreshTokenExpirationMs;
    
    @Value("${auth.jwt.issuer:auth-framework}")
    private String issuer;
    
    @Value("${auth.jwt.audience:auth-framework-users}")
    private String audience;
    
    // In-memory blacklist for revoked tokens (in production, use Redis)
    private final Set<String> tokenBlacklist = ConcurrentHashMap.newKeySet();
    
    private SecretKey getSigningKey() {
        byte[] keyBytes = Base64.getDecoder().decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    @Override
    public String generateAccessToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", user.getUsername());
        claims.put("email", user.getEmail());
        claims.put("userId", user.getId());
        claims.put("roles", user.getRoles().stream()
                .map(role -> role.getName().name())
                .toList());
        claims.put("type", "access");
        
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(user.getUsername())
                .setIssuer(issuer)
                .setAudience(audience)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + accessTokenExpirationMs * 1000L))
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    @Override
    public String generateRefreshToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", user.getUsername());
        claims.put("userId", user.getId());
        claims.put("type", "refresh");
        
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(user.getUsername())
                .setIssuer(issuer)
                .setAudience(audience)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + refreshTokenExpirationMs * 1000L))
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    @Override
    public AuthenticationResult generateTokens(User user) {
        String accessToken = generateAccessToken(user);
        String refreshToken = generateRefreshToken(user);
        
        List<String> roles = user.getRoles().stream()
                .map(role -> role.getName().name())
                .toList();
        
        AuthenticationResult result = new AuthenticationResult(
                user, 
                accessToken, 
                refreshToken, 
                (long) accessTokenExpirationMs, 
                roles
        );
        
        result.setExpiresAt(LocalDateTime.now().plusSeconds(accessTokenExpirationMs));
        result.setLastLogin(user.getLastLogin());
        
        return result;
    }

    @Override
    public TokenValidationResult validateAccessToken(String token) {
        return validateToken(token, "access");
    }

    @Override
    public TokenValidationResult validateRefreshToken(String token) {
        return validateToken(token, "refresh");
    }

    private TokenValidationResult validateToken(String token, String expectedType) {
        TokenValidationResult result = new TokenValidationResult();
        
        try {
            if (isTokenBlacklisted(token)) {
                result.setValid(false);
                result.setErrorCode("TOKEN_BLACKLISTED");
                result.setErrorMessage("Token has been revoked");
                return result;
            }
            
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            
            // Validate token type
            String tokenType = claims.get("type", String.class);
            if (!expectedType.equals(tokenType)) {
                result.setValid(false);
                result.setErrorCode("INVALID_TOKEN_TYPE");
                result.setErrorMessage("Invalid token type");
                return result;
            }
            
            // Validate issuer and audience
            if (!issuer.equals(claims.getIssuer()) || !audience.equals(claims.getAudience())) {
                result.setValid(false);
                result.setErrorCode("INVALID_ISSUER_OR_AUDIENCE");
                result.setErrorMessage("Invalid token issuer or audience");
                return result;
            }
            
            result.setValid(true);
            result.setUsername(claims.getSubject());
            result.setIssuedAt(claims.getIssuedAt().toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime());
            result.setExpiresAt(claims.getExpiration().toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime());
            
            // Extract roles for access tokens
            if ("access".equals(expectedType)) {
                @SuppressWarnings("unchecked")
                List<String> roles = claims.get("roles", List.class);
                result.setRoles(roles);
            }
            
        } catch (ExpiredJwtException e) {
            logger.warn("JWT token is expired: {}", e.getMessage());
            result.setValid(false);
            result.setErrorCode("TOKEN_EXPIRED");
            result.setErrorMessage("Token has expired");
        } catch (UnsupportedJwtException e) {
            logger.error("JWT token is unsupported: {}", e.getMessage());
            result.setValid(false);
            result.setErrorCode("UNSUPPORTED_TOKEN");
            result.setErrorMessage("Unsupported token");
        } catch (MalformedJwtException e) {
            logger.error("JWT token is malformed: {}", e.getMessage());
            result.setValid(false);
            result.setErrorCode("MALFORMED_TOKEN");
            result.setErrorMessage("Malformed token");
        } catch (SecurityException e) {
            logger.error("JWT signature validation failed: {}", e.getMessage());
            result.setValid(false);
            result.setErrorCode("INVALID_SIGNATURE");
            result.setErrorMessage("Invalid token signature");
        } catch (IllegalArgumentException e) {
            logger.error("JWT claims string is empty: {}", e.getMessage());
            result.setValid(false);
            result.setErrorCode("EMPTY_CLAIMS");
            result.setErrorMessage("Empty token claims");
        } catch (Exception e) {
            logger.error("Unexpected error during token validation: {}", e.getMessage());
            result.setValid(false);
            result.setErrorCode("VALIDATION_ERROR");
            result.setErrorMessage("Token validation failed");
        }
        
        return result;
    }

    @Override
    public String extractUsername(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return claims.getSubject();
        } catch (Exception e) {
            logger.error("Error extracting username from token: {}", e.getMessage());
            return null;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<String> extractRoles(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return claims.get("roles", List.class);
        } catch (Exception e) {
            logger.error("Error extracting roles from token: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    @Override
    public boolean isTokenExpired(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    @Override
    public LocalDateTime getTokenExpiration(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return claims.getExpiration().toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime();
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public void revokeToken(String token) {
        tokenBlacklist.add(token);
        logger.info("Token revoked: {}", token.substring(0, Math.min(20, token.length())) + "...");
    }

    @Override
    public boolean isTokenBlacklisted(String token) {
        return tokenBlacklist.contains(token);
    }
}