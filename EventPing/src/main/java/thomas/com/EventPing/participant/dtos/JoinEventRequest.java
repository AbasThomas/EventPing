package thomas.com.EventPing.participant.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class JoinEventRequest {
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;
    
    private String name; // Optional participant name

    private String phoneNumber; // Optional phone number for WhatsApp/SMS
    
    private java.util.Map<String, String> customFieldResponses; // fieldName -> responseValue
}
