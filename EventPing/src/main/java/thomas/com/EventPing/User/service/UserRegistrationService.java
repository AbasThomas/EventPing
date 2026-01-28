package thomas.com.EventPing.User.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import thomas.com.EventPing.User.dtos.RegisterRequest;
import thomas.com.EventPing.User.dtos.UserResponseDto;
import thomas.com.EventPing.User.exception.UserAlreadyExistsException;
import thomas.com.EventPing.User.mapper.UserMapper;
import thomas.com.EventPing.User.model.User;
import thomas.com.EventPing.User.repository.UserRepository;
import thomas.com.EventPing.security.service.AuditLoggingService;
import thomas.com.EventPing.security.entity.AuditEvent;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserRegistrationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final AuditLoggingService auditLoggingService;
    private final thomas.com.EventPing.plan.repository.PlanRepository planRepository;

    @Transactional
    public UserResponseDto registerUser(RegisterRequest request) {
        log.info("Attempting to register user with email: {}", request.getEmail());

        // Validate email uniqueness
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            log.warn("Registration failed: Email {} already exists", request.getEmail());
            throw new UserAlreadyExistsException("Email already registered");
        }

        try {
            User user = new User();
            user.setEmail(request.getEmail());
            user.setFullName(request.getFullName());
            user.setPhoneNumber(request.getPhoneNumber());
            user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
            user.setRole(User.UserRole.USER);
            
            // Assign default FREE plan
            thomas.com.EventPing.plan.model.Plan freePlan = planRepository.findByName(thomas.com.EventPing.plan.model.Plan.PlanName.FREE)
                    .orElseThrow(() -> new RuntimeException("Default FREE plan not found"));
            user.setPlan(freePlan);
            
            // Set defaults
            user.setAccountLocked(false);
            user.setFailedLoginAttempts(0);
            
            User savedUser = userRepository.save(user);
            log.info("Successfully registered user with ID: {}", savedUser.getId());
            
            // Log user registration
            auditLoggingService.logCustomEvent(
                    AuditEvent.AuditEventType.USER_CREATED,
                    savedUser.getEmail(),
                    "REGISTER",
                    "User",
                    null,
                    null,
                    AuditEvent.AuditSeverity.LOW
            );
            
            return userMapper.toUserResponseDto(savedUser);
            
        } catch (Exception e) {
            log.error("Error registering user: {}", request.getEmail(), e);
            throw new RuntimeException("Registration failed", e);
        }
    }
}
