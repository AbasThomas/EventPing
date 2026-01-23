package thomas.com.EventPing.security.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class JwtToken {
    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private Long expiresIn; // in seconds
}