package thomas.com.EventPing.security.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import net.jqwik.api.*;
import net.jqwik.api.lifecycle.BeforeProperty;
import thomas.com.EventPing.User.model.User;
import thomas.com.EventPing.config.SecurityProperties;
import thomas.com.EventPing.security.dto.JwtToken;
import org.mockito.Mockito;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

/**
 * Property-based tests for JWT Authentication Service
 * **Feature: eventping-security-hardening, Property 1: Authentication Token Validity**
 * **Validates: Requirements 1.2, 1.4**
 */
class JwtAuthenticationServicePropertyTest {

    private JwtAuthenticationService jwtAuthenticationService;
    private SecurityProperties securityProperties;
    private AuditLoggingService auditLoggingService;

    @BeforeProperty
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

        auditLoggingService = Mockito.mock(AuditLoggingService.class);
        jwtAuthenticationService = new JwtAuthenticationService(securityProperties, auditLoggingService);
        
        // Clear blacklist before each test to avoid interference
        jwtAuthenticationService.clearBlacklist();
    }

    /**
     * Property 1: Authentication Token Validity
     * For any valid user, the generated JWT token should be valid according to the configured 
     * signing key and should contain all required claims (user ID, roles, expiration)
     */
    @Property(tries = 100)
    @Label("For any valid user, generated JWT tokens should be valid and contain required claims")
    void generatedTokensShouldBeValidAndContainRequiredClaims(
            @ForAll("validUsers") User user) {
        
        // Generate token for the user
        JwtToken jwtToken = jwtAuthenticationService.generateToken(user);
        
        // Verify token structure
        assertThat(jwtToken).isNotNull();
        assertThat(jwtToken.getAccessToken()).isNotNull().isNotEmpty();
        assertThat(jwtToken.getRefreshToken()).isNotNull().isNotEmpty();
        assertThat(jwtToken.getTokenType()).isEqualTo("Bearer");
        assertThat(jwtToken.getExpiresIn()).isPositive();
        
        // Validate access token
        Claims accessClaims = jwtAuthenticationService.validateToken(jwtToken.getAccessToken());
        
        // Verify required claims are present
        assertThat(accessClaims.get("userId", Long.class)).isEqualTo(user.getId());
        assertThat(accessClaims.get("email", String.class)).isEqualTo(user.getEmail());
        assertThat(accessClaims.get("type", String.class)).isEqualTo("access");
        assertThat(accessClaims.getSubject()).isEqualTo(user.getEmail());
        assertThat(accessClaims.getIssuer()).isEqualTo("EventPing");
        assertThat(accessClaims.getAudience()).contains("EventPing-Users");
        assertThat(accessClaims.getIssuedAt()).isNotNull();
        assertThat(accessClaims.getExpiration()).isNotNull();
        
        // Verify token is not expired
        assertThat(jwtAuthenticationService.isTokenExpired(jwtToken.getAccessToken())).isFalse();
        
        // Verify user ID and email extraction
        assertThat(jwtAuthenticationService.extractUserId(jwtToken.getAccessToken())).isEqualTo(user.getId());
        assertThat(jwtAuthenticationService.extractEmail(jwtToken.getAccessToken())).isEqualTo(user.getEmail());
    }

    @Property(tries = 100)
    @Label("For any valid refresh token, token refresh should generate new valid tokens")
    void refreshTokenShouldGenerateNewValidTokens(@ForAll("validUsers") User user) {
        
        // Create a fresh service instance for this test iteration to avoid state sharing
        JwtAuthenticationService freshService = new JwtAuthenticationService(securityProperties, auditLoggingService);
        
        // Generate initial token
        JwtToken initialToken = freshService.generateToken(user);
        
        // Refresh the token
        JwtToken refreshedToken = freshService.refreshToken(initialToken.getRefreshToken());
        
        // Verify new token is valid
        assertThat(refreshedToken).isNotNull();
        assertThat(refreshedToken.getAccessToken()).isNotNull().isNotEmpty();
        assertThat(refreshedToken.getRefreshToken()).isNotNull().isNotEmpty();
        
        // Verify new tokens are different from original
        assertThat(refreshedToken.getAccessToken()).isNotEqualTo(initialToken.getAccessToken());
        assertThat(refreshedToken.getRefreshToken()).isNotEqualTo(initialToken.getRefreshToken());
        
        // Verify new access token is valid
        Claims newClaims = freshService.validateToken(refreshedToken.getAccessToken());
        assertThat(newClaims.get("userId", Long.class)).isEqualTo(user.getId());
        assertThat(newClaims.get("email", String.class)).isEqualTo(user.getEmail());
        
        // Verify old refresh token is blacklisted (should throw exception when used again)
        assertThatThrownBy(() -> 
            freshService.refreshToken(initialToken.getRefreshToken())
        ).isInstanceOf(JwtException.class)
         .satisfies(ex -> {
             // Check either the main message or the cause message contains "blacklisted"
             String message = ex.getMessage();
             String causeMessage = ex.getCause() != null ? ex.getCause().getMessage() : "";
             assertThat(message + " " + causeMessage).containsIgnoringCase("blacklisted");
         });
    }

    @Property(tries = 100)
    @Label("For any valid token, blacklisting should prevent further use")
    void blacklistedTokensShouldBeRejected(@ForAll("validUsers") User user) {
        
        // Create a fresh service instance for this test iteration to avoid state sharing
        JwtAuthenticationService freshService = new JwtAuthenticationService(securityProperties, auditLoggingService);
        
        // Generate token
        JwtToken jwtToken = freshService.generateToken(user);
        
        // Verify token is initially valid
        assertThatCode(() -> 
            freshService.validateToken(jwtToken.getAccessToken())
        ).doesNotThrowAnyException();
        
        // Blacklist the token
        freshService.blacklistToken(jwtToken.getAccessToken());
        
        // Verify blacklisted token is rejected
        assertThatThrownBy(() -> 
            freshService.validateToken(jwtToken.getAccessToken())
        ).hasMessageContaining("blacklisted");
    }

    @Property(tries = 50)
    @Label("For any user, token expiration time should be correctly calculated")
    void tokenExpirationShouldBeCorrectlyCalculated(@ForAll("validUsers") User user) {
        
        LocalDateTime beforeGeneration = LocalDateTime.now();
        JwtToken jwtToken = jwtAuthenticationService.generateToken(user);
        LocalDateTime afterGeneration = LocalDateTime.now();
        
        LocalDateTime tokenExpiration = jwtAuthenticationService.getTokenExpiration(jwtToken.getAccessToken());
        
        // Token expiration should be approximately 1 hour from now (configured expiration)
        LocalDateTime expectedMinExpiration = beforeGeneration.plusSeconds(3590); // 59 minutes 50 seconds
        LocalDateTime expectedMaxExpiration = afterGeneration.plusSeconds(3610); // 60 minutes 10 seconds
        
        assertThat(tokenExpiration).isBetween(expectedMinExpiration, expectedMaxExpiration);
    }

    /**
     * Generator for valid users
     */
    @Provide
    Arbitrary<User> validUsers() {
        return Combinators.combine(
            Arbitraries.longs().between(1L, 1000000L),
            Arbitraries.strings().alpha().ofMinLength(3).ofMaxLength(20),
            Arbitraries.strings().alpha().ofMinLength(5).ofMaxLength(30)
        ).as((id, name, emailPrefix) -> {
            User user = new User();
            user.setId(id);
            user.setEmail(emailPrefix.toLowerCase() + "@example.com");
            user.setFullName(name);
            user.setCreatedAt(LocalDateTime.now());
            user.setUpdatedAt(LocalDateTime.now());
            return user;
        });
    }
}