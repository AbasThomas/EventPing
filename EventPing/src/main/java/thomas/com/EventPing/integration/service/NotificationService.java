package thomas.com.EventPing.integration.service;

import thomas.com.EventPing.User.model.User;
import thomas.com.EventPing.event.model.Event;

public interface NotificationService {
    boolean sendReminder(User user, Event event);
    boolean verifyCredentials(User user);
}
