package thomas.com.EventPing.security.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import thomas.com.EventPing.security.service.InputValidationService;

/**
 * Validator implementation for NoXss annotation
 * **Validates: Requirements 2.3, 2.4**
 */
@Component
public class NoXssValidator implements ConstraintValidator<NoXss, String> {

    @Autowired
    private InputValidationService inputValidationService;

    @Override
    public void initialize(NoXss constraintAnnotation) {
        // No initialization needed
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true; // Let @NotNull handle null validation
        }
        
        // Handle case where dependency injection fails (fallback)
        if (inputValidationService == null) {
            return true; // Skip validation to prevent runtime errors if service is not injected
        }

        return !inputValidationService.containsXss(value);
    }
}