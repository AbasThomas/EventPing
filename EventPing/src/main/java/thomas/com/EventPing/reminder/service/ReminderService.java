package thomas.com.EventPing.reminder.service;

public interface ReminderService {
    void sendDueReminders();
    void cleanupOldReminders();
}
