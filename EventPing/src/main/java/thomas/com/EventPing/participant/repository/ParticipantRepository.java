package thomas.com.EventPing.participant.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import thomas.com.EventPing.event.model.Event;
import thomas.com.EventPing.participant.model.Participant;

import java.util.List;
import java.util.Optional;

public interface ParticipantRepository extends JpaRepository<Participant, Long> {
    List<Participant> findByEvent(Event event);
    
    Optional<Participant> findByEventAndEmail(Event event, String email);
    
    long countByEvent(Event event);
}
