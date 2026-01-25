package thomas.com.EventPing.security.exception;

import lombok.Getter;

import java.util.Map;

/**
 * Exception thrown when input validation fails.
 * Contains validation error details without exposing sensitive information.
 */
@Getter
public class ValidationException extends RuntimeException {
    private final Map<String, String> validationErrors;

    public ValidationException(String message, Map<String, String> validationErrors) {
        super(message);
        this.validationErrors = validationErrors;
    }

    public ValidationException(String message) {
        super(message);
        this.validationErrors = Map.of();
    }
}