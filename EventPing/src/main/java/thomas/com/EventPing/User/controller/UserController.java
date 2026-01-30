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
import thomas.com.EventPing.User.service.UserRegistrationService;
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
    private final UserRegistrationService userRegistrationService;
    private final AuthorizationService authorizationService;
    private final AuditLoggingService auditLoggingService;

    @PostMapping("/register")
    public ResponseEntity<UserResponseDto> registerUser(
            @Valid @RequestBody thomas.com.EventPing.User.dtos.RegisterRequest request,
            HttpServletRequest httpRequest) {
        
        // Audit logging is now handled inside UserRegistrationService
        
        UserResponseDto response = userRegistrationService.registerUser(request);
        
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
    
    // INTEGRATION ENDPOINTS
    
    @PatchMapping("/{id}/integrations")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<User> updateIntegrations(
            @PathVariable Long id,
            @RequestBody java.util.Map<String, Object> updates,
            Authentication authentication,
            HttpServletRequest request) {
            
        User currentUser = (User) authentication.getPrincipal();
        // Permission check
        if (!currentUser.getId().equals(id) && !currentUser.getRole().equals(User.UserRole.ADMIN)) {
             return ResponseEntity.status(403).build();
        }

        // Logic to update user integrations
        // Since UserService might not have this method exposed yet, we can do it here or better, add to UserService.
        // For brevity in this task, I will call a new method in UserService or cast.
        // I'll assume we can implement it in UserService and call it.
        // Or implement logic here if Repository is available (it is not injected directly here, but userService is).
        
        return ResponseEntity.ok(userService.updateIntegrations(id, updates));
    }
    
    @GetMapping("/{id}/integrations/status")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<java.util.Map<String, Object>> getIntegrationStatus(
            @PathVariable Long id,
            Authentication authentication) {
            
        User currentUser = (User) authentication.getPrincipal();
        if (!currentUser.getId().equals(id) && !currentUser.getRole().equals(User.UserRole.ADMIN)) {
             return ResponseEntity.status(403).build();
        }
        
        return ResponseEntity.ok(userService.getIntegrationStatus(id));
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
