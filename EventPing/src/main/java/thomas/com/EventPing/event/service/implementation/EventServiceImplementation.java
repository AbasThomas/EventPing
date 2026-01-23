package thomas.com.EventPing.event.service.implementation;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import thomas.com.EventPing.User.model.User;
import thomas.com.EventPing.common.service.RateLimitService;
import thomas.com.EventPing.event.dtos.CreateEventRequest;
import thomas.com.EventPing.event.dtos.EventResponseDto;
import thomas.com.EventPing.event.mapper.EventMapper;
import thomas.com.EventPing.event.model.Event;
import thomas.com.EventPing.event.repository.EventRepository;
import thomas.com.EventPing.event.service.EventService;
import thomas.com.EventPing.participant.repository.ParticipantRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class EventServiceImplementation implements EventService {
    private final EventRepository eventRepository;
    private final ParticipantRepository participantRepository;
    private final EventMapper eventMapper;
    private final RateLimitService rateLimitService;

    @Override
    public EventResponseDto createEvent(User creator, CreateEventRequest request) {
        // Check rate limit
        if (!rateLimitService.canCreateEvent(creator)) {
            throw new RuntimeException("Event creation limit reached for today");
        }

        // Create event
        Event event = new Event();
        event.setTitle(request.getTitle());
        event.setDescription(request.getDescription());
        event.setEventDateTime(request.getEventDateTime());
        event.setStatus(Event.EventStatus.ACTIVE);
        event.setSlug(generateSlug());
        event.setCreator(creator);

        Event savedEvent = eventRepository.save(event);

        return toResponseDto(savedEvent);
    }

    @Override
    public EventResponseDto getEventBySlug(String slug) {
        Event event = eventRepository.findBySlug(slug)
                .orElseThrow(() -> new RuntimeException("Event not found"));
        return toResponseDto(event);
    }

    @Override
    public List<EventResponseDto> getUserEvents(User user) {
        return eventRepository.findByCreator(user).stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public void markExpiredEvents() {
        List<Event> expiredEvents = eventRepository.findByStatusAndEventDateTimeBefore(
                Event.EventStatus.ACTIVE,
                LocalDateTime.now()
        );

        for (Event event : expiredEvents) {
            event.setStatus(Event.EventStatus.EXPIRED);
        }

        eventRepository.saveAll(expiredEvents);
    }

    private String generateSlug() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    private EventResponseDto toResponseDto(Event event) {
        EventResponseDto dto = eventMapper.toEventResponseDto(event);
        dto.setParticipantCount(participantRepository.countByEvent(event));
        return dto;
    }
}
