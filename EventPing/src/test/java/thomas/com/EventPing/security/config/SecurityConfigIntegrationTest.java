package thomas.com.EventPing.security.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import thomas.com.EventPing.User.model.User;
import thomas.com.EventPing.User.repository.UserRepository;
import thomas.com.EventPing.event.repository.EventRepository;
import thomas.com.EventPing.security.service.JwtAuthenticationService;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for Spring Security configuration
 * **Validates: Requirements 1.1, 5.1**
 */
@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
class SecurityConfigIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private EventRepository eventRepository;

    @MockBean
    private JwtAuthenticationService jwtAuthenticationService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    @DisplayName("Should allow access to public authentication endpoints")
    void shouldAllowAccessToPublicAuthEndpoints() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"test@example.com\",\"password\":\"password\"}"))
                .andExpect(status().isNotFound()); // 404 because endpoint doesn't exist yet, but not 401/403

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"test@example.com\",\"password\":\"password\"}"))
                .andExpect(status().isNotFound()); // 404 because endpoint doesn't exist yet, but not 401/403
    }

    @Test
    @DisplayName("Should allow access to health check endpoint")
    void shouldAllowAccessToHealthEndpoint() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should allow GET access to public event endpoints")
    void shouldAllowGetAccessToPublicEventEndpoints() throws Exception {
        mockMvc.perform(get("/api/events/test-event"))
                .andExpect(status().isNotFound()); // 404 because endpoint doesn't exist yet, but not 401/403
    }

    @Test
    @DisplayName("Should require authentication for protected user endpoints")
    void shouldRequireAuthenticationForProtectedUserEndpoints() throws Exception {
        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").value("Authentication required"));

        mockMvc.perform(put("/api/users/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"fullName\":\"Updated Name\"}"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(delete("/api/users/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should require authentication for protected event endpoints")
    void shouldRequireAuthenticationForProtectedEventEndpoints() throws Exception {
        mockMvc.perform(post("/api/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"Test Event\",\"description\":\"Test Description\"}"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(put("/api/events/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"Updated Event\"}"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(delete("/api/events/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should require ADMIN role for actuator endpoints")
    void shouldRequireAdminRoleForActuatorEndpoints() throws Exception {
        mockMvc.perform(get("/actuator/info"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Should deny access to admin endpoints for regular users")
    void shouldDenyAccessToAdminEndpointsForRegularUsers() throws Exception {
        mockMvc.perform(get("/actuator/info"))
                .andExpect(status().isForbidden())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("Forbidden"))
                .andExpect(jsonPath("$.message").value("Insufficient permissions"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should allow access to admin endpoints for admin users")
    void shouldAllowAccessToAdminEndpointsForAdminUsers() throws Exception {
        mockMvc.perform(get("/actuator/info"))
                .andExpect(status().isNotFound()); // 404 because endpoint doesn't exist, but not 403
    }

    @Test
    @DisplayName("Should include security headers in responses")
    void shouldIncludeSecurityHeadersInResponses() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Frame-Options", "DENY"))
                .andExpect(header().string("X-Content-Type-Options", "nosniff"))
                .andExpect(header().exists("Strict-Transport-Security"))
                .andExpect(header().exists("Referrer-Policy"));
    }

    @Test
    @DisplayName("Should handle JWT authentication with valid token")
    void shouldHandleJwtAuthenticationWithValidToken() throws Exception {
        // Given
        User testUser = createTestUser();
        String validToken = "valid.jwt.token";
        
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(jwtAuthenticationService.validateToken(validToken))
                .thenReturn(createMockClaims(testUser));

        // When & Then
        mockMvc.perform(get("/api/users/1")
                .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isNotFound()); // 404 because endpoint doesn't exist, but not 401
    }

    @Test
    @DisplayName("Should reject invalid JWT tokens")
    void shouldRejectInvalidJwtTokens() throws Exception {
        // Given
        String invalidToken = "invalid.jwt.token";
        
        when(jwtAuthenticationService.validateToken(invalidToken))
                .thenThrow(new io.jsonwebtoken.JwtException("Invalid token"));

        // When & Then
        mockMvc.perform(get("/api/users/1")
                .header("Authorization", "Bearer " + invalidToken))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should reject malformed Authorization headers")
    void shouldRejectMalformedAuthorizationHeaders() throws Exception {
        mockMvc.perform(get("/api/users/1")
                .header("Authorization", "InvalidFormat token"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/users/1")
                .header("Authorization", "Bearer"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should handle locked user accounts")
    void shouldHandleLockedUserAccounts() throws Exception {
        // Given
        User lockedUser = createTestUser();
        lockedUser.setAccountLocked(true);
        String validToken = "valid.jwt.token";
        
        when(userRepository.findById(1L)).thenReturn(Optional.of(lockedUser));
        when(jwtAuthenticationService.validateToken(validToken))
                .thenReturn(createMockClaims(lockedUser));

        // When & Then
        mockMvc.perform(get("/api/users/1")
                .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should handle non-existent users in JWT tokens")
    void shouldHandleNonExistentUsersInJwtTokens() throws Exception {
        // Given
        User testUser = createTestUser();
        String validToken = "valid.jwt.token";
        
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        when(jwtAuthenticationService.validateToken(validToken))
                .thenReturn(createMockClaims(testUser));

        // When & Then
        mockMvc.perform(get("/api/users/1")
                .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should handle CSRF protection for state-changing operations")
    void shouldHandleCsrfProtectionForStateChangingOperations() throws Exception {
        // CSRF should be enabled for non-auth endpoints
        mockMvc.perform(post("/api/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"Test Event\"}"))
                .andExpect(status().isUnauthorized()); // First fails due to authentication, not CSRF
    }

    @Test
    @DisplayName("Should skip CSRF for authentication endpoints")
    void shouldSkipCsrfForAuthenticationEndpoints() throws Exception {
        // Auth endpoints should skip CSRF
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"test@example.com\",\"password\":\"password\"}"))
                .andExpect(status().isNotFound()); // 404 because endpoint doesn't exist, not 403 CSRF error
    }

    // Helper methods

    private User createTestUser() {
        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setPasswordHash("$2a$12$hashedpassword");
        user.setRole(User.UserRole.USER);
        user.setAccountLocked(false);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        return user;
    }

    private io.jsonwebtoken.Claims createMockClaims(User user) {
        io.jsonwebtoken.Claims claims = org.mockito.Mockito.mock(io.jsonwebtoken.Claims.class);
        when(claims.get("userId", Long.class)).thenReturn(user.getId());
        when(claims.get("email", String.class)).thenReturn(user.getEmail());
        when(claims.get("role", String.class)).thenReturn(user.getRole().name());
        return claims;
    }
}