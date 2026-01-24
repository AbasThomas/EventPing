package thomas.com.EventPing.security.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import thomas.com.EventPing.security.model.RateLimitTracking;
import thomas.com.EventPing.security.model.RateLimitType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for RateLimitTracking entity
 * **Validates: Requirements 3.1, 3.2, 3.4**
 */
@Repository
public interface RateLimitTrackingRepository extends JpaRepository<RateLimitTracking, Long> {

    /**
     * Find rate limit tracking by identifier and type
     */
    Optional<RateLimitTracking> findByIdentifierAndLimitType(String identifier, RateLimitType limitType);

    /**
     * Find all blocked identifiers of a specific type
     */
    @Query("SELECT r FROM RateLimitTracking r WHERE r.limitType = :limitType AND r.blocked = true AND (r.blockExpiresAt IS NULL OR r.blockExpiresAt > :now)")
    List<RateLimitTracking> findBlockedByType(@Param("limitType") RateLimitType limitType, @Param("now") LocalDateTime now);

    /**
     * Find all rate limit records that need window reset
     */
    @Query("SELECT r FROM RateLimitTracking r WHERE r.windowStart < :cutoffTime")
    List<RateLimitTracking> findExpiredWindows(@Param("cutoffTime") LocalDateTime cutoffTime);

    /**
     * Clean up old rate limit records
     */
    @Modifying
    @Query("DELETE FROM RateLimitTracking r WHERE r.lastUpdated < :cutoffTime AND r.blocked = false")
    int deleteOldRecords(@Param("cutoffTime") LocalDateTime cutoffTime);

    /**
     * Find rate limit records by identifier pattern (for IP subnet blocking)
     */
    @Query("SELECT r FROM RateLimitTracking r WHERE r.identifier LIKE :pattern AND r.limitType = :limitType")
    List<RateLimitTracking> findByIdentifierPattern(@Param("pattern") String pattern, @Param("limitType") RateLimitType limitType);

    /**
     * Count active rate limit violations in the last period
     */
    @Query("SELECT COUNT(r) FROM RateLimitTracking r WHERE r.limitType = :limitType AND r.violationCount > 0 AND r.lastUpdated > :since")
    long countViolationsSince(@Param("limitType") RateLimitType limitType, @Param("since") LocalDateTime since);

    /**
     * Find top violators by violation count
     */
    @Query("SELECT r FROM RateLimitTracking r WHERE r.limitType = :limitType AND r.violationCount > :minViolations ORDER BY r.violationCount DESC")
    List<RateLimitTracking> findTopViolators(@Param("limitType") RateLimitType limitType, @Param("minViolations") int minViolations);

    /**
     * Update block status for an identifier
     */
    @Modifying
    @Query("UPDATE RateLimitTracking r SET r.blocked = :blocked, r.blockExpiresAt = :expiresAt, r.lastUpdated = :now WHERE r.identifier = :identifier AND r.limitType = :limitType")
    int updateBlockStatus(@Param("identifier") String identifier, @Param("limitType") RateLimitType limitType, 
                         @Param("blocked") boolean blocked, @Param("expiresAt") LocalDateTime expiresAt, @Param("now") LocalDateTime now);

    /**
     * Find rate limits by plan type
     */
    @Query("SELECT r FROM RateLimitTracking r WHERE r.planType = :planType AND r.limitType = :limitType")
    List<RateLimitTracking> findByPlanTypeAndLimitType(@Param("planType") String planType, @Param("limitType") RateLimitType limitType);
}