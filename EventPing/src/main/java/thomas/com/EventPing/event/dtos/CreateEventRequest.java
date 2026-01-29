package thomas.com.EventPing.event.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class CreateEventRequest {
    @NotBlank(message = "Title is required")
    private String title;
    
    private String description;
    
    @NotNull(message = "Event date and time is required")
    private LocalDateTime eventDateTime;
    
    @NotNull(message = "At least one reminder offset is required")
    private List<Long> reminderOffsetMinutes; // e.g., [60, 1440] for 1h and 1day
    
    private List<CustomFieldDto> customFields;
    
    private List<String> integrations; // Integration type names: EMAIL, WHATSAPP, etc.
    
    private Boolean registrationEnabled = true;
}
