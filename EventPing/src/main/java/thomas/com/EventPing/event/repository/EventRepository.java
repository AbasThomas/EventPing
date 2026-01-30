package thomas.com.EventPing.event.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import thomas.com.EventPing.User.model.User;
import thomas.com.EventPing.event.model.Event;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long> {
    Optional<Event> findBySlug(String slug);
    
    List<Event> findByCreator(User creator);
    
    @Query("SELECT COUNT(e) FROM Event e WHERE e.creator = :creator AND e.createdAt >= :since")
    long countByCreatorAndCreatedAtAfter(@Param("creator") User creator, @Param("since") LocalDateTime since);
    
    List<Event> findByStatusAndEventDateTimeBefore(Event.EventStatus status, LocalDateTime dateTime);
    
    @Query("SELECT DISTINCT e FROM Event e JOIN e.reminderTimes rt WHERE rt BETWEEN :start AND :end AND e.status = 'ACTIVE'")
    List<Event> findEventsWithRemindersBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
