package thomas.com.EventPing.security.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Secure error response model that prevents information disclosure.
 * Never includes sensitive information like stack traces, internal system details,
 * database connection strings, or configuration values.
 */
@Data
@Builder
public class ErrorResponse {
    private LocalDateTime timestamp;
    private int status;
    private String error;
    private String message;
    private String path;
    private Map<String, String> validationErrors;
}