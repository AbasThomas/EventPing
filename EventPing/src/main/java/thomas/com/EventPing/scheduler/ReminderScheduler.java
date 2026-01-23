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

    /**
     * Send due reminders every minute
     */
    @Scheduled(cron = "0 * * * * *")
    public void sendReminders() {
        log.info("Running reminder sender cron job");
        reminderService.sendDueReminders();
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
