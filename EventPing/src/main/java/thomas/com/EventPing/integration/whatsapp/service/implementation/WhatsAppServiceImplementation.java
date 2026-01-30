package thomas.com.EventPing.integration.whatsapp.service.implementation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import thomas.com.EventPing.integration.whatsapp.service.WhatsAppService;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class WhatsAppServiceImplementation implements WhatsAppService {

    @Value("${whatsapp.bot.url}")
    private String botUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public void sendMessage(String phoneNumber, String message) {
        if (phoneNumber == null || message == null) {
            log.warn("Cannot send WhatsApp message: phoneNumber or message is null");
            return;
        }

        try {
            // Remove '+' if present as some bots might prefer without, or keep it. 
            // Baileys usually handles international format. 
            // I will keep it as is based on the User entity comment "// Format: +1234567890"
            
            String url = botUrl + "/send-message"; 
            
            Map<String, String> payload = new HashMap<>();
            payload.put("phoneNumber", phoneNumber);
            payload.put("message", message);

            restTemplate.postForEntity(url, payload, String.class);
            log.info("WhatsApp message sent to {}", phoneNumber);
        } catch (Exception e) {
            log.error("Failed to send WhatsApp message to {}: {}", phoneNumber, e.getMessage());
        }
    }
}
