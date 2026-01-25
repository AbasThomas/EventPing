package thomas.com.EventPing.security.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import thomas.com.EventPing.security.dto.ErrorResponse;
import thomas.com.EventPing.security.exception.RateLimitExceededException;
import thomas.com.EventPing.security.exception.ValidationException;
import thomas.com.EventPing.security.service.AuditLoggingService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler that provides centralized exception handling
 * with secure error responses that prevent information disclosure.
 * 
 * Requirements addressed:
 * - 8.1: Return generic error messages to clients
 * - 8.2: Log detailed error information server-side only
 * - 8.3: Never expose stack traces to external clients
 * - 8.5: Log security-related exceptions
 */
@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final AuditLoggingService auditLoggingService;

    /**
     * Handle authentication failures with secure error response and audit logging.
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(
            AuthenticationException ex, HttpServletRequest request) {
        
        // Log authentication failure for security monitoring
        auditLoggingService.logAuthenticationFailure(
            "unknown",
            getClientIpAddress(request),
            ex.getMessage()
        );

        // Return generic error message without exposing internal details
        ErrorResponse error = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.UNAUTHORIZED.value())
            .error("Authentication Failed")
            .message("Invalid credentials")
            .path(request.getRequestURI())
            .build();

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    /**
     * Handle authorization failures with secure error response and audit logging.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(
            AccessDeniedException ex, HttpServletRequest request) {
        
        // Log authorization failure for security monitoring
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        auditLoggingService.logAuthorizationFailure(
            auth != null ? auth.getName() : "anonymous",
            getClientIpAddress(request),
            request.getRequestURI(),
            "ACCESS_DENIED"
        );

        // Return generic error message without exposing resource details
        ErrorResponse error = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.FORBIDDEN.value())
            .error("Access Denied")
            .message("Insufficient permissions")
            .path(request.getRequestURI())
            .build();

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    /**
     * Handle custom validation exceptions with helpful but non-revealing error messages.
     */
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            ValidationException ex, HttpServletRequest request) {
        
        // Log validation failure without exposing sensitive data
        log.warn("Validation failed for request {}: {}", request.getRequestURI(), ex.getMessage());
        
        ErrorResponse error = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.BAD_REQUEST.value())
            .error("Validation Failed")
            .message("Invalid input data")
            .validationErrors(ex.getValidationErrors())
            .path(request.getRequestURI())
            .build();

        return ResponseEntity.badRequest().body(error);
    }

    /**
     * Handle Spring validation exceptions (Bean Validation).
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        
        // Extract validation errors without exposing internal field names
        Map<String, String> validationErrors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> {
            String fieldName = error.getField();
            String errorMessage = error.getDefaultMessage();
            validationErrors.put(fieldName, errorMessage);
        });

        log.warn("Bean validation failed for request {}: {}", request.getRequestURI(), validationErrors);
        
        ErrorResponse error = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.BAD_REQUEST.value())
            .error("Validation Failed")
            .message("Invalid input data")
            .validationErrors(validationErrors)
            .path(request.getRequestURI())
            .build();

        return ResponseEntity.badRequest().body(error);
    }

    /**
     * Handle constraint violation exceptions.
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(
            ConstraintViolationException ex, HttpServletRequest request) {
        
        Map<String, String> validationErrors = new HashMap<>();
        ex.getConstraintViolations().forEach(violation -> {
            String propertyPath = violation.getPropertyPath().toString();
            String message = violation.getMessage();
            validationErrors.put(propertyPath, message);
        });

        log.warn("Constraint validation failed for request {}: {}", request.getRequestURI(), validationErrors);
        
        ErrorResponse error = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.BAD_REQUEST.value())
            .error("Validation Failed")
            .message("Invalid input data")
            .validationErrors(validationErrors)
            .path(request.getRequestURI())
            .build();

        return ResponseEntity.badRequest().body(error);
    }

    /**
     * Handle rate limit exceeded exceptions with retry information.
     */
    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<ErrorResponse> handleRateLimitExceededException(
            RateLimitExceededException ex, HttpServletRequest request) {
        
        // Log rate limit violation for security monitoring
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        auditLoggingService.logSecurityViolation(
            auth != null ? auth.getName() : "anonymous",
            getClientIpAddress(request),
            "RATE_LIMIT_EXCEEDED",
            "Rate limit exceeded for " + request.getRequestURI(),
            thomas.com.EventPing.security.entity.AuditEvent.AuditSeverity.MEDIUM
        );
        
        ErrorResponse error = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.TOO_MANY_REQUESTS.value())
            .error("Rate Limit Exceeded")
            .message("Too many requests. Please try again later.")
            .path(request.getRequestURI())
            .build();

        HttpHeaders headers = new HttpHeaders();
        headers.add("Retry-After", String.valueOf(ex.getRetryAfterSeconds()));

        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
            .headers(headers)
            .body(error);
    }

    /**
     * Handle all other exceptions with generic error response.
     * Logs full exception details server-side but returns safe response to client.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex, HttpServletRequest request) {
        
        // Log full exception details server-side for debugging
        log.error("Unexpected error occurred for request {}: {}", request.getRequestURI(), ex.getMessage(), ex);
        
        // Log security event for monitoring
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        auditLoggingService.logCustomEvent(
            thomas.com.EventPing.security.entity.AuditEvent.AuditEventType.SECURITY_VIOLATION,
            auth != null ? auth.getName() : "anonymous",
            "SYSTEM_ERROR",
            "REQUEST",
            request.getRequestURI(),
            Map.of("error", ex.getClass().getSimpleName()),
            thomas.com.EventPing.security.entity.AuditEvent.AuditSeverity.HIGH
        );
        
        // Return generic error to client without exposing internal details
        ErrorResponse error = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
            .error("Internal Server Error")
            .message("An unexpected error occurred")
            .path(request.getRequestURI())
            .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    /**
     * Extract client IP address from request, handling proxy headers.
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
}