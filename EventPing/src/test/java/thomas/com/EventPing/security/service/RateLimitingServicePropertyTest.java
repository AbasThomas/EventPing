package thomas.com.EventPing.security.service;

import net.jqwik.api.*;
import net.jqwik.api.lifecycle.BeforeProperty;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import thomas.com.EventPing.User.model.User;
import thomas.com.EventPing.config.RateLimitProperties;
import thomas.com.EventPing.security.model.RateLimitTracking;
import thomas.com.EventPing.security.model.RateLimitType;
import thomas.com.EventPing.security.repository.RateLimitTrackingRepository;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Property-based tests for Rate Limiting Service
 * **Feature: eventping-security-hardening, Property 4: Rate Limiting Enforcement**
 * **Validates: Requirements 3.1, 3.2**
 */
@TestPropertySource(properties = {
    "eventping.rate-limit.enabled=true",
    "eventping.rate-limit.ip.requests-per-minute=10",
    "eventping.rate-limit.user.api-requests-per-minute=20"
})
class RateLimitingServicePropertyTest {

    private RateLimitingService rateLimitingService;
    private RateLimitTrackingRepository mockRepository;
    private RateLimitProperties rateLimitProperties;

    @BeforeProperty
    void setUp() {
        // Set up rate limit properties for testing
        rateLimitProperties = new RateLimitProperties();
        
        // Configure IP limits
        RateLimitProperties.Ip ipConfig = new RateLimitProperties.Ip();
        ipConfig.setRequestsPerMinute(10);
        ipConfig.setRequestsPerHour(100);
        rateLimitProperties.setIp(ipConfig);
        
        // Configure User limits
        RateLimitProperties.User userConfig = new RateLimitProperties.User();
        userConfig.setApiRequestsPerMinute(20);
        userConfig.setApiRequestsPerHour(200);
        rateLimitProperties.setUser(userConfig);
        
        // Configure Global limits
        RateLimitProperties.Global globalConfig = new RateLimitProperties.Global();
        globalConfig.setRequestsPerSecond(5);
        rateLimitProperties.setGlobal(globalConfig);
        
        // Configure Endpoint limits
        RateLimitProperties.Endpoint endpointConfig = new RateLimitProperties.Endpoint();
        endpointConfig.setRequestsPerMinute(30);
        rateLimitProperties.setEndpoint(endpointConfig);
        
        // Configure Plan limits
        RateLimitProperties.Plan planConfig = new RateLimitProperties.Plan();
        planConfig.setBasicRequestsPerHour(100);
        rateLimitProperties.setPlan(planConfig);

        // Mock repository
        mockRepository = mock(RateLimitTrackingRepository.class);
        
        // Create service with mocked dependencies
        rateLimitingService = new RateLimitingService(mockRepository, rateLimitProperties);
    }

    /**
     * Property 4: Rate Limiting Enforcement
     * For any sequence of API requests from a single source, when the requests exceed 
     * the defined rate limits, the system should return HTTP 429 and block further 
     * requests until the rate limit window resets
     */
    @Property(tries = 100)
    @Label("For any identifier and rate limit type, exceeding limits should result in rate limiting")
    void exceedingRateLimitsShouldResultInBlocking(
            @ForAll("validIdentifiers") String identifier,
            @ForAll("rateLimitTypes") RateLimitType type) {
        
        // Mock no existing record initially
        when(mockRepository.findByIdentifierAndLimitType(anyString(), any(RateLimitType.class)))
            .thenReturn(Optional.empty());
        
        // Mock save operation
        when(mockRepository.save(any(RateLimitTracking.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
        
        int maxRequests = getMaxRequestsForType(type);
        
        // Make requests up to the limit - should all be allowed
        for (int i = 1; i <= maxRequests; i++) {
            RateLimitResult result = rateLimitingService.checkRateLimit(identifier, type);
            
            assertThat(result.isAllowed())
                .as("Request %d should be allowed (limit: %d)", i, maxRequests)
                .isTrue();
            assertThat(result.getCurrentCount()).isEqualTo(i);
            assertThat(result.getMaxRequests()).isEqualTo(maxRequests);
            assertThat(result.getRemainingRequests()).isEqualTo(maxRequests - i);
        }
        
        // The next request should exceed the limit and be blocked
        RateLimitResult exceededResult = rateLimitingService.checkRateLimit(identifier, type);
        
        assertThat(exceededResult.isAllowed())
            .as("Request exceeding limit should be blocked")
            .isFalse();
        assertThat(exceededResult.getCurrentCount()).isGreaterThan(maxRequests);
        assertThat(exceededResult.getRemainingRequests()).isZero();
        assertThat(exceededResult.getRetryAfterSeconds()).isPositive();
        assertThat(exceededResult.getReason()).containsIgnoringCase("rate limit exceeded");
    }

    @Property(tries = 100)
    @Label("For any IP address, rate limiting should be enforced consistently")
    void ipRateLimitingShouldBeEnforcedConsistently(@ForAll("validIpAddresses") String ipAddress) {
        
        // Mock no existing record initially
        when(mockRepository.findByIdentifierAndLimitType(anyString(), any(RateLimitType.class)))
            .thenReturn(Optional.empty());
        
        when(mockRepository.save(any(RateLimitTracking.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
        
        int maxRequests = rateLimitProperties.getIp().getRequestsPerMinute();
        
        // Test IP rate limiting
        for (int i = 1; i <= maxRequests; i++) {
            RateLimitResult result = rateLimitingService.checkIpRateLimit(ipAddress);
            assertThat(result.isAllowed()).isTrue();
        }
        
        // Exceed limit
        RateLimitResult exceededResult = rateLimitingService.checkIpRateLimit(ipAddress);
        assertThat(exceededResult.isAllowed()).isFalse();
        assertThat(exceededResult.getRetryAfterSeconds()).isPositive();
    }

    @Property(tries = 100)
    @Label("For any user, rate limiting should respect user-specific limits")
    void userRateLimitingShouldRespectUserLimits(@ForAll("validUsers") User user) {
        
        // Mock no existing record initially
        when(mockRepository.findByIdentifierAndLimitType(anyString(), any(RateLimitType.class)))
            .thenReturn(Optional.empty());
        
        when(mockRepository.save(any(RateLimitTracking.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
        
        int maxRequests = rateLimitProperties.getUser().getApiRequestsPerMinute();
        
        // Test user rate limiting
        for (int i = 1; i <= maxRequests; i++) {
            RateLimitResult result = rateLimitingService.checkUserRateLimit(user, "api");
            assertThat(result.isAllowed()).isTrue();
        }
        
        // Exceed limit
        RateLimitResult exceededResult = rateLimitingService.checkUserRateLimit(user, "api");
        assertThat(exceededResult.isAllowed()).isFalse();
    }

    @Property(tries = 50)
    @Label("For any blocked identifier, all requests should be rejected until unblocked")
    void blockedIdentifiersShouldRejectAllRequests(
            @ForAll("validIdentifiers") String identifier,
            @ForAll("rateLimitTypes") RateLimitType type) {
        
        // Create a blocked tracking record
        RateLimitTracking blockedRecord = RateLimitTracking.builder()
            .identifier(identifier)
            .limitType(type)
            .windowStart(LocalDateTime.now())
            .requestCount(0)
            .maxRequests(getMaxRequestsForType(type))
            .windowDurationSeconds(60)
            .blocked(true)
            .blockExpiresAt(LocalDateTime.now().plusMinutes(5))
            .violationCount(3)
            .lastUpdated(LocalDateTime.now())
            .build();
        
        when(mockRepository.findByIdentifierAndLimitType(identifier, type))
            .thenReturn(Optional.of(blockedRecord));
        
        // All requests should be blocked
        for (int i = 0; i < 5; i++) {
            RateLimitResult result = rateLimitingService.checkRateLimit(identifier, type);
            
            assertThat(result.isAllowed()).isFalse();
            assertThat(result.isBlocked()).isTrue();
            assertThat(result.getBlockExpiresAt()).isNotNull();
            assertThat(result.getRetryAfterSeconds()).isPositive();
            assertThat(result.getReason()).containsIgnoringCase("blocked");
        }
    }

    @Property(tries = 50)
    @Label("For any rate limit window reset, request counts should start fresh")
    void windowResetShouldStartCountsFresh(
            @ForAll("validIdentifiers") String identifier,
            @ForAll("rateLimitTypes") RateLimitType type) {
        
        // Create an expired window tracking record
        RateLimitTracking expiredRecord = RateLimitTracking.builder()
            .identifier(identifier)
            .limitType(type)
            .windowStart(LocalDateTime.now().minusMinutes(2)) // Expired window
            .requestCount(getMaxRequestsForType(type)) // At limit
            .maxRequests(getMaxRequestsForType(type))
            .windowDurationSeconds(60)
            .blocked(false)
            .violationCount(0)
            .lastUpdated(LocalDateTime.now().minusMinutes(2))
            .build();
        
        when(mockRepository.findByIdentifierAndLimitType(identifier, type))
            .thenReturn(Optional.of(expiredRecord));
        
        when(mockRepository.save(any(RateLimitTracking.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
        
        // First request after window reset should be allowed
        RateLimitResult result = rateLimitingService.checkRateLimit(identifier, type);
        
        assertThat(result.isAllowed()).isTrue();
        assertThat(result.getCurrentCount()).isEqualTo(1); // Reset to 1
        assertThat(result.getRemainingRequests()).isEqualTo(getMaxRequestsForType(type) - 1);
    }

    /**
     * Helper method to get max requests for a rate limit type
     */
    private int getMaxRequestsForType(RateLimitType type) {
        switch (type) {
            case IP:
                return rateLimitProperties.getIp().getRequestsPerMinute();
            case USER:
                return rateLimitProperties.getUser().getApiRequestsPerMinute();
            case GLOBAL:
                return rateLimitProperties.getGlobal().getRequestsPerSecond();
            case ENDPOINT:
                return rateLimitProperties.getEndpoint().getRequestsPerMinute();
            case PLAN:
                return rateLimitProperties.getPlan().getBasicRequestsPerHour();
            default:
                return 100; // Default fallback
        }
    }

    /**
     * Generator for valid identifiers
     */
    @Provide
    Arbitrary<String> validIdentifiers() {
        return Arbitraries.oneOf(
            Arbitraries.strings().alpha().ofMinLength(5).ofMaxLength(20),
            Arbitraries.strings().numeric().ofMinLength(3).ofMaxLength(10),
            Arbitraries.strings().withCharRange('a', 'z').ofMinLength(8).ofMaxLength(15)
        );
    }

    /**
     * Generator for rate limit types
     */
    @Provide
    Arbitrary<RateLimitType> rateLimitTypes() {
        return Arbitraries.of(RateLimitType.values());
    }

    /**
     * Generator for valid IP addresses
     */
    @Provide
    Arbitrary<String> validIpAddresses() {
        return Combinators.combine(
            Arbitraries.integers().between(1, 255),
            Arbitraries.integers().between(0, 255),
            Arbitraries.integers().between(0, 255),
            Arbitraries.integers().between(1, 255)
        ).as((a, b, c, d) -> String.format("%d.%d.%d.%d", a, b, c, d));
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