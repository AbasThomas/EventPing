package thomas.com.EventPing.reminder.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import thomas.com.EventPing.reminder.dtos.ReminderResponseDto;
import thomas.com.EventPing.reminder.model.Reminder;

@Mapper(componentModel = "spring")
public interface ReminderMapper {
    @Mapping(source = "event.id", target = "eventId")
    @Mapping(source = "participant.id", target = "participantId")
    ReminderResponseDto toReminderResponseDto(Reminder reminder);
}
