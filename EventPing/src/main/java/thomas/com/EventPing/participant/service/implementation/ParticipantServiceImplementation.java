package thomas.com.EventPing.participant.service.implementation;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import thomas.com.EventPing.User.model.User;
import thomas.com.EventPing.common.service.RateLimitService;
import thomas.com.EventPing.event.model.Event;
import thomas.com.EventPing.event.repository.EventRepository;
import thomas.com.EventPing.participant.dtos.JoinEventRequest;
import thomas.com.EventPing.participant.dtos.ParticipantResponseDto;
import thomas.com.EventPing.participant.mapper.ParticipantMapper;
import thomas.com.EventPing.participant.model.Participant;
import thomas.com.EventPing.participant.repository.ParticipantRepository;
import thomas.com.EventPing.participant.service.ParticipantService;
import thomas.com.EventPing.reminder.model.Reminder;
import thomas.com.EventPing.reminder.repository.ReminderRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ParticipantServiceImplementation implements ParticipantService {
    private final ParticipantRepository participantRepository;
    private final EventRepository eventRepository;
    private final ReminderRepository reminderRepository;
    private final ParticipantMapper participantMapper;
    private final RateLimitService rateLimitService;

    @Override
    public ParticipantResponseDto joinEvent(String eventSlug, JoinEventRequest request, List<Long> reminderOffsetMinutes) {
        // Find event
        Event event = eventRepository.findBySlug(eventSlug)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        // Check if event is active
        if (event.getStatus() != Event.EventStatus.ACTIVE) {
            throw new RuntimeException("Event is no longer active");
        }

        // Check rate limit
        if (!rateLimitService.canAddParticipant(event)) {
            throw new RuntimeException("Event has reached maximum participant capacity");
        }

        // Check if already joined
        if (participantRepository.findByEventAndEmail(event, request.getEmail()).isPresent()) {
            throw new RuntimeException("Email already registered for this event");
        }

        // Create participant
        Participant participant = new Participant();
        participant.setEvent(event);
        participant.setEmail(request.getEmail());
        participant.setUnsubscribed(false);

        Participant savedParticipant = participantRepository.save(participant);

        // Get creator's plan for feature validation
        thomas.com.EventPing.plan.model.Plan plan = event.getCreator().getPlan();

        // Validate reminder intervals - only allowed if plan supports custom intervals
        List<Long> finalOffsets = reminderOffsetMinutes;
        if (finalOffsets != null && !finalOffsets.isEmpty() && (plan == null || !plan.isCustomIntervalsEnabled())) {
            // Force default offsets for FREE users (or if plan missing) if they try to customize
            finalOffsets = List.of(60L, 1440L);
        }

        // Create reminders for allowed channels
        if (finalOffsets != null && !finalOffsets.isEmpty()) {
            String[] allowedChannels = (plan != null ? plan.getReminderChannels() : "EMAIL").split(",");
            
            for (String channelStr : allowedChannels) {
                try {
                    Reminder.ReminderChannel channel = Reminder.ReminderChannel.valueOf(channelStr.trim());
                    
                    for (Long offsetMinutes : finalOffsets) {
                        Reminder reminder = new Reminder();
                        reminder.setEvent(event);
                        reminder.setParticipant(savedParticipant);
                        reminder.setSendAt(event.getEventDateTime().minusMinutes(offsetMinutes));
                        reminder.setChannel(channel);
                        reminder.setSent(false);
                        
                        reminderRepository.save(reminder);
                    }
                } catch (IllegalArgumentException e) {
                    // Log or ignore invalid channels in plan config
                }
            }
        }

        return participantMapper.toParticipantResponseDto(savedParticipant);
    }

    @Override
    public void unsubscribe(Long participantId) {
        Participant participant = participantRepository.findById(participantId)
                .orElseThrow(() -> new RuntimeException("Participant not found"));
        
        participant.setUnsubscribed(true);
        participantRepository.save(participant);
    }

    @Override
    public List<ParticipantResponseDto> getEventParticipants(String eventSlug) {
        Event event = eventRepository.findBySlug(eventSlug)
                .orElseThrow(() -> new RuntimeException("Event not found"));
        
        return participantRepository.findByEvent(event).stream()
                .map(participantMapper::toParticipantResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public void updateRsvp(Long participantId, Participant.RsvpStatus status) {
        Participant participant = participantRepository.findById(participantId)
                .orElseThrow(() -> new RuntimeException("Participant not found"));
        participant.setRsvpStatus(status);
        participantRepository.save(participant);
    }

    @Override
    public java.util.Map<Participant.RsvpStatus, Long> getRsvpSummary(String eventSlug) {
        Event event = eventRepository.findBySlug(eventSlug)
                .orElseThrow(() -> new RuntimeException("Event not found"));
        
        List<Participant> participants = participantRepository.findByEvent(event);
        return participants.stream()
                .collect(Collectors.groupingBy(Participant::getRsvpStatus, Collectors.counting()));
    }
}
