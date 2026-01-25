package thomas.com.EventPing.security.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import thomas.com.EventPing.security.entity.AuditEvent;

import java.time.format.DateTimeFormatter;
import java.util.concurrent.CompletableFuture;

/**
 * Service for sending security alerts and notifications
 * **Validates: Requirements 6.4, 6.5**
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SecurityAlertService {

    @Value("${eventping.security.alerts.enabled:true}")
    private boolean alertsEnabled;

    @Value("${eventping.security.alerts.email.enabled:false}")
    private boolean emailAlertsEnabled;

    @Value("${eventping.security.alerts.webhook.enabled:false}")
    private boolean webhookAlertsEnabled;

    @Value("${eventping.security.alerts.webhook.url:}")
    private String webhookUrl;

    @Value("${eventping.security.alerts.admin-email:}")
    private String adminEmail;

    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Send security alert for high-severity events
     */
    @Async
    public CompletableFuture<Void> sendSecurityAlert(AuditEvent event) {
        if (!alertsEnabled) {
            return CompletableFuture.completedFuture(null);
        }

        try {
            String alertMessage = formatAlertMessage(event);
            
            // Log the alert
            logAlert(event, alertMessage);
            
            // Send email alert if enabled
            if (emailAlertsEnabled && !adminEmail.isEmpty()) {
                sendEmailAlert(event, alertMessage);
            }
            
            // Send webhook alert if enabled
            if (webhookAlertsEnabled && !webhookUrl.isEmpty()) {
                sendWebhookAlert(event, alertMessage);
            }
            
        } catch (Exception e) {
            log.error("Failed to send security alert for event: {}", event.getId(), e);
        }
        
        return CompletableFuture.completedFuture(null);
    }

    /**
     * Send brute force attack alert
     */
    @Async
    public void sendBruteForceAlert(String ipAddress, String username, int attemptCount) {
        if (!alertsEnabled) {
            return;
        }

        String message = String.format(
            "BRUTE FORCE ATTACK DETECTED\n" +
            "IP Address: %s\n" +
            "Target User: %s\n" +
            "Failed Attempts: %d\n" +
            "Time: %s\n" +
            "Action: IP has been temporarily blocked",
            ipAddress, username, attemptCount, 
            java.time.LocalDateTime.now().format(TIMESTAMP_FORMAT)
        );

        log.error("SECURITY ALERT: {}", message);
        
        if (emailAlertsEnabled) {
            sendEmailNotification("Brute Force Attack Detected", message);
        }
        
        if (webhookAlertsEnabled) {
            sendWebhookNotification("brute_force_attack", message);
        }
    }

    /**
     * Send suspicious activity alert
     */
    @Async
    public void sendSuspiciousActivityAlert(String ipAddress, String activityType, String details) {
        if (!alertsEnabled) {
            return;
        }

        String message = String.format(
            "SUSPICIOUS ACTIVITY DETECTED\n" +
            "IP Address: %s\n" +
            "Activity Type: %s\n" +
            "Details: %s\n" +
            "Time: %s",
            ipAddress, activityType, details,
            java.time.LocalDateTime.now().format(TIMESTAMP_FORMAT)
        );

        log.warn("SECURITY ALERT: {}", message);
        
        if (emailAlertsEnabled) {
            sendEmailNotification("Suspicious Activity Detected", message);
        }
        
        if (webhookAlertsEnabled) {
            sendWebhookNotification("suspicious_activity", message);
        }
    }

    /**
     * Send rate limit exceeded alert
     */
    @Async
    public void sendRateLimitAlert(String ipAddress, String limitType, int currentCount, int limit) {
        if (!alertsEnabled) {
            return;
        }

        String message = String.format(
            "RATE LIMIT EXCEEDED\n" +
            "IP Address: %s\n" +
            "Limit Type: %s\n" +
            "Current Count: %d\n" +
            "Limit: %d\n" +
            "Time: %s",
            ipAddress, limitType, currentCount, limit,
            java.time.LocalDateTime.now().format(TIMESTAMP_FORMAT)
        );

        log.info("RATE LIMIT ALERT: {}", message);
        
        // Only send notifications for severe rate limit violations
        if (currentCount > limit * 2) {
            if (emailAlertsEnabled) {
                sendEmailNotification("Rate Limit Severely Exceeded", message);
            }
            
            if (webhookAlertsEnabled) {
                sendWebhookNotification("rate_limit_exceeded", message);
            }
        }
    }

    /**
     * Send system security alert
     */
    @Async
    public void sendSystemSecurityAlert(String alertType, String message) {
        if (!alertsEnabled) {
            return;
        }

        String fullMessage = String.format(
            "SYSTEM SECURITY ALERT\n" +
            "Alert Type: %s\n" +
            "Message: %s\n" +
            "Time: %s",
            alertType, message,
            java.time.LocalDateTime.now().format(TIMESTAMP_FORMAT)
        );

        log.error("SYSTEM SECURITY ALERT: {}", fullMessage);
        
        if (emailAlertsEnabled) {
            sendEmailNotification("System Security Alert", fullMessage);
        }
        
        if (webhookAlertsEnabled) {
            sendWebhookNotification("system_security_alert", fullMessage);
        }
    }

    /**
     * Format alert message for audit event
     */
    private String formatAlertMessage(AuditEvent event) {
        StringBuilder message = new StringBuilder();
        message.append("SECURITY ALERT\n");
        message.append("Event Type: ").append(event.getEventType()).append("\n");
        message.append("Severity: ").append(event.getSeverity()).append("\n");
        message.append("Time: ").append(event.getTimestamp().format(TIMESTAMP_FORMAT)).append("\n");
        
        if (event.getUsername() != null) {
            message.append("User: ").append(event.getUsername()).append("\n");
        }
        
        if (event.getIpAddress() != null) {
            message.append("IP Address: ").append(event.getIpAddress()).append("\n");
        }
        
        if (event.getResourceType() != null) {
            message.append("Resource: ").append(event.getResourceType());
            if (event.getResourceId() != null) {
                message.append("/").append(event.getResourceId());
            }
            message.append("\n");
        }
        
        if (event.getAction() != null) {
            message.append("Action: ").append(event.getAction()).append("\n");
        }
        
        if (event.getErrorMessage() != null) {
            message.append("Details: ").append(event.getErrorMessage()).append("\n");
        }
        
        if (event.getRequestUri() != null) {
            message.append("Request URI: ").append(event.getRequestUri()).append("\n");
        }
        
        if (event.getUserAgent() != null) {
            message.append("User Agent: ").append(event.getUserAgent()).append("\n");
        }
        
        return message.toString();
    }

    /**
     * Log security alert
     */
    private void logAlert(AuditEvent event, String message) {
        switch (event.getSeverity()) {
            case CRITICAL:
                log.error("CRITICAL SECURITY ALERT: {}", message);
                break;
            case HIGH:
                log.error("HIGH SECURITY ALERT: {}", message);
                break;
            case MEDIUM:
                log.warn("MEDIUM SECURITY ALERT: {}", message);
                break;
            case LOW:
                log.info("LOW SECURITY ALERT: {}", message);
                break;
        }
    }

    /**
     * Send email alert (placeholder implementation)
     */
    private void sendEmailAlert(AuditEvent event, String message) {
        try {
            // In a real implementation, this would use Spring Mail or another email service
            log.info("EMAIL ALERT would be sent to: {} with message: {}", adminEmail, message);
            
            // Example implementation:
            // SimpleMailMessage mailMessage = new SimpleMailMessage();
            // mailMessage.setTo(adminEmail);
            // mailMessage.setSubject("Security Alert: " + event.getEventType());
            // mailMessage.setText(message);
            // mailSender.send(mailMessage);
            
        } catch (Exception e) {
            log.error("Failed to send email alert", e);
        }
    }

    /**
     * Send webhook alert (placeholder implementation)
     */
    private void sendWebhookAlert(AuditEvent event, String message) {
        try {
            // In a real implementation, this would make HTTP POST to webhook URL
            log.info("WEBHOOK ALERT would be sent to: {} with message: {}", webhookUrl, message);
            
            // Example implementation:
            // WebhookPayload payload = new WebhookPayload();
            // payload.setEventType(event.getEventType().toString());
            // payload.setSeverity(event.getSeverity().toString());
            // payload.setMessage(message);
            // payload.setTimestamp(event.getTimestamp());
            // 
            // restTemplate.postForEntity(webhookUrl, payload, String.class);
            
        } catch (Exception e) {
            log.error("Failed to send webhook alert", e);
        }
    }

    /**
     * Send email notification
     */
    private void sendEmailNotification(String subject, String message) {
        try {
            log.info("EMAIL NOTIFICATION: {} - {}", subject, message);
            // Implementation would go here
        } catch (Exception e) {
            log.error("Failed to send email notification", e);
        }
    }

    /**
     * Send webhook notification
     */
    private void sendWebhookNotification(String eventType, String message) {
        try {
            log.info("WEBHOOK NOTIFICATION: {} - {}", eventType, message);
            // Implementation would go here
        } catch (Exception e) {
            log.error("Failed to send webhook notification", e);
        }
    }

    /**
     * Check if alerts are enabled
     */
    public boolean areAlertsEnabled() {
        return alertsEnabled;
    }

    /**
     * Check if email alerts are enabled
     */
    public boolean areEmailAlertsEnabled() {
        return emailAlertsEnabled && !adminEmail.isEmpty();
    }

    /**
     * Check if webhook alerts are enabled
     */
    public boolean areWebhookAlertsEnabled() {
        return webhookAlertsEnabled && !webhookUrl.isEmpty();
    }
}