package thomas.com.EventPing.security.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import thomas.com.EventPing.security.entity.AuditEvent;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for audit event persistence and querying
 * **Validates: Requirements 6.1, 6.2, 6.3**
 */
@Repository
public interface AuditEventRepository extends JpaRepository<AuditEvent, Long> {

    /**
     * Find audit events by event type
     */
    List<AuditEvent> findByEventTypeOrderByTimestampDesc(AuditEvent.AuditEventType eventType);

    /**
     * Find audit events by username
     */
    Page<AuditEvent> findByUsernameOrderByTimestampDesc(String username, Pageable pageable);

    /**
     * Find audit events by IP address
     */
    List<AuditEvent> findByIpAddressOrderByTimestampDesc(String ipAddress);

    /**
     * Find audit events by severity
     */
    Page<AuditEvent> findBySeverityOrderByTimestampDesc(AuditEvent.AuditSeverity severity, Pageable pageable);

    /**
     * Find audit events within a time range
     */
    @Query("SELECT a FROM AuditEvent a WHERE a.timestamp BETWEEN :startTime AND :endTime ORDER BY a.timestamp DESC")
    Page<AuditEvent> findByTimestampBetween(@Param("startTime") LocalDateTime startTime, 
                                          @Param("endTime") LocalDateTime endTime, 
                                          Pageable pageable);

    /**
     * Find recent authentication failures for a user
     */
    @Query("SELECT a FROM AuditEvent a WHERE a.username = :username " +
           "AND a.eventType IN ('AUTHENTICATION_FAILURE', 'BRUTE_FORCE_ATTEMPT') " +
           "AND a.timestamp >= :since ORDER BY a.timestamp DESC")
    List<AuditEvent> findRecentAuthenticationFailures(@Param("username") String username, 
                                                     @Param("since") LocalDateTime since);

    /**
     * Find recent authentication failures from an IP address
     */
    @Query("SELECT a FROM AuditEvent a WHERE a.ipAddress = :ipAddress " +
           "AND a.eventType IN ('AUTHENTICATION_FAILURE', 'BRUTE_FORCE_ATTEMPT') " +
           "AND a.timestamp >= :since ORDER BY a.timestamp DESC")
    List<AuditEvent> findRecentAuthenticationFailuresByIp(@Param("ipAddress") String ipAddress, 
                                                         @Param("since") LocalDateTime since);

    /**
     * Find security violations within a time period
     */
    @Query("SELECT a FROM AuditEvent a WHERE a.eventType IN ('SECURITY_VIOLATION', 'SUSPICIOUS_ACTIVITY', " +
           "'SQL_INJECTION_ATTEMPT', 'XSS_ATTEMPT') AND a.timestamp >= :since ORDER BY a.timestamp DESC")
    List<AuditEvent> findRecentSecurityViolations(@Param("since") LocalDateTime since);

    /**
     * Find high and critical severity events
     */
    @Query("SELECT a FROM AuditEvent a WHERE a.severity IN ('HIGH', 'CRITICAL') " +
           "AND a.timestamp >= :since ORDER BY a.timestamp DESC")
    List<AuditEvent> findHighSeverityEvents(@Param("since") LocalDateTime since);

    /**
     * Count events by type within a time period
     */
    @Query("SELECT COUNT(a) FROM AuditEvent a WHERE a.eventType = :eventType " +
           "AND a.timestamp BETWEEN :startTime AND :endTime")
    Long countByEventTypeAndTimestampBetween(@Param("eventType") AuditEvent.AuditEventType eventType,
                                           @Param("startTime") LocalDateTime startTime,
                                           @Param("endTime") LocalDateTime endTime);

    /**
     * Find events for a specific resource
     */
    @Query("SELECT a FROM AuditEvent a WHERE a.resourceType = :resourceType " +
           "AND a.resourceId = :resourceId ORDER BY a.timestamp DESC")
    List<AuditEvent> findByResource(@Param("resourceType") String resourceType, 
                                   @Param("resourceId") String resourceId);

    /**
     * Find data modification events for a user
     */
    @Query("SELECT a FROM AuditEvent a WHERE a.username = :username " +
           "AND a.eventType IN ('DATA_CREATE', 'DATA_UPDATE', 'DATA_DELETE') " +
           "ORDER BY a.timestamp DESC")
    Page<AuditEvent> findDataModificationsByUser(@Param("username") String username, Pageable pageable);

    /**
     * Find events by correlation ID (for tracking related events)
     */
    List<AuditEvent> findByCorrelationIdOrderByTimestampAsc(String correlationId);

    /**
     * Delete old audit events (for cleanup)
     */
    @Query("DELETE FROM AuditEvent a WHERE a.timestamp < :cutoffDate")
    void deleteOldEvents(@Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * Find suspicious activity patterns
     */
    @Query("SELECT a FROM AuditEvent a WHERE a.ipAddress = :ipAddress " +
           "AND a.eventType IN ('RATE_LIMIT_EXCEEDED', 'AUTHENTICATION_FAILURE', 'AUTHORIZATION_FAILURE') " +
           "AND a.timestamp >= :since " +
           "GROUP BY a.ipAddress HAVING COUNT(a) >= :threshold " +
           "ORDER BY a.timestamp DESC")
    List<AuditEvent> findSuspiciousActivityByIp(@Param("ipAddress") String ipAddress,
                                               @Param("since") LocalDateTime since,
                                               @Param("threshold") Long threshold);

    /**
     * Get audit statistics for dashboard
     */
    @Query("SELECT a.eventType, COUNT(a) FROM AuditEvent a " +
           "WHERE a.timestamp >= :since " +
           "GROUP BY a.eventType ORDER BY COUNT(a) DESC")
    List<Object[]> getEventTypeStatistics(@Param("since") LocalDateTime since);

    /**
     * Get severity statistics
     */
    @Query("SELECT a.severity, COUNT(a) FROM AuditEvent a " +
           "WHERE a.timestamp >= :since " +
           "GROUP BY a.severity ORDER BY a.severity")
    List<Object[]> getSeverityStatistics(@Param("since") LocalDateTime since);
}