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
    private final AuditLoggingService auditLoggingService;

    /**
     * Check if user can access a specific resource
     * @param user The user requesting access
     * @param resourceType The type of resource (e.g., "event", "user")
     * @param resourceId The ID of the resource
     * @return true if access is allowed, false otherwise
     */
    public boolean canAccessResource(User user, String resourceType, Long resourceId) {
        return canAccessResource(user, resourceType, resourceId, null);
    }

    /**
     * Check if user can access a specific resource with IP address for audit logging
     * @param user The user requesting access
     * @param resourceType The type of resource (e.g., "event", "user")
     * @param resourceId The ID of the resource
     * @param ipAddress The IP address of the request
     * @return true if access is allowed, false otherwise
     */
    public boolean canAccessResource(User user, String resourceType, Long resourceId, String ipAddress) {
        if (user == null || resourceId == null) {
            log.warn("Authorization check failed: null user or resource ID");
            auditLoggingService.logAuthorizationFailure(
                    user != null ? user.getEmail() : "unknown",
                    ipAddress,
                    resourceType + "/" + resourceId,
                    "ACCESS"
            );
            return false;
        }

        // Admins can access all resources
        if (user.getRole() == User.UserRole.ADMIN) {
            log.debug("Admin user {} granted access to {} {}", user.getEmail(), resourceType, resourceId);
            return true;
        }

        boolean hasAccess = switch (resourceType.toLowerCase()) {
            case "event" -> canAccessEvent(user, resourceId);
            case "user" -> canAccessUser(user, resourceId);
            default -> {
                log.warn("Unknown resource type: {}", resourceType);
                yield false;
            }
        };

        // Log authorization failure if access is denied
        if (!hasAccess) {
            auditLoggingService.logAuthorizationFailure(
                    user.getEmail(),
                    ipAddress,
                    resourceType + "/" + resourceId,
                    "ACCESS"
            );
        }

        return hasAccess;
    }

    /**
     * Validate user permissions for specific actions
     * @param user The user requesting permission
     * @param permission The permission being requested
     * @param target The target object (optional)
     * @return true if permission is granted, false otherwise
     */
    public boolean hasPermission(User user, String permission, Object target) {
        return hasPermission(user, permission, target, null);
    }

    /**
     * Validate user permissions for specific actions with IP address for audit logging
     * @param user The user requesting permission
     * @param permission The permission being requested
     * @param target The target object (optional)
     * @param ipAddress The IP address of the request
     * @return true if permission is granted, false otherwise
     */
    public boolean hasPermission(User user, String permission, Object target, String ipAddress) {
        if (user == null || permission == null) {
            log.warn("Permission check failed: null user or permission");
            auditLoggingService.logAuthorizationFailure(
                    user != null ? user.getEmail() : "unknown",
                    ipAddress,
                    target != null ? target.getClass().getSimpleName() : "unknown",
                    permission
            );
            return false;
        }

        // Check account status
        if (user.getAccountLocked()) {
            log.warn("Permission denied for locked account: {}", user.getEmail());
            auditLoggingService.logAuthorizationFailure(
                    user.getEmail(),
                    ipAddress,
                    target != null ? target.getClass().getSimpleName() : "unknown",
                    permission + " (account locked)"
            );
            return false;
        }

        boolean hasPermission = switch (permission.toLowerCase()) {
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

        // Log authorization failure if permission is denied
        if (!hasPermission) {
            auditLoggingService.logAuthorizationFailure(
                    user.getEmail(),
                    ipAddress,
                    target != null ? target.getClass().getSimpleName() : "unknown",
                    permission
            );
        }

        return hasPermission;
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
        return canModifyUser(requestingUser, targetUserId, null);
    }

    /**
     * Check if user can modify a specific user resource with IP address for audit logging
     */
    public boolean canModifyUser(User requestingUser, Long targetUserId, String ipAddress) {
        if (requestingUser == null || targetUserId == null) {
            auditLoggingService.logAuthorizationFailure(
                    requestingUser != null ? requestingUser.getEmail() : "unknown",
                    ipAddress,
                    "user/" + targetUserId,
                    "MODIFY"
            );
            return false;
        }

        // Users can modify their own profile
        if (requestingUser.getId().equals(targetUserId)) {
            return true;
        }

        // Only admins can modify other users
        boolean canModify = requestingUser.getRole() == User.UserRole.ADMIN;
        
        if (!canModify) {
            auditLoggingService.logAuthorizationFailure(
                    requestingUser.getEmail(),
                    ipAddress,
                    "user/" + targetUserId,
                    "MODIFY"
            );
        }
        
        return canModify;
    }

    /**
     * Check if user can delete a specific user resource
     */
    public boolean canDeleteUser(User requestingUser, Long targetUserId) {
        return canDeleteUser(requestingUser, targetUserId, null);
    }

    /**
     * Check if user can delete a specific user resource with IP address for audit logging
     */
    public boolean canDeleteUser(User requestingUser, Long targetUserId, String ipAddress) {
        if (requestingUser == null || targetUserId == null) {
            auditLoggingService.logAuthorizationFailure(
                    requestingUser != null ? requestingUser.getEmail() : "unknown",
                    ipAddress,
                    "user/" + targetUserId,
                    "DELETE"
            );
            return false;
        }

        // Users cannot delete their own account (business rule)
        // Only admins can delete user accounts
        boolean canDelete = requestingUser.getRole() == User.UserRole.ADMIN && 
                           !requestingUser.getId().equals(targetUserId);
        
        if (!canDelete) {
            auditLoggingService.logAuthorizationFailure(
                    requestingUser.getEmail(),
                    ipAddress,
                    "user/" + targetUserId,
                    "DELETE"
            );
        }
        
        return canDelete;
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
        return canModifyEvent(user, eventId, null);
    }

    /**
     * Check if user can modify a specific event with IP address for audit logging
     */
    public boolean canModifyEvent(User user, Long eventId, String ipAddress) {
        if (user == null || eventId == null) {
            auditLoggingService.logAuthorizationFailure(
                    user != null ? user.getEmail() : "unknown",
                    ipAddress,
                    "event/" + eventId,
                    "MODIFY"
            );
            return false;
        }

        Optional<Event> eventOpt = eventRepository.findById(eventId);
        if (eventOpt.isEmpty()) {
            auditLoggingService.logAuthorizationFailure(
                    user.getEmail(),
                    ipAddress,
                    "event/" + eventId,
                    "MODIFY (event not found)"
            );
            return false;
        }

        Event event = eventOpt.get();
        
        // Event owners can modify their events
        if (event.getCreator().getId().equals(user.getId())) {
            return true;
        }

        // Admins and moderators can modify any event
        boolean canModify = user.getRole() == User.UserRole.ADMIN || 
                           user.getRole() == User.UserRole.MODERATOR;
        
        if (!canModify) {
            auditLoggingService.logAuthorizationFailure(
                    user.getEmail(),
                    ipAddress,
                    "event/" + eventId,
                    "MODIFY"
            );
        }
        
        return canModify;
    }

    /**
     * Check if user can delete a specific event
     */
    public boolean canDeleteEvent(User user, Long eventId) {
        return canDeleteEvent(user, eventId, null);
    }

    /**
     * Check if user can delete a specific event with IP address for audit logging
     */
    public boolean canDeleteEvent(User user, Long eventId, String ipAddress) {
        if (user == null || eventId == null) {
            auditLoggingService.logAuthorizationFailure(
                    user != null ? user.getEmail() : "unknown",
                    ipAddress,
                    "event/" + eventId,
                    "DELETE"
            );
            return false;
        }

        Optional<Event> eventOpt = eventRepository.findById(eventId);
        if (eventOpt.isEmpty()) {
            auditLoggingService.logAuthorizationFailure(
                    user.getEmail(),
                    ipAddress,
                    "event/" + eventId,
                    "DELETE (event not found)"
            );
            return false;
        }

        Event event = eventOpt.get();
        
        // Event owners can delete their events
        if (event.getCreator().getId().equals(user.getId())) {
            return true;
        }

        // Admins can delete any event
        boolean canDelete = user.getRole() == User.UserRole.ADMIN;
        
        if (!canDelete) {
            auditLoggingService.logAuthorizationFailure(
                    user.getEmail(),
                    ipAddress,
                    "event/" + eventId,
                    "DELETE"
            );
        }
        
        return canDelete;
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