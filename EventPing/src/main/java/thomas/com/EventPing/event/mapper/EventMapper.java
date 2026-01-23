package thomas.com.EventPing.event.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import thomas.com.EventPing.event.dtos.EventResponseDto;
import thomas.com.EventPing.event.model.Event;

@Mapper(componentModel = "spring")
public interface EventMapper {
    @Mapping(source = "creator.id", target = "creatorId")
    @Mapping(source = "creator.email", target = "creatorEmail")
    @Mapping(target = "participantCount", ignore = true)
    EventResponseDto toEventResponseDto(Event event);
}
