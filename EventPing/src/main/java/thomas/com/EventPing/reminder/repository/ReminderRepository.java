package thomas.com.EventPing.reminder.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import thomas.com.EventPing.reminder.model.Reminder;

import java.time.LocalDateTime;
import java.util.List;

public interface ReminderRepository extends JpaRepository<Reminder, Long> {
    List<Reminder> findBySendAtBeforeAndSentFalse(LocalDateTime dateTime);
    
    List<Reminder> findBySentTrueAndSentAtBefore(LocalDateTime dateTime);
}
