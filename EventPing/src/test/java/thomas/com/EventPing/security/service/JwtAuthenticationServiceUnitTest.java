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
    private User testUser;

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

        // Create test user
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@e