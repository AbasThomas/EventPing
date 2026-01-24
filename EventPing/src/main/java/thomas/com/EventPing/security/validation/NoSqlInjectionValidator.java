package thomas.com.EventPing.security.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import thomas.com.EventPing.security.service.InputValidationService;

/**
 * Validator implementation for NoSqlInjection annotation
 * **Validates: Requirements 2.1, 2.2**
 */
@RequiredArgsConstructor
public class NoSqlInjectionValidator implements ConstraintValidator<NoSqlInjection, String> {

    private final InputValidationService inputValidationService;

    @Override
    public void initialize(NoSqlInjection constraintAnnotation) {
        // No initialization needed
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true; // Let @NotNull handle null validation
        }

        return !inputValidationService.containsSqlInjection(value);
    }
}