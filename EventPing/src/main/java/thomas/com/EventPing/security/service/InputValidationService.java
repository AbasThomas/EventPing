package thomas.com.EventPing.security.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.regex.Pattern;

/**
 * Service for comprehensive input validation and sanitization
 * **Validates: Requirements 2.1, 2.2, 2.3, 2.4**
 */
@Service
@Slf4j
public class InputValidationService {

    // SQL injection patterns - common SQL keywords and injection techniques
    private static final Pattern[] SQL_INJECTION_PATTERNS = {
        Pattern.compile("(?i).*\\b(SELECT|INSERT|UPDATE|DELETE|DROP|CREATE|ALTER|EXEC|EXECUTE|UNION|SCRIPT)\\b.*"),
        Pattern.compile("(?i).*(OR|AND)\\s+['\"]?\\w+['\"]?\\s*=\\s*['\"]?\\w+['\"]?.*"),
        Pattern.compile("(?i).*['\"];.*"),
        Pattern.compile("(?i).*--.*"),
        Pattern.compile("(?i).*/\\*.*\\*/.*"),
        Pattern.compile("(?i).*\\bxp_cmdshell\\b.*"),
        Pattern.compile("(?i).*\\bsp_.*"),
        Pattern.compile("(?i).*\\\\x[0-9a-fA-F]{2}.*"),
        Pattern.compile("(?i).*\\bCHAR\\s*\\(.*"),
        Pattern.compile("(?i).*\\bCONCAT\\s*\\(.*"),
        Pattern.compile("(?i).*\\bSUBSTRING\\s*\\(.*")
    };

    // XSS patterns - common XSS attack vectors
    private static final Pattern[] XSS_PATTERNS = {
        Pattern.compile("(?i).*<\\s*script[^>]*>.*", Pattern.DOTALL),
        Pattern.compile("(?i).*</\\s*script\\s*>.*", Pattern.DOTALL),
        Pattern.compile("(?i).*javascript\\s*:.*", Pattern.DOTALL),
        Pattern.compile("(?i).*on\\w+\\s*=.*", Pattern.DOTALL),
        Pattern.compile("(?i).*<\\s*iframe[^>]*>.*", Pattern.DOTALL),
        Pattern.compile("(?i).*<\\s*object[^>]*>.*", Pattern.DOTALL),
        Pattern.compile("(?i).*<\\s*embed[^>]*>.*", Pattern.DOTALL),
        Pattern.compile("(?i).*<\\s*link[^>]*>.*", Pattern.DOTALL),
        Pattern.compile("(?i).*<\\s*meta[^>]*>.*", Pattern.DOTALL),
        Pattern.compile("(?i).*<\\s*style[^>]*>.*", Pattern.DOTALL),
        Pattern.compile("(?i).*expression\\s*\\(.*", Pattern.DOTALL),
        Pattern.compile("(?i).*vbscript\\s*:.*", Pattern.DOTALL),
        Pattern.compile("(?i).*data\\s*:.*", Pattern.DOTALL),
        Pattern.compile("(?i).*\\\\x[0-9a-fA-F]{2}.*"),
        Pattern.compile("(?i).*&#\\d+;.*"),
        Pattern.compile("(?i).*&#x[0-9a-fA-F]+;.*")
    };

    // Email validation pattern - RFC 5322 compliant
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$"
    );

    // Allowed domains for email validation (can be configured)
    private static final Pattern SUSPICIOUS_DOMAINS = Pattern.compile(
        "(?i).*(tempmail|10minutemail|guerrillamail|mailinator|throwaway).*"
    );

    /**
     * Check if input contains potential SQL injection patterns
     * @param input The input string to validate
     * @return true if SQL injection patterns are detected, false otherwise
     */
    public boolean containsSqlInjection(String input) {
        if (!StringUtils.hasText(input)) {
            return false;
        }

        // Normalize whitespace and case for better pattern matching
        String normalizedInput = input.trim().replaceAll("\\s+", " ");
        
        for (Pattern pattern : SQL_INJECTION_PATTERNS) {
            if (pattern.matcher(normalizedInput).matches()) {
                log.warn("SQL injection pattern detected: {}", pattern.pattern());
                return true;
            }
        }
        
        return false;
    }

    /**
     * Check if input contains potential XSS patterns
     * @param input The input string to validate
     * @return true if XSS patterns are detected, false otherwise
     */
    public boolean containsXss(String input) {
        if (!StringUtils.hasText(input)) {
            return false;
        }

        // Normalize whitespace for better pattern matching
        String normalizedInput = input.trim().replaceAll("\\s+", " ");
        
        for (Pattern pattern : XSS_PATTERNS) {
            if (pattern.matcher(normalizedInput).matches()) {
                log.warn("XSS pattern detected: {}", pattern.pattern());
                return true;
            }
        }
        
        return false;
    }

    /**
     * Sanitize HTML content by removing potentially dangerous elements
     * @param input The input string to sanitize
     * @return Sanitized string with dangerous HTML removed
     */
    public String sanitizeHtml(String input) {
        if (!StringUtils.hasText(input)) {
            return input;
        }

        String sanitized = input;
        
        // Remove script tags and their content
        sanitized = sanitized.replaceAll("(?i)<\\s*script[^>]*>.*?</\\s*script\\s*>", "");
        
        // Remove dangerous event handlers
        sanitized = sanitized.replaceAll("(?i)\\s*on\\w+\\s*=\\s*[\"'][^\"']*[\"']", "");
        
        // Remove javascript: and vbscript: protocols
        sanitized = sanitized.replaceAll("(?i)javascript\\s*:", "");
        sanitized = sanitized.replaceAll("(?i)vbscript\\s*:", "");
        
        // Remove dangerous tags
        sanitized = sanitized.replaceAll("(?i)<\\s*/?\\s*(script|iframe|object|embed|link|meta|style)\\b[^>]*>", "");
        
        // Remove CSS expressions
        sanitized = sanitized.replaceAll("(?i)expression\\s*\\([^)]*\\)", "");
        
        // Remove data: URLs
        sanitized = sanitized.replaceAll("(?i)data\\s*:[^\\s>\"']*", "");
        
        return sanitized.trim();
    }

    /**
     * Validate email address format and check for suspicious domains
     * @param email The email address to validate
     * @return true if email is valid and from a trusted domain, false otherwise
     */
    public boolean isValidEmail(String email) {
        if (!StringUtils.hasText(email)) {
            return false;
        }

        String normalizedEmail = email.trim().toLowerCase();
        
        // Check basic email format
        if (!EMAIL_PATTERN.matcher(normalizedEmail).matches()) {
            return false;
        }
        
        // Check for suspicious domains
        if (SUSPICIOUS_DOMAINS.matcher(normalizedEmail).matches()) {
            log.warn("Suspicious email domain detected: {}", normalizedEmail);
            return false;
        }
        
        return true;
    }

    /**
     * Comprehensive input validation that checks for multiple attack vectors
     * @param input The input string to validate
     * @return ValidationResult containing validation status and details
     */
    public ValidationResult validateInput(String input) {
        if (!StringUtils.hasText(input)) {
            return ValidationResult.valid();
        }

        ValidationResult result = new ValidationResult();
        
        // Check for SQL injection
        if (containsSqlInjection(input)) {
            result.addViolation("SQL injection pattern detected");
        }
        
        // Check for XSS
        if (containsXss(input)) {
            result.addViolation("XSS pattern detected");
        }
        
        // Check for excessive length (potential DoS)
        if (input.length() > 10000) {
            result.addViolation("Input exceeds maximum allowed length");
        }
        
        // Check for null bytes (potential path traversal)
        if (input.contains("\0")) {
            result.addViolation("Null byte detected");
        }
        
        // Check for control characters
        if (input.chars().anyMatch(c -> c < 32 && c != 9 && c != 10 && c != 13)) {
            result.addViolation("Control characters detected");
        }
        
        return result;
    }

    /**
     * Validate and sanitize user input in one operation
     * @param input The input string to validate and sanitize
     * @return Sanitized input if validation passes, null if validation fails
     */
    public String validateAndSanitize(String input) {
        ValidationResult result = validateInput(input);
        
        if (!result.isValid()) {
            log.warn("Input validation failed: {}", result.getViolations());
            return null;
        }
        
        return sanitizeHtml(input);
    }

    /**
     * Check if a string contains only safe characters for identifiers
     * @param identifier The identifier to validate
     * @return true if identifier is safe, false otherwise
     */
    public boolean isSafeIdentifier(String identifier) {
        if (!StringUtils.hasText(identifier)) {
            return false;
        }
        
        // Allow only alphanumeric characters, hyphens, and underscores
        return identifier.matches("^[a-zA-Z0-9_-]+$") && identifier.length() <= 50;
    }

    /**
     * Validate phone number format
     * @param phoneNumber The phone number to validate
     * @return true if phone number format is valid, false otherwise
     */
    public boolean isValidPhoneNumber(String phoneNumber) {
        if (!StringUtils.hasText(phoneNumber)) {
            return false;
        }
        
        String normalized = phoneNumber.trim();
        
        // E.164 format validation - must not start with 0 after country code
        if (normalized.startsWith("+")) {
            // International format: +[1-9][0-9]{1,14}
            return normalized.matches("^\\+[1-9]\\d{1,14}$");
        } else {
            // National format: [1-9][0-9]{9,14}
            return normalized.matches("^[1-9]\\d{9,14}$");
        }
    }
}