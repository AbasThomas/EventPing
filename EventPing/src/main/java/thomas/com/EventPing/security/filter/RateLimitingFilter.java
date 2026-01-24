package thomas.com.EventPing.security.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import thomas.com.EventPing.User.model.User;
import thomas.com.EventPing.security.model.RateLimitType;
import thomas.com.EventPing.security.service.RateLimitResult;
import thomas.com.EventPing.security.service.RateLimitingService;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Filter for applying global rate limiting to HTTP requests
 * **Validates: Requirements 3.1, 3.2, 3.3**
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RateLimitingFilter implements Filter {
    
    private final RateLimitingService rateLimitingService;
    private final ObjectMapper objectMapper;
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) 
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        // Skip rate limiting for certain paths
        if (shouldSkipRateLimiting(httpRequest)) {
            chain.doFilter(request, response);
            return;
        }
        
        try {
            // Apply IP-based rate limiting first
            String clientIp = getClientIpAddress(httpRequest);
            RateLimitResult ipResult = rateLimitingService.checkIpRateLimit(clientIp);
            
            if (!ipResult.isAllowed()) {
                handleRateLimitExceeded(httpResponse, ipResult, "IP rate limit exceeded");
                return;
            }
            
            // Apply user-based rate limiting if authenticated
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated() && 
                !"anonymousUser".equals(authentication.getPrincipal())) {
                
                User user = extractUserFromAuthentication(authentication);
                if (user != null) {
                    RateLimitResult userResult = rateLimitingService.checkUserRateLimit(user, "api");
                    
                    if (!userResult.isAllowed()) {
                        handleRateLimitExceeded(httpResponse, userResult, "User rate limit exceeded");
                        return;
                    }
                }
            }
            
            // Apply global rate limiting
            RateLimitResult globalResult = rateLimitingService.checkRateLimit("global", RateLimitType.GLOBAL);
            if (!globalResult.isAllowed()) {
                handleRateLimitExceeded(httpResponse, globalResult, "Global rate limit exceeded");
                return;
            }
            
            // Add rate limit headers to response
            addRateLimitHeaders(httpResponse, ipResult);
            
            // Continue with the request
            chain.doFilter(request, response);
            
        } catch (Exception e) {
            log.error("Error in rate limiting filter", e);
            // Continue with request if rate limiting fails to avoid breaking the application
            chain.doFilter(request, response);
        }
    }
    
    /**
     * Check if rate limiting should be skipped for this request
     */
    private boolean shouldSkipRateLimiting(HttpServletRequest request) {
        String path = request.getRequestURI();
        String method = request.getMethod();
        
        // Skip rate limiting for health checks and static resources
        if (path.startsWith("/actuator/health") ||
            path.startsWith("/static/") ||
            path.startsWith("/css/") ||
            path.startsWith("/js/") ||
            path.startsWith("/images/") ||
            path.endsWith(".ico")) {
            return true;
        }
        
        // Skip for OPTIONS requests (CORS preflight)
        if ("OPTIONS".equals(method)) {
            return true;
        }
        
        return false;
    }
    
    /**
     * Handle rate limit exceeded scenario
     */
    private void handleRateLimitExceeded(HttpServletResponse response, RateLimitResult result, String message) 
            throws IOException {
        
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setHeader("Retry-After", String.valueOf(result.getRetryAfterSeconds()));
        
        // Add rate limit headers
        addRateLimitHeaders(response, result);
        
        // Create error response
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now().toString());
        errorResponse.put("status", HttpStatus.TOO_MANY_REQUESTS.value());
        errorResponse.put("error", "Too Many Requests");
        errorResponse.put("message", message);
        errorResponse.put("retryAfter", result.getRetryAfterSeconds());
        
        if (result.isBlocked()) {
            errorResponse.put("blocked", true);
            errorResponse.put("blockExpiresAt", result.getBlockExpiresAt());
        }
        
        String jsonResponse = objectMapper.writeValueAsString(errorResponse);
        response.getWriter().write(jsonResponse);
        
        log.warn("Rate limit exceeded: {} - Retry after: {} seconds", message, result.getRetryAfterSeconds());
    }
    
    /**
     * Add rate limit headers to the response
     */
    private void addRateLimitHeaders(HttpServletResponse response, RateLimitResult result) {
        response.setHeader("X-RateLimit-Limit", String.valueOf(result.getMaxRequests()));
        response.setHeader("X-RateLimit-Remaining", String.valueOf(result.getRemainingRequests()));
        response.setHeader("X-RateLimit-Reset", String.valueOf(result.getWindowResetTime()));
        
        if (!result.isAllowed()) {
            response.setHeader("X-RateLimit-RetryAfter", String.valueOf(result.getRetryAfterSeconds()));
        }
    }
    
    /**
     * Extract client IP address from request
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            // Take the first IP in the chain
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        String xOriginalForwardedFor = request.getHeader("X-Original-Forwarded-For");
        if (xOriginalForwardedFor != null && !xOriginalForwardedFor.isEmpty()) {
            return xOriginalForwardedFor.split(",")[0].trim();
        }
        
        return request.getRemoteAddr();
    }
    
    /**
     * Extract User from Authentication object
     */
    private User extractUserFromAuthentication(Authentication authentication) {
        Object principal = authentication.getPrincipal();
        
        if (principal instanceof User) {
            return (User) principal;
        }
        
        // If principal is a UserDetails or similar, you might need to load the User
        // For now, return null to skip user-based rate limiting
        return null;
    }
}