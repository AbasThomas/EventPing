package thomas.com.EventPing.security.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import thomas.com.EventPing.User.model.User;
import thomas.com.EventPing.config.RateLimitProperties;
import thomas.com.EventPing.security.model.RateLimitTracking;
import thomas.com.EventPing.security.model.RateLimitType;
import thomas.com.EventPing.security.repository.RateLimitTrackingRepository;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Service for handling rate limiting logic
 * **Validates: Requirements 3.1, 3.2, 3.3, 3.4, 3.6**
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RateLimitingService {
    
    private final RateLimitTrackingRepository rateLimitRepository;
    private final RateLimitProperties rateLimitProperties;
    private final AuditLoggingService auditLoggingService;
    
    /**
     * Check if request is within rate limits
     * **Validates: Requirements 3.1, 3.2**
     */
    @Transactional
    public RateLimitResult checkRateLimit(String identifier, RateLimitType type) {
        log.debug("Checking rate limit for identifier: {} with type: {}", identifier, type);
        
        Optional<RateLimitTracking> existingRecord = rateLimitRepository
            .findByIdentifierAndLimitType(identifier, type);
        
        RateLimitTracking tracking;
        if (existingRecord.isPresent()) {
            tracking = existingRecord.get();
            
            // Check if currently blocked
            if (tracking.isCurrentlyBlocked()) {
                log.warn("Identifier {} is currently blocked until {}", identifier, tracking.getBlockExpiresAt());
                return RateLimitResult.blocked(tracking.getBlockExpiresAt(), 
                    "Identifier temporarily blocked due to rate limit violations");
            }
            
            // Check if window needs reset
            if (!tracking.isWindowValid()) {
                tracking.resetWindow();
                log.debug("Reset rate limit window for identifier: {}", identifier);
            }
        } else {
            // Create new tracking record
            tracking = createNewTrackingRecord(identifier, type);
            log.debug("Created new rate limit tracking for identifier: {}", identifier);
        }
        
        // Increment request count
        tracking.incrementRequestCount();
        
        // Check if limit exceeded
        if (tracking.isLimitExceeded()) {
            tracking.setViolationCount(tracking.getViolationCount() + 1);
            
            // Log rate limit exceeded
            auditLoggingService.logRateLimitExceeded(
                    extractUsernameFromIdentifier(identifier),
                    extractIpFromIdentifier(identifier),
                    type.toString()
            );
            
            // Apply progressive blocking for repeated violations
            if (tracking.getViolationCount() >= getBlockThreshold(type)) {
                Duration blockDuration = calculateBlockDuration(tracking.getViolationCount());
                tracking.setBlocked(true);
                tracking.setBlockExpiresAt(LocalDateTime.now().plus(blockDuration));
                
                log.warn("Blocking identifier {} for {} due to {} violations", 
                    identifier, blockDuration, tracking.getViolationCount());
                
                // Log security violation for blocking
                auditLoggingService.logSecurityViolation(
                        extractUsernameFromIdentifier(identifier),
                        extractIpFromIdentifier(identifier),
                        "RATE_LIMIT_BLOCK",
                        String.format("Identifier blocked for %s due to %d violations", 
                                blockDuration, tracking.getViolationCount()),
                        thomas.com.EventPing.security.entity.AuditEvent.AuditSeverity.HIGH
                );
            }
            
            rateLimitRepository.save(tracking);
            
            LocalDateTime windowEnd = tracking.getWindowStart().plusSeconds(tracking.getWindowDurationSeconds());
            return RateLimitResult.rateLimited(
                tracking.getRequestCount(),
                tracking.getMaxRequests(),
                windowEnd,
                "Rate limit exceeded"
            );
        }
        
        // Save updated tracking
        rateLimitRepository.save(tracking);
        
        LocalDateTime windowEnd = tracking.getWindowStart().plusSeconds(tracking.getWindowDurationSeconds());
        return RateLimitResult.allowed(
            tracking.getRequestCount(),
            tracking.getMaxRequests(),
            windowEnd
        );
    }
    
    /**
     * Apply rate limit based on user plan
     * **Validates: Requirements 3.6**
     */
    @Transactional
    public RateLimitResult checkUserRateLimit(User user, String operation) {
        if (user == null) {
            return checkRateLimit("anonymous", RateLimitType.USER);
        }
        
        String identifier = "user:" + user.getId();
        String planType = determinePlanType(user);
        
        // Check user-specific rate limit
        RateLimitResult result = checkRateLimit(identifier, RateLimitType.USER);
        
        // If user limit is exceeded, also check plan-specific limits
        if (!result.isAllowed()) {
            String planIdentifier = "plan:" + planType + ":" + operation;
            RateLimitResult planResult = checkRateLimit(planIdentifier, RateLimitType.PLAN);
            
            // Return the more restrictive result
            if (planResult.getRetryAfterSeconds() > result.getRetryAfterSeconds()) {
                return planResult;
            }
        }
        
        return result;
    }
    
    /**
     * Apply IP-based rate limiting
     * **Validates: Requirements 3.1, 3.2**
     */
    @Transactional
    public RateLimitResult checkIpRateLimit(String ipAddress) {
        if (ipAddress == null || ipAddress.trim().isEmpty()) {
            ipAddress = "unknown";
        }
        
        return checkRateLimit("ip:" + ipAddress, RateLimitType.IP);
    }
    
    /**
     * Block suspicious IPs temporarily
     * **Validates: Requirements 3.2, 3.3**
     */
    @Transactional
    public void blockIpTemporarily(String ipAddress, Duration duration) {
        log.warn("Temporarily blocking IP {} for {}", ipAddress, duration);
        
        String identifier = "ip:" + ipAddress;
        Optional<RateLimitTracking> existingRecord = rateLimitRepository
            .findByIdentifierAndLimitType(identifier, RateLimitType.IP);
        
        RateLimitTracking tracking;
        if (existingRecord.isPresent()) {
            tracking = existingRecord.get();
        } else {
            tracking = createNewTrackingRecord(identifier, RateLimitType.IP);
        }
        
        tracking.setBlocked(true);
        tracking.setBlockExpiresAt(LocalDateTime.now().plus(duration));
        tracking.setViolationCount(tracking.getViolationCount() + 1);
        tracking.setMetadata("Manually blocked due to suspicious activity");
        
        rateLimitRepository.save(tracking);
        
        // Log security violation for manual IP blocking
        auditLoggingService.logSecurityViolation(
                null,
                ipAddress,
                "MANUAL_IP_BLOCK",
                String.format("IP manually blocked for %s due to suspicious activity", duration),
                thomas.com.EventPing.security.entity.AuditEvent.AuditSeverity.HIGH
        );
    }
    
    /**
     * Check if an identifier is currently blocked
     */
    public boolean isBlocked(String identifier, RateLimitType type) {
        Optional<RateLimitTracking> record = rateLimitRepository
            .findByIdentifierAndLimitType(identifier, type);
        
        return record.map(RateLimitTracking::isCurrentlyBlocked).orElse(false);
    }
    
    /**
     * Unblock an identifier
     */
    @Transactional
    public void unblock(String identifier, RateLimitType type) {
        Optional<RateLimitTracking> record = rateLimitRepository
            .findByIdentifierAndLimitType(identifier, type);
        
        if (record.isPresent()) {
            RateLimitTracking tracking = record.get();
            tracking.setBlocked(false);
            tracking.setBlockExpiresAt(null);
            tracking.setViolationCount(0);
            rateLimitRepository.save(tracking);
            
            log.info("Unblocked identifier: {} with type: {}", identifier, type);
        }
    }
    
    /**
     * Clean up expired rate limit records
     */
    @Transactional
    public void cleanupExpiredRecords() {
        LocalDateTime cutoffTime = LocalDateTime.now().minusDays(7); // Keep records for 7 days
        int deletedCount = rateLimitRepository.deleteOldRecords(cutoffTime);
        
        if (deletedCount > 0) {
            log.info("Cleaned up {} expired rate limit records", deletedCount);
        }
    }
    
    /**
     * Create a new tracking record with appropriate limits
     */
    private RateLimitTracking createNewTrackingRecord(String identifier, RateLimitType type) {
        RateLimitConfig config = getRateLimitConfig(type);
        
        return RateLimitTracking.builder()
            .identifier(identifier)
            .limitType(type)
            .windowStart(LocalDateTime.now())
            .requestCount(0)
            .maxRequests(config.maxRequests)
            .windowDurationSeconds(config.windowDurationSeconds)
            .blocked(false)
            .violationCount(0)
            .lastUpdated(LocalDateTime.now())
            .build();
    }
    
    /**
     * Get rate limit configuration for a specific type
     */
    private RateLimitConfig getRateLimitConfig(RateLimitType type) {
        switch (type) {
            case IP:
                return new RateLimitConfig(
                    rateLimitProperties.getIp().getRequestsPerMinute(),
                    60
                );
            case USER:
                return new RateLimitConfig(
                    rateLimitProperties.getUser().getApiRequestsPerMinute(),
                    60
                );
            case GLOBAL:
                return new RateLimitConfig(
                    rateLimitProperties.getGlobal().getRequestsPerSecond(),
                    1
                );
            case ENDPOINT:
                return new RateLimitConfig(
                    rateLimitProperties.getEndpoint().getRequestsPerMinute(),
                    60
                );
            case PLAN:
                return new RateLimitConfig(
                    rateLimitProperties.getPlan().getBasicRequestsPerHour(),
                    3600
                );
            default:
                return new RateLimitConfig(100, 60); // Default fallback
        }
    }
    
    /**
     * Determine user's plan type
     */
    private String determinePlanType(User user) {
        // This would typically check user's subscription/plan
        // For now, return a default plan
        return "basic";
    }
    
    /**
     * Get the violation threshold before blocking
     */
    private int getBlockThreshold(RateLimitType type) {
        switch (type) {
            case IP:
                return 3; // Block after 3 violations
            case USER:
                return 5; // More lenient for authenticated users
            default:
                return 3;
        }
    }
    
    /**
     * Calculate block duration based on violation count
     */
    private Duration calculateBlockDuration(int violationCount) {
        // Progressive blocking: 1 min, 5 min, 15 min, 1 hour, 24 hours
        switch (violationCount) {
            case 1:
                return Duration.ofMinutes(1);
            case 2:
                return Duration.ofMinutes(5);
            case 3:
                return Duration.ofMinutes(15);
            case 4:
                return Duration.ofHours(1);
            default:
                return Duration.ofHours(24);
        }
    }
    
    /**
     * Extract username from identifier for audit logging
     */
    private String extractUsernameFromIdentifier(String identifier) {
        if (identifier.startsWith("user:")) {
            // For user identifiers, we have the user ID, not username
            // In a real implementation, you might want to look up the username
            return "user_id_" + identifier.substring(5);
        }
        return null;
    }
    
    /**
     * Extract IP address from identifier for audit logging
     */
    private String extractIpFromIdentifier(String identifier) {
        if (identifier.startsWith("ip:")) {
            return identifier.substring(3);
        }
        return null;
    }
    
    /**
     * Internal configuration class for rate limits
     */
    private static class RateLimitConfig {
        final int maxRequests;
        final int windowDurationSeconds;
        
        RateLimitConfig(int maxRequests, int windowDurationSeconds) {
            this.maxRequests = maxRequests;
            this.windowDurationSeconds = windowDurationSeconds;
        }
    }
}