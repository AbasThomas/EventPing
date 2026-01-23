package thomas.com.EventPing.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import thomas.com.EventPing.event.service.EventService;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventExpiryScheduler {
    private final EventService eventService;

    /**
     * Mark expired events every hour
     */
    @Scheduled(cron = "0 0 * * * *")
    public void markExpiredEvents() {
        log.info("Running event expiry cron job");
        eventService.markExpiredEvents();
    }
}
