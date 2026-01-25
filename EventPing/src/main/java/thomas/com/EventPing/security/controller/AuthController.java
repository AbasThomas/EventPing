package thomas.com.EventPing.security.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import thomas.com.EventPing.User.dtos.UserResponseDto;
import thomas.com.EventPing.User.mapper.UserMapper;
import thomas.com.EventPing.User.model.User;
import thomas.com.EventPing.User.repository.UserRepository;
import thomas.com.EventPing.security.dto.AuthenticationResponse;
import thomas.com.EventPing.security.dto.JwtToken;
import thomas.com.EventPing.security.dto.LoginRequest;
import thomas.com.EventPing.security.service.AuditLoggingService;
import thomas.com.EventPing.security.service.JwtAuthenticationService;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtAuthenticationService jwtAuthenticationService;
    private final UserMapper userMapper;
    private final AuditLoggingService auditLoggingService;

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login attempt for user: {}", request.getEmail());

        // Find user
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> {
                    auditLoggingService.logAuthenticationFailure(request.getEmail(), "UNKNOWN_IP", "User not found");
                    return new RuntimeException("Invalid credentials");
                });

        // Verify password
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            auditLoggingService.logAuthenticationFailure(request.getEmail(), "UNKNOWN_IP", "Invalid password");
            throw new RuntimeException("Invalid credentials");
        }

        // Check if account is locked
        if (user.getAccountLocked()) {
            auditLoggingService.logAuthenticationFailure(request.getEmail(), "UNKNOWN_IP", "Account locked");
            throw new RuntimeException("Account is locked");
        }

        // Generate token
        JwtToken token = jwtAuthenticationService.generateToken(user);
        
        // Log success
        auditLoggingService.logAuthenticationSuccess(user.getEmail(), "UNKNOWN_IP");

        UserResponseDto userDto = userMapper.toUserResponseDto(user);
        
        return ResponseEntity.ok(AuthenticationResponse.success(token, userDto));
    }
}
