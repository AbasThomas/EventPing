package thomas.com.EventPing.common.exception;

import lombok.Getter;

/**
 * Exception thrown when rate limits are exceeded
 * **Validates: Requirements 3.1, 3.5**
 */
@Getter
public class RateLimitExceededException extends RuntimeException {
    
    private final long retryAfterSeconds;
    private final int currentCount;
    private final int maxRequests;
    
    public RateLimitExceededException(String message, long retryAfterSeconds, int currentCount, int maxRequests) {
        super(message);
        this.retryAfterSeconds = retryAfterSeconds;
        this.currentCount = currentCount;
        this.maxRequests = maxRequests;
    }
    
    public RateLimitExceededException(String message, long retryAfterSeconds) {
        this(message, retryAfterSeconds, 0, 0);
    }
    
    public RateLimitExceededException(String message) {
        this(message, 60); // Default 60 seconds retry
    }
}