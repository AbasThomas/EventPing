package thomas.com.EventPing.User.service.implementation;

import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import thomas.com.EventPing.User.service.UserService;
import thomas.com.EventPing.User.repository.UserRepository;
import thomas.com.EventPing.User.mapper.UserMapper;
import thomas.com.EventPing.User.dtos.UserResponseDto;
import thomas.com.EventPing.User.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImplementation implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final thomas.com.EventPing.security.service.AuditLoggingService auditLoggingService;
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @Override
    public UserResponseDto registerUser(thomas.com.EventPing.User.dtos.RegisterRequest request) {
        log.info("Registering user with email: {}", request.getEmail());
        
        // Check if email already exists
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email already registered");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setFullName(request.getFullName());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(User.UserRole.USER);
        
        User savedUser = userRepository.save(user);
        
        // Log user registration
        auditLoggingService.logCustomEvent(
                thomas.com.EventPing.security.entity.AuditEvent.AuditEventType.USER_CREATED,
                savedUser.getEmail(),
                "REGISTER",
                "User",
                null,
                null,
                thomas.com.EventPing.security.entity.AuditEvent.AuditSeverity.LOW
        );
        
        return userMapper.toUserResponseDto(savedUser);
    }

    @Override
    public UserResponseDto createUser(thomas.com.EventPing.User.dtos.UserRequest request) {
        log.info("Creating user with email: {}", request.getEmail());
        User user = userMapper.toUser(request);
        User savedUser = userRepository.save(user);
        
        // Log user creation
        auditLoggingService.logDataModification(
                savedUser.getEmail(),
                thomas.com.EventPing.security.entity.AuditEvent.AuditEventType.DATA_CREATE,
                "User",
                savedUser.getId().toString(),
                null,
                savedUser
        );
        
        return userMapper.toUserResponseDto(savedUser);
    }

    @Override
    public UserResponseDto getUserById(Long id) {
        log.info("Fetching user with id: {}", id);
        User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
        return userMapper.toUserResponseDto(user);
    }

    @Override
    public UserResponseDto getUserByEmail(String email) {
        log.info("Fetching user with email: {}", email);
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
        return userMapper.toUserResponseDto(user);
    }

    @Override
    public List<UserResponseDto> getAllUsers() {
        log.info("Fetching all users");
        return userRepository.findAll().stream().map(userMapper::toUserResponseDto).collect(Collectors.toList());
    }

    @Override
    public UserResponseDto updateUser(Long id, thomas.com.EventPing.User.dtos.UserRequest request) {
        log.info("Updating user with id: {}", id);
        User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
        
        // Store old values for audit logging
        User oldUser = new User();
        oldUser.setId(user.getId());
        oldUser.setEmail(user.getEmail());
        oldUser.setFullName(user.getFullName());
        oldUser.setPhoneNumber(user.getPhoneNumber());
        
        // Manual update of fields since we don't have a partial update DTO/mapper method ideally
        user.setEmail(request.getEmail());
        user.setFullName(request.getFullName());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setUpdatedAt(LocalDateTime.now());
        
        User savedUser = userRepository.save(user);
        
        // Log user modification
        auditLoggingService.logDataModification(
                savedUser.getEmail(),
                thomas.com.EventPing.security.entity.AuditEvent.AuditEventType.DATA_UPDATE,
                "User",
                savedUser.getId().toString(),
                oldUser,
                savedUser
        );
        
        return userMapper.toUserResponseDto(savedUser);
    }

    @Override
    public void deleteUser(Long id) {
        log.info("Deleting user with id: {}", id);
        if(!userRepository.existsById(id)) {
             throw new RuntimeException("User not found");
        }
        
        // Get user details before deletion for audit logging
        User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
        
        userRepository.deleteById(id);
        
        // Log user deletion
        auditLoggingService.logDataModification(
                user.getEmail(),
                thomas.com.EventPing.security.entity.AuditEvent.AuditEventType.DATA_DELETE,
                "User",
                user.getId().toString(),
                user,
                null
        );
    }
}
