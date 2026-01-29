package thomas.com.EventPing.event.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import thomas.com.EventPing.User.model.User;
import thomas.com.EventPing.event.dtos.CreateEventRequest;
import thomas.com.EventPing.event.dtos.EventResponseDto;
import thomas.com.EventPing.event.service.EventService;
import thomas.com.EventPing.security.service.AuthorizationService;
import thomas.com.EventPing.security.service.RateLimitingService;

import java.util.List;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {
    
    private final EventService eventService;
    private final AuthorizationService authorizationService;
    private final RateLimitingService rateLimitingService;

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<EventResponseDto> createEvent(
            @Valid @RequestBody CreateEventRequest request,
            Authentication authentication,
            HttpServletRequest httpRequest) {
        
        User user = (User) authentication.getPrincipal();
        String clientIp = getClientIpAddress(httpRequest);
        
        // Check rate limit for event creation
        var rateLimitResult = rateLimitingService.checkUserRateLimit(user, "event_creation");
        if (!rateLimitResult.isAllowed()) {
            return ResponseEntity.status(429)
                    .header("Retry-After", String.valueOf(rateLimitResult.getRetryAfterSeconds()))
                    .build();
        }
        
        EventResponseDto event = eventService.createEvent(user, request);
        return ResponseEntity.ok(event);
    }

    @GetMapping("/{slug}")
    public ResponseEntity<EventResponseDto> getEvent(@PathVariable String slug) {
        // Public endpoint - no authentication required for viewing events
        EventResponseDto event = eventService.getEventBySlug(slug);
        return ResponseEntity.ok(event);
    }

    @GetMapping("/{id}/details")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<EventResponseDto> getEventById(
            @PathVariable Long id,
            Authentication authentication,
            HttpServletRequest request) {
        
        User currentUser = (User) authentication.getPrincipal();
        String clientIp = getClientIpAddress(request);
        
        // Check if user can access this event
        if (!authorizationService.canAccessResource(currentUser, "event", id, clientIp)) {
            return ResponseEntity.status(403).build();
        }
        
        return ResponseEntity.ok(eventService.getEventById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<EventResponseDto> updateEvent(
            @PathVariable Long id,
            @Valid @RequestBody CreateEventRequest request,
            Authentication authentication,
            HttpServletRequest httpRequest) {
        
        User currentUser = (User) authentication.getPrincipal();
        String clientIp = getClientIpAddress(httpRequest);
        
        // Check if user can modify this event
        if (!authorizationService.canModifyEvent(currentUser, id, clientIp)) {
            return ResponseEntity.status(403).build();
        }
        
        return ResponseEntity.ok(eventService.updateEvent(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> deleteEvent(
            @PathVariable Long id,
            Authentication authentication,
            HttpServletRequest request) {
        
        User currentUser = (User) authentication.getPrincipal();
        String clientIp = getClientIpAddress(request);
        
        // Check if user can delete this event
        if (!authorizationService.canDeleteEvent(currentUser, id, clientIp)) {
            return ResponseEntity.status(403).build();
        }
        
        eventService.deleteEvent(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<EventResponseDto>> getUserEvents(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        List<EventResponseDto> events = eventService.getUserEvents(user);
        return ResponseEntity.ok(events);
    }

    @GetMapping("/{eventId}/custom-fields")
    public ResponseEntity<List<thomas.com.EventPing.event.model.EventCustomField>> getEventCustomFields(@PathVariable Long eventId) {
        // Public endpoint - no authentication required
        List<thomas.com.EventPing.event.model.EventCustomField> fields = 
            eventService.getCustomFieldsByEventId(eventId);
        return ResponseEntity.ok(fields);
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
