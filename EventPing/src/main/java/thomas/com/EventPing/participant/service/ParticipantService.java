package thomas.com.EventPing.participant.service;

import thomas.com.EventPing.participant.dtos.JoinEventRequest;
import thomas.com.EventPing.participant.dtos.ParticipantResponseDto;

import java.util.List;

public interface ParticipantService {
    ParticipantResponseDto joinEvent(String eventSlug, JoinEventRequest request, List<Long> reminderOffsetMinutes);
    void unsubscribe(Long participantId);
    List<ParticipantResponseDto> getEventParticipants(String eventSlug);
    void updateRsvp(Long participantId, thomas.com.EventPing.participant.model.Participant.RsvpStatus status);
    java.util.Map<thomas.com.EventPing.participant.model.Participant.RsvpStatus, Long> getRsvpSummary(String eventSlug);
}
