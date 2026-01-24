package thomas.com.EventPing.security.annotation;

import thomas.com.EventPing.security.model.RateLimitType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for applying rate limiting to controller methods
 * **Validates: Requirements 3.1, 3.6**
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {
    
    /**
     * Type of rate limiting to apply
     */
    RateLimitType type() default RateLimitType.USER;
    
    /**
     * Maximum number of requests allowed
     */
    int limit() default 60;
    
    /**
     * Time window for the rate limit (in seconds)
     */
    int window() default 60;
    
    /**
     * Operation name for plan-based rate limiting
     */
    String operation() default "";
    
    /**
     * Whether to apply rate limiting per IP address
     */
    boolean perIp() default false;
    
    /**
     * Whether to apply rate limiting per authenticated user
     */
    boolean perUser() default true;
    
    /**
     * Custom identifier for rate limiting (SpEL expression)
     */
    String identifier() default "";
    
    /**
     * Message to return when rate limit is exceeded
     */
    String message() default "Rate limit exceeded. Please try again later.";
    
    /**
     * Whether to skip rate limiting for admin users
     */
    boolean skipForAdmin() default true;
}