package thomas.com.EventPing.security.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import thomas.com.EventPing.security.entity.AuditEvent;
import thomas.com.EventPing.security.repository.AuditEventRepository;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Comprehensive audit logging service for security events and data modifications
 * **Validates: Requirements 6.1, 6.2, 6.3, 6.4**
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuditLoggingService {

    private final AuditEventRepository auditEventRepository;
    private final ObjectMapper objectMapper;
    private final SensitiveDataEncryptionService encryptionService;

    @Value("${eventping.audit.enabled:true}")
    private boolean auditEnabled;

    @Value("${eventping.audit.log-authentication:true}")
    private boolean logAuthentication;

    @Value("${eventping.audit.log-authorization-failures:true}")
    private boolean logAuthorizationFailures;

    @Value("${eventping.audit.log-data-modifications:true}")
    private boolean logDataModifications;

    @Value("${eventping.audit.log-security-violations:true}")
    private boolean logSecurityViolations;

    @Value("${eventping.audit.retention-days:90}")
    private int retentionDays;

    /**
     * Log authentication success event
     */
    @Async
    public void logAuthenticationSuccess(String username, String ipAddress) {
        if (!auditEnabled || !logAuthentication) {
            return;
        }

        try {
            AuditEvent event = AuditEvent.authenticationSuccess(username, ipAddress);
            enrichEventWithRequestContext(event);
            auditEventRepository.save(event);
            
            log.debug("Logged authentication success for user: {}", username);
        } catch (Exception e) {
            log.error("Failed to log authentication success", e);
        }
    }

    /**
     * Log authentication failure event
     */
    @Async
    public void logAuthenticationFailure(String username, String ipAddress, String reason) {
        if (!auditEnabled || !logAuthentication) {
            return;
        }

        try {
            AuditEvent event = AuditEvent.authenticationFailure(username, ipAddress, reason);
            enrichEventWithRequestContext(event);
            auditEventRepository.save(event);
            
            log.debug("Logged authentication failure for user: {}", username);
        } catch (Exception e) {
            log.error("Failed to log authentication failure", e);
        }
    }

    /**
     * Log authorization failure event
     */
    @Async
    public void logAuthorizationFailure(String username, String ipAddress, String resource, String action) {
        if (!auditEnabled || !logAuthorizationFailures) {
            return;
        }

        try {
            AuditEvent event = AuditEvent.authorizationFailure(username, ipAddress, resource, action);
            enrichEventWithRequestContext(event);
            auditEventRepository.save(event);
            
            log.debug("Logged authorization failure for user: {} on resource: {}", username, resource);
        } catch (Exception e) {
            log.error("Failed to log authorization failure", e);
        }
    }

    /**
     * Log data modification event (create, update, delete)
     */
    @Async
    public void logDataModification(String username, AuditEvent.AuditEventType eventType, 
                                  String resourceType, String resourceId, 
                                  Object oldValue, Object newValue) {
        if (!auditEnabled || !logDataModifications) {
            return;
        }

        try {
            AuditEvent event = AuditEvent.dataModification(username, eventType, resourceType, resourceId);
            
            // Add old and new values to details (encrypted if sensitive)
            Map<String, Object> details = new HashMap<>();
            if (oldValue != null) {
                details.put("oldValue", sanitizeForAudit(oldValue));
            }
            if (newValue != null) {
                details.put("newValue", sanitizeForAudit(newValue));
            }
            event.setDetails(details);
            
            enrichEventWithRequestContext(event);
            auditEventRepository.save(event);
            
            log.debug("Logged data modification: {} for resource: {}/{}", eventType, resourceType, resourceId);
        } catch (Exception e) {
            log.error("Failed to log data modification", e);
        }
    }

    /**
     * Log security violation event
     */
    @Async
    public void logSecurityViolation(String username, String ipAddress, String violationType, 
                                   String details, AuditEvent.AuditSeverity severity) {
        if (!auditEnabled || !logSecurityViolations) {
            return;
        }

        try {
            AuditEvent event = AuditEvent.securityViolation(username, ipAddress, violationType, details);
            event.setSeverity(severity);
            enrichEventWithRequestContext(event);
            auditEventRepository.save(event);
            
            log.warn("Logged security violation: {} from IP: {} for user: {}", violationType, ipAddress, username);
            
            // Send alert for high severity events
            if (severity == AuditEvent.AuditSeverity.HIGH || severity == AuditEvent.AuditSeverity.CRITICAL) {
                sendSecurityAlert(event);
            }
        } catch (Exception e) {
            log.error("Failed to log security violation", e);
        }
    }

    /**
     * Log rate limit exceeded event
     */
    @Async
    public void logRateLimitExceeded(String username, String ipAddress, String limitType) {
        if (!auditEnabled) {
            return;
        }

        try {
            AuditEvent event = AuditEvent.rateLimitExceeded(username, ipAddress, limitType);
            enrichEventWithRequestContext(event);
            auditEventRepository.save(event);
            
            log.debug("Logged rate limit exceeded: {} for user: {} from IP: {}", limitType, username, ipAddress);
        } catch (Exception e) {
            log.error("Failed to log rate limit exceeded", e);
        }
    }

    /**
     * Log custom audit event
     */
    @Async
    public void logCustomEvent(AuditEvent.AuditEventType eventType, String username, 
                             String action, String resourceType, String resourceId,
                             Map<String, Object> details, AuditEvent.AuditSeverity severity) {
        if (!auditEnabled) {
            return;
        }

        try {
            AuditEvent event = AuditEvent.builder()
                    .eventType(eventType)
                    .username(username)
                    .action(action)
                    .resourceType(resourceType)
                    .resourceId(resourceId)
                    .details(details)
                    .severity(severity)
                    .result("SUCCESS")
                    .build();
            
            enrichEventWithRequestContext(event);
            auditEventRepository.save(event);
            
            log.debug("Logged custom event: {} for user: {}", eventType, username);
        } catch (Exception e) {
            log.error("Failed to log custom event", e);
        }
    }

    /**
     * Log session events
     */
    @Async
    public void logSessionEvent(AuditEvent.AuditEventType eventType, String username, 
                              String sessionId, String ipAddress) {
        if (!auditEnabled) {
            return;
        }

        try {
            AuditEvent event = AuditEvent.builder()
                    .eventType(eventType)
                    .username(username)
                    .sessionId(sessionId)
                    .ipAddress(ipAddress)
                    .result("SUCCESS")
                    .severity(AuditEvent.AuditSeverity.LOW)
                    .build();
            
            enrichEventWithRequestContext(event);
            auditEventRepository.save(event);
            
            log.debug("Logged session event: {} for user: {}", eventType, username);
        } catch (Exception e) {
            log.error("Failed to log session event", e);
        }
    }

    /**
     * Get audit events for a user
     */
    public Page<AuditEvent> getAuditEventsForUser(String username, Pageable pageable) {
        return auditEventRepository.findByUsernameOrderByTimestampDesc(username, pageable);
    }

    /**
     * Get recent security violations
     */
    public List<AuditEvent> getRecentSecurityViolations(LocalDateTime since) {
        return auditEventRepository.findRecentSecurityViolations(since);
    }

    /**
     * Get high severity events
     */
    public List<AuditEvent> getHighSeverityEvents(LocalDateTime since) {
        return auditEventRepository.findHighSeverityEvents(since);
    }

    /**
     * Get authentication failures for a user
     */
    public List<AuditEvent> getAuthenticationFailures(String username, LocalDateTime since) {
        return auditEventRepository.findRecentAuthenticationFailures(username, since);
    }

    /**
     * Get authentication failures from an IP
     */
    public List<AuditEvent> getAuthenticationFailuresByIp(String ipAddress, LocalDateTime since) {
        return auditEventRepository.findRecentAuthenticationFailuresByIp(ipAddress, since);
    }

    /**
     * Get audit statistics
     */
    public Map<String, Object> getAuditStatistics(LocalDateTime since) {
        Map<String, Object> stats = new HashMap<>();
        
        List<Object[]> eventTypeStats = auditEventRepository.getEventTypeStatistics(since);
        List<Object[]> severityStats = auditEventRepository.getSeverityStatistics(since);
        
        stats.put("eventTypeStatistics", eventTypeStats);
        stats.put("severityStatistics", severityStats);
        stats.put("totalEvents", auditEventRepository.count());
        
        return stats;
    }

    /**
     * Clean up old audit events
     */
    @Transactional
    public void cleanupOldEvents() {
        if (retentionDays > 0) {
            LocalDateTime cutoffDate = LocalDateTime.now().minusDays(retentionDays);
            auditEventRepository.deleteOldEvents(cutoffDate);
            log.info("Cleaned up audit events older than {} days", retentionDays);
        }
    }

    /**
     * Check for suspicious activity patterns
     */
    public boolean isSuspiciousActivity(String ipAddress, LocalDateTime since, int threshold) {
        List<AuditEvent> suspiciousEvents = auditEventRepository.findSuspiciousActivityByIp(
                ipAddress, since, (long) threshold);
        return !suspiciousEvents.isEmpty();
    }

    /**
     * Generate correlation ID for related events
     */
    public String generateCorrelationId() {
        return UUID.randomUUID().toString();
    }

    /**
     * Enrich audit event with current request context
     */
    private void enrichEventWithRequestContext(AuditEvent event) {
        try {
            ServletRequestAttributes attributes = 
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                
                if (event.getIpAddress() == null) {
                    event.setIpAddress(getClientIpAddress(request));
                }
                
                event.setUserAgent(request.getHeader("User-Agent"));
                event.setRequestUri(request.getRequestURI());
                event.setRequestMethod(request.getMethod());
                
                // Get session ID if available
                if (request.getSession(false) != null) {
                    event.setSessionId(request.getSession().getId());
                }
            }
            
            // Get username from security context if not already set
            if (event.getUsername() == null) {
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
                    event.setUsername(auth.getName());
                }
            }
        } catch (Exception e) {
            log.debug("Could not enrich audit event with request context", e);
        }
    }

    /**
     * Get client IP address from request
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String[] headerNames = {
                "X-Forwarded-For",
                "X-Real-IP",
                "Proxy-Client-IP",
                "WL-Proxy-Client-IP",
                "HTTP_X_FORWARDED_FOR",
                "HTTP_X_FORWARDED",
                "HTTP_X_CLUSTER_CLIENT_IP",
                "HTTP_CLIENT_IP",
                "HTTP_FORWARDED_FOR",
                "HTTP_FORWARDED",
                "HTTP_VIA",
                "REMOTE_ADDR"
        };
        
        for (String header : headerNames) {
            String ip = request.getHeader(header);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                // Handle comma-separated IPs (take the first one)
                if (ip.contains(",")) {
                    ip = ip.split(",")[0].trim();
                }
                return ip;
            }
        }
        
        return request.getRemoteAddr();
    }

    /**
     * Sanitize sensitive data for audit logging
     */
    private Object sanitizeForAudit(Object value) {
        if (value == null) {
            return null;
        }
        
        try {
            String jsonValue = objectMapper.writeValueAsString(value);
            
            // Check if the value contains sensitive data
            if (encryptionService.containsSensitiveData(jsonValue)) {
                // Mask sensitive data for audit logs
                return encryptionService.maskForLogging(jsonValue);
            }
            
            return value;
        } catch (JsonProcessingException e) {
            log.debug("Could not serialize value for audit", e);
            return value.toString();
        }
    }

    /**
     * Send security alert for high-severity events
     */
    private void sendSecurityAlert(AuditEvent event) {
        // In a real implementation, this would send alerts via email, Slack, etc.
        log.error("SECURITY ALERT: {} - User: {}, IP: {}, Details: {}", 
                event.getEventType(), event.getUsername(), event.getIpAddress(), event.getErrorMessage());
        
        // Could integrate with alerting systems like:
        // - Email notifications
        // - Slack/Teams webhooks
        // - PagerDuty
        // - SIEM systems
    }
}