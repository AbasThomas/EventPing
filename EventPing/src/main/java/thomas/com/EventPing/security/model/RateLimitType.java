package thomas.com.EventPing.security.model;

/**
 * Types of rate limiting
 * **Validates: Requirements 3.1, 3.2**
 */
public enum RateLimitType {
    /**
     * Rate limiting by IP address
     */
    IP,
    
    /**
     * Rate limiting by authenticated user
     */
    USER,
    
    /**
     * Global rate limiting across all requests
     */
    GLOBAL,
    
    /**
     * Rate limiting by API endpoint
     */
    ENDPOINT,
    
    /**
     * Rate limiting by user plan type
     */
    PLAN
}