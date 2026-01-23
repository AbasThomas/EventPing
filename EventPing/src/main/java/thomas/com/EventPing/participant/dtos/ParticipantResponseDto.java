package thomas.com.EventPing.participant.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParticipantResponseDto {
    private Long id;
    private Long eventId;
    private String email;
    private LocalDateTime joinedAt;
    private Boolean unsubscribed;
}
