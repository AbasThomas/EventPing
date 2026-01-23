package thomas.com.EventPing.User.service;

import java.util.List;

import thomas.com.EventPing.User.dtos.UserResponseDto;
import thomas.com.EventPing.User.model.User;

public interface UserService {
    UserResponseDto createUser(User user); // create a new user
    UserResponseDto getUserById(Long id); // get user by id
    UserResponseDto getUserByEmail(String email); // get user by email
    List<UserResponseDto> getAllUsers(); // get all users
    UserResponseDto updateUser(User user); // update user
    void deleteUser(Long id); // delete user
}
