package thomas.com.EventPing.security.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for PasswordService
 * Tests password hashing and verification functionality
 * **Validates: Requirements 1.1, 1.2, 1.3**
 */
class PasswordServiceTest {

    private PasswordService passwordService;

    @BeforeEach
    void setUp() {
        passwordService = new PasswordService();
    }

    @Test
    @DisplayName("Should hash password successfully")
    void shouldHashPasswordSuccessfully() {
        // Given
        String plainPassword = "TestPassword123!";

        // When
        String hashedPassword = passwordService.hashPassword(plainPassword);

        // Then
        assertThat(hashedPassword).isNotNull();
        assertThat(hashedPassword).isNotEmpty();
        assertThat(hashedPassword).isNotEqualTo(plainPassword);
        assertThat(hashedPassword).startsWith("$2");
        assertThat(hashedPassword.length()).isGreaterThan(50);
    }

    @Test
    @DisplayName("Should verify correct password")
    void shouldVerifyCorrectPassword() {
        // Given
        String plainPassword = "TestPassword123!";
        String hashedPassword = passwordService.hashPassword(plainPassword);

        // When
        boolean isValid = passwordService.verifyPassword(plainPassword, hashedPassword);

        // Then
        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("Should reject incorrect password")
    void shouldRejectIncorrectPassword() {
        // Given
        String plainPassword = "TestPassword123!";
        String wrongPassword = "WrongPassword456!";
        String hashedPassword = passwordService.hashPassword(plainPassword);

        // When
        boolean isValid = passwordService.verifyPassword(wrongPassword, hashedPassword);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Should generate different hashes for same password")
    void shouldGenerateDifferentHashesForSamePassword() {
        // Given
        String plainPassword = "TestPassword123!";

        // When
        String hash1 = passwordService.hashPassword(plainPassword);
        String hash2 = passwordService.hashPassword(plainPassword);

        // Then
        assertThat(hash1).isNotEqualTo(hash2);
        assertThat(passwordService.verifyPassword(plainPassword, hash1)).isTrue();
        assertThat(passwordService.verifyPassword(plainPassword, hash2)).isTrue();
    }

    @Test
    @DisplayName("Should throw exception for null password")
    void shouldThrowExceptionForNullPassword() {
        // When & Then
        assertThatThrownBy(() -> passwordService.hashPassword(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Password cannot be null or empty");
    }

    @Test
    @DisplayName("Should throw exception for empty password")
    void shouldThrowExceptionForEmptyPassword() {
        // When & Then
        assertThatThrownBy(() -> passwordService.hashPassword(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Password cannot be null or empty");

        assertThatThrownBy(() -> passwordService.hashPassword("   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Password cannot be null or empty");
    }

    @Test
    @DisplayName("Should return false for null password verification")
    void shouldReturnFalseForNullPasswordVerification() {
        // Given
        String hashedPassword = passwordService.hashPassword("TestPassword123!");

        // When & Then
        assertThat(passwordService.verifyPassword(null, hashedPassword)).isFalse();
        assertThat(passwordService.verifyPassword("TestPassword123!", null)).isFalse();
        assertThat(passwordService.verifyPassword(null, null)).isFalse();
    }

    @Test
    @DisplayName("Should detect if password needs rehash")
    void shouldDetectIfPasswordNeedsRehash() {
        // Given
        String plainPassword = "TestPassword123!";
        String currentHash = passwordService.hashPassword(plainPassword);
        
        // Simulate old hash with different cost
        String oldHash = "$2a$10$abcdefghijklmnopqrstuvwxyz1234567890123456789012";

        // When & Then
        assertThat(passwordService.needsRehash(currentHash)).isFalse();
        assertThat(passwordService.needsRehash(oldHash)).isTrue();
        assertThat(passwordService.needsRehash(null)).isTrue();
        assertThat(passwordService.needsRehash("invalid-hash")).isTrue();
    }

    @Test
    @DisplayName("Should handle special characters in password")
    void shouldHandleSpecialCharactersInPassword() {
        // Given
        String passwordWithSpecialChars = "P@ssw0rd!#$%^&*()_+-=[]{}|;':\",./<>?";

        // When
        String hashedPassword = passwordService.hashPassword(passwordWithSpecialChars);
        boolean isValid = passwordService.verifyPassword(passwordWithSpecialChars, hashedPassword);

        // Then
        assertThat(hashedPassword).isNotNull();
        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("Should handle unicode characters in password")
    void shouldHandleUnicodeCharactersInPassword() {
        // Given
        String unicodePassword = "Pässwörd123!こんにちは";

        // When
        String hashedPassword = passwordService.hashPassword(unicodePassword);
        boolean isValid = passwordService.verifyPassword(unicodePassword, hashedPassword);

        // Then
        assertThat(hashedPassword).isNotNull();
        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("Should handle very long passwords")
    void shouldHandleVeryLongPasswords() {
        // Given
        String longPassword = "a".repeat(1000) + "B1!";

        // When
        String hashedPassword = passwordService.hashPassword(longPassword);
        boolean isValid = passwordService.verifyPassword(longPassword, hashedPassword);

        // Then
        assertThat(hashedPassword).isNotNull();
        assertThat(isValid).isTrue();
    }
}