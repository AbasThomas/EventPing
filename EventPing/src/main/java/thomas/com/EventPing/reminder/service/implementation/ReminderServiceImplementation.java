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

    @Override
    public void sendDueReminders() {
        List<Reminder> dueReminders = reminderRepository.findBySendAtBeforeAndSentFalse(LocalDateTime.now());
        
        log.info("Found {} due reminders to send", dueReminders.size());
        
        for (Reminder reminder : dueReminders) {
            try {
                // Skip if participant unsubscribed
                if (reminder.getParticipant().getUnsubscribed()) {
                    log.info("Skipping reminder {} - participant unsubscribed", reminder.getId());
                    reminder.setSent(true);
                    reminder.setSentAt(LocalDateTime.now());
                    continue;
                }
                
                // Send email
                sendEmail(reminder);
                
                // Mark as sent
                reminder.setSent(true);
                reminder.setSentAt(LocalDateTime.now());
                
                log.info("Sent reminder {} to {}", reminder.getId(), reminder.getParticipant().getEmail());
            } catch (Exception e) {
                log.error("Failed to send reminder {}: {}", reminder.getId(), e.getMessage());
            }
        }
        
        reminderRepository.saveAll(dueReminders);
    }

    @Override
    public void cleanupOldReminders() {
        // Delete reminders older than 30 days
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
            log.info("Email sent successfully to {}", reminder.getParticipant().getEmail());
        } catch (Exception e) {
            log.error("Failed to send email: {}", e.getMessage());
            throw e;
        }
    }
}
