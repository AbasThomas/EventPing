package thomas.com.EventPing.participant.service.implementation;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
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

        // Create reminders
        if (reminderOffsetMinutes != null && !reminderOffsetMinutes.isEmpty()) {
            for (Long offsetMinutes : reminderOffsetMinutes) {
                Reminder reminder = new Reminder();
                reminder.setEvent(event);
                reminder.setParticipant(savedParticipant);
                reminder.setSendAt(event.getEventDateTime().minusMinutes(offsetMinutes));
                reminder.setChannel(Reminder.ReminderChannel.EMAIL);
                reminder.setSent(false);
                
                reminderRepository.save(reminder);
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
}
