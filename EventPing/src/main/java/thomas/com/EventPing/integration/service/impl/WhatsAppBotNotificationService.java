package thomas.com.EventPing.integration.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import thomas.com.EventPing.User.model.User;
import thomas.com.EventPing.event.model.Event;
import thomas.com.EventPing.integration.service.NotificationService;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class WhatsAppBotNotificationService implements NotificationService {

    @Value("${whatsapp.bot.url:http://localhost:3000}")
    private String botUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public boolean sendReminder(User user, Event event) {
        if (!user.getEnableWhatsApp() || user.getPhoneNumber() == null) {
            return false;
        }

        try {
            String endpoint = botUrl + "/send-reminder";

            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("phoneNumber", user.getPhoneNumber());
            requestBody.put("eventTitle", event.getTitle());
            requestBody.put("eventDateTime", event.getEventDateTime().toString());
            requestBody.put("description", event.getDescription() != null ? event.getDescription() : "");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, String>> request = new HttpEntity<>(requestBody, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(endpoint, request, Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                boolean success = Boolean.TRUE.equals(response.getBody().get("success"));

                if (success) {
                    log.info("✅ WhatsApp reminder sent via bot to: {}", user.getPhoneNumber());
                    return true;
                } else {
                    log.error("❌ Bot failed to send WhatsApp: {}", response.getBody().get("message"));
                    return false;
                }
            }

            return false;

        } catch (Exception e) {
            log.error("Error calling WhatsApp bot: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public boolean verifyCredentials(User user) {
        String phoneNumber = user.getPhoneNumber();
        boolean validPhone = phoneNumber != null && phoneNumber.matches("\\+?\\d{10,15}");

        if (!validPhone) {
            return false;
        }

        try {
            String healthUrl = botUrl + "/health";
            ResponseEntity<Map> response = restTemplate.getForEntity(healthUrl, Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return Boolean.TRUE.equals(response.getBody().get("whatsappConnected"));
            }
        } catch (Exception e) {
            log.error("WhatsApp bot not available: {}", e.getMessage());
        }

        return false;
    }
}
