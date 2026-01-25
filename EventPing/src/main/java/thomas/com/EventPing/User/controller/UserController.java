package thomas.com.EventPing.User.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import thomas.com.EventPing.User.dtos.UserRequest;
import thomas.com.EventPing.User.dtos.UserResponseDto;
import thomas.com.EventPing.User.model.User;
import thomas.com.EventPing.User.service.UserService;
import thomas.com.EventPing.security.service.AuthorizationService;
import thomas.com.EventPing.security.service.AuditLoggingService;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    
    private final UserService userService;
    private final AuthorizationService authorizationService;
    private final AuditLoggingService auditLoggingService;

    @PostMapping("/register")
    public ResponseEntity<UserResponseDto> registerUser(
            @Valid @RequestBody UserRequest request,
            HttpServletRequest httpRequest) {
        
        // Log user registration attempt
        auditLoggingService.logCustomEvent(
                thomas.com.EventPing.security.entity.AuditEvent.AuditEventType.USER_CREATED,
                request.getEmail(),
                "REGISTER",
                "User",
                null,
                null,
                thomas.com.EventPing.security.entity.AuditEvent.AuditSeverity.LOW
        );
        
        UserResponseDto response = userService.createUser(request);
        
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.getId())
                .toUri();
        
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<UserResponseDto> getUser(
            @PathVariable Long id,
            Authentication authentication,
            HttpServletRequest request) {
        
        User currentUser = (User) authentication.getPrincipal();
        String clientIp = getClientIpAddress(request);
        
        // Check if user can access this resource
        if (!authorizationService.canAccessResource(currentUser, "user", id, clientIp)) {
            return ResponseEntity.status(403).build();
        }
        
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponseDto>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<UserResponseDto> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserRequest request,
            Authentication authentication,
            HttpServletRequest httpRequest) {
        
        User currentUser = (User) authentication.getPrincipal();
        String clientIp = getClientIpAddress(httpRequest);
        
        // Check if user can modify this resource
        if (!authorizationService.canModifyUser(currentUser, id, clientIp)) {
            return ResponseEntity.status(403).build();
        }
        
        return ResponseEntity.ok(userService.updateUser(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(
            @PathVariable Long id,
            Authentication authentication,
            HttpServletRequest request) {
        
        User currentUser = (User) authentication.getPrincipal();
        String clientIp = getClientIpAddress(request);
        
        // Check if user can delete this resource
        if (!authorizationService.canDeleteUser(currentUser, id, clientIp)) {
            return ResponseEntity.status(403).build();
        }
        
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get client IP address from request
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String[] headerNames = {
                "X-Forwarded-For",
                "X-Real-IP",
                "Proxy-Client-IP",
                "WL-Proxy-Client-IP",
                "HTTP_X_FORWARDED_FOR",
                "HTTP_X_FORWARDED",
                "HTTP_X_CLUSTER_CLIENT_IP",
                "HTTP_CLIENT_IP",
                "HTTP_FORWARDED_FOR",
                "HTTP_FORWARDED",
                "HTTP_VIA",
                "REMOTE_ADDR"
        };
        
        for (String header : headerNames) {
            String ip = request.getHeader(header);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                // Handle comma-separated IPs (take the first one)
                if (ip.contains(",")) {
                    ip = ip.split(",")[0].trim();
                }
                return ip;
            }
        }
        
        return request.getRemoteAddr();
    }
}
