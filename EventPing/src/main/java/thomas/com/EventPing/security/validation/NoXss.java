package thomas.com.EventPing.security.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Custom validation annotation to prevent XSS attacks
 * **Validates: Requirements 2.3, 2.4**
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = NoXssValidator.class)
public @interface NoXss {
    String message() default "Input contains potential XSS";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}