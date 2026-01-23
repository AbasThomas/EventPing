package thomas.com.EventPing.event.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import thomas.com.EventPing.User.model.User;
import thomas.com.EventPing.User.repository.UserRepository;
import thomas.com.EventPing.event.dtos.CreateEventRequest;
import thomas.com.EventPing.event.dtos.EventResponseDto;
import thomas.com.EventPing.event.service.EventService;

import java.util.List;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {
    private final EventService eventService;
    private final UserRepository userRepository;

    @PostMapping
    public ResponseEntity<EventResponseDto> createEvent(
            @RequestParam Long userId,
            @Valid @RequestBody CreateEventRequest request) {
        User creator = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        EventResponseDto event = eventService.createEvent(creator, request);
        return ResponseEntity.ok(event);
    }

    @GetMapping("/{slug}")
    public ResponseEntity<EventResponseDto> getEvent(@PathVariable String slug) {
        EventResponseDto event = eventService.getEventBySlug(slug);
        return ResponseEntity.ok(event);
    }

    @GetMapping
    public ResponseEntity<List<EventResponseDto>> getUserEvents(@RequestParam Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        List<EventResponseDto> events = eventService.getUserEvents(user);
        return ResponseEntity.ok(events);
    }
}
