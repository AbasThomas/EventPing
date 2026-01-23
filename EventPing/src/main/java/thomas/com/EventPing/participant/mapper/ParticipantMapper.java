package thomas.com.EventPing.participant.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import thomas.com.EventPing.participant.dtos.ParticipantResponseDto;
import thomas.com.EventPing.participant.model.Participant;

@Mapper(componentModel = "spring")
public interface ParticipantMapper {
    @Mapping(source = "event.id", target = "eventId")
    ParticipantResponseDto toParticipantResponseDto(Participant participant);
}
