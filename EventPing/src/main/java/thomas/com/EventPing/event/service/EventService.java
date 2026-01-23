package thomas.com.EventPing.event.service;

import thomas.com.EventPing.User.model.User;
import thomas.com.EventPing.event.dtos.CreateEventRequest;
import thomas.com.EventPing.event.dtos.EventResponseDto;

import java.util.List;

public interface EventService {
    EventResponseDto createEvent(User creator, CreateEventRequest request);
    EventResponseDto getEventBySlug(String slug);
    List<EventResponseDto> getUserEvents(User user);
    void markExpiredEvents();
}
