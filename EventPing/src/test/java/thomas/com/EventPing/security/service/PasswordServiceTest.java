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
    @DisplayName("Should handle null password gracefully")
    void shouldHandleNullPasswordGracefully() {
        // When & Then
        assertThatThrownBy(() -> passwordService.hashPassword(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Password cannot be null or empty");
    }

    @Test
    @DisplayName("Should handle empty password gracefully")
    void shouldHandleEmptyPasswordGracefully() {
        // When & Then
        assertThatThrownBy(() -> passwordService.hashPassword(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Password cannot be null or empty");

        assertThatThrownBy(() -> passwordService.hashPassword("   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Password cannot be null or empty");
    }

    @Test
    @DisplayName("Should return false for null verification inputs")
    void shouldReturnFalseForNullVerificationInputs() {
        // Given
        String validPassword = "TestPassword123!";
        String hashedPassword = passwordService.hashPassword(validPassword);

        // When & Then
        assertThat(passwordService.verifyPassword(null, hashedPassword)).isFalse();
        assertThat(passwordService.verifyPassword(validPassword, null)).isFalse();
        assertThat(passwordService.verifyPassword(null, null)).isFalse();
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
    @DisplayName("Should detect when password needs rehash")
    void shouldDetectWhenPasswordNeedsRehash() {
        // Given
        String oldFormatHash = "$2a$10$abcdefghijklmnopqrstuvwxyz"; // Cost 10, not 12
        String currentFormatHash = passwordService.hashPassword("TestPassword123!");

        // When & Then
        assertThat(passwordService.needsRehash(null)).isTrue();
        assertThat(passwordService.needsRehash(oldFormatHash)).isTrue();
        assertThat(passwordService.needsRehash(currentFormatHash)).isFalse();
    }

    @Test
    @DisplayName("Should handle complex passwords correctly")
    void shouldHandleComplexPasswordsCorrectly() {
        // Given
        String complexPassword = "Th1s!s@V3ry#C0mpl3x$P@ssw0rd%W1th&M@ny*Ch@r@ct3rs";

        // When
        String hashedPassword = passwordService.hashPassword(complexPassword);

        // Then
        assertThat(hashedPassword).isNotNull();
        assertThat(passwordService.verifyPassword(complexPassword, hashedPassword)).isTrue();
        assertThat(passwordService.verifyPassword("WrongPassword", hashedPassword)).isFalse();
    }

    @Test
    @DisplayName("Should handle unicode characters in passwords")
    void shouldHandleUnicodeCharactersInPasswords() {
        // Given
        String unicodePassword = "Pässwörd123!@#中文密码";

        // When
        String hashedPassword = passwordService.hashPassword(unicodePassword);

        // Then
        assertThat(hashedPassword).isNotNull();
        assertThat(passwordService.verifyPassword(unicodePassword, hashedPassword)).isTrue();
        assertThat(passwordService.verifyPassword("WrongPassword", hashedPassword)).isFalse();
    }
}