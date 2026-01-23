package thomas.com.EventPing.User.mapper;

import org.mapstruct.Mapper;
import thomas.com.EventPing.User.dtos.UserResponseDto;
import thomas.com.EventPing.User.model.User;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserResponseDto toUserResponseDto(User user);
}
