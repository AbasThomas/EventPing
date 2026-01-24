package thomas.com.EventPing.security.service;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Result of a rate limit check
 * **Validates: Requirements 3.1, 3.2, 3.5**
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RateLimitResult {
    
    /**
     * Whether the request is allowed
     */
    private boolean allowed;
    
    /**
     * Current request count in the window
     */
    private int currentCount;
    
    /**
     * Maximum requests allowed in the window
     */
    private int maxRequests;
    
    /**
     * Remaining requests in the current window
     */
    private int remainingRequests;
    
    /**
     * When the current window resets
     */
    private LocalDateTime windowResetTime;
    
    /**
     * Seconds until the client can retry (for 429 responses)
     */
    private long retryAfterSeconds;
    
    /**
     * Whether the identifier is currently blocked
     */
    private boolean blocked;
    
    /**
     * When the block expires (if blocked)
     */
    private LocalDateTime blockExpiresAt;
    
    /**
     * Reason for rate limiting (for logging/debugging)
     */
    private String reason;
    
    /**
     * Additional metadata
     */
    private String metadata;
    
    /**
     * Create an allowed result
     */
    public static RateLimitResult allowed(int currentCount, int maxRequests, LocalDateTime windowResetTime) {
        return RateLimitResult.builder()
                .allowed(true)
                .currentCount(currentCount)
                .maxRequests(maxRequests)
                .remainingRequests(Math.max(0, maxRequests - currentCount))
                .windowResetTime(windowResetTime)
                .retryAfterSeconds(0)
                .blocked(false)
                .build();
    }
    
    /**
     * Create a rate limited result
     */
    public static RateLimitResult rateLimited(int currentCount, int maxRequests, LocalDateTime windowResetTime, String reason) {
        long retryAfter = java.time.Duration.between(LocalDateTime.now(), windowResetTime).getSeconds();
        return RateLimitResult.builder()
                .allowed(false)
                .currentCount(currentCount)
                .maxRequests(maxRequests)
                .remainingRequests(0)
                .windowResetTime(windowResetTime)
                .retryAfterSeconds(Math.max(1, retryAfter))
                .blocked(false)
                .reason(reason)
                .build();
    }
    
    /**
     * Create a blocked result
     */
    public static RateLimitResult blocked(LocalDateTime blockExpiresAt, String reason) {
        long retryAfter = blockExpiresAt != null ? 
            java.time.Duration.between(LocalDateTime.now(), blockExpiresAt).getSeconds() : 
            3600; // Default 1 hour for permanent blocks
            
        return RateLimitResult.builder()
                .allowed(false)
                .currentCount(0)
                .maxRequests(0)
                .remainingRequests(0)
                .retryAfterSeconds(Math.max(1, retryAfter))
                .blocked(true)
                .blockExpiresAt(blockExpiresAt)
                .reason(reason)
                .build();
    }
}