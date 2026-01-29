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
    private final thomas.com.EventPing.security.service.AuditLoggingService auditLoggingService;
    private final thomas.com.EventPing.event.repository.EventCustomFieldRepository customFieldRepository;
    private final thomas.com.EventPing.event.repository.EventIntegrationRepository integrationRepository;

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
        event.setRegistrationEnabled(request.getRegistrationEnabled() != null ? request.getRegistrationEnabled() : true);

        Event savedEvent = eventRepository.save(event);
        
        // Save custom fields
        if (request.getCustomFields() != null && !request.getCustomFields().isEmpty()) {
            for (thomas.com.EventPing.event.dtos.CustomFieldDto fieldDto : request.getCustomFields()) {
                thomas.com.EventPing.event.model.EventCustomField field = new thomas.com.EventPing.event.model.EventCustomField();
                field.setEvent(savedEvent);
                field.setFieldName(fieldDto.getFieldName());
                field.setFieldType(fieldDto.getFieldType());
                field.setRequired(fieldDto.isRequired());
                field.setPlaceholderText(fieldDto.getPlaceholderText());
                field.setFieldOptions(fieldDto.getFieldOptions());
                field.setDisplayOrder(fieldDto.getDisplayOrder());
                customFieldRepository.save(field);
            }
        }
        
        // Save integrations with plan validation
        if (request.getIntegrations() != null && !request.getIntegrations().isEmpty()) {
            validateAndSaveIntegrations(savedEvent, request.getIntegrations(), creator);
        }

        // Log event creation
        auditLoggingService.logDataModification(
                creator.getEmail(),
                thomas.com.EventPing.security.entity.AuditEvent.AuditEventType.DATA_CREATE,
                "Event",
                savedEvent.getId().toString(),
                null,
                savedEvent
        );

        return toResponseDto(savedEvent);
    }

    @Override
    public EventResponseDto getEventBySlug(String slug) {
        Event event = eventRepository.findBySlug(slug)
                .orElseThrow(() -> new RuntimeException("Event not found"));
        return toResponseDto(event);
    }

    @Override
    public EventResponseDto getEventById(Long id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Event not found"));
        return toResponseDto(event);
    }

    @Override
    public EventResponseDto updateEvent(Long id, CreateEventRequest request) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        // Store old values for audit logging
        Event oldEvent = new Event();
        oldEvent.setId(event.getId());
        oldEvent.setTitle(event.getTitle());
        oldEvent.setDescription(event.getDescription());
        oldEvent.setEventDateTime(event.getEventDateTime());

        // Update event fields
        event.setTitle(request.getTitle());
        event.setDescription(request.getDescription());
        event.setEventDateTime(request.getEventDateTime());

        Event savedEvent = eventRepository.save(event);

        // Log event modification
        auditLoggingService.logDataModification(
                event.getCreator().getEmail(),
                thomas.com.EventPing.security.entity.AuditEvent.AuditEventType.DATA_UPDATE,
                "Event",
                savedEvent.getId().toString(),
                oldEvent,
                savedEvent
        );

        return toResponseDto(savedEvent);
    }

    @Override
    public void deleteEvent(Long id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        // Log event deletion
        auditLoggingService.logDataModification(
                event.getCreator().getEmail(),
                thomas.com.EventPing.security.entity.AuditEvent.AuditEventType.DATA_DELETE,
                "Event",
                event.getId().toString(),
                event,
                null
        );

        eventRepository.deleteById(id);
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

    private void validateAndSaveIntegrations(Event event, List<String> integrationTypes, User creator) {
        // Get user's plan allowed integration channels
        String allowedChannels = creator.getPlan().getReminderChannels(); // e.g., "EMAIL,WHATSAPP"
        List<String> allowed = allowedChannels != null ? 
            List.of(allowedChannels.split(",")) : 
            List.of("EMAIL");
        
        for (String integrationType : integrationTypes) {
            // Validate integration is allowed by user's plan
            if (!allowed.contains(integrationType.toUpperCase())) {
                throw new RuntimeException("Integration " + integrationType + " is not available in your plan");
            }
            
            // Create and save integration
            thomas.com.EventPing.event.model.EventIntegration integration = new thomas.com.EventPing.event.model.EventIntegration();
            integration.setEvent(event);
            try {
                integration.setIntegrationType(
                    thomas.com.EventPing.event.model.EventIntegration.IntegrationType.valueOf(integrationType.toUpperCase())
                );
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Invalid integration type: " + integrationType);
            }
            integration.setActive(true);
            integration.setConfiguration(null); // No configuration needed for now
            integrationRepository.save(integration);
        }
    }

    @Override
    public List<thomas.com.EventPing.event.model.EventCustomField> getCustomFieldsByEventId(Long eventId) {
        return customFieldRepository.findByEventIdOrderByDisplayOrder(eventId);
    }

    private EventResponseDto toResponseDto(Event event) {
        EventResponseDto dto = eventMapper.toEventResponseDto(event);
        dto.setParticipantCount(participantRepository.countByEvent(event));
        return dto;
    }
}
