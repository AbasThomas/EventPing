package thomas.com.EventPing.integration.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import thomas.com.EventPing.User.model.User;
import thomas.com.EventPing.event.model.Event;
import thomas.com.EventPing.integration.service.NotificationService;

@Slf4j
@Service
public class GoogleCalendarService implements NotificationService {
    @Override
    public boolean sendReminder(User user, Event event) {
        log.info("📅 Mock Calendar event created for {}", user.getEmail());
        return true;
    }

    @Override
    public boolean verifyCredentials(User user) {
        return user.getGoogleCalendarAccessToken() != null;
    }
}
