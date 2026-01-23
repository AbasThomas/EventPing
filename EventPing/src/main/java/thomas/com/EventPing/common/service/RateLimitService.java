package thomas.com.EventPing.common.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import thomas.com.EventPing.User.model.User;
import thomas.com.EventPing.event.model.Event;
import thomas.com.EventPing.event.repository.EventRepository;
import thomas.com.EventPing.participant.repository.ParticipantRepository;
import thomas.com.EventPing.plan.model.Plan;
import thomas.com.EventPing.plan.repository.PlanRepository;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class RateLimitService {
    private final EventRepository eventRepository;
    private final ParticipantRepository participantRepository;
    private final PlanRepository planRepository;

    public boolean canCreateEvent(User user) {
        // Get user's plan (default to FREE if not set)
        Plan plan = planRepository.findByName(Plan.PlanName.FREE)
                .orElseThrow(() -> new RuntimeException("FREE plan not found"));

        // Count events created today
        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        long eventsToday = eventRepository.countByCreatorAndCreatedAtAfter(user, startOfDay);

        return eventsToday < plan.getMaxEventsPerDay();
    }

    public boolean canAddParticipant(Event event) {
        // Get FREE plan limits (for now all events use FREE plan limits)
        Plan plan = planRepository.findByName(Plan.PlanName.FREE)
                .orElseThrow(() -> new RuntimeException("FREE plan not found"));

        long currentParticipants = participantRepository.countByEvent(event);

        return currentParticipants < plan.getMaxParticipantsPerEvent();
    }
}
