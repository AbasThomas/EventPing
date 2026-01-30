package thomas.com.EventPing.User.service;

import java.util.List;

import thomas.com.EventPing.User.dtos.UserResponseDto;


public interface UserService {
    UserResponseDto createUser(thomas.com.EventPing.User.dtos.UserRequest request); // create a new user from request
    UserResponseDto registerUser(thomas.com.EventPing.User.dtos.RegisterRequest request); // register a new user
    UserResponseDto getUserById(Long id); // get user by id
    UserResponseDto getUserByEmail(String email); // get user by email
    List<UserResponseDto> getAllUsers(); // get all users
    UserResponseDto updateUser(Long id, thomas.com.EventPing.User.dtos.UserRequest request); // update user
    void deleteUser(Long id); // delete user
    
    // Integration methods
    thomas.com.EventPing.User.model.User updateIntegrations(Long id, java.util.Map<String, Object> updates);
    java.util.Map<String, Object> getIntegrationStatus(Long id);
}
