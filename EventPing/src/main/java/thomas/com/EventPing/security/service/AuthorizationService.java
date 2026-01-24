package thomas.com.EventPing.security.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import thomas.com.EventPing.User.model.User;
import thomas.com.EventPing.event.model.Event;
import thomas.com.EventPing.event.repository.EventRepository;
import thomas.com.EventPing.User.repository.UserRepository;

import java.util.Optional;

/**
 * Service for handling authorization and access control
 * Implements resource ownership validation and role-based permission checking
 * **Validates: Requirements 1.5**
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthorizationService {
    
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    /**
     * Check if user can access a specific resource
     * @param user The user requesting access
     * @param resourceType The type of resource (e.g., "event", "user")
     * @param resourceId The ID of the resource
     * @return true if access is allowed, false otherwise
     */
    public boolean canAccessResource(User user, String resourceType, Long resourceId) {
        if (user == null || resourceId == null) {
            log.warn("Authorization check failed: null user or resource ID");
            return false;
        }

        // Admins can access all resources
        if (user.getRole() == User.UserRole.ADMIN) {
            log.debug("Admin user {} granted access to {} {}", user.getEmail(), resourceType, resourceId);
            return true;
        }

        return switch (resourceType.toLowerCase()) {
            case "event" -> canAccessEvent(user, resourceId);
            case "user" -> canAccessUser(user, resourceId);
            default -> {
                log.warn("Unknown resource type: {}", resourceType);
                yield false;
            }
        };
    }

    /**
     * Validate user permissions for specific actions
     * @param user The user requesting permission
     * @param permission The permission being requested
     * @param target The target object (optional)
     * @return true if permission is granted, false otherwise
     */
    public boolean hasPermission(User user, String permission, Object target) {
        if (user == null || permission == null) {
            log.warn("Permission check failed: null user or permission");
            return false;
        }

        // Check account status
        if (user.getAccountLocked()) {
            log.warn("Permission denied for locked account: {}", user.getEmail());
            return false;
        }

        return switch (permission.toLowerCase()) {
            case "read" -> hasReadPermission(user, target);
            case "write" -> hasWritePermission(user, target);
            case "delete" -> hasDeletePermission(user, target);
            case "admin" -> hasAdminPermission(user);
            case "moderate" -> hasModeratePermission(user);
            default -> {
                log.warn("Unknown permission: {}", permission);
                yield false;
            }
        };
    }

    /**
     * Check if user owns the specified resource
     * @param user The user to check ownership for
     * @param resourceType The type of resource
     * @param resourceId The ID of the resource
     * @return true if user owns the resource, false otherwise
     */
    public boolean isResourceOwner(User user, String resourceType, Long resourceId) {
        if (user == null || resourceId == null) {
            return false;
        }

        return switch (resourceType.toLowerCase()) {
            case "event" -> isEventOwner(user, resourceId);
            case "user" -> isUserOwner(user, resourceId);
            default -> false;
        };
    }

    /**
     * Check if user can access a specific user resource
     */
    public boolean canAccessUser(User requestingUser, Long targetUserId) {
        if (requestingUser == null || targetUserId == null) {
            return false;
        }

        // Users can access their own profile
        if (requestingUser.getId().equals(targetUserId)) {
            return true;
        }

        // Admins and moderators can access other user profiles
        return requestingUser.getRole() == User.UserRole.ADMIN || 
               requestingUser.getRole() == User.UserRole.MODERATOR;
    }

    /**
     * Check if user can modify a specific user resource
     */
    public boolean canModifyUser(User requestingUser, Long targetUserId) {
        if (requestingUser == null || targetUserId == null) {
            return false;
        }

        // Users can modify their own profile
        if (requestingUser.getId().equals(targetUserId)) {
            return true;
        }

        // Only admins can modify other users
        return requestingUser.getRole() == User.UserRole.ADMIN;
    }

    /**
     * Check if user can delete a specific user resource
     */
    public boolean canDeleteUser(User requestingUser, Long targetUserId) {
        if (requestingUser == null || targetUserId == null) {
            return false;
        }

        // Users cannot delete their own account (business rule)
        // Only admins can delete user accounts
        return requestingUser.getRole() == User.UserRole.ADMIN && 
               !requestingUser.getId().equals(targetUserId);
    }

    /**
     * Check if user can access a specific event
     */
    public boolean canAccessEvent(User user, Long eventId) {
        if (user == null || eventId == null) {
            return false;
        }

        Optional<Event> eventOpt = eventRepository.findById(eventId);
        if (eventOpt.isEmpty()) {
            log.warn("Event not found: {}", eventId);
            return false;
        }

        Event event = eventOpt.get();
        
        // Event owners can always access their events
        if (event.getCreator().getId().equals(user.getId())) {
            return true;
        }

        // Admins and moderators can access all events
        if (user.getRole() == User.UserRole.ADMIN || user.getRole() == User.UserRole.MODERATOR) {
            return true;
        }

        // For now, all authenticated users can view events (business rule)
        // This could be enhanced with private events in the future
        return true;
    }

    /**
     * Check if user can modify a specific event
     */
    public boolean canModifyEvent(User user, Long eventId) {
        if (user == null || eventId == null) {
            return false;
        }

        Optional<Event> eventOpt = eventRepository.findById(eventId);
        if (eventOpt.isEmpty()) {
            return false;
        }

        Event event = eventOpt.get();
        
        // Event owners can modify their events
        if (event.getCreator().getId().equals(user.getId())) {
            return true;
        }

        // Admins and moderators can modify any event
        return user.getRole() == User.UserRole.ADMIN || 
               user.getRole() == User.UserRole.MODERATOR;
    }

    /**
     * Check if user can delete a specific event
     */
    public boolean canDeleteEvent(User user, Long eventId) {
        if (user == null || eventId == null) {
            return false;
        }

        Optional<Event> eventOpt = eventRepository.findById(eventId);
        if (eventOpt.isEmpty()) {
            return false;
        }

        Event event = eventOpt.get();
        
        // Event owners can delete their events
        if (event.getCreator().getId().equals(user.getId())) {
            return true;
        }

        // Admins can delete any event
        return user.getRole() == User.UserRole.ADMIN;
    }

    // Private helper methods

    private boolean hasReadPermission(User user, Object target) {
        // All authenticated users have read permission by default
        return true;
    }

    private boolean hasWritePermission(User user, Object target) {
        // Users can write to resources they own, admins can write to anything
        if (user.getRole() == User.UserRole.ADMIN) {
            return true;
        }

        // Check ownership if target is provided
        if (target instanceof Event event) {
            return event.getCreator().getId().equals(user.getId());
        }

        // Default write permission for authenticated users
        return true;
    }

    private boolean hasDeletePermission(User user, Object target) {
        // Only owners and admins can delete resources
        if (user.getRole() == User.UserRole.ADMIN) {
            return true;
        }

        // Check ownership if target is provided
        if (target instanceof Event event) {
            return event.getCreator().getId().equals(user.getId());
        }

        return false;
    }

    private boolean hasAdminPermission(User user) {
        return user.getRole() == User.UserRole.ADMIN;
    }

    private boolean hasModeratePermission(User user) {
        return user.getRole() == User.UserRole.ADMIN || 
               user.getRole() == User.UserRole.MODERATOR;
    }

    private boolean isEventOwner(User user, Long eventId) {
        Optional<Event> eventOpt = eventRepository.findById(eventId);
        return eventOpt.map(event -> event.getCreator().getId().equals(user.getId()))
                      .orElse(false);
    }

    private boolean isUserOwner(User user, Long userId) {
        return user.getId().equals(userId);
    }
}