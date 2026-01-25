package thomas.com.EventPing.security.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.jqwik.api.*;
import net.jqwik.api.constraints.AlphaChars;
import net.jqwik.api.constraints.StringLength;
import net.jqwik.api.lifecycle.BeforeProperty;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import thomas.com.EventPing.security.entity.AuditEvent;
import thomas.com.EventPing.security.repository.AuditEventRepository;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Property-based tests for audit logging completeness
 * **Feature: eventping-security-hardening, Property 7: Audit Logging Completeness**
 * **Validates: Requirements 6.1, 6.2, 6.3**
 */
class AuditLoggingPropertyTest {

    private AuditLoggingService auditLoggingService;
    
    @Mock
    private AuditEventRepository auditEventRepository;
    
    @Mock
    private SensitiveDataEncryptionService encryptionService;
    
    private ObjectMapper objectMapper;

    @BeforeProperty
    void setUp() {
        MockitoAnnotations.openMocks(this);
        objectMapper = new ObjectMapper();
        
        // Create service with mocked dependencies
        auditLoggingService = new AuditLoggingService(
            auditEventRepository, 
            objectMapper, 
            encryptionService
        );
        
        // Configure mocks
        when(auditEventRepository.save(any(AuditEvent.class)))
            .thenAnswer(invocation -> {
                AuditEvent event = invocation.getArgument(0);
                event.setId(1L); // Simulate saved entity
                return event;
            });
        
        when(encryptionService.containsSensitiveData(anyString())).thenReturn(false);
        when(encryptionService.maskForLogging(anyString())).thenAnswer(i -> i.getArgument(0));
    }

    @Property(tries = 100)
    @Label("Property 7: Audit Logging Completeness - For any authentication event, all required fields should be logged")
    void authenticationEventsAreCompletelyLogged(
            @ForAll @StringLength(min = 3, max = 50) @AlphaChars String username,
            @ForAll("ipAddresses") String ipAddress,
            @ForAll("authenticationReasons") String reason) {
        
        // Test authentication success
        auditLoggingService.logAuthenticationSuccess(username, ipAddress);
        
        verify(auditEventRepository, atLeastOnce()).save(argThat(event -> 
            event.getEventType() == AuditEvent.AuditEventType.AUTHENTICATION_SUCCESS &&
            event.getUsername().equals(username) &&
            event.getIpAddress().equals(ipAddress) &&
            event.getResult().equals("SUCCESS") &&
            event.getSeverity() == AuditEvent.AuditSeverity.LOW &&
            event.getTimestamp() != null
        ));
        
        // Test authentication failure
        auditLoggingService.logAuthenticationFailure(username, ipAddress, reason);
        
        verify(auditEventRepository, atLeastOnce()).save(argThat(event -> 
            event.getEventType() == AuditEvent.AuditEventType.AUTHENTICATION_FAILURE &&
            event.getUsername().equals(username) &&
            event.getIpAddress().equals(ipAddress) &&
            event.getResult().equals("FAILURE") &&
            event.getErrorMessage().equals(reason) &&
            event.getSeverity() == AuditEvent.AuditSeverity.MEDIUM &&
            event.getTimestamp() != null
        ));
    }

    @Property(tries = 100)
    @Label("Property 7a: Authorization failure events contain all required context")
    void authorizationFailureEventsAreComplete(
            @ForAll @StringLength(min = 3, max = 50) @AlphaChars String username,
            @ForAll("ipAddresses") String ipAddress,
            @ForAll @StringLength(min = 3, max = 30) @AlphaChars String resource,
            @ForAll @StringLength(min = 3, max = 20) @AlphaChars String action) {
        
        auditLoggingService.logAuthorizationFailure(username, ipAddress, resource, action);
        
        verify(auditEventRepository, atLeastOnce()).save(argThat(event -> 
            event.getEventType() == AuditEvent.AuditEventType.AUTHORIZATION_FAILURE &&
            event.getUsername().equals(username) &&
            event.getIpAddress().equals(ipAddress) &&
            event.getResourceType().equals(resource) &&
            event.getAction().equals(action) &&
            event.getResult().equals("FAILURE") &&
            event.getSeverity() == AuditEvent.AuditSeverity.MEDIUM &&
            event.getTimestamp() != null
        ));
    }

    @Property(tries = 100)
    @Label("Property 7b: Data modification events capture old and new values")
    void dataModificationEventsAreComplete(
            @ForAll @StringLength(min = 3, max = 50) @AlphaChars String username,
            @ForAll("dataModificationTypes") AuditEvent.AuditEventType eventType,
            @ForAll @StringLength(min = 3, max = 30) @AlphaChars String resourceType,
            @ForAll @StringLength(min = 1, max = 20) String resourceId,
            @ForAll @StringLength(min = 5, max = 100) String oldValue,
            @ForAll @StringLength(min = 5, max = 100) String newValue) {
        
        auditLoggingService.logDataModification(username, eventType, resourceType, resourceId, oldValue, newValue);
        
        verify(auditEventRepository, atLeastOnce()).save(argThat(event -> 
            event.getEventType() == eventType &&
            event.getUsername().equals(username) &&
            event.getResourceType().equals(resourceType) &&
            event.getResourceId().equals(resourceId) &&
            event.getResult().equals("SUCCESS") &&
            event.getSeverity() == AuditEvent.AuditSeverity.LOW &&
            event.getTimestamp() != null &&
            event.getDetails() != null &&
            event.getDetails().containsKey("oldValue") &&
            event.getDetails().containsKey("newValue")
        ));
    }

    @Property(tries = 100)
    @Label("Property 7c: Security violation events have appropriate severity levels")
    void securityViolationEventsHaveCorrectSeverity(
            @ForAll @StringLength(min = 3, max = 50) @AlphaChars String username,
            @ForAll("ipAddresses") String ipAddress,
            @ForAll @StringLength(min = 5, max = 50) String violationType,
            @ForAll @StringLength(min = 10, max = 200) String details,
            @ForAll("severityLevels") AuditEvent.AuditSeverity severity) {
        
        auditLoggingService.logSecurityViolation(username, ipAddress, violationType, details, severity);
        
        verify(auditEventRepository, atLeastOnce()).save(argThat(event -> 
            event.getEventType() == AuditEvent.AuditEventType.SECURITY_VIOLATION &&
            event.getUsername().equals(username) &&
            event.getIpAddress().equals(ipAddress) &&
            event.getAction().equals(violationType) &&
            event.getErrorMessage().equals(details) &&
            event.getResult().equals("VIOLATION") &&
            event.getSeverity() == severity &&
            event.getTimestamp() != null
        ));
    }

    @Property(tries = 100)
    @Label("Property 7d: Rate limit events contain limit type and context")
    void rateLimitEventsAreComplete(
            @ForAll @StringLength(min = 3, max = 50) @AlphaChars String username,
            @ForAll("ipAddresses") String ipAddress,
            @ForAll @StringLength(min = 5, max = 30) String limitType) {
        
        auditLoggingService.logRateLimitExceeded(username, ipAddress, limitType);
        
        verify(auditEventRepository, atLeastOnce()).save(argThat(event -> 
            event.getEventType() == AuditEvent.AuditEventType.RATE_LIMIT_EXCEEDED &&
            event.getUsername().equals(username) &&
            event.getIpAddress().equals(ipAddress) &&
            event.getAction().equals(limitType) &&
            event.getResult().equals("BLOCKED") &&
            event.getSeverity() == AuditEvent.AuditSeverity.MEDIUM &&
            event.getTimestamp() != null
        ));
    }

    @Property(tries = 100)
    @Label("Property 7e: Custom events preserve all provided details")
    void customEventsPreserveAllDetails(
            @ForAll("auditEventTypes") AuditEvent.AuditEventType eventType,
            @ForAll @StringLength(min = 3, max = 50) @AlphaChars String username,
            @ForAll @StringLength(min = 3, max = 30) @AlphaChars String action,
            @ForAll @StringLength(min = 3, max = 30) @AlphaChars String resourceType,
            @ForAll @StringLength(min = 1, max = 20) String resourceId,
            @ForAll("severityLevels") AuditEvent.AuditSeverity severity) {
        
        Map<String, Object> details = new HashMap<>();
        details.put("key1", "value1");
        details.put("key2", 123);
        details.put("key3", true);
        
        auditLoggingService.logCustomEvent(eventType, username, action, resourceType, resourceId, details, severity);
        
        verify(auditEventRepository, atLeastOnce()).save(argThat(event -> 
            event.getEventType() == eventType &&
            event.getUsername().equals(username) &&
            event.getAction().equals(action) &&
            event.getResourceType().equals(resourceType) &&
            event.getResourceId().equals(resourceId) &&
            event.getSeverity() == severity &&
            event.getResult().equals("SUCCESS") &&
            event.getTimestamp() != null &&
            event.getDetails() != null &&
            event.getDetails().equals(details)
        ));
    }

    @Property(tries = 100)
    @Label("Property 7f: Session events include session context")
    void sessionEventsIncludeContext(
            @ForAll("sessionEventTypes") AuditEvent.AuditEventType eventType,
            @ForAll @StringLength(min = 3, max = 50) @AlphaChars String username,
            @ForAll @StringLength(min = 10, max = 50) String sessionId,
            @ForAll("ipAddresses") String ipAddress) {
        
        auditLoggingService.logSessionEvent(eventType, username, sessionId, ipAddress);
        
        verify(auditEventRepository, atLeastOnce()).save(argThat(event -> 
            event.getEventType() == eventType &&
            event.getUsername().equals(username) &&
            event.getSessionId().equals(sessionId) &&
            event.getIpAddress().equals(ipAddress) &&
            event.getResult().equals("SUCCESS") &&
            event.getSeverity() == AuditEvent.AuditSeverity.LOW &&
            event.getTimestamp() != null
        ));
    }

    @Property(tries = 50)
    @Label("Property 7g: All audit events have timestamps within reasonable bounds")
    void auditEventsHaveValidTimestamps(
            @ForAll @StringLength(min = 3, max = 50) @AlphaChars String username,
            @ForAll("ipAddresses") String ipAddress) {
        
        LocalDateTime beforeCall = LocalDateTime.now().minusSeconds(1);
        
        auditLoggingService.logAuthenticationSuccess(username, ipAddress);
        
        LocalDateTime afterCall = LocalDateTime.now().plusSeconds(1);
        
        verify(auditEventRepository, atLeastOnce()).save(argThat(event -> {
            LocalDateTime timestamp = event.getTimestamp();
            return timestamp != null && 
                   timestamp.isAfter(beforeCall) && 
                   timestamp.isBefore(afterCall);
        }));
    }

    @Property(tries = 100)
    @Label("Property 7h: Audit events are persisted exactly once per call")
    void auditEventsArePersistedOncePerCall(
            @ForAll @StringLength(min = 3, max = 50) @AlphaChars String username,
            @ForAll("ipAddresses") String ipAddress,
            @ForAll @StringLength(min = 5, max = 100) String reason) {
        
        reset(auditEventRepository); // Clear previous interactions
        
        auditLoggingService.logAuthenticationFailure(username, ipAddress, reason);
        
        // Verify exactly one save call was made
        verify(auditEventRepository, times(1)).save(any(AuditEvent.class));
    }

    // Generators for test data
    @Provide
    Arbitrary<String> ipAddresses() {
        return Arbitraries.oneOf(
            // IPv4 addresses
            Arbitraries.strings()
                .withCharRange('0', '9')
                .withChars('.')
                .filter(s -> s.matches("^(?:[0-9]{1,3}\\.){3}[0-9]{1,3}$"))
                .filter(s -> {
                    String[] parts = s.split("\\.");
                    return parts.length == 4 && 
                           java.util.Arrays.stream(parts)
                               .allMatch(part -> {
                                   try {
                                       int num = Integer.parseInt(part);
                                       return num >= 0 && num <= 255;
                                   } catch (NumberFormatException e) {
                                       return false;
                                   }
                               });
                }),
            // Simple test IPs
            Arbitraries.of("192.168.1.1", "10.0.0.1", "172.16.0.1", "127.0.0.1")
        );
    }

    @Provide
    Arbitrary<String> authenticationReasons() {
        return Arbitraries.of(
            "Invalid credentials",
            "Account locked",
            "Password expired",
            "User not found",
            "Account disabled",
            "Too many failed attempts"
        );
    }

    @Provide
    Arbitrary<AuditEvent.AuditEventType> dataModificationTypes() {
        return Arbitraries.of(
            AuditEvent.AuditEventType.DATA_CREATE,
            AuditEvent.AuditEventType.DATA_UPDATE,
            AuditEvent.AuditEventType.DATA_DELETE
        );
    }

    @Provide
    Arbitrary<AuditEvent.AuditSeverity> severityLevels() {
        return Arbitraries.of(AuditEvent.AuditSeverity.values());
    }

    @Provide
    Arbitrary<AuditEvent.AuditEventType> auditEventTypes() {
        return Arbitraries.of(AuditEvent.AuditEventType.values());
    }

    @Provide
    Arbitrary<AuditEvent.AuditEventType> sessionEventTypes() {
        return Arbitraries.of(
            AuditEvent.AuditEventType.SESSION_START,
            AuditEvent.AuditEventType.SESSION_END,
            AuditEvent.AuditEventType.SESSION_TIMEOUT
        );
    }
}