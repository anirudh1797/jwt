package com.auth.framework.core.service.impl;

import com.auth.framework.core.domain.RefreshToken;
import com.auth.framework.core.domain.User;
import com.auth.framework.core.dto.AuthenticationResult;
import com.auth.framework.core.exception.TokenRefreshException;
import com.auth.framework.core.repository.RefreshTokenRepository;
import com.auth.framework.core.service.JwtService;
import com.auth.framework.core.service.RefreshTokenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of refresh token service.
 * Follows Single Responsibility Principle - only handles refresh token operations.
 * Follows Dependency Inversion Principle - depends on abstractions.
 */
@Service
@Transactional
public class RefreshTokenServiceImpl implements RefreshTokenService {
    
    private static final Logger logger = LoggerFactory.getLogger(RefreshTokenServiceImpl.class);
    
    @Value("${auth.jwt.refresh-token-expiration:86400}") // 24 hours
    private int refreshTokenExpirationMs;
    
    @Value("${auth.jwt.max-refresh-tokens-per-user:5}")
    private int maxRefreshTokensPerUser;
    
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;
    private final SecureRandom secureRandom = new SecureRandom();

    @Autowired
    public RefreshTokenServiceImpl(RefreshTokenRepository refreshTokenRepository, JwtService jwtService) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtService = jwtService;
    }

    @Override
    public RefreshToken createRefreshToken(User user) {
        return createRefreshToken(user, null, null);
    }

    @Override
    public RefreshToken createRefreshToken(User user, String ipAddress, String userAgent) {
        // Check if user has too many active tokens
        long activeTokenCount = getActiveTokenCount(user);
        if (activeTokenCount >= maxRefreshTokensPerUser) {
            // Revoke oldest tokens
            List<RefreshToken> userTokens = refreshTokenRepository.findByUser(user);
            userTokens.stream()
                    .filter(RefreshToken::isActive)
                    .sorted((t1, t2) -> t1.getCreatedAt().compareTo(t2.getCreatedAt()))
                    .limit(activeTokenCount - maxRefreshTokensPerUser + 1)
                    .forEach(token -> {
                        token.setRevoked(true);
                        refreshTokenRepository.save(token);
                    });
        }
        
        // Generate new refresh token
        String tokenValue = generateRefreshTokenValue();
        LocalDateTime expiryDate = LocalDateTime.now().plusSeconds(refreshTokenExpirationMs);
        
        RefreshToken refreshToken = new RefreshToken(tokenValue, user, expiryDate);
        refreshToken.setIpAddress(ipAddress);
        refreshToken.setUserAgent(userAgent);
        
        return refreshTokenRepository.save(refreshToken);
    }

    @Override
    public AuthenticationResult refreshAccessToken(String refreshToken) throws TokenRefreshException {
        RefreshToken token = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new TokenRefreshException(refreshToken, "Refresh token not found"));
        
        token = verifyExpiration(token);
        
        User user = token.getUser();
        
        // Generate new access token
        String newAccessToken = jwtService.generateAccessToken(user);
        
        // Optionally generate new refresh token (refresh token rotation)
        String newRefreshToken = jwtService.generateRefreshToken(user);
        
        // Revoke old refresh token
        token.setRevoked(true);
        refreshTokenRepository.save(token);
        
        // Create new refresh token
        RefreshToken newRefreshTokenEntity = createRefreshToken(user, token.getIpAddress(), token.getUserAgent());
        
        // Build authentication result
        List<String> roles = user.getRoles().stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toList());
        
        AuthenticationResult result = new AuthenticationResult(
                user,
                newAccessToken,
                newRefreshTokenEntity.getToken(),
                900L, // 15 minutes
                roles
        );
        
        result.setExpiresAt(LocalDateTime.now().plusSeconds(900));
        result.setLastLogin(user.getLastLogin());
        
        return result;
    }

    @Override
    public RefreshToken verifyExpiration(RefreshToken token) throws TokenRefreshException {
        if (token.getRevoked()) {
            throw new TokenRefreshException(token.getToken(), "Refresh token has been revoked");
        }
        
        if (token.isExpired()) {
            throw new TokenRefreshException(token.getToken(), "Refresh token has expired");
        }
        
        return token;
    }

    @Override
    public void deleteByUser(User user) {
        refreshTokenRepository.deleteByUser(user);
        logger.info("Deleted all refresh tokens for user: {}", user.getUsername());
    }

    @Override
    public void deleteByUserId(Long userId) {
        refreshTokenRepository.deleteByUserId(userId);
        logger.info("Deleted all refresh tokens for user ID: {}", userId);
    }

    @Override
    public void revokeToken(String token) {
        refreshTokenRepository.findByToken(token)
                .ifPresent(refreshToken -> {
                    refreshToken.setRevoked(true);
                    refreshTokenRepository.save(refreshToken);
                    logger.info("Revoked refresh token for user: {}", refreshToken.getUser().getUsername());
                });
    }

    @Override
    public void revokeByUser(User user) {
        refreshTokenRepository.revokeByUser(user);
        logger.info("Revoked all refresh tokens for user: {}", user.getUsername());
    }

    @Override
    public void revokeByUserId(Long userId) {
        refreshTokenRepository.revokeByUserId(userId);
        logger.info("Revoked all refresh tokens for user ID: {}", userId);
    }

    @Override
    @Scheduled(fixedRate = 3600000) // Run every hour
    public void cleanupExpiredTokens() {
        LocalDateTime now = LocalDateTime.now();
        refreshTokenRepository.deleteExpiredTokens(now);
        logger.debug("Cleaned up expired refresh tokens");
    }

    @Override
    public long getActiveTokenCount(User user) {
        return refreshTokenRepository.countActiveTokensByUser(user, LocalDateTime.now());
    }
    
    private String generateRefreshTokenValue() {
        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }
}