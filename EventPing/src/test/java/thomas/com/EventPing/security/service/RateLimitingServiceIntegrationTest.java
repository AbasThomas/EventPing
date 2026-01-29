package thomas.com.EventPing.security.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;
import thomas.com.EventPing.User.model.User;
import thomas.com.EventPing.security.model.RateLimitTracking;
import thomas.com.EventPing.security.model.RateLimitType;
import thomas.com.EventPing.security.repository.RateLimitTrackingRepository;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.*;

/**
 * Integration tests for Rate Limiting Service
 * **Validates: Requirements 3.1, 3.2, 3.6**
 */
@SpringBootTest
@TestPropertySource(properties = {
    "eventping.rate-limit.enabled=true",
    "eventping.rate-limit.ip.requests-per-minute=5",
    "eventping.rate-limit.user.api-requests-per-minute=10"
})
class RateLimitingServiceIntegrationTest {

    @Autowired
    private RateLimitingService rateLimitingService;
    
    @Autowired
    private RateLimitTrackingRepository rateLimitRepository;

    @BeforeEach
    void setUp() {
        // Clean up any existing rate limit records
        rateLimitRepository.deleteAll();
    }

    @Test
    void shouldEnforceIpRateLimiting() {
        String ipAddress = "192.168.1.100";
        
        // Make requests up to the limit (5 per minute)
        for (int i = 1; i <= 5; i++) {
            RateLimitResult result = rateLimitingService.checkIpRateLimit(ipAddress);
            
            assertThat(result.isAllowed()).isTrue();
            assertThat(result.getCurrentCount()).isEqualTo(i);
            assertThat(result.getRemainingRequests()).isEqualTo(5 - i);
        }
        
        // The 6th request should be rate limited
        RateLimitResult exceededResult = rateLimitingService.checkIpRateLimit(ipAddress);
        
        assertThat(exceededResult.isAllowed()).isFalse();
        assertThat(exceededResult.getCurrentCount()).isEqualTo(6);
        assertThat(exceededResult.getRemainingRequests()).isZero();
        assertThat(exceededResult.getRetryAfterSeconds()).isPositive();
    }

    @Test
    void shouldEnforceUserRateLimiting() {
        User user = createTestUser(1L, "test@example.com");
        
        // Make requests up to the limit (10 per minute)
        for (int i = 1; i <= 10; i++) {
            RateLimitResult result = rateLimitingService.checkUserRateLimit(user, "api");
            
            assertThat(result.isAllowed()).isTrue();
            assertThat(result.getCurrentCount()).isEqualTo(i);
        }
        
        // The 11th request should be rate limited
        RateLimitResult exceededResult = rateLimitingService.checkUserRateLimit(user, "api");
        
        assertThat(exceededResult.isAllowed()).isFalse();
        assertThat(exceededResult.getCurrentCount()).isEqualTo(11);
        assertThat(exceededResult.getRemainingRequests()).isZero();
    }

    @Test
    void shouldHandleConcurrentRequests() throws InterruptedException {
        String ipAddress = "192.168.1.200";
        int numberOfThreads = 10;
        int requestsPerThread = 2;
        
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        
        // Submit concurrent requests
        CompletableFuture<?>[] futures = IntStream.range(0, numberOfThreads)
            .mapToObj(i -> CompletableFuture.runAsync(() -> {
                for (int j = 0; j < requestsPerThread; j++) {
                    rateLimitingService.checkIpRateLimit(ipAddress);
                }
            }, executor))
            .toArray(CompletableFuture[]::new);
        
        // Wait for all requests to complete and handle any exceptions
        try {
            CompletableFuture.allOf(futures).join();
        } catch (Exception e) {
            // Some requests might fail during concurrency test due to race conditions on creation
            // which is expected if not using UPSERT. But we want to see at least some success.
        }
        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);
        
        // Clear persistence context to ensure we see database changes
        rateLimitRepository.flush();
        RateLimitTracking tracking = rateLimitRepository
            .findByIdentifierAndLimitType("ip:" + ipAddress, RateLimitType.IP)
            .orElse(null);
        
        assertThat(tracking).isNotNull();
        assertThat(tracking.getRequestCount()).isEqualTo(numberOfThreads * requestsPerThread);
        
        // Since we made 20 requests and limit is 5, it should be over the limit
        assertThat(tracking.getRequestCount()).isGreaterThan(5);
    }

    @Test
    void shouldBlockSuspiciousIpTemporarily() {
        String suspiciousIp = "192.168.1.300";
        Duration blockDuration = Duration.ofMinutes(5);
        
        // Block the IP
        rateLimitingService.blockIpTemporarily(suspiciousIp, blockDuration);
        
        // Verify the IP is blocked
        assertThat(rateLimitingService.isBlocked("ip:" + suspiciousIp, RateLimitType.IP)).isTrue();
        
        // Try to make a request - should be blocked
        RateLimitResult result = rateLimitingService.checkIpRateLimit(suspiciousIp);
        
        assertThat(result.isAllowed()).isFalse();
        assertThat(result.isBlocked()).isTrue();
        assertThat(result.getBlockExpiresAt()).isNotNull();
        assertThat(result.getRetryAfterSeconds()).isPositive();
    }

    @Test
    void shouldUnblockIdentifier() {
        String identifier = "test-identifier";
        RateLimitType type = RateLimitType.USER;
        
        // Create a blocked record
        RateLimitTracking blockedRecord = RateLimitTracking.builder()
            .identifier(identifier)
            .limitType(type)
            .windowStart(LocalDateTime.now())
            .requestCount(0)
            .maxRequests(10)
            .windowDurationSeconds(60)
            .blocked(true)
            .blockExpiresAt(LocalDateTime.now().plusMinutes(10))
            .violationCount(3)
            .lastUpdated(LocalDateTime.now())
            .build();
        
        rateLimitRepository.save(blockedRecord);
        
        // Verify it's blocked
        assertThat(rateLimitingService.isBlocked(identifier, type)).isTrue();
        
        // Unblock it
        rateLimitingService.unblock(identifier, type);
        
        // Verify it's no longer blocked
        assertThat(rateLimitingService.isBlocked(identifier, type)).isFalse();
        
        // Verify the record was updated
        RateLimitTracking updatedRecord = rateLimitRepository
            .findByIdentifierAndLimitType(identifier, type)
            .orElse(null);
        
        assertThat(updatedRecord).isNotNull();
        assertThat(updatedRecord.getBlocked()).isFalse();
        assertThat(updatedRecord.getBlockExpiresAt()).isNull();
        assertThat(updatedRecord.getViolationCount()).isZero();
    }

    @Test
    void shouldResetWindowAfterExpiration() {
        String identifier = "test-reset";
        RateLimitType type = RateLimitType.IP;
        
        // Create an expired window record
        RateLimitTracking expiredRecord = RateLimitTracking.builder()
            .identifier(identifier)
            .limitType(type)
            .windowStart(LocalDateTime.now().minusMinutes(2)) // Expired
            .requestCount(5) // At limit
            .maxRequests(5)
            .windowDurationSeconds(60)
            .blocked(false)
            .violationCount(0)
            .lastUpdated(LocalDateTime.now().minusMinutes(2))
            .build();
        
        rateLimitRepository.save(expiredRecord);
        
        LocalDateTime oldWindowStart = expiredRecord.getWindowStart();
        
        // Make a request - should reset the window and be allowed
        RateLimitResult result = rateLimitingService.checkRateLimit(identifier, type);
        
        assertThat(result.isAllowed()).isTrue();
        assertThat(result.getCurrentCount()).isEqualTo(1); // Reset to 1
        assertThat(result.getRemainingRequests()).isEqualTo(4);
        
        // Flush and clear to ensure we get a fresh copy from DB
        rateLimitRepository.flush();
        
        // Verify the record was updated
        RateLimitTracking updatedRecord = rateLimitRepository
            .findByIdentifierAndLimitType(identifier, type)
            .orElse(null);
        
        assertThat(updatedRecord).isNotNull();
        assertThat(updatedRecord.getRequestCount()).isEqualTo(1);
        assertThat(updatedRecord.getWindowStart()).isAfter(oldWindowStart);
    }

    @Test
    void shouldEnforcePlanBasedQuotas() {
        User basicUser = createTestUser(1L, "basic@example.com");
        User premiumUser = createTestUser(2L, "premium@example.com");
        
        // Test basic user limits (should use default basic plan)
        for (int i = 1; i <= 10; i++) {
            RateLimitResult result = rateLimitingService.checkUserRateLimit(basicUser, "api");
            assertThat(result.isAllowed()).isTrue();
        }
        
        // 11th request should be rate limited for basic user
        RateLimitResult basicExceeded = rateLimitingService.checkUserRateLimit(basicUser, "api");
        assertThat(basicExceeded.isAllowed()).isFalse();
        
        // Premium user should have different limits (this would require plan detection logic)
        // For now, just verify the service handles different users separately
        RateLimitResult premiumResult = rateLimitingService.checkUserRateLimit(premiumUser, "api");
        assertThat(premiumResult.isAllowed()).isTrue();
    }

    @Test
    void shouldCleanupExpiredRecords() {
        // Create some old records
        LocalDateTime oldTime = LocalDateTime.now().minusDays(8);
        
        RateLimitTracking oldRecord1 = RateLimitTracking.builder()
            .identifier("old-1")
            .limitType(RateLimitType.IP)
            .windowStart(oldTime)
            .requestCount(1)
            .maxRequests(10)
            .windowDurationSeconds(60)
            .blocked(false)
            .violationCount(0)
            .lastUpdated(oldTime)
            .build();
        
        RateLimitTracking oldRecord2 = RateLimitTracking.builder()
            .identifier("old-2")
            .limitType(RateLimitType.USER)
            .windowStart(oldTime)
            .requestCount(1)
            .maxRequests(10)
            .windowDurationSeconds(60)
            .blocked(false)
            .violationCount(0)
            .lastUpdated(oldTime)
            .build();
        
        // Create a recent record that should not be deleted
        RateLimitTracking recentRecord = RateLimitTracking.builder()
            .identifier("recent")
            .limitType(RateLimitType.IP)
            .windowStart(LocalDateTime.now())
            .requestCount(1)
            .maxRequests(10)
            .windowDurationSeconds(60)
            .blocked(false)
            .violationCount(0)
            .lastUpdated(LocalDateTime.now())
            .build();
        
        rateLimitRepository.save(oldRecord1);
        rateLimitRepository.save(oldRecord2);
        rateLimitRepository.save(recentRecord);
        
        // Verify all records exist
        assertThat(rateLimitRepository.count()).isEqualTo(3);
        
        // Run cleanup
        rateLimitingService.cleanupExpiredRecords();
        
        // Verify old records were deleted but recent record remains
        assertThat(rateLimitRepository.count()).isEqualTo(1);
        assertThat(rateLimitRepository.findByIdentifierAndLimitType("recent", RateLimitType.IP))
            .isPresent();
    }

    private User createTestUser(Long id, String email) {
        User user = new User();
        user.setId(id);
        user.setEmail(email);
        user.setFullName("Test User");
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        return user;
    }
}