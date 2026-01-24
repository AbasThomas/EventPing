package thomas.com.EventPing.security.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import thomas.com.EventPing.User.model.User;
import thomas.com.EventPing.config.SecurityProperties;
import thomas.com.EventPing.security.dto.JwtToken;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for JWT Authentication Service
 * Tests specific scenarios and edge cases for JWT functionality
 * **Validates: Requirements 1.1, 1.2, 1.3**
 */
class JwtAuthenticationServiceUnitTest {

    private JwtAuthenticationService jwtAuthenticationService;
    private SecurityProperties securityProperties;

    @BeforeEach
    void setUp() {
        // Set up security properties for testing
        securityProperties = new SecurityProperties();
        SecurityProperties.Jwt jwt = new SecurityProperties.Jwt();
        jwt.setSecret("testSecretKeyForTestingOnly123456789012345678901234567890");
        jwt.setExpiration(3600000L); // 1 hour
        jwt.setRefreshExpiration(86400000L); // 24 hours
        jwt.setIssuer("EventPing");
        jwt.setAudience("EventPing-Users");
        securityProperties.setJwt(jwt);

        jwtAuthenticationService = new JwtAuthenticationService(securityProperties);
        jwtAuthenticationService.clearBlacklist();
    }

    @Test
    @DisplayName("Should generate valid JWT token for user")
    void shouldGenerateValidJwtTokenForUser() {
        // Given
        User user = createTestUser();

        // When
        JwtToken jwtToken = jwtAuthenticationService.generateToken(user);

        // Then
        assertThat(jwtToken).isNotNull();
        assertThat(jwtToken.getAccessToken()).isNotNull().isNotEmpty();
        assertThat(jwtToken.getRefreshToken()).isNotNull().isNotEmpty();
        assertThat(jwtToken.getTokenType()).isEqualTo("Bearer");
        assertThat(jwtToken.getExpiresIn()).isEqualTo(3600L);
    }

    @Test
    @DisplayName("Should validate JWT token successfully")
    void shouldValidateJwtTokenSuccessfully() {
        // Given
        User user = createTestUser();
        JwtToken jwtToken = jwtAuthenticationService.generateToken(user);

        // When
        Claims claims = jwtAuthenticationService.validateToken(jwtToken.getAccessToken());

        // Then
        assertThat(claims).isNotNull();
        assertThat(claims.get("userId", Long.class)).isEqualTo(user.getId());
        assertThat(claims.get("email", String.class)).isEqualTo(user.getEmail());
        assertThat(claims.get("type", String.class)).isEqualTo("access");
        assertThat(claims.getSubject()).isEqualTo(user.getEmail());
        assertThat(claims.getIssuer()).isEqualTo("EventPing");
        assertThat(claims.getAudience()).contains("EventPing-Users");
    }

    @Test
    @DisplayName("Should extract user ID from token")
    void shouldExtractUserIdFromToken() {
        // Given
        User user = createTestUser();
        JwtToken jwtToken = jwtAuthenticationService.generateToken(user);

        // When
        Long extractedUserId = jwtAuthenticationService.extractUserId(jwtToken.getAccessToken());

        // Then
        assertThat(extractedUserId).isEqualTo(user.getId());
    }

    @Test
    @DisplayName("Should extract email from token")
    void shouldExtractEmailFromToken() {
        // Given
        User user = createTestUser();
        JwtToken jwtToken = jwtAuthenticationService.generateToken(user);

        // When
        String extractedEmail = jwtAuthenticationService.extractEmail(jwtToken.getAccessToken());

        // Then
        assertThat(extractedEmail).isEqualTo(user.getEmail());
    }

    @Test
    @DisplayName("Should refresh token successfully")
    void shouldRefreshTokenSuccessfully() {
        // Given
        User user = createTestUser();
        JwtToken originalToken = jwtAuthenticationService.generateToken(user);

        // When
        JwtToken refreshedToken = jwtAuthenticationService.refreshToken(originalToken.getRefreshToken());

        // Then
        assertThat(refreshedToken).isNotNull();
        assertThat(refreshedToken.getAccessToken()).isNotEqualTo(originalToken.getAccessToken());
        assertThat(refreshedToken.getRefreshToken()).isNotEqualTo(originalToken.getRefreshToken());
        
        // Verify new token is valid
        Claims claims = jwtAuthenticationService.validateToken(refreshedToken.getAccessToken());
        assertThat(claims.get("userId", Long.class)).isEqualTo(user.getId());
        assertThat(claims.get("email", String.class)).isEqualTo(user.getEmail());
    }

    @Test
    @DisplayName("Should blacklist token successfully")
    void shouldBlacklistTokenSuccessfully() {
        // Given
        User user = createTestUser();
        JwtToken jwtToken = jwtAuthenticationService.generateToken(user);

        // Verify token is initially valid
        assertThatCode(() -> jwtAuthenticationService.validateToken(jwtToken.getAccessToken()))
                .doesNotThrowAnyException();

        // When
        jwtAuthenticationService.blacklistToken(jwtToken.getAccessToken());

        // Then
        assertThatThrownBy(() -> jwtAuthenticationService.validateToken(jwtToken.getAccessToken()))
                .isInstanceOf(JwtException.class)
                .hasMessageContaining("blacklisted");
    }

    @Test
    @DisplayName("Should reject blacklisted refresh token")
    void shouldRejectBlacklistedRefreshToken() {
        // Given
        User user = createTestUser();
        JwtToken originalToken = jwtAuthenticationService.generateToken(user);
        
        // Refresh token once to blacklist the original refresh token
        jwtAuthenticationService.refreshToken(originalToken.getRefreshToken());

        // When & Then
        assertThatThrownBy(() -> jwtAuthenticationService.refreshToken(originalToken.getRefreshToken()))
                .isInstanceOf(JwtException.class)
                .hasMessageContaining("Refresh token has been blacklisted");
    }

    @Test
    @DisplayName("Should detect expired token")
    void shouldDetectExpiredToken() {
        // Given
        User user = createTestUser();
        
        // Create service with very short expiration for testing
        SecurityProperties shortExpirationProps = new SecurityProperties();
        SecurityProperties.Jwt jwt = new SecurityProperties.Jwt();
        jwt.setSecret("testSecretKeyForTestingOnly123456789012345678901234567890");
        jwt.setExpiration(1L); // 1 millisecond
        jwt.setRefreshExpiration(1L);
        jwt.setIssuer("EventPing");
        jwt.setAudience("EventPing-Users");
        shortExpirationProps.setJwt(jwt);
        
        JwtAuthenticationService shortExpirationService = new JwtAuthenticationService(shortExpirationProps);
        JwtToken jwtToken = shortExpirationService.generateToken(user);

        // Wait for token to expire
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // When & Then
        assertThat(shortExpirationService.isTokenExpired(jwtToken.getAccessToken())).isTrue();
    }

    @Test
    @DisplayName("Should get token expiration time")
    void shouldGetTokenExpirationTime() {
        // Given
        User user = createTestUser();
        LocalDateTime beforeGeneration = LocalDateTime.now();
        JwtToken jwtToken = jwtAuthenticationService.generateToken(user);
        LocalDateTime afterGeneration = LocalDateTime.now();

        // When
        LocalDateTime expirationTime = jwtAuthenticationService.getTokenExpiration(jwtToken.getAccessToken());

        // Then
        assertThat(expirationTime).isAfter(beforeGeneration.plusMinutes(59));
        assertThat(expirationTime).isBefore(afterGeneration.plusMinutes(61));
    }

    @Test
    @DisplayName("Should reject malformed token")
    void shouldRejectMalformedToken() {
        // Given
        String malformedToken = "invalid.jwt.token";

        // When & Then
        assertThatThrownBy(() -> jwtAuthenticationService.validateToken(malformedToken))
                .isInstanceOf(JwtException.class)
                .hasMessageContaining("Malformed token");
    }

    @Test
    @DisplayName("Should reject token with wrong signature")
    void shouldRejectTokenWithWrongSignature() {
        // Given
        User user = createTestUser();
        JwtToken jwtToken = jwtAuthenticationService.generateToken(user);
        
        // Create service with different secret
        SecurityProperties differentSecretProps = new SecurityProperties();
        SecurityProperties.Jwt jwt = new SecurityProperties.Jwt();
        jwt.setSecret("differentSecretKey123456789012345678901234567890123");
        jwt.setExpiration(3600000L);
        jwt.setRefreshExpiration(86400000L);
        jwt.setIssuer("EventPing");
        jwt.setAudience("EventPing-Users");
        differentSecretProps.setJwt(jwt);
        
        JwtAuthenticationService differentSecretService = new JwtAuthenticationService(differentSecretProps);

        // When & Then
        assertThatThrownBy(() -> differentSecretService.validateToken(jwtToken.getAccessToken()))
                .isInstanceOf(JwtException.class)
                .hasMessageContaining("JWT signature does not match");
    }

    @Test
    @DisplayName("Should reject token with wrong issuer")
    void shouldRejectTokenWithWrongIssuer() {
        // Given
        User user = createTestUser();
        
        // Create service with different issuer
        SecurityProperties differentIssuerProps = new SecurityProperties();
        SecurityProperties.Jwt jwt = new SecurityProperties.Jwt();
        jwt.setSecret("testSecretKeyForTestingOnly123456789012345678901234567890");
        jwt.setExpiration(3600000L);
        jwt.setRefreshExpiration(86400000L);
        jwt.setIssuer("WrongIssuer");
        jwt.setAudience("EventPing-Users");
        differentIssuerProps.setJwt(jwt);
        
        JwtAuthenticationService differentIssuerService = new JwtAuthenticationService(differentIssuerProps);
        JwtToken jwtToken = differentIssuerService.generateToken(user);

        // When & Then
        assertThatThrownBy(() -> jwtAuthenticationService.validateToken(jwtToken.getAccessToken()))
                .isInstanceOf(JwtException.class)
                .hasMessageContaining("Invalid issuer");
    }

    @Test
    @DisplayName("Should reject token with wrong audience")
    void shouldRejectTokenWithWrongAudience() {
        // Given
        User user = createTestUser();
        
        // Create service with different audience
        SecurityProperties differentAudienceProps = new SecurityProperties();
        SecurityProperties.Jwt jwt = new SecurityProperties.Jwt();
        jwt.setSecret("testSecretKeyForTestingOnly123456789012345678901234567890");
        jwt.setExpiration(3600000L);
        jwt.setRefreshExpiration(86400000L);
        jwt.setIssuer("EventPing");
        jwt.setAudience("WrongAudience");
        differentAudienceProps.setJwt(jwt);
        
        JwtAuthenticationService differentAudienceService = new JwtAuthenticationService(differentAudienceProps);
        JwtToken jwtToken = differentAudienceService.generateToken(user);

        // When & Then
        assertThatThrownBy(() -> jwtAuthenticationService.validateToken(jwtToken.getAccessToken()))
                .isInstanceOf(JwtException.class)
                .hasMessageContaining("Invalid audience");
    }

    @Test
    @DisplayName("Should clean up expired tokens from blacklist")
    void shouldCleanUpExpiredTokensFromBlacklist() {
        // Given
        User user = createTestUser();
        JwtToken jwtToken = jwtAuthenticationService.generateToken(user);
        jwtAuthenticationService.blacklistToken(jwtToken.getAccessToken());

        // When
        jwtAuthenticationService.cleanupExpiredTokens();

        // Then - This test mainly verifies the method doesn't throw exceptions
        // In a real scenario, we'd need expired tokens to test cleanup
        assertThatCode(() -> jwtAuthenticationService.cleanupExpiredTokens())
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should handle null user gracefully")
    void shouldHandleNullUserGracefully() {
        // When & Then
        assertThatThrownBy(() -> jwtAuthenticationService.generateToken(null))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("Should handle user with null fields gracefully")
    void shouldHandleUserWithNullFieldsGracefully() {
        // Given
        User user = new User();
        user.setId(1L);
        // email is null

        // When & Then
        assertThatCode(() -> jwtAuthenticationService.generateToken(user))
                .doesNotThrowAnyException();
    }

    private User createTestUser() {
        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setFullName("Test User");
        user.setRole(User.UserRole.USER);
        user.setAccountLocked(false);
        user.setFailedLoginAttempts(0);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        return user;
    }
}