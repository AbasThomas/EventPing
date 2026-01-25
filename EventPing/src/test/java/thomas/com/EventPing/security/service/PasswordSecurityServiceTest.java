package thomas.com.EventPing.security.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for PasswordSecurityService
 * **Validates: Requirements 4.3, 4.4**
 */
class PasswordSecurityServiceTest {

    private PasswordSecurityService passwordSecurityService;

    @BeforeEach
    void setUp() {
        passwordSecurityService = new PasswordSecurityService(12, 8, true);
    }

    @Test
    @DisplayName("Should hash password with BCrypt and verify correctly")
    void shouldHashPasswordWithBCryptAndVerify() {
        // Given
        String plainPassword = "SecurePass123!";
        
        // When
        String hashedPassword = passwordSecurityService.hashPassword(plainPassword);
        
        // Then
        assertNotNull(hashedPassword);
        assertTrue(hashedPassword.startsWith("$2"));
        assertNotEquals(plainPassword, hashedPassword);
        assertTrue(passwordSecurityService.verifyPassword(plainPassword, hashedPassword));
        assertFalse(passwordSecurityService.verifyPassword("WrongPassword", hashedPassword));
    }

    @Test
    @DisplayName("Should use minimum 12 BCrypt rounds")
    void shouldUseMinimum12BCryptRounds() {
        // Given
        String plainPassword = "SecurePass123!";
        
        // When
        String hashedPassword = passwordSecurityService.hashPassword(plainPassword);
        
        // Then
        String[] parts = hashedPassword.split("\\$");
        int rounds = Integer.parseInt(parts[2]);
        assertTrue(rounds >= 12, "BCrypt rounds should be at least 12, but was " + rounds);
    }

    @Test
    @DisplayName("Should validate password strength requirements")
    void shouldValidatePasswordStrengthRequirements() {
        // Valid password should not throw exception
        assertDoesNotThrow(() -> passwordSecurityService.validatePasswordStrength("SecurePass123!"));
        
        // Test various invalid passwords
        assertThrows(IllegalArgumentException.class, 
            () -> passwordSecurityService.validatePasswordStrength("short"), 
            "Should reject short passwords");
        
        assertThrows(IllegalArgumentException.class, 
            () -> passwordSecurityService.validatePasswordStrength("nouppercase123!"), 
            "Should reject passwords without uppercase");
        
        assertThrows(IllegalArgumentException.class, 
            () -> passwordSecurityService.validatePasswordStrength("NOLOWERCASE123!"), 
            "Should reject passwords without lowercase");
        
        assertThrows(IllegalArgumentException.class, 
            () -> passwordSecurityService.validatePasswordStrength("NoDigits!"), 
            "Should reject passwords without digits");
        
        assertThrows(IllegalArgumentException.class, 
            () -> passwordSecurityService.validatePasswordStrength("NoSpecialChars123"), 
            "Should reject passwords without special characters");
    }

    @Test
    @DisplayName("Should reject null and empty passwords")
    void shouldRejectNullAndEmptyPasswords() {
        assertThrows(IllegalArgumentException.class, 
            () -> passwordSecurityService.hashPassword(null));
        
        assertThrows(IllegalArgumentException.class, 
            () -> passwordSecurityService.hashPassword(""));
        
        assertThrows(IllegalArgumentException.class, 
            () -> passwordSecurityService.validatePasswordStrength(null));
    }

    @Test
    @DisplayName("Should reject passwords exceeding BCrypt limit")
    void shouldRejectPasswordsExceedingBCryptLimit() {
        String longPassword = "A".repeat(73) + "1!"; // 75 characters
        
        assertThrows(IllegalArgumentException.class, 
            () -> passwordSecurityService.validatePasswordStrength(longPassword),
            "Should reject passwords over 72 characters");
    }

    @Test
    @DisplayName("Should reject common weak passwords")
    void shouldRejectCommonWeakPasswords() {
        String[] weakPasswords = {
            "Password123!",
            "Admin123!",
            "Qwerty123!",
            "Welcome123!",
            "Letmein123!"
        };
        
        for (String weakPassword : weakPasswords) {
            assertThrows(IllegalArgumentException.class, 
                () -> passwordSecurityService.validatePasswordStrength(weakPassword),
                "Should reject weak password: " + weakPassword);
        }
    }

    @Test
    @DisplayName("Should generate secure passwords meeting requirements")
    void shouldGenerateSecurePasswordsMeetingRequirements() {
        // When
        String generatedPassword = passwordSecurityService.generateSecurePassword(16);
        
        // Then
        assertNotNull(generatedPassword);
        assertEquals(16, generatedPassword.length());
        assertDoesNotThrow(() -> passwordSecurityService.validatePasswordStrength(generatedPassword));
    }

    @Test
    @DisplayName("Should handle password verification edge cases")
    void shouldHandlePasswordVerificationEdgeCases() {
        String validPassword = "SecurePass123!";
        String hashedPassword = passwordSecurityService.hashPassword(validPassword);
        
        // Null inputs should return false
        assertFalse(passwordSecurityService.verifyPassword(null, hashedPassword));
        assertFalse(passwordSecurityService.verifyPassword(validPassword, null));
        assertFalse(passwordSecurityService.verifyPassword(null, null));
        
        // Invalid hash format should return false
        assertFalse(passwordSecurityService.verifyPassword(validPassword, "invalid-hash"));
    }

    @Test
    @DisplayName("Should detect when password needs rehashing")
    void shouldDetectWhenPasswordNeedsRehashing() {
        // Valid BCrypt hash with 12 rounds should not need rehashing
        String validHash = passwordSecurityService.hashPassword("TestPass123!");
        assertFalse(passwordSecurityService.needsRehash(validHash));
        
        // Invalid or old format hashes should need rehashing
        assertTrue(passwordSecurityService.needsRehash(null));
        assertTrue(passwordSecurityService.needsRehash(""));
        assertTrue(passwordSecurityService.needsRehash("plain-text-password"));
        assertTrue(passwordSecurityService.needsRehash("$2a$10$invalidhash")); // Lower rounds
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "SecurePass123!",
        "MyStr0ngP@ssw0rd",
        "C0mpl3x!P@ssw0rd",
        "Sup3rS3cur3#2024"
    })
    @DisplayName("Should calculate reasonable password strength scores")
    void shouldCalculateReasonablePasswordStrengthScores(String password) {
        int strength = passwordSecurityService.getPasswordStrength(password);
        
        assertTrue(strength >= 70, "Strong passwords should score at least 70, got " + strength);
        assertTrue(strength <= 100, "Password strength should not exceed 100, got " + strength);
    }

    @Test
    @DisplayName("Should give low scores to weak passwords")
    void shouldGiveLowScoresToWeakPasswords() {
        String[] weakPasswords = {
            "",
            "123",
            "password",
            "abc123",
            "aaaaaa"
        };
        
        for (String weakPassword : weakPasswords) {
            int strength = passwordSecurityService.getPasswordStrength(weakPassword);
            assertTrue(strength < 50, 
                "Weak password '" + weakPassword + "' should score less than 50, got " + strength);
        }
    }

    @Test
    @DisplayName("Should handle minimum password length configuration")
    void shouldHandleMinimumPasswordLengthConfiguration() {
        // Create service with different minimum length
        PasswordSecurityService customService = new PasswordSecurityService(12, 10, true);
        
        // Should reject passwords shorter than configured minimum
        assertThrows(IllegalArgumentException.class, 
            () -> customService.validatePasswordStrength("Short1!"));
        
        // Should accept passwords meeting the minimum
        assertDoesNotThrow(() -> customService.validatePasswordStrength("LongEnough1!"));
    }

    @Test
    @DisplayName("Should handle special character requirement configuration")
    void shouldHandleSpecialCharacterRequirementConfiguration() {
        // Create service without special character requirement
        PasswordSecurityService noSpecialCharsService = new PasswordSecurityService(12, 8, false);
        
        // Should accept passwords without special characters
        assertDoesNotThrow(() -> noSpecialCharsService.validatePasswordStrength("Password123"));
        
        // Should still require other criteria
        assertThrows(IllegalArgumentException.class, 
            () -> noSpecialCharsService.validatePasswordStrength("password123"));
    }

    @Test
    @DisplayName("Should generate different passwords each time")
    void shouldGenerateDifferentPasswordsEachTime() {
        String password1 = passwordSecurityService.generateSecurePassword(12);
        String password2 = passwordSecurityService.generateSecurePassword(12);
        String password3 = passwordSecurityService.generateSecurePassword(12);
        
        assertNotEquals(password1, password2);
        assertNotEquals(password2, password3);
        assertNotEquals(password1, password3);
    }

    @Test
    @DisplayName("Should respect password length limits in generation")
    void shouldRespectPasswordLengthLimitsInGeneration() {
        // Test minimum length enforcement
        String shortPassword = passwordSecurityService.generateSecurePassword(4);
        assertTrue(shortPassword.length() >= 8, "Generated password should respect minimum length");
        
        // Test maximum length enforcement
        String longPassword = passwordSecurityService.generateSecurePassword(100);
        assertTrue(longPassword.length() <= 72, "Generated password should respect maximum length");
    }
}