package thomas.com.EventPing.reminder.service.implementation;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import thomas.com.EventPing.reminder.model.Reminder;
import thomas.com.EventPing.reminder.repository.ReminderRepository;
import thomas.com.EventPing.reminder.service.ReminderService;
import thomas.com.EventPing.integration.service.impl.WhatsAppBotNotificationService;
import thomas.com.EventPing.integration.service.impl.GmailNotificationService;
import thomas.com.EventPing.integration.service.impl.DiscordNotificationService;
import thomas.com.EventPing.integration.service.impl.SlackNotificationService;
import thomas.com.EventPing.User.model.User;
import thomas.com.EventPing.User.repository.UserRepository;
import thomas.com.EventPing.common.service.RateLimitService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ReminderServiceImplementation implements ReminderService {
    private final ReminderRepository reminderRepository;
    private final JavaMailSender mailSender;
    private final RateLimitService rateLimitService;
    private final UserRepository userRepository;
    
    private final WhatsAppBotNotificationService whatsAppBotService;
    private final GmailNotificationService gmailService;
    private final DiscordNotificationService discordService;
    private final SlackNotificationService slackService;

    private final EventRepository eventRepository;
    private final thomas.com.EventPing.event.repository.EventRepository eventRepo; // Alias if needed or reuse

    @Override
    public void sendDueReminders() {
        // 1. Participant Reminders (Existing Logic)
        List<Reminder> dueReminders = reminderRepository.findBySendAtBeforeAndSentFalse(LocalDateTime.now());
        
        log.info("Found {} due participant reminders to send", dueReminders.size());
        
        for (Reminder reminder : dueReminders) {
            // ... existing loop ...
        }
        
        // 2. Creator Reminders (New Logic)
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oneMinuteLater = now.plusMinutes(1);
        List<thomas.com.EventPing.event.model.Event> eventsWithReminders = eventRepository.findEventsWithRemindersBetween(now, oneMinuteLater);
        
        log.info("Found {} events with due creator reminders", eventsWithReminders.size());
        
        for (thomas.com.EventPing.event.model.Event event : eventsWithReminders) {
             sendCreatorReminder(event);
        }
        
        reminderRepository.saveAll(dueReminders);
    }
    
    private void sendCreatorReminder(thomas.com.EventPing.event.model.Event event) {
        User creator = event.getCreator();
        // Check enabled integrations for creator and send
        if (creator.isEnableWhatsApp()) {
             whatsAppBotService.sendReminder(creator, event);
        }
        if (creator.isEnableGmail()) {
             gmailService.sendReminder(creator, event);
        }
        // ... discord, slack ...
    }

    @Override
    public void cleanupOldReminders() {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(30);
        List<Reminder> oldReminders = reminderRepository.findBySentTrueAndSentAtBefore(cutoffDate);
        log.info("Cleaning up {} old reminders", oldReminders.size());
        reminderRepository.deleteAll(oldReminders);
    }

    private void sendEmail(Reminder reminder) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(reminder.getParticipant().getEmail());
            message.setSubject("Reminder: " + reminder.getEvent().getTitle());
            
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM dd, yyyy 'at' hh:mm a");
            String eventTime = reminder.getEvent().getEventDateTime().format(formatter);
            
            String body = String.format(
                "Hello,\n\n" +
                "This is a reminder for the upcoming event:\n\n" +
                "Event: %s\n" +
                "Date & Time: %s\n" +
                "Description: %s\n\n" +
                "We look forward to seeing you there!\n\n" +
                "---\n" +
                "EventPing Reminder Service",
                reminder.getEvent().getTitle(),
                eventTime,
                reminder.getEvent().getDescription() != null ? reminder.getEvent().getDescription() : "No description"
            );
            
            message.setText(body);
            
            mailSender.send(message);
        } catch (Exception e) {
            log.error("Failed to send email: {}", e.getMessage());
            throw e;
        }
    }
}
