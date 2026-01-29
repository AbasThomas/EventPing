package thomas.com.EventPing.participant.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import thomas.com.EventPing.participant.model.RegistrationResponse;

import java.util.List;

@Repository
public interface RegistrationResponseRepository extends JpaRepository<RegistrationResponse, Long> {
    List<RegistrationResponse> findByParticipantId(Long participantId);
}
