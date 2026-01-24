package thomas.com.EventPing.security.aspect;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import thomas.com.EventPing.User.model.User;
import thomas.com.EventPing.common.exception.RateLimitExceededException;
import thomas.com.EventPing.security.annotation.RateLimit;
import thomas.com.EventPing.security.model.RateLimitType;
import thomas.com.EventPing.security.service.RateLimitResult;
import thomas.com.EventPing.security.service.RateLimitingService;

import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;

/**
 * Aspect for handling @RateLimit annotation on controller methods
 * **Validates: Requirements 3.1, 3.6**
 */
@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class RateLimitingAspect {
    
    private final RateLimitingService rateLimitingService;
    private final ExpressionParser expressionParser = new SpelExpressionParser();
    
    @Around("@annotation(rateLimit)")
    public Object handleRateLimit(ProceedingJoinPoint joinPoint, RateLimit rateLimit) throws Throwable {
        
        // Check if rate limiting should be skipped
        if (shouldSkipRateLimit(rateLimit)) {
            return joinPoint.proceed();
        }
        
        // Get current request
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            log.warn("No request attributes found, skipping rate limiting");
            return joinPoint.proceed();
        }
        
        HttpServletRequest request = attributes.getRequest();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        try {
            // Apply rate limiting based on annotation configuration
            RateLimitResult result = applyRateLimit(rateLimit, request, authentication, joinPoint);
            
            if (!result.isAllowed()) {
                throw new RateLimitExceededException(
                    rateLimit.message(),
                    result.getRetryAfterSeconds(),
                    result.getCurrentCount(),
                    result.getMaxRequests()
                );
            }
            
            // Add rate limit headers to response
            addRateLimitHeaders(result);
            
            return joinPoint.proceed();
            
        } catch (RateLimitExceededException e) {
            log.warn("Rate limit exceeded for method {}: {}", 
                joinPoint.getSignature().toShortString(), e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error applying rate limit to method {}", 
                joinPoint.getSignature().toShortString(), e);
            // Continue with method execution if rate limiting fails
            return joinPoint.proceed();
        }
    }
    
    /**
     * Apply rate limiting based on annotation configuration
     */
    private RateLimitResult applyRateLimit(RateLimit rateLimit, HttpServletRequest request, 
                                         Authentication authentication, ProceedingJoinPoint joinPoint) {
        
        String identifier = determineIdentifier(rateLimit, request, authentication, joinPoint);
        RateLimitType type = rateLimit.type();
        
        // For user-based rate limiting with operation
        if (type == RateLimitType.USER && !rateLimit.operation().isEmpty()) {
            User user = extractUserFromAuthentication(authentication);
            if (user != null) {
                return rateLimitingService.checkUserRateLimit(user, rateLimit.operation());
            }
        }
        
        // For IP-based rate limiting
        if (rateLimit.perIp()) {
            String clientIp = getClientIpAddress(request);
            RateLimitResult ipResult = rateLimitingService.checkIpRateLimit(clientIp);
            if (!ipResult.isAllowed()) {
                return ipResult;
            }
        }
        
        // Apply general rate limiting
        return rateLimitingService.checkRateLimit(identifier, type);
    }
    
    /**
     * Determine the identifier for rate limiting
     */
    private String determineIdentifier(RateLimit rateLimit, HttpServletRequest request, 
                                     Authentication authentication, ProceedingJoinPoint joinPoint) {
        
        // Use custom identifier if provided (SpEL expression)
        if (!rateLimit.identifier().isEmpty()) {
            return evaluateSpelExpression(rateLimit.identifier(), request, authentication, joinPoint);
        }
        
        // Use IP address if perIp is true
        if (rateLimit.perIp()) {
            return "ip:" + getClientIpAddress(request);
        }
        
        // Use user ID if authenticated and perUser is true
        if (rateLimit.perUser() && authentication != null && authentication.isAuthenticated()) {
            User user = extractUserFromAuthentication(authentication);
            if (user != null) {
                return "user:" + user.getId();
            }
        }
        
        // Default to IP address
        return "ip:" + getClientIpAddress(request);
    }
    
    /**
     * Evaluate SpEL expression for custom identifier
     */
    private String evaluateSpelExpression(String expression, HttpServletRequest request, 
                                        Authentication authentication, ProceedingJoinPoint joinPoint) {
        try {
            Expression expr = expressionParser.parseExpression(expression);
            EvaluationContext context = new StandardEvaluationContext();
            
            // Add variables to context
            context.setVariable("request", request);
            context.setVariable("authentication", authentication);
            context.setVariable("method", joinPoint.getSignature().getName());
            context.setVariable("args", joinPoint.getArgs());
            
            if (authentication != null) {
                context.setVariable("user", extractUserFromAuthentication(authentication));
            }
            
            Object result = expr.getValue(context);
            return result != null ? result.toString() : "unknown";
            
        } catch (Exception e) {
            log.warn("Failed to evaluate SpEL expression: {}", expression, e);
            return "unknown";
        }
    }
    
    /**
     * Check if rate limiting should be skipped
     */
    private boolean shouldSkipRateLimit(RateLimit rateLimit) {
        // Skip for admin users if configured
        if (rateLimit.skipForAdmin()) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getAuthorities().stream()
                    .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"))) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Add rate limit headers to response
     */
    private void addRateLimitHeaders(RateLimitResult result) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            // Headers will be added by the filter or exception handler
            // This is a placeholder for future enhancement
        }
    }
    
    /**
     * Extract client IP address from request
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
    
    /**
     * Extract User from Authentication object
     */
    private User extractUserFromAuthentication(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        
        Object principal = authentication.getPrincipal();
        if (principal instanceof User) {
            return (User) principal;
        }
        
        // Handle other principal types if needed
        return null;
    }
}