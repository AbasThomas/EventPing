package thomas.com.EventPing.User.service;

import thomas.com.EventPing.User.model.User;

public interface UserService {
    User createUser(User user); // create a new user
    User getUserById(Long id); // get user by id
    User getUserByEmail(String email); // get user by email
    User updateUser(User user); // update user
    void deleteUser(Long id); // delete user
}
