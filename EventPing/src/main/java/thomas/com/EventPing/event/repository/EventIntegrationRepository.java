package thomas.com.EventPing.event.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import thomas.com.EventPing.event.model.EventIntegration;

import java.util.List;

@Repository
public interface EventIntegrationRepository extends JpaRepository<EventIntegration, Long> {
    List<EventIntegration> findByEventId(Long eventId);
}
