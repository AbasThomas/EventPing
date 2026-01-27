package thomas.com.EventPing.security.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import thomas.com.EventPing.security.service.InputValidationService;

/**
 * Validator implementation for NoSqlInjection annotation
 * **Validates: Requirements 2.1, 2.2**
 */
@Component
public class NoSqlInjectionValidator implements ConstraintValidator<NoSqlInjection, String> {

    @Autowired
    private InputValidationService inputValidationService;

    @Override
    public void initialize(NoSqlInjection constraintAnnotation) {
        // No initialization needed
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true; // Let @NotNull handle null validation
        }
        
        // Handle case where dependency injection fails (fallback)
        if (inputValidationService == null) {
            return true; 
        }

        return !inputValidationService.containsSqlInjection(value);
    }
}