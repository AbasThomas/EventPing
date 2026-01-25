package thomas.com.EventPing.security.exception;

import lombok.Getter;

/**
 * Exception thrown when rate limit is exceeded.
 * Contains retry information without exposing internal rate limiting details.
 */
@Getter
public class RateLimitExceededException extends RuntimeException {
    private final long retryAfterSeconds;

    public RateLimitExceededException(String message, long retryAfterSeconds) {
        super(message);
        this.retryAfterSeconds = retryAfterSeconds;
    }

    public RateLimitExceededException(String message) {
        super(message);
        this.retryAfterSeconds = 60; // Default retry after 1 minute
    }
}