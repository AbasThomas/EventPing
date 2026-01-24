package thomas.com.EventPing.security.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for InputValidationService
 * **Validates: Requirements 2.2, 2.3, 2.6**
 */
class InputValidationServiceUnitTest {

    private InputValidationService inputValidationService;

    @BeforeEach
    void setUp() {
        inputValidationService = new InputValidationService();
    }

    @Test
    @DisplayName("Should detect SQL injection patterns accurately")
    void shouldDetectSqlInjectionPatterns() {
        // SQL injection attempts
        assertThat(inputValidationService.containsSqlInjection("SELECT * FROM users")).isTrue();
        assertThat(inputValidationService.containsSqlInjection("' OR '1'='1")).isTrue();
        assertThat(inputValidationService.containsSqlInjection("'; DROP TABLE users;--")).isTrue();
        assertThat(inputValidationService.containsSqlInjection("admin'--")).isTrue();
        assertThat(inputValidationService.containsSqlInjection("' UNION SELECT * FROM users--")).isTrue();
        
        // Clean input should not be detected
        assertThat(inputValidationService.containsSqlInjection("John Doe")).isFalse();
        assertThat(inputValidationService.containsSqlInjection("Normal text input")).isFalse();
        assertThat(inputValidationService.containsSqlInjection("Hello World 123")).isFalse();
    }

    @Test
    @DisplayName("Should detect XSS patterns accurately")
    void shouldDetectXssPatterns() {
        // XSS attempts
        assertThat(inputValidationService.containsXss("<script>alert('xss')</script>")).isTrue();
        assertThat(inputValidationService.containsXss("<img src=x onerror=alert('xss')>")).isTrue();
        assertThat(inputValidationService.containsXss("javascript:alert('xss')")).isTrue();
        assertThat(inputValidationService.containsXss("onclick='alert(\"xss\")'")).isTrue();
        assertThat(inputValidationService.containsXss("<iframe src='javascript:alert(\"xss\")'></iframe>")).isTrue();
        
        // Clean input should not be detected
        assertThat(inputValidationService.containsXss("Hello World")).isFalse();
        assertThat(inputValidationService.containsXss("user@example.com")).isFalse();
        assertThat(inputValidationService.containsXss("Normal HTML like <b>bold</b>")).isFalse();
    }

    @Test
    @DisplayName("Should validate email addresses with domain checking")
    void shouldValidateEmailsWithDomainChecking() {
        // Valid emails
        assertThat(inputValidationService.isValidEmail("user@example.com")).isTrue();
        assertThat(inputValidationService.isValidEmail("test.email@domain.org")).isTrue();
        assertThat(inputValidationService.isValidEmail("user+tag@example.co.uk")).isTrue();
        
        // Invalid format
        assertThat(inputValidationService.isValidEmail("invalid-email")).isFalse();
        assertThat(inputValidationService.isValidEmail("@example.com")).isFalse();
        assertThat(inputValidationService.isValidEmail("user@")).isFalse();
        
        // Suspicious domains
        assertThat(inputValidationService.isValidEmail("user@tempmail.com")).isFalse();
        assertThat(inputValidationService.isValidEmail("user@10minutemail.com")).isFalse();
        assertThat(inputValidationService.isValidEmail("user@guerrillamail.com")).isFalse();
    }

    @Test
    @DisplayName("Should sanitize HTML content properly")
    void shouldSanitizeHtmlContent() {
        // Script tags should be removed
        String input = "<script>alert('xss')</script>Hello World";
        String sanitized = inputValidationService.sanitizeHtml(input);
        assertThat(sanitized).isEqualTo("Hello World");
        assertThat(inputValidationService.containsXss(sanitized)).isFalse();
        
        // Event handlers should be removed
        input = "<div onclick='alert(\"xss\")'>Content</div>";
        sanitized = inputValidationService.sanitizeHtml(input);
        assertThat(sanitized).doesNotContain("onclick");
        
        // JavaScript protocols should be removed
        input = "<a href='javascript:alert(\"xss\")'>Link</a>";
        sanitized = inputValidationService.sanitizeHtml(input);
        assertThat(sanitized).doesNotContain("javascript:");
    }

    @Test
    @DisplayName("Should validate safe identifiers")
    void shouldValidateSafeIdentifiers() {
        // Safe identifiers
        assertThat(inputValidationService.isSafeIdentifier("user123")).isTrue();
        assertThat(inputValidationService.isSafeIdentifier("test-identifier")).isTrue();
        assertThat(inputValidationService.isSafeIdentifier("safe_name")).isTrue();
        assertThat(inputValidationService.isSafeIdentifier("ID_123")).isTrue();
        
        // Unsafe identifiers
        assertThat(inputValidationService.isSafeIdentifier("user@domain")).isFalse();
        assertThat(inputValidationService.isSafeIdentifier("test identifier")).isFalse(); // space
        assertThat(inputValidationService.isSafeIdentifier("user/path")).isFalse(); // slash
        assertThat(inputValidationService.isSafeIdentifier("test<script>")).isFalse(); // HTML
        assertThat(inputValidationService.isSafeIdentifier("")).isFalse(); // empty
        assertThat(inputValidationService.isSafeIdentifier("a".repeat(51))).isFalse(); // too long
    }

    @Test
    @DisplayName("Should validate phone numbers correctly")
    void shouldValidatePhoneNumbers() {
        // Valid phone numbers
        assertThat(inputValidationService.isValidPhoneNumber("+1234567890")).isTrue();
        assertThat(inputValidationService.isValidPhoneNumber("1234567890")).isTrue();
        assertThat(inputValidationService.isValidPhoneNumber("+12345678901234")).isTrue();
        
        // Invalid phone numbers
        assertThat(inputValidationService.isValidPhoneNumber("")).isFalse();
        assertThat(inputValidationService.isValidPhoneNumber("123")).isFalse(); // too short
        assertThat(inputValidationService.isValidPhoneNumber("123456789012345678")).isFalse(); // too long
        assertThat(inputValidationService.isValidPhoneNumber("+0123456789")).isFalse(); // starts with 0
        assertThat(inputValidationService.isValidPhoneNumber("abc123456789")).isFalse(); // contains letters
    }

    @Test
    @DisplayName("Should perform comprehensive input validation")
    void shouldPerformComprehensiveInputValidation() {
        // Clean input should pass
        ValidationResult result = inputValidationService.validateInput("Hello World");
        assertThat(result.isValid()).isTrue();
        assertThat(result.hasViolations()).isFalse();
        
        // SQL injection should fail
        result = inputValidationService.validateInput("SELECT * FROM users");
        assertThat(result.isValid()).isFalse();
        assertThat(result.getViolations()).contains("SQL injection pattern detected");
        
        // XSS should fail
        result = inputValidationService.validateInput("<script>alert('xss')</script>");
        assertThat(result.isValid()).isFalse();
        assertThat(result.getViolations()).contains("XSS pattern detected");
        
        // Excessive length should fail
        result = inputValidationService.validateInput("a".repeat(10001));
        assertThat(result.isValid()).isFalse();
        assertThat(result.getViolations()).contains("Input exceeds maximum allowed length");
        
        // Null bytes should fail
        result = inputValidationService.validateInput("test\0null");
        assertThat(result.isValid()).isFalse();
        assertThat(result.getViolations()).contains("Null byte detected");
    }

    @Test
    @DisplayName("Should validate and sanitize input in one operation")
    void shouldValidateAndSanitizeInput() {
        // Clean input should be returned as-is
        String result = inputValidationService.validateAndSanitize("Hello World");
        assertThat(result).isEqualTo("Hello World");
        
        // Malicious input should return null
        result = inputValidationService.validateAndSanitize("SELECT * FROM users");
        assertThat(result).isNull();
        
        result = inputValidationService.validateAndSanitize("<script>alert('xss')</script>");
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("Should handle null and empty inputs gracefully")
    void shouldHandleNullAndEmptyInputsGracefully() {
        // Null inputs
        assertThat(inputValidationService.containsSqlInjection(null)).isFalse();
        assertThat(inputValidationService.containsXss(null)).isFalse();
        assertThat(inputValidationService.isValidEmail(null)).isFalse();
        assertThat(inputValidationService.isSafeIdentifier(null)).isFalse();
        assertThat(inputValidationService.isValidPhoneNumber(null)).isFalse();
        
        // Empty inputs
        assertThat(inputValidationService.containsSqlInjection("")).isFalse();
        assertThat(inputValidationService.containsXss("")).isFalse();
        assertThat(inputValidationService.isValidEmail("")).isFalse();
        assertThat(inputValidationService.isSafeIdentifier("")).isFalse();
        assertThat(inputValidationService.isValidPhoneNumber("")).isFalse();
        
        // Whitespace-only inputs
        assertThat(inputValidationService.containsSqlInjection("   ")).isFalse();
        assertThat(inputValidationService.containsXss("   ")).isFalse();
        assertThat(inputValidationService.isValidEmail("   ")).isFalse();
    }

    @Test
    @DisplayName("Should detect edge case attack patterns")
    void shouldDetectEdgeCaseAttackPatterns() {
        // Case variations
        assertThat(inputValidationService.containsSqlInjection("SeLeCt * FrOm UsErS")).isTrue();
        assertThat(inputValidationService.containsXss("<ScRiPt>alert('xss')</ScRiPt>")).isTrue();
        
        // Whitespace variations
        assertThat(inputValidationService.containsSqlInjection("SELECT\t*\nFROM\rusers")).isTrue();
        assertThat(inputValidationService.containsXss("<script >alert('xss')</script >")).isTrue();
    }
}