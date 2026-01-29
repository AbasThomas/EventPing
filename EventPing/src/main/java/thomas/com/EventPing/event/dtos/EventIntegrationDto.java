package thomas.com.EventPing.event.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import thomas.com.EventPing.event.model.EventIntegration;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventIntegrationDto {
    
    @NotNull(message = "Integration type is required")
    private EventIntegration.IntegrationType integrationType;

    private String configuration; // JSON string for future OAuth tokens, webhooks
    
    private boolean active = true;
}
