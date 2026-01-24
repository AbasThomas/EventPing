package thomas.com.EventPing.security.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import thomas.com.EventPing.security.service.InputValidationService;

/**
 * Validator implementation for NoXss annotation
 * **Validates: Requirements 2.3, 2.4**
 */
@RequiredArgsConstructor
public class NoXssValidator implements ConstraintValidator<NoXss, String> {

    private final InputValidationService inputValidationService;

    @Override
    public void initialize(NoXss constraintAnnotation) {
        // No initialization needed
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true; // Let @NotNull handle null validation
        }

        return !inputValidationService.containsXss(value);
    }
}