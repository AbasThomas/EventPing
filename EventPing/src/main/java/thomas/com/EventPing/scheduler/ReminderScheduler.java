package thomas.com.EventPing.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import thomas.com.EventPing.reminder.service.ReminderService;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReminderScheduler {
    private final ReminderService reminderService;
    private final thomas.com.EventPing.User.repository.UserRepository userRepository;

    /**
     * Send due reminders every minute
     */
    @Scheduled(cron = "0 * * * * *")
    public void sendReminders() {
        log.info("Running reminder sender cron job");
        reminderService.sendDueReminders();
    }

    /**
     * Reset user usage credits monthly (checked daily at 1 AM)
     */
    @Scheduled(cron = "0 0 1 * * *")
    @jakarta.transaction.Transactional
    public void resetUserUsage() {
        log.info("Running user credit usage reset job");
        java.time.LocalDateTime cutoff = java.time.LocalDateTime.now().minusDays(30);
        java.util.List<thomas.com.EventPing.User.model.User> usersToReset = userRepository.findByLastUsageResetAtBefore(cutoff);
        
        for (thomas.com.EventPing.User.model.User user : usersToReset) {
            user.setMonthlyCreditsUsed(0);
            user.setLastUsageResetAt(java.time.LocalDateTime.now());
        }
        
        userRepository.saveAll(usersToReset);
        log.info("Reset credit usage for {} users", usersToReset.size());
    }

    /**
     * Cleanup old reminders daily at midnight
     */
    @Scheduled(cron = "0 0 0 * * *")
    public void cleanupReminders() {
        log.info("Running reminder cleanup cron job");
        reminderService.cleanupOldReminders();
    }
}
