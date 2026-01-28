package thomas.com.EventPing.security.service;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import thomas.com.EventPing.User.model.User;
import thomas.com.EventPing.config.SecurityProperties;
import thomas.com.EventPing.security.dto.JwtToken;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class JwtAuthenticationService {

    private final SecurityProperties securityProperties;
    private final AuditLoggingService auditLoggingService;
    
    // In-memory blacklist for demonstration - in production, use Redis
    private final Set<String> blacklistedTokens = ConcurrentHashMap.newKeySet();

    @PostConstruct
    public void init() {
        validateJwtConfiguration();
    }

    private void validateJwtConfiguration() {
        if (securityProperties.getJwt() == null) {
            throw new IllegalStateException("JWT configuration is missing");
        }
        String secret = securityProperties.getJwt().getSecret();
        if (secret == null || secret.length() < 32) {
            throw new IllegalStateException("JWT secret must be at least 32 characters long for HMAC-SHA algorithms");
        }
        if (securityProperties.getJwt().getExpiration() <= 0) {
            throw new IllegalStateException("JWT expiration must be positive");
        }
    }

    /**
     * Generate JWT token with proper claims and expiration
     */
    public JwtToken generateToken(User user) {
        return generateToken(user, null);
    }

    /**
     * Generate JWT token with proper claims and expiration
     */
    public JwtToken generateToken(User user, String ipAddress) {
        try {
            Date now = new Date();
            Date expirationDate = new Date(now.getTime() + securityProperties.getJwt().getExpiration());
            Date refreshExpirationDate = new Date(now.getTime() + securityProperties.getJwt().getRefreshExpiration());

            // Create access token claims
            Map<String, Object> claims = new HashMap<>();
            claims.put("userId", user.getId());
            claims.put("email", user.getEmail());
            claims.put("fullName", user.getFullName());
            claims.put("role", user.getRole().name());
            claims.put("type", "access");

            // Generate access token
            String accessToken = Jwts.builder()
                    .claims(claims)
                    .subject(user.getEmail())
                    .issuer(securityProperties.getJwt().getIssuer())
                    .audience().add(securityProperties.getJwt().getAudience()).and()
                    .issuedAt(now)
                    .expiration(expirationDate)
                    .signWith(getSigningKey())
                    .compact();

            Map<String, Object> refreshClaims = new HashMap<>();
            refreshClaims.put("userId", user.getId());
            refreshClaims.put("email", user.getEmail());
            refreshClaims.put("role", user.getRole().name());
            refreshClaims.put("type", "refresh");
            refreshClaims.put("tokenId", java.util.UUID.randomUUID().toString()); // Add unique identifier

            // Generate refresh token with slightly different timestamp to ensure uniqueness
            Date refreshIssuedAt = new Date(now.getTime() + 1); // Add 1ms to ensure different timestamp
            String refreshToken = Jwts.builder()
                    .claims(refreshClaims)
                    .subject(user.getEmail())
                    .issuer(securityProperties.getJwt().getIssuer())
                    .audience().add(securityProperties.getJwt().getAudience()).and()
                    .issuedAt(refreshIssuedAt)
                    .expiration(refreshExpirationDate)
                    .signWith(getSigningKey())
                    .compact();

            log.debug("Generated JWT token for user: {}", user.getEmail());

            // Log successful authentication
            auditLoggingService.logAuthenticationSuccess(user.getEmail(), ipAddress);

            return JwtToken.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .tokenType("Bearer")
                    .expiresIn(securityProperties.getJwt().getExpiration() / 1000) // Convert to seconds
                    .build();

        } catch (Exception e) {
            log.error("Error generating JWT token for user: {}", user.getEmail(), e);
            
            // Log authentication failure
            auditLoggingService.logAuthenticationFailure(user.getEmail(), ipAddress, 
                    "Token generation failed: " + e.getMessage());
            
            throw new RuntimeException("Failed to generate JWT token", e);
        }
    }

    /**
     * Validate and parse JWT token
     */
    public Claims validateToken(String token) {
        return validateToken(token, null);
    }

    /**
     * Validate and parse JWT token with IP address for audit logging
     */
    public Claims validateToken(String token, String ipAddress) {
        try {
            // Check if token is blacklisted
            if (blacklistedTokens.contains(token)) {
                log.warn("Attempted to use blacklisted token");
                
                // Log security violation for blacklisted token usage
                auditLoggingService.logSecurityViolation(null, ipAddress, 
                        "BLACKLISTED_TOKEN_USAGE", 
                        "Attempted to use blacklisted JWT token",
                        thomas.com.EventPing.security.entity.AuditEvent.AuditSeverity.HIGH);
                
                throw new JwtException("Token has been blacklisted");
            }

            // Parse and validate token
            Claims claims = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            // Manual validation of issuer and audience
            if (!securityProperties.getJwt().getIssuer().equals(claims.getIssuer())) {
                auditLoggingService.logSecurityViolation(claims.getSubject(), ipAddress,
                        "INVALID_TOKEN_ISSUER",
                        "JWT token with invalid issuer: " + claims.getIssuer(),
                        thomas.com.EventPing.security.entity.AuditEvent.AuditSeverity.MEDIUM);
                throw new JwtException("Invalid issuer: " + claims.getIssuer());
            }
            
            // Handle audience as Set<String> in newer JJWT versions
            Set<String> audienceSet = claims.getAudience();
            String expectedAudience = securityProperties.getJwt().getAudience();
            
            if (audienceSet == null || !audienceSet.contains(expectedAudience)) {
                auditLoggingService.logSecurityViolation(claims.getSubject(), ipAddress,
                        "INVALID_TOKEN_AUDIENCE",
                        "JWT token with invalid audience: " + audienceSet,
                        thomas.com.EventPing.security.entity.AuditEvent.AuditSeverity.MEDIUM);
                throw new JwtException("Invalid audience: " + audienceSet);
            }

            // Validate token type
            String tokenType = claims.get("type", String.class);
            if (!"access".equals(tokenType)) {
                auditLoggingService.logSecurityViolation(claims.getSubject(), ipAddress,
                        "INVALID_TOKEN_TYPE",
                        "JWT token with invalid type: " + tokenType,
                        thomas.com.EventPing.security.entity.AuditEvent.AuditSeverity.MEDIUM);
                throw new JwtException("Invalid token type: " + tokenType);
            }

            log.debug("Successfully validated JWT token for user: {}", claims.getSubject());
            return claims;

        } catch (ExpiredJwtException e) {
            log.warn("JWT token has expired: {}", e.getMessage());
            auditLoggingService.logAuthenticationFailure(e.getClaims().getSubject(), ipAddress,
                    "Token expired");
            throw new JwtException("Token has expired", e);
        } catch (UnsupportedJwtException e) {
            log.warn("Unsupported JWT token: {}", e.getMessage());
            auditLoggingService.logSecurityViolation(null, ipAddress,
                    "UNSUPPORTED_TOKEN_FORMAT",
                    "Unsupported JWT token format",
                    thomas.com.EventPing.security.entity.AuditEvent.AuditSeverity.MEDIUM);
            throw new JwtException("Unsupported token format", e);
        } catch (MalformedJwtException e) {
            log.warn("Malformed JWT token: {}", e.getMessage());
            auditLoggingService.logSecurityViolation(null, ipAddress,
                    "MALFORMED_TOKEN",
                    "Malformed JWT token",
                    thomas.com.EventPing.security.entity.AuditEvent.AuditSeverity.MEDIUM);
            throw new JwtException("Malformed token", e);
        } catch (SecurityException e) {
            log.warn("Invalid JWT signature: {}", e.getMessage());
            auditLoggingService.logSecurityViolation(null, ipAddress,
                    "INVALID_TOKEN_SIGNATURE",
                    "Invalid JWT token signature",
                    thomas.com.EventPing.security.entity.AuditEvent.AuditSeverity.HIGH);
            throw new JwtException("Invalid token signature", e);
        } catch (IllegalArgumentException e) {
            log.warn("JWT token compact of handler are invalid: {}", e.getMessage());
            auditLoggingService.logSecurityViolation(null, ipAddress,
                    "INVALID_TOKEN_FORMAT",
                    "Invalid JWT token format",
                    thomas.com.EventPing.security.entity.AuditEvent.AuditSeverity.MEDIUM);
            throw new JwtException("Invalid token", e);
        }
    }

    /**
     * Refresh expired token using refresh token
     */
    public JwtToken refreshToken(String refreshToken) {
        try {
            // Check if refresh token is blacklisted
            if (blacklistedTokens.contains(refreshToken)) {
                log.warn("Attempted to use blacklisted refresh token");
                throw new JwtException("Refresh token has been blacklisted");
            }

            // Parse refresh token
            Claims claims = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(refreshToken)
                    .getPayload();

            // Manual validation of issuer and audience
            if (!securityProperties.getJwt().getIssuer().equals(claims.getIssuer())) {
                throw new JwtException("Invalid issuer: " + claims.getIssuer());
            }
            
            // Handle audience as Set<String> in newer JJWT versions
            Set<String> audienceSet = claims.getAudience();
            String expectedAudience = securityProperties.getJwt().getAudience();
            
            if (audienceSet == null || !audienceSet.contains(expectedAudience)) {
                throw new JwtException("Invalid audience: " + audienceSet);
            }

            // Validate token type
            String tokenType = claims.get("type", String.class);
            if (!"refresh".equals(tokenType)) {
                throw new JwtException("Invalid refresh token type: " + tokenType);
            }

            // Extract user information
            Long userId = claims.get("userId", Long.class);
            String email = claims.get("email", String.class);
            String role = claims.get("role", String.class);

            // Create a minimal user object for token generation
            User user = new User();
            user.setId(userId);
            user.setEmail(email);
            if (role != null) {
                user.setRole(User.UserRole.valueOf(role));
            }

            // Blacklist the old refresh token
            blacklistedTokens.add(refreshToken);

            log.debug("Successfully refreshed JWT token for user: {}", email);

            // Generate new token pair
            return generateToken(user);

        } catch (ExpiredJwtException e) {
            log.warn("Refresh token has expired: {}", e.getMessage());
            throw new JwtException("Refresh token has expired", e);
        } catch (Exception e) {
            log.error("Error refreshing JWT token", e);
            throw new JwtException("Failed to refresh token", e);
        }
    }

    /**
     * Blacklist token on logout
     */
    public void blacklistToken(String token) {
        blacklistToken(token, null);
    }

    /**
     * Blacklist token on logout with IP address for audit logging
     */
    public void blacklistToken(String token, String ipAddress) {
        try {
            // Add token to blacklist
            blacklistedTokens.add(token);
            
            // Also try to extract and blacklist refresh token if this is an access token
            Claims claims = validateTokenWithoutBlacklistCheck(token);
            
            // Log session termination
            auditLoggingService.logSessionEvent(
                    thomas.com.EventPing.security.entity.AuditEvent.AuditEventType.SESSION_END,
                    claims.getSubject(), 
                    null, // session ID not available here
                    ipAddress);
            
            log.info("Successfully blacklisted token for user: {}", claims.getSubject());
            
        } catch (Exception e) {
            log.warn("Error blacklisting token: {}", e.getMessage());
            // Still add to blacklist even if parsing fails
            blacklistedTokens.add(token);
        }
    }

    /**
     * Extract user ID from token
     */
    public Long extractUserId(String token) {
        Claims claims = validateToken(token);
        return claims.get("userId", Long.class);
    }

    /**
     * Extract email from token
     */
    public String extractEmail(String token) {
        Claims claims = validateToken(token);
        return claims.getSubject();
    }

    /**
     * Check if token is expired
     */
    public boolean isTokenExpired(String token) {
        try {
            Claims claims = validateTokenWithoutBlacklistCheck(token);
            return claims.getExpiration().before(new Date());
        } catch (ExpiredJwtException e) {
            return true;
        } catch (Exception e) {
            return true; // Consider invalid tokens as expired
        }
    }

    /**
     * Get token expiration time
     */
    public LocalDateTime getTokenExpiration(String token) {
        Claims claims = validateToken(token);
        return claims.getExpiration().toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
    }

    /**
     * Validate token without checking blacklist (internal use)
     */
    private Claims validateTokenWithoutBlacklistCheck(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Get signing key from configuration
     */
    private SecretKey getSigningKey() {
        String secret = securityProperties.getJwt().getSecret();
        if (secret == null || secret.length() < 32) {
            throw new IllegalStateException("JWT secret must be at least 32 characters long");
        }
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Clean up expired tokens from blacklist (should be called periodically)
     */
    public void cleanupExpiredTokens() {
        blacklistedTokens.removeIf(token -> {
            try {
                return isTokenExpired(token);
            } catch (Exception e) {
                // Remove tokens that can't be parsed
                return true;
            }
        });
        log.debug("Cleaned up expired tokens from blacklist");
    }

    /**
     * Clear all blacklisted tokens (for testing purposes)
     */
    public void clearBlacklist() {
        blacklistedTokens.clear();
        log.debug("Cleared all blacklisted tokens");
    }
}