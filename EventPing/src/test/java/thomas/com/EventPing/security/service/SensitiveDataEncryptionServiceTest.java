package thomas.com.EventPing.security.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for SensitiveDataEncryptionService
 * **Validates: Requirements 4.1, 4.3, 4.4**
 */
class SensitiveDataEncryptionServiceTest {

    private SensitiveDataEncryptionService encryptionService;
    private SensitiveDataEncryptionService disabledEncryptionService;

    @BeforeEach
    void setUp() {
        // Service with encryption enabled
        encryptionService = new SensitiveDataEncryptionService("", true);
        
        // Service with encryption disabled
        disabledEncryptionService = new SensitiveDataEncryptionService("", false);
    }

    @Test
    @DisplayName("Should encrypt and decrypt sensitive data correctly")
    void shouldEncryptAndDecryptSensitiveDataCorrectly() {
        // Given
        String sensitiveData = "john.doe@example.com";
        
        // When
        String encrypted = encryptionService.encryptSensitiveData(sensitiveData);
        String decrypted = encryptionService.decryptSensitiveData(encrypted);
        
        // Then
        assertNotNull(encrypted);
        assertTrue(encrypted.startsWith("ENC:"));
        assertNotEquals(sensitiveData, encrypted);
        assertEquals(sensitiveData, decrypted);
    }

    @Test
    @DisplayName("Should handle null and empty data gracefully")
    void shouldHandleNullAndEmptyDataGracefully() {
        // Null data
        assertNull(encryptionService.encryptSensitiveData(null));
        assertNull(encryptionService.decryptSensitiveData(null));
        
        // Empty data
        assertEquals("", encryptionService.encryptSensitiveData(""));
        assertEquals("", encryptionService.decryptSensitiveData(""));
    }

    @Test
    @DisplayName("Should not double-encrypt already encrypted data")
    void shouldNotDoubleEncryptAlreadyEncryptedData() {
        // Given
        String sensitiveData = "sensitive-information";
        String encrypted = encryptionService.encryptSensitiveData(sensitiveData);
        
        // When
        String doubleEncrypted = encryptionService.encryptSensitiveData(encrypted);
        
        // Then
        assertEquals(encrypted, doubleEncrypted);
    }

    @Test
    @DisplayName("Should correctly identify encrypted data")
    void shouldCorrectlyIdentifyEncryptedData() {
        String plainText = "plain text data";
        String encrypted = encryptionService.encryptSensitiveData(plainText);
        
        assertFalse(encryptionService.isEncrypted(plainText));
        assertTrue(encryptionService.isEncrypted(encrypted));
        assertFalse(encryptionService.isEncrypted(null));
        assertFalse(encryptionService.isEncrypted(""));
    }

    @Test
    @DisplayName("Should handle email encryption specifically")
    void shouldHandleEmailEncryptionSpecifically() {
        String validEmail = "user@example.com";
        String invalidEmail = "not-an-email";
        
        // Valid email should be encrypted
        String encryptedEmail = encryptionService.encryptEmail(validEmail);
        assertTrue(encryptedEmail.startsWith("ENC:"));
        assertEquals(validEmail, encryptionService.decryptEmail(encryptedEmail));
        
        // Invalid email should pass through unchanged
        assertEquals(invalidEmail, encryptionService.encryptEmail(invalidEmail));
    }

    @Test
    @DisplayName("Should handle phone number encryption specifically")
    void shouldHandlePhoneNumberEncryptionSpecifically() {
        String validPhone = "+1234567890";
        String invalidPhone = "not-a-phone";
        
        // Valid phone should be encrypted
        String encryptedPhone = encryptionService.encryptPhoneNumber(validPhone);
        assertTrue(encryptedPhone.startsWith("ENC:"));
        assertEquals(validPhone, encryptionService.decryptPhoneNumber(encryptedPhone));
        
        // Invalid phone should pass through unchanged
        assertEquals(invalidPhone, encryptionService.encryptPhoneNumber(invalidPhone));
    }

    @Test
    @DisplayName("Should handle personal information encryption")
    void shouldHandlePersonalInformationEncryption() {
        String personalInfo = "John Doe";
        String emptyInfo = "";
        String nullInfo = null;
        
        // Valid personal info should be encrypted
        String encrypted = encryptionService.encryptPersonalInfo(personalInfo);
        assertTrue(encrypted.startsWith("ENC:"));
        assertEquals(personalInfo, encryptionService.decryptPersonalInfo(encrypted));
        
        // Empty and null should pass through
        assertEquals(emptyInfo, encryptionService.encryptPersonalInfo(emptyInfo));
        assertEquals(nullInfo, encryptionService.encryptPersonalInfo(nullInfo));
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "john.doe@example.com",
        "user@domain.org",
        "+1234567890",
        "+44123456789",
        "123-45-6789",
        "John Smith",
        "Dr. Jane Doe"
    })
    @DisplayName("Should detect sensitive data patterns")
    void shouldDetectSensitiveDataPatterns(String sensitiveData) {
        assertTrue(encryptionService.containsSensitiveData(sensitiveData),
            "Should detect sensitive data: " + sensitiveData);
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "regular text",
        "123456",
        "not-sensitive",
        "random data"
    })
    @DisplayName("Should not detect non-sensitive data as sensitive")
    void shouldNotDetectNonSensitiveDataAsSensitive(String nonSensitiveData) {
        assertFalse(encryptionService.containsSensitiveData(nonSensitiveData),
            "Should not detect as sensitive: " + nonSensitiveData);
    }

    @Test
    @DisplayName("Should mask sensitive data for logging")
    void shouldMaskSensitiveDataForLogging() {
        assertEquals("***", encryptionService.maskForLogging(null));
        assertEquals("***", encryptionService.maskForLogging(""));
        assertEquals("a***", encryptionService.maskForLogging("ab"));
        assertEquals("a***", encryptionService.maskForLogging("abc"));
        assertEquals("a**d", encryptionService.maskForLogging("abcd"));
        assertEquals("j***************m", encryptionService.maskForLogging("john.doe@example.com"));
    }

    @Test
    @DisplayName("Should work with disabled encryption")
    void shouldWorkWithDisabledEncryption() {
        String sensitiveData = "sensitive-data";
        
        // With encryption disabled, data should pass through unchanged
        assertEquals(sensitiveData, disabledEncryptionService.encryptSensitiveData(sensitiveData));
        assertEquals(sensitiveData, disabledEncryptionService.decryptSensitiveData(sensitiveData));
        
        // Should still detect sensitive patterns
        assertTrue(disabledEncryptionService.containsSensitiveData("user@example.com"));
        
        // Should still mask for logging
        assertEquals("u***************m", disabledEncryptionService.maskForLogging("user@example.com"));
    }

    @Test
    @DisplayName("Should generate different encrypted values for same input")
    void shouldGenerateDifferentEncryptedValuesForSameInput() {
        String sensitiveData = "same-input-data";
        
        String encrypted1 = encryptionService.encryptSensitiveData(sensitiveData);
        String encrypted2 = encryptionService.encryptSensitiveData(sensitiveData);
        
        // Should be different due to random IVs
        assertNotEquals(encrypted1, encrypted2);
        
        // But both should decrypt to same original
        assertEquals(sensitiveData, encryptionService.decryptSensitiveData(encrypted1));
        assertEquals(sensitiveData, encryptionService.decryptSensitiveData(encrypted2));
    }

    @Test
    @DisplayName("Should handle decryption of non-encrypted data")
    void shouldHandleDecryptionOfNonEncryptedData() {
        String plainData = "not-encrypted-data";
        
        // Should return original data if not encrypted
        assertEquals(plainData, encryptionService.decryptSensitiveData(plainData));
    }

    @Test
    @DisplayName("Should handle various email formats")
    void shouldHandleVariousEmailFormats() {
        String[] validEmails = {
            "simple@example.com",
            "user.name@example.com",
            "user+tag@example.com",
            "user123@example-domain.com",
            "test@sub.domain.org"
        };
        
        for (String email : validEmails) {
            String encrypted = encryptionService.encryptEmail(email);
            assertTrue(encrypted.startsWith("ENC:"), "Should encrypt valid email: " + email);
            assertEquals(email, encryptionService.decryptEmail(encrypted));
        }
    }

    @Test
    @DisplayName("Should handle various phone number formats")
    void shouldHandleVariousPhoneNumberFormats() {
        String[] validPhones = {
            "+1234567890",
            "+44123456789",
            "+861234567890",
            "1234567890"
        };
        
        for (String phone : validPhones) {
            String encrypted = encryptionService.encryptPhoneNumber(phone);
            assertTrue(encrypted.startsWith("ENC:"), "Should encrypt valid phone: " + phone);
            assertEquals(phone, encryptionService.decryptPhoneNumber(encrypted));
        }
    }

    @Test
    @DisplayName("Should generate new encryption key successfully")
    void shouldGenerateNewEncryptionKeySuccessfully() {
        assertDoesNotThrow(() -> {
            var key = encryptionService.generateNewEncryptionKey();
            assertNotNull(key);
            assertEquals("AES", key.getAlgorithm());
        });
    }

    @Test
    @DisplayName("Should handle encryption errors gracefully")
    void shouldHandleEncryptionErrorsGracefully() {
        // This test verifies that the service handles internal encryption errors
        // In a real scenario, this might involve corrupted data or key issues
        
        // Test with malformed encrypted data
        assertThrows(RuntimeException.class, () -> {
            encryptionService.decryptSensitiveData("ENC:invalid-base64-data!");
        });
    }

    @Test
    @DisplayName("Should maintain data integrity across multiple operations")
    void shouldMaintainDataIntegrityAcrossMultipleOperations() {
        String[] testData = {
            "user@example.com",
            "+1234567890",
            "John Doe",
            "Sensitive Information",
            "123 Main St, City, State"
        };
        
        for (String data : testData) {
            // Encrypt
            String encrypted = encryptionService.encryptSensitiveData(data);
            
            // Verify encrypted format
            assertTrue(encrypted.startsWith("ENC:"));
            assertNotEquals(data, encrypted);
            
            // Decrypt and verify
            String decrypted = encryptionService.decryptSensitiveData(encrypted);
            assertEquals(data, decrypted);
            
            // Verify idempotency
            String reEncrypted = encryptionService.encryptSensitiveData(encrypted);
            assertEquals(encrypted, reEncrypted);
        }
    }
}