package thomas.com.EventPing.participant.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import thomas.com.EventPing.participant.dtos.JoinEventRequest;
import thomas.com.EventPing.participant.dtos.ParticipantResponseDto;
import thomas.com.EventPing.participant.service.ParticipantService;
import thomas.com.EventPing.security.service.RateLimitingService;
import thomas.com.EventPing.security.service.AuditLoggingService;

import java.util.List;

@RestController
@RequestMapping("/api/participants")
@RequiredArgsConstructor
public class ParticipantController {
    
    private final ParticipantService participantService;
    private final RateLimitingService rateLimitingService;
    private final AuditLoggingService auditLoggingService;

    @PostMapping("/events/{slug}/join")
    public ResponseEntity<ParticipantResponseDto> joinEvent(
            @PathVariable String slug,
            @Valid @RequestBody JoinEventRequest request,
            @RequestParam(required = false) List<Long> reminderOffsetMinutes,
            HttpServletRequest httpRequest) {
        
        String clientIp = getClientIpAddress(httpRequest);
        
        // Apply rate limiting to prevent spam registrations
        var rateLimitResult = rateLimitingService.checkIpRateLimit(clientIp);
        if (!rateLimitResult.isAllowed()) {
            // Log rate limit violation
            auditLoggingService.logSecurityViolation(
                    null,
                    clientIp,
                    "PARTICIPANT_REGISTRATION_RATE_LIMIT",
                    "Rate limit exceeded for participant registration",
                    thomas.com.EventPing.security.entity.AuditEvent.AuditSeverity.MEDIUM
            );
            
            return ResponseEntity.status(429)
                    .header("Retry-After", String.valueOf(rateLimitResult.getRetryAfterSeconds()))
                    .build();
        }
        
        // Validate email format (additional security layer)
        if (!isValidEmail(request.getEmail())) {
            auditLoggingService.logSecurityViolation(
                    null,
                    clientIp,
                    "INVALID_EMAIL_FORMAT",
                    "Invalid email format in participant registration: " + request.getEmail(),
                    thomas.com.EventPing.security.entity.AuditEvent.AuditSeverity.LOW
            );
            
            return ResponseEntity.badRequest().build();
        }
        
        // Default reminder offsets: 1 hour and 1 day before
        if (reminderOffsetMinutes == null || reminderOffsetMinutes.isEmpty()) {
            reminderOffsetMinutes = List.of(60L, 1440L);
        }
        
        // Log participant registration attempt
        auditLoggingService.logCustomEvent(
                thomas.com.EventPing.security.entity.AuditEvent.AuditEventType.DATA_CREATE,
                request.getEmail(),
                "JOIN_EVENT",
                "Participant",
                slug,
                null,
                thomas.com.EventPing.security.entity.AuditEvent.AuditSeverity.LOW
        );
        
        ParticipantResponseDto participant = participantService.joinEvent(slug, request, reminderOffsetMinutes);
        return ResponseEntity.ok(participant);
    }

    @PostMapping("/{id}/unsubscribe")
    public ResponseEntity<Void> unsubscribe(
            @PathVariable Long id,
            HttpServletRequest request) {
        
        String clientIp = getClientIpAddress(request);
        
        // Apply rate limiting to prevent abuse
        var rateLimitResult = rateLimitingService.checkIpRateLimit(clientIp);
        if (!rateLimitResult.isAllowed()) {
            return ResponseEntity.status(429)
                    .header("Retry-After", String.valueOf(rateLimitResult.getRetryAfterSeconds()))
                    .build();
        }
        
        // Log unsubscribe attempt
        auditLoggingService.logCustomEvent(
                thomas.com.EventPing.security.entity.AuditEvent.AuditEventType.DATA_DELETE,
                null,
                "UNSUBSCRIBE",
                "Participant",
                id.toString(),
                null,
                thomas.com.EventPing.security.entity.AuditEvent.AuditSeverity.LOW
        );
        
        participantService.unsubscribe(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/events/{slug}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<List<ParticipantResponseDto>> getEventParticipants(@PathVariable String slug) {
        // Only authenticated users can view participant lists
        List<ParticipantResponseDto> participants = participantService.getEventParticipants(slug);
        return ResponseEntity.ok(participants);
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

    /**
     * Basic email validation
     */
    private boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        
        // Basic email regex pattern
        String emailPattern = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
        return email.matches(emailPattern);
    }
}
