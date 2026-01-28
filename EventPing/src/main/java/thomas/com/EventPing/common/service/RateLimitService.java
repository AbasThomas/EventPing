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
        // Get user's plan
        Plan plan = user.getPlan();
        if (plan == null) {
            plan = planRepository.findByName(Plan.PlanName.FREE)
                    .orElseThrow(() -> new RuntimeException("FREE plan not found"));
        }

        // Unlimited if maxEventsPerDay is null
        if (plan.getMaxEventsPerDay() == null) {
            return true;
        }

        // Count events created today
        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        long eventsToday = eventRepository.countByCreatorAndCreatedAtAfter(user, startOfDay);

        return eventsToday < plan.getMaxEventsPerDay();
    }

    public boolean canAddParticipant(Event event) {
        // Get event creator's plan
        User creator = event.getCreator();
        Plan plan = creator.getPlan();
        if (plan == null) {
            plan = planRepository.findByName(Plan.PlanName.FREE)
                    .orElseThrow(() -> new RuntimeException("FREE plan not found"));
        }

        // Unlimited if maxParticipantsPerEvent is null
        if (plan.getMaxParticipantsPerEvent() == null) {
            return true;
        }

        long currentParticipants = participantRepository.countByEvent(event);

        return currentParticipants < plan.getMaxParticipantsPerEvent();
    }

    public boolean canAddTeamMember(User owner) {
        Plan plan = owner.getPlan();
        if (plan == null) {
            plan = planRepository.findByName(Plan.PlanName.FREE)
                    .orElseThrow(() -> new RuntimeException("FREE plan not found"));
        }

        // We need a repository for team members, but for now let's assume one
        // Long currentTeamMembers = teamMemberRepository.countByOwner(owner);
        // return currentTeamMembers < plan.getMaxTeamMembers();
        return plan.getMaxTeamMembers() > 0; // Simplified for now
    }

    public boolean hasCredits(User user) {
        Plan plan = user.getPlan();
        if (plan == null) return false;
        
        // Unlimited credits if monthlyCreditLimit is null
        if (plan.getMonthlyCreditLimit() == null) {
            return true;
        }

        return user.getMonthlyCreditsUsed() < plan.getMonthlyCreditLimit();
    }
}
