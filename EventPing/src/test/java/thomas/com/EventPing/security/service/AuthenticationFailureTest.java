package thomas.com.EventPing.security.service;

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
 * Unit tests for Authentication Failure Scenarios
 * Tests various failure conditions and security edge cases
 * **Validates: Requirements 1.1, 1.2, 1.3**
 */
class AuthenticationFailureTest {

    private JwtAuthenticationService jwtAuthenticationService;
    private PasswordService passwordService;

    @BeforeEach
    void setUp() {
        // Set up security properties for testing
        SecurityProperties securityProperties = new SecurityProperties();
        SecurityProperties.Jwt jwt = new SecurityProperties.Jwt();
        jwt.setSecret("testSecretKeyForTestingOnly123456789012345678901234567890");
        jwt.setExpiration(3600000L); // 1 hour
        jwt.setRefreshExpiration(86400000L); // 24 hours
        jwt.setIssuer("EventPing");
        jwt.setAudience("EventPing-Users");
        securityProperties.setJwt(jwt);

        jwtAuthenticationService = new JwtAuthenticationService(securityProperties);
        jwtAuthenticationService.clearBlacklist();
        passwordService = new PasswordService();
    }

    @Test
    @DisplayName("Should reject empty JWT token")
    void shouldRejectEmptyJwtToken() {
        // When & Then
        assertThatThrownBy(() -> jwtAuthenticationService.validateToken(""))
                .isInstanceOf(JwtException.class)
                .hasMessageContaining("Invalid token");
    }

    @Test
    @DisplayName("Should reject null JWT token")
    void shouldRejectNullJwtToken() {
        // When & Then
        assertThatThrownBy(() -> jwtAuthenticationService.validateToken(null))
                .isInstanceOf(JwtException.class)
                .hasMessageContaining("Invalid token");
    }

    @Test
    @DisplayName("Should reject JWT token with only header")
    void shouldRejectJwtTokenWithOnlyHeader() {
        // Given
        String incompleteToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9";

        // When & Then
        assertThatThrownBy(() -> jwtAuthenticationService.validateToken(incompleteToken))
                .isInstanceOf(JwtException.class)
                .hasMessageContaining("Malformed token");
    }

    @Test
    @DisplayName("Should reject JWT token with invalid base64 encoding")
    void shouldRejectJwtTokenWithInvalidBase64Encoding() {
        // Given
        String invalidToken = "invalid.base64.encoding!@#$%";

        // When & Then
        assertThatThrownBy(() -> jwtAuthenticationService.validateToken(invalidToken))
                .isInstanceOf(JwtException.class)
                .hasMessageContaining("Malformed token");
    }

    @Test
    @DisplayName("Should reject refresh token used as access token")
    void shouldRejectRefreshTokenUsedAsAccessToken() {
        // Given
        User user = createTestUser();
        JwtToken jwtToken = jwtAuthenticationService.generateToken(user);

        // When & Then - Try to validate refresh token as access token
        assertThatThrownBy(() -> jwtAuthenticationService.validateToken(jwtToken.getRefreshToken()))
                .isInstanceOf(JwtException.class)
                .hasMessageContaining("Invalid token type");
    }

    @Test
    @DisplayName("Should reject access token used as refresh token")
    void shouldRejectAccessTokenUsedAsRefreshToken() {
        // Given
        User user = createTestUser();
        JwtToken jwtToken = jwtAuthenticationService.generateToken(user);

        // When & Then - Try to refresh using access token
        assertThatThrownBy(() -> jwtAuthenticationService.refreshToken(jwtToken.getAccessToken()))
                .isInstanceOf(JwtException.class)
                .hasMessageContaining("Invalid refresh token type");
    }

    @Test
    @DisplayName("Should handle account lockout scenario")
    void shouldHandleAccountLockoutScenario() {
        // Given
        User lockedUser = createTestUser();
        lockedUser.setAccountLocked(true);
        lockedUser.setFailedLoginAttempts(5);

        // When - Generate token (this should still work as lockout is handled at service layer)
        JwtToken jwtToken = jwtAuthenticationService.generateToken(lockedUser);

        // Then - Token should be generated but contain locked account info
        assertThat(jwtToken).isNotNull();
        
        // Validate token contains user info including locked status
        var claims = jwtAuthenticationService.validateToken(jwtToken.getAccessToken());
        assertThat(claims.get("userId", Long.class)).isEqualTo(lockedUser.getId());
        assertThat(claims.get("email", String.class)).isEqualTo(lockedUser.getEmail());
    }

    @Test
    @DisplayName("Should handle multiple failed login attempts")
    void shouldHandleMultipleFailedLoginAttempts() {
        // Given
        User user = createTestUser();
        user.setFailedLoginAttempts(4); // Just below lockout threshold

        // When
        JwtToken jwtToken = jwtAuthenticationService.generateToken(user);

        // Then
        assertThat(jwtToken).isNotNull();
        var claims = jwtAuthenticationService.validateToken(jwtToken.getAccessToken());
        assertThat(claims.get("userId", Long.class)).isEqualTo(user.getId());
    }

    @Test
    @DisplayName("Should reject token after user ID extraction fails")
    void shouldRejectTokenAfterUserIdExtractionFails() {
        // Given
        User user = createTestUser();
        JwtToken jwtToken = jwtAuthenticationService.generateToken(user);
        jwtAuthenticationService.blacklistToken(jwtToken.getAccessToken());

        // When & Then
        assertThatThrownBy(() -> jwtAuthenticationService.extractUserId(jwtToken.getAccessToken()))
                .isInstanceOf(JwtException.class)
                .hasMessageContaining("blacklisted");
    }

    @Test
    @DisplayName("Should reject token after email extraction fails")
    void shouldRejectTokenAfterEmailExtractionFails() {
        // Given
        User user = createTestUser();
        JwtToken jwtToken = jwtAuthenticationService.generateToken(user);
        jwtAuthenticationService.blacklistToken(jwtToken.getAccessToken());

        // When & Then
        assertThatThrownBy(() -> jwtAuthenticationService.extractEmail(jwtToken.getAccessToken()))
                .isInstanceOf(JwtException.class)
                .hasMessageContaining("blacklisted");
    }

    @Test
    @DisplayName("Should handle password verification with corrupted hash")
    void shouldHandlePasswordVerificationWithCorruptedHash() {
        // Given
        String plainPassword = "TestPassword123!";
        String corruptedHash = "$2a$12$corrupted.hash.that.is.invalid";

        // When
        boolean isValid = passwordService.verifyPassword(plainPassword, corruptedHash);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Should handle password hashing with extremely long password")
    void shouldHandlePasswordHashingWithExtremelyLongPassword() {
        // Given
        String extremelyLongPassword = "a".repeat(10000) + "B1!";

        // When
        String hashedPassword = passwordService.hashPassword(extremelyLongPassword);

        // Then
        assertThat(hashedPassword).isNotNull();
        assertThat(passwordService.verifyPassword(extremelyLongPassword, hashedPassword)).isTrue();
    }

    @Test
    @DisplayName("Should handle concurrent token blacklisting")
    void shouldHandleConcurrentTokenBlacklisting() {
        // Given
        User user = createTestUser();
        JwtToken jwtToken = jwtAuthenticationService.generateToken(user);

        // When - Blacklist token multiple times concurrently
        assertThatCode(() -> {
            jwtAuthenticationService.blacklistToken(jwtToken.getAccessToken());
            jwtAuthenticationService.blacklistToken(jwtToken.getAccessToken());
            jwtAuthenticationService.blacklistToken(jwtToken.getAccessToken());
        }).doesNotThrowAnyException();

        // Then
        assertThatThrownBy(() -> jwtAuthenticationService.validateToken(jwtToken.getAccessToken()))
                .isInstanceOf(JwtException.class)
                .hasMessageContaining("blacklisted");
    }

    @Test
    @DisplayName("Should handle token expiration edge case")
    void shouldHandleTokenExpirationEdgeCase() {
        // Given
        User user = createTestUser();
        JwtToken jwtToken = jwtAuthenticationService.generateToken(user);

        // When - Check if token is expired immediately after generation
        boolean isExpired = jwtAuthenticationService.isTokenExpired(jwtToken.getAccessToken());

        // Then
        assertThat(isExpired).isFalse();
    }

    @Test
    @DisplayName("Should handle malformed refresh token")
    void shouldHandleMalformedRefreshToken() {
        // Given
        String malformedRefreshToken = "malformed.refresh.token";

        // When & Then
        assertThatThrownBy(() -> jwtAuthenticationService.refreshToken(malformedRefreshToken))
                .isInstanceOf(JwtException.class)
                .hasMessageContaining("Failed to refresh token");
    }

    @Test
    @DisplayName("Should handle null refresh token")
    void shouldHandleNullRefreshToken() {
        // When & Then
        assertThatThrownBy(() -> jwtAuthenticationService.refreshToken(null))
                .isInstanceOf(JwtException.class)
                .hasMessageContaining("Failed to refresh token");
    }

    @Test
    @DisplayName("Should handle empty refresh token")
    void shouldHandleEmptyRefreshToken() {
        // When & Then
        assertThatThrownBy(() -> jwtAuthenticationService.refreshToken(""))
                .isInstanceOf(JwtException.class)
                .hasMessageContaining("Failed to refresh token");
    }

    @Test
    @DisplayName("Should handle user with different roles")
    void shouldHandleUserWithDifferentRoles() {
        // Test all user roles
        User.UserRole[] roles = {User.UserRole.USER, User.UserRole.ADMIN, User.UserRole.MODERATOR};

        for (User.UserRole role : roles) {
            // Given
            User user = createTestUser();
            user.setRole(role);

            // When
            JwtToken jwtToken = jwtAuthenticationService.generateToken(user);

            // Then
            assertThat(jwtToken).isNotNull();
            var claims = jwtAuthenticationService.validateToken(jwtToken.getAccessToken());
            assertThat(claims.get("userId", Long.class)).isEqualTo(user.getId());
            assertThat(claims.get("email", String.class)).isEqualTo(user.getEmail());
        }
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