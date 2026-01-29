package thomas.com.EventPing.event.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import thomas.com.EventPing.event.model.EventCustomField;

import java.util.List;

@Repository
public interface EventCustomFieldRepository extends JpaRepository<EventCustomField, Long> {
    List<EventCustomField> findByEventIdOrderByDisplayOrder(Long eventId);
}
