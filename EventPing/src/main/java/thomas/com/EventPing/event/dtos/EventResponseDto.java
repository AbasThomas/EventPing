package thomas.com.EventPing.event.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import thomas.com.EventPing.event.model.Event;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventResponseDto {
    private Long id;
    private String title;
    private String description;
    private LocalDateTime eventDateTime;
    private Event.EventStatus status;
    private String slug;
    private Long creatorId;
    private String creatorEmail;
    private LocalDateTime createdAt;
    private Long participantCount;
}
