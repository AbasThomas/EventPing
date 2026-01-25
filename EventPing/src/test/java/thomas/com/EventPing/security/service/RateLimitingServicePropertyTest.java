package thomas.com.EventPing.security.service;

import net.jqwik.api.*;
import net.jqwik.api.lifecycle.BeforeProperty;
import thomas.com.EventPing.User.model.User;
import thomas.com.EventPing.config.RateLimitProperties;
import thomas.com.EventPing.security.model.RateLimitTracking;
import thomas.com.EventPing.security.model.RateLimitType;
import thomas.com.EventPing.security.repository.RateLimitTrackingRepository;
import thomas.com.EventPing.security.service.AuditLoggingService;
import org.mockito.Mockito;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * Property-based tests for Rate Limiting Service
 * **Feature: eventping-security-hardening, Property 4: Rate Limiting Enforcement**
 * **Validates: Requirements 3.1, 3.2**
 */
class RateLimitingServicePropertyTest {

    private RateLimitingService rateLimitingService;
    private InMemoryRateLimitRepository inMemoryRepository;
    private RateLimitProperties rateLimitProperties;

    @BeforeProperty
    void setUp() {
        // Set up rate limit properties for testing
        rateLimitProperties = new RateLimitProperties();
        
        // Configure IP limits
        RateLimitProperties.Ip ipConfig = new RateLimitProperties.Ip();
        ipConfig.setRequestsPerMinute(5); // Lower limit for faster testing
        ipConfig.setRequestsPerHour(100);
        rateLimitProperties.setIp(ipConfig);
        
        // Configure User limits
        RateLimitProperties.User userConfig = new RateLimitProperties.User();
        userConfig.setApiRequestsPerMinute(10); // Lower limit for faster testing
        userConfig.setApiRequestsPerHour(200);
        rateLimitProperties.setUser(userConfig);
        
        // Configure Global limits
        RateLimitProperties.Global globalConfig = new RateLimitProperties.Global();
        globalConfig.setRequestsPerSecond(3); // Lower limit for faster testing
        rateLimitProperties.setGlobal(globalConfig);
        
        // Configure Endpoint limits
        RateLimitProperties.Endpoint endpointConfig = new RateLimitProperties.Endpoint();
        endpointConfig.setRequestsPerMinute(8);
        rateLimitProperties.setEndpoint(endpointConfig);
        
        // Configure Plan limits
        RateLimitProperties.Plan planConfig = new RateLimitProperties.Plan();
        planConfig.setBasicRequestsPerHour(50);
        rateLimitProperties.setPlan(planConfig);

        // Create a fresh in-memory repository for each test iteration
        inMemoryRepository = new InMemoryRateLimitRepository();
        
        // Create service with fresh in-memory repository
        rateLimitingService = new RateLimitingService(inMemoryRepository, rateLimitProperties);
    }

    /**
     * Property 4: Rate Limiting Enforcement
     * For any sequence of API requests from a single source, when the requests exceed 
     * the defined rate limits, the system should return HTTP 429 and block further 
     * requests until the rate limit window resets
     */
    @Property(tries = 50)
    @Label("For any identifier and rate limit type, exceeding limits should result in rate limiting")
    void exceedingRateLimitsShouldResultInBlocking(
            @ForAll("validIdentifiers") String identifier,
            @ForAll("rateLimitTypes") RateLimitType type) {
        
        // Create fresh service for this test iteration
        InMemoryRateLimitRepository freshRepository = new InMemoryRateLimitRepository();
        RateLimitingService freshService = new RateLimitingService(freshRepository, rateLimitProperties, auditLoggingService);
        
        int maxRequests = getMaxRequestsForType(type);
        
        // Make requests up to the limit - should all be allowed
        for (int i = 1; i <= maxRequests; i++) {
            RateLimitResult result = freshService.checkRateLimit(identifier, type);
            
            assertThat(result.isAllowed())
                .as("Request %d should be allowed (limit: %d)", i, maxRequests)
                .isTrue();
            assertThat(result.getCurrentCount()).isEqualTo(i);
            assertThat(result.getMaxRequests()).isEqualTo(maxRequests);
            assertThat(result.getRemainingRequests()).isEqualTo(maxRequests - i);
        }
        
        // The next request should exceed the limit and be blocked
        RateLimitResult exceededResult = freshService.checkRateLimit(identifier, type);
        
        assertThat(exceededResult.isAllowed())
            .as("Request exceeding limit should be blocked")
            .isFalse();
        assertThat(exceededResult.getCurrentCount()).isGreaterThan(maxRequests);
        assertThat(exceededResult.getRemainingRequests()).isZero();
        assertThat(exceededResult.getRetryAfterSeconds()).isPositive();
        assertThat(exceededResult.getReason()).containsIgnoringCase("rate limit exceeded");
    }

    @Property(tries = 50)
    @Label("For any IP address, rate limiting should be enforced consistently")
    void ipRateLimitingShouldBeEnforcedConsistently(@ForAll("validIpAddresses") String ipAddress) {
        
        // Create fresh service for this test iteration
        InMemoryRateLimitRepository freshRepository = new InMemoryRateLimitRepository();
        RateLimitingService freshService = new RateLimitingService(freshRepository, rateLimitProperties);
        
        int maxRequests = rateLimitProperties.getIp().getRequestsPerMinute();
        
        // Test IP rate limiting
        for (int i = 1; i <= maxRequests; i++) {
            RateLimitResult result = freshService.checkIpRateLimit(ipAddress);
            assertThat(result.isAllowed()).isTrue();
        }
        
        // Exceed limit
        RateLimitResult exceededResult = freshService.checkIpRateLimit(ipAddress);
        assertThat(exceededResult.isAllowed()).isFalse();
        assertThat(exceededResult.getRetryAfterSeconds()).isPositive();
    }

    @Property(tries = 50)
    @Label("For any user, rate limiting should respect user-specific limits")
    void userRateLimitingShouldRespectUserLimits(@ForAll("validUsers") User user) {
        
        // Create fresh service for this test iteration
        InMemoryRateLimitRepository freshRepository = new InMemoryRateLimitRepository();
        RateLimitingService freshService = new RateLimitingService(freshRepository, rateLimitProperties, auditLoggingService);
        
        int maxRequests = rateLimitProperties.getUser().getApiRequestsPerMinute();
        
        // Test user rate limiting
        for (int i = 1; i <= maxRequests; i++) {
            RateLimitResult result = freshService.checkUserRateLimit(user, "api");
            assertThat(result.isAllowed()).isTrue();
        }
        
        // Exceed limit
        RateLimitResult exceededResult = freshService.checkUserRateLimit(user, "api");
        assertThat(exceededResult.isAllowed()).isFalse();
    }

    @Property(tries = 30)
    @Label("For any blocked identifier, all requests should be rejected until unblocked")
    void blockedIdentifiersShouldRejectAllRequests(
            @ForAll("validIdentifiers") String identifier,
            @ForAll("rateLimitTypes") RateLimitType type) {
        
        // Create fresh service for this test iteration
        InMemoryRateLimitRepository freshRepository = new InMemoryRateLimitRepository();
        RateLimitingService freshService = new RateLimitingService(freshRepository, rateLimitProperties);
        
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
        
        freshRepository.save(blockedRecord);
        
        // All requests should be blocked
        for (int i = 0; i < 3; i++) {
            RateLimitResult result = freshService.checkRateLimit(identifier, type);
            
            assertThat(result.isAllowed()).isFalse();
            assertThat(result.isBlocked()).isTrue();
            assertThat(result.getBlockExpiresAt()).isNotNull();
            assertThat(result.getRetryAfterSeconds()).isPositive();
            assertThat(result.getReason()).containsIgnoringCase("blocked");
        }
    }

    @Property(tries = 30)
    @Label("For any rate limit window reset, request counts should start fresh")
    void windowResetShouldStartCountsFresh(
            @ForAll("validIdentifiers") String identifier,
            @ForAll("rateLimitTypes") RateLimitType type) {
        
        // Create fresh service for this test iteration
        InMemoryRateLimitRepository freshRepository = new InMemoryRateLimitRepository();
        RateLimitingService freshService = new RateLimitingService(freshRepository, rateLimitProperties, auditLoggingService);
        
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
        
        freshRepository.save(expiredRecord);
        
        // First request after window reset should be allowed
        RateLimitResult result = freshService.checkRateLimit(identifier, type);
        
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
                return 5; // Default fallback for testing
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

    /**
     * In-memory implementation of RateLimitTrackingRepository for testing
     */
    private static class InMemoryRateLimitRepository implements RateLimitTrackingRepository {
        private final Map<String, RateLimitTracking> storage = new HashMap<>();
        private Long nextId = 1L;

        @Override
        public Optional<RateLimitTracking> findByIdentifierAndLimitType(String identifier, RateLimitType limitType) {
            String key = identifier + ":" + limitType;
            return Optional.ofNullable(storage.get(key));
        }

        @Override
        public <S extends RateLimitTracking> S save(S entity) {
            if (entity.getId() == null) {
                entity.setId(nextId++);
            }
            String key = entity.getIdentifier() + ":" + entity.getLimitType();
            storage.put(key, entity);
            return entity;
        }

        // Implement other required methods with minimal functionality for testing
        @Override
        public java.util.List<RateLimitTracking> findBlockedByType(RateLimitType limitType, LocalDateTime now) {
            return storage.values().stream()
                .filter(r -> r.getLimitType() == limitType && r.isCurrentlyBlocked())
                .collect(java.util.stream.Collectors.toList());
        }

        @Override
        public java.util.List<RateLimitTracking> findExpiredWindows(LocalDateTime cutoffTime) {
            return storage.values().stream()
                .filter(r -> r.getWindowStart().isBefore(cutoffTime))
                .collect(java.util.stream.Collectors.toList());
        }

        @Override
        public int deleteOldRecords(LocalDateTime cutoffTime) {
            int count = 0;
            var iterator = storage.entrySet().iterator();
            while (iterator.hasNext()) {
                var entry = iterator.next();
                if (entry.getValue().getLastUpdated().isBefore(cutoffTime) && !entry.getValue().getBlocked()) {
                    iterator.remove();
                    count++;
                }
            }
            return count;
        }

        // Minimal implementations for other methods
        @Override public java.util.List<RateLimitTracking> findByIdentifierPattern(String pattern, RateLimitType limitType) { return java.util.Collections.emptyList(); }
        @Override public long countViolationsSince(RateLimitType limitType, LocalDateTime since) { return 0; }
        @Override public java.util.List<RateLimitTracking> findTopViolators(RateLimitType limitType, int minViolations) { return java.util.Collections.emptyList(); }
        @Override public int updateBlockStatus(String identifier, RateLimitType limitType, boolean blocked, LocalDateTime expiresAt, LocalDateTime now) { return 0; }
        @Override public java.util.List<RateLimitTracking> findByPlanTypeAndLimitType(String planType, RateLimitType limitType) { return java.util.Collections.emptyList(); }
        @Override public void flush() {}
        @Override public <S extends RateLimitTracking> S saveAndFlush(S entity) { return save(entity); }
        @Override public <S extends RateLimitTracking> java.util.List<S> saveAllAndFlush(Iterable<S> entities) { return (java.util.List<S>) saveAll(entities); }
        @Override public void deleteAllInBatch(Iterable<RateLimitTracking> entities) { entities.forEach(e -> deleteById(e.getId())); }
        @Override public void deleteAllByIdInBatch(Iterable<Long> longs) { longs.forEach(this::deleteById); }
        @Override public void deleteAllInBatch() { storage.clear(); }
        @Override public RateLimitTracking getOne(Long aLong) { return findById(aLong).orElse(null); }
        @Override public RateLimitTracking getById(Long aLong) { return findById(aLong).orElse(null); }
        @Override public RateLimitTracking getReferenceById(Long aLong) { return findById(aLong).orElse(null); }
        @Override public <S extends RateLimitTracking> java.util.List<S> findAll(org.springframework.data.domain.Example<S> example) { return java.util.Collections.emptyList(); }
        @Override public <S extends RateLimitTracking> java.util.List<S> findAll(org.springframework.data.domain.Example<S> example, org.springframework.data.domain.Sort sort) { return java.util.Collections.emptyList(); }
        @Override public <S extends RateLimitTracking> java.util.List<S> saveAll(Iterable<S> entities) { entities.forEach(this::save); return (java.util.List<S>) entities; }
        @Override public java.util.List<RateLimitTracking> findAll() { return new java.util.ArrayList<>(storage.values()); }
        @Override public java.util.List<RateLimitTracking> findAllById(Iterable<Long> longs) { return java.util.Collections.emptyList(); }
        @Override public long count() { return storage.size(); }
        @Override public void deleteById(Long aLong) { storage.entrySet().removeIf(e -> e.getValue().getId().equals(aLong)); }
        @Override public void delete(RateLimitTracking entity) { deleteById(entity.getId()); }
        @Override public void deleteAllById(Iterable<? extends Long> longs) { longs.forEach(this::deleteById); }
        @Override public void deleteAll(Iterable<? extends RateLimitTracking> entities) { entities.forEach(this::delete); }
        @Override public void deleteAll() { storage.clear(); }
        @Override public java.util.List<RateLimitTracking> findAll(org.springframework.data.domain.Sort sort) { return findAll(); }
        @Override public org.springframework.data.domain.Page<RateLimitTracking> findAll(org.springframework.data.domain.Pageable pageable) { return null; }
        @Override public <S extends RateLimitTracking> Optional<S> findOne(org.springframework.data.domain.Example<S> example) { return Optional.empty(); }
        @Override public <S extends RateLimitTracking> org.springframework.data.domain.Page<S> findAll(org.springframework.data.domain.Example<S> example, org.springframework.data.domain.Pageable pageable) { return null; }
        @Override public <S extends RateLimitTracking> long count(org.springframework.data.domain.Example<S> example) { return 0; }
        @Override public <S extends RateLimitTracking> boolean exists(org.springframework.data.domain.Example<S> example) { return false; }
        @Override public <S extends RateLimitTracking, R> R findBy(org.springframework.data.domain.Example<S> example, java.util.function.Function<org.springframework.data.repository.query.FluentQuery.FetchableFluentQuery<S>, R> queryFunction) { return null; }
        @Override public Optional<RateLimitTracking> findById(Long aLong) { return storage.values().stream().filter(r -> r.getId().equals(aLong)).findFirst(); }
        @Override public boolean existsById(Long aLong) { return findById(aLong).isPresent(); }
    }
}