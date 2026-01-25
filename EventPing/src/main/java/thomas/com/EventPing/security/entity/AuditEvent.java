package thomas.com.EventPing.security.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Entity for storing audit events and security-related activities
 * **Validates: Requirements 6.1, 6.2, 6.3**
 */
@Entity
@Table(name = "audit_events", indexes = {
    @Index(name = "idx_audit_event_type", columnList = "event_type"),
    @Index(name = "idx_audit_username", columnList = "username"),
    @Index(name = "idx_audit_timestamp", columnList = "timestamp"),
    @Index(name = "idx_audit_severity", columnList = "severity"),
    @Index(name = "idx_audit_ip_address", columnList = "ip_address")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 50)
    private AuditEventType eventType;

    @Column(name = "username", length = 255)
    private String username;

    @Column(name = "ip_address", length = 45) // IPv6 compatible
    private String ipAddress;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @Column(name = "request_uri", length = 500)
    private String requestUri;

    @Column(name = "request_method", length = 10)
    private String requestMethod;

    @Column(name = "session_id", length = 255)
    private String sessionId;

    @Column(name = "resource_type", length = 100)
    private String resourceType;

    @Column(name = "resource_id", length = 100)
    private String resourceId;

    @Column(name = "action", length = 100)
    private String action;

    @Column(name = "result", length = 50)
    private String result; // SUCCESS, FAILURE, ERROR

    @Column(name = "error_message", length = 1000)
    private String errorMessage;

    @Type(io.hypersistence.utils.hibernate.type.json.JsonType.class)
    @Column(name = "details", columnDefinition = "jsonb")
    private Map<String, Object> details;

    @Enumerated(EnumType.STRING)
    @Column(name = "severity", nullable = false, length = 20)
    @Builder.Default
    private AuditSeverity severity = AuditSeverity.MEDIUM;

    @Column(name = "timestamp", nullable = false)
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    @Column(name = "correlation_id", length = 100)
    private String correlationId;

    @Column(name = "tenant_id", length = 100)
    private String tenantId;

    @PrePersist
    public void prePersist() {
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
        if (severity == null) {
            severity = AuditSeverity.MEDIUM;
        }
    }

    /**
     * Types of audit events that can be logged
     */
    public enum AuditEventType {
        // Authentication events
        AUTHENTICATION_SUCCESS,
        AUTHENTICATION_FAILURE,
        AUTHENTICATION_LOCKED,
        PASSWORD_CHANGE,
        PASSWORD_RESET,
        
        // Authorization events
        AUTHORIZATION_SUCCESS,
        AUTHORIZATION_FAILURE,
        PERMISSION_DENIED,
        ROLE_CHANGE,
        
        // Data modification events
        DATA_CREATE,
        DATA_READ,
        DATA_UPDATE,
        DATA_DELETE,
        DATA_EXPORT,
        DATA_IMPORT,
        
        // Security events
        SECURITY_VIOLATION,
        RATE_LIMIT_EXCEEDED,
        SUSPICIOUS_ACTIVITY,
        BRUTE_FORCE_ATTEMPT,
        SQL_INJECTION_ATTEMPT,
        XSS_ATTEMPT,
        
        // System events
        SYSTEM_START,
        SYSTEM_STOP,
        CONFIGURATION_CHANGE,
        BACKUP_CREATED,
        BACKUP_RESTORED,
        
        // Session events
        SESSION_START,
        SESSION_END,
        SESSION_TIMEOUT,
        SESSION_HIJACK_ATTEMPT,
        
        // API events
        API_KEY_CREATED,
        API_KEY_REVOKED,
        API_RATE_LIMIT_EXCEEDED,
        API_UNAUTHORIZED_ACCESS,
        
        // File events
        FILE_UPLOAD,
        FILE_DOWNLOAD,
        FILE_DELETE,
        FILE_ACCESS_DENIED,
        
        // Admin events
        ADMIN_LOGIN,
        ADMIN_ACTION,
        USER_CREATED,
        USER_DELETED,
        USER_SUSPENDED,
        USER_ACTIVATED
    }

    /**
     * Severity levels for audit events
     */
    public enum AuditSeverity {
        LOW,      // Informational events
        MEDIUM,   // Normal security events
        HIGH,     // Important security events requiring attention
        CRITICAL  // Critical security events requiring immediate action
    }

    /**
     * Helper method to create authentication success event
     */
    public static AuditEvent authenticationSuccess(String username, String ipAddress) {
        return AuditEvent.builder()
                .eventType(AuditEventType.AUTHENTICATION_SUCCESS)
                .username(username)
                .ipAddress(ipAddress)
                .result("SUCCESS")
                .severity(AuditSeverity.LOW)
                .build();
    }

    /**
     * Helper method to create authentication failure event
     */
    public static AuditEvent authenticationFailure(String username, String ipAddress, String reason) {
        return AuditEvent.builder()
                .eventType(AuditEventType.AUTHENTICATION_FAILURE)
                .username(username)
                .ipAddress(ipAddress)
                .result("FAILURE")
                .errorMessage(reason)
                .severity(AuditSeverity.MEDIUM)
                .build();
    }

    /**
     * Helper method to create authorization failure event
     */
    public static AuditEvent authorizationFailure(String username, String ipAddress, String resource, String action) {
        return AuditEvent.builder()
                .eventType(AuditEventType.AUTHORIZATION_FAILURE)
                .username(username)
                .ipAddress(ipAddress)
                .resourceType(resource)
                .action(action)
                .result("FAILURE")
                .severity(AuditSeverity.MEDIUM)
                .build();
    }

    /**
     * Helper method to create security violation event
     */
    public static AuditEvent securityViolation(String username, String ipAddress, String violationType, String details) {
        return AuditEvent.builder()
                .eventType(AuditEventType.SECURITY_VIOLATION)
                .username(username)
                .ipAddress(ipAddress)
                .action(violationType)
                .errorMessage(details)
                .result("VIOLATION")
                .severity(AuditSeverity.HIGH)
                .build();
    }

    /**
     * Helper method to create data modification event
     */
    public static AuditEvent dataModification(String username, AuditEventType eventType, String resourceType, String resourceId) {
        return AuditEvent.builder()
                .eventType(eventType)
                .username(username)
                .resourceType(resourceType)
                .resourceId(resourceId)
                .result("SUCCESS")
                .severity(AuditSeverity.LOW)
                .build();
    }

    /**
     * Helper method to create rate limit exceeded event
     */
    public static AuditEvent rateLimitExceeded(String username, String ipAddress, String limitType) {
        return AuditEvent.builder()
                .eventType(AuditEventType.RATE_LIMIT_EXCEEDED)
                .username(username)
                .ipAddress(ipAddress)
                .action(limitType)
                .result("BLOCKED")
                .severity(AuditSeverity.MEDIUM)
                .build();
    }
}