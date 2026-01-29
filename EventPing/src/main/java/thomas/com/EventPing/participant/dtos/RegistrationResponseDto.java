package thomas.com.EventPing.participant.dtos;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import thomas.com.EventPing.security.validation.NoSqlInjection;
import thomas.com.EventPing.security.validation.NoXss;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegistrationResponseDto {
    
    private Long customFieldId;
    
    @Size(max = 5000, message = "Response value must not exceed 5000 characters")
    @NoXss
    @NoSqlInjection
    private String responseValue;
}
