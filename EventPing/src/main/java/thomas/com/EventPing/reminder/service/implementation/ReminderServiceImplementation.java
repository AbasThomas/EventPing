package thomas.com.EventPing.reminder.service.implementation;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import thomas.com.EventPing.reminder.model.Reminder;
import thomas.com.EventPing.reminder.repository.ReminderRepository;
import thomas.com.EventPing.reminder.service.ReminderService;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ReminderServiceImplementation implements ReminderService {
    private final ReminderRepository reminderRepository;

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
                
                // Send email (placeholder - actual email sending will be implemented later)
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
        // Placeholder for actual email sending
        // TODO: Implement with JavaMailSender
        log.info("EMAIL SENT: To={}, Event={}, SendAt={}", 
                reminder.getParticipant().getEmail(),
                reminder.getEvent().getTitle(),
                reminder.getSendAt());
    }
}
