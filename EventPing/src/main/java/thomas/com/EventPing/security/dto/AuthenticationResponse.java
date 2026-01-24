package thomas.com.EventPing.security.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import thomas.com.EventPing.User.dtos.UserResponseDto;

/**
 * DTO for authentication response containing JWT token and user information
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthenticationResponse {
    
    private JwtToken token;
    private UserResponseDto user;
    private String message;
    
    /**
     * Create successful authentication response
     */
    public static AuthenticationResponse success(JwtToken token, UserResponseDto user) {
        return AuthenticationResponse.builder()
                .token(token)
                .user(user)
                .message("Authentication successful")
                .build();
    }
}