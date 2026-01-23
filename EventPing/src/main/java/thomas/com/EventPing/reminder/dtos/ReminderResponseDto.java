package thomas.com.EventPing.reminder.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import thomas.com.EventPing.reminder.model.Reminder;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReminderResponseDto {
    private Long id;
    private Long eventId;
    private Long participantId;
    private LocalDateTime sendAt;
    private Reminder.ReminderChannel channel;
    private Boolean sent;
    private LocalDateTime sentAt;
}
