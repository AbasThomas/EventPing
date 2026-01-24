package thomas.com.EventPing.security.service;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Result object for input validation operations
 * **Validates: Requirements 2.1, 2.2, 2.3, 2.4**
 */
@Data
public class ValidationResult {
    private boolean valid = true;
    private List<String> violations = new ArrayList<>();

    public static ValidationResult valid() {
        return new ValidationResult();
    }

    public static ValidationResult invalid(String violation) {
        ValidationResult result = new ValidationResult();
        result.addViolation(violation);
        return result;
    }

    public void addViolation(String violation) {
        this.valid = false;
        this.violations.add(violation);
    }

    public boolean hasViolations() {
        return !violations.isEmpty();
    }
}