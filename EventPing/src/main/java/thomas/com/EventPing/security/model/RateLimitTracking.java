package thomas.com.EventPing.security.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entity for tracking rate limiting data
 * **Validates: Requirements 3.1, 3.2, 3.4**
 */
@Entity
@Table(name = "rate_limit_tracking", indexes = {
    @Index(name = "idx_rate_limit_identifier", columnList = "identifier"),
    @Index(name = "idx_rate_limit_type", columnList = "limitType"),
    @Index(name = "idx_rate_limit_window", columnList = "windowStart"),
    @Index(name = "idx_rate_limit_composite", columnList = "identifier, limitType, windowStart")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RateLimitTracking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Identifier for rate limiting (IP address, user ID, etc.)
     */
    @Column(nullable = false, length = 255)
    private String identifier;

    /**
     * Type of rate limit (IP, USER, GLOBAL, etc.)
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private RateLimitType limitType;

    /**
     * Start of the current time window
     */
    @Column(nullable = false)
    private LocalDateTime windowStart;

    /**
     * Number of requests in the current window
     */
    @Column(nullable = false)
    private Integer requestCount;

    /**
     * Maximum requests allowed in this window
     */
    @Column(nullable = false)
    private Integer maxRequests;

    /**
     * Duration of the time window in seconds
     */
    @Column(nullable = false)
    private Integer windowDurationSeconds;

    /**
     * Whether this identifier is currently blocked
     */
    @Column(nullable = false)
    private Boolean blocked;

    /**
     * When the block expires (if blocked)
     */
    private LocalDateTime blockExpiresAt;

    /**
     * Number of consecutive violations
     */
    @Column(nullable = false)
    private Integer violationCount;

    /**
     * Last time this record was updated
     */
    @Column(nullable = false)
    private LocalDateTime lastUpdated;

    /**
     * Additional context or metadata
     */
    @Column(length = 1000)
    private String metadata;

    /**
     * User plan type for plan-specific quotas
     */
    @Column(length = 50)
    private String planType;

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        this.lastUpdated = LocalDateTime.now();
    }

    /**
     * Check if the current time window is still valid
     */
    public boolean isWindowValid() {
        if (windowStart == null) {
            return false;
        }
        LocalDateTime windowEnd = windowStart.plusSeconds(windowDurationSeconds);
        return LocalDateTime.now().isBefore(windowEnd);
    }

    /**
     * Check if the identifier is currently blocked
     */
    public boolean isCurrentlyBlocked() {
        if (!blocked) {
            return false;
        }
        if (blockExpiresAt == null) {
            return true; // Permanent block
        }
        return LocalDateTime.now().isBefore(blockExpiresAt);
    }

    /**
     * Reset the rate limit window
     */
    public void resetWindow() {
        this.windowStart = LocalDateTime.now();
        this.requestCount = 0;
        this.blocked = false;
        this.blockExpiresAt = null;
    }

    /**
     * Increment the request count
     */
    public void incrementRequestCount() {
        this.requestCount = (this.requestCount == null) ? 1 : this.requestCount + 1;
    }

    /**
     * Check if the rate limit is exceeded
     */
    public boolean isLimitExceeded() {
        return requestCount != null && maxRequests != null && requestCount > maxRequests;
    }
}