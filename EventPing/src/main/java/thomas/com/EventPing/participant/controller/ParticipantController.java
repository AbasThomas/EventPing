package thomas.com.EventPing.participant.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import thomas.com.EventPing.participant.dtos.JoinEventRequest;
import thomas.com.EventPing.participant.dtos.ParticipantResponseDto;
import thomas.com.EventPing.participant.service.ParticipantService;

import java.util.List;

@RestController
@RequestMapping("/api/participants")
@RequiredArgsConstructor
public class ParticipantController {
    private final ParticipantService participantService;

    @PostMapping("/events/{slug}/join")
    public ResponseEntity<ParticipantResponseDto> joinEvent(
            @PathVariable String slug,
            @Valid @RequestBody JoinEventRequest request,
            @RequestParam(required = false) List<Long> reminderOffsetMinutes) {
        
        // Default reminder offsets: 1 hour and 1 day before
        if (reminderOffsetMinutes == null || reminderOffsetMinutes.isEmpty()) {
            reminderOffsetMinutes = List.of(60L, 1440L);
        }
        
        ParticipantResponseDto participant = participantService.joinEvent(slug, request, reminderOffsetMinutes);
        return ResponseEntity.ok(participant);
    }

    @PostMapping("/{id}/unsubscribe")
    public ResponseEntity<Void> unsubscribe(@PathVariable Long id) {
        participantService.unsubscribe(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/events/{slug}")
    public ResponseEntity<List<ParticipantResponseDto>> getEventParticipants(@PathVariable String slug) {
        List<ParticipantResponseDto> participants = participantService.getEventParticipants(slug);
        return ResponseEntity.ok(participants);
    }
}
