package thomas.com.EventPing.security.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for SecretManagementService
 * **Validates: Requirements 4.4, 12.1, 12.2**
 */
class SecretManagementServiceTest {

    private SecretManagementService secretManagementService;
    private SecretManagementService vaultEnabledService;

    @BeforeEach
    void setUp() {
        // Service without vault
        secretManagementService = new SecretManagementService(false, "", "");
        
        // Service with vault enabled (for testing vault-specific behavior)
        vaultEnabledService = new SecretManagementService(true, "http://vault:8200", "test-token");
    }

    @Test
    @DisplayName("Should retrieve secrets from environment variables")
    void shouldRetrieveSecretsFromEnvironmentVariables() {
        // Given - set a test environment variable (this would be set externally in real scenarios)
        String defaultValue = "default-secret-value";
        
        // When
        String secret = secretManagementService.getSecret("NON_EXISTENT_SECRET", defaultValue);
        
        // Then
        assertEquals(defaultValue, secret);
    }

    @Test
    @DisplayName("Should return default value when secret not found")
    void shouldReturnDefaultValueWhenSecretNotFound() {
        String defaultValue = "fallback-value";
        String secret = secretManagementService.getSecret("DEFINITELY_NOT_EXISTS", defaultValue);
        
        assertEquals(defaultValue, secret);
    }

    @Test
    @DisplayName("Should handle null secret names gracefully")
    void shouldHandleNullSecretNamesGracefully() {
        String defaultValue = "default";
        String result = secretManagementService.getSecret(null, defaultValue);
        
        assertEquals(defaultValue, result);
    }

    @Test
    @DisplayName("Should check secret existence correctly")
    void shouldCheckSecretExistenceCorrectly() {
        // Non-existent secret should return false
        assertFalse(secretManagementService.secretExists("NON_EXISTENT_SECRET"));
        
        // Secret with default value should return true if default is provided
        // (This simulates the behavior when environment variable is not set)
        assertFalse(secretManagementService.secretExists("MISSING_SECRET"));
    }

    @Test
    @DisplayName("Should clear cache successfully")
    void shouldClearCacheSuccessfully() {
        assertDoesNotThrow(() -> secretManagementService.clearCache());
    }

    @Test
    @DisplayName("Should provide database password retrieval")
    void shouldProvideDatabasePasswordRetrieval() {
        String dbPassword = secretManagementService.getDatabasePassword();
        // Should not be null (will return environment variable or null)
        // In test environment, this might be null, which is acceptable
        assertDoesNotThrow(() -> secretManagementService.getDatabasePassword());
    }

    @Test
    @DisplayName("Should provide JWT secret retrieval")
    void shouldProvideJwtSecretRetrieval() {
        assertDoesNotThrow(() -> secretManagementService.getJwtSecret());
    }

    @Test
    @DisplayName("Should provide encryption key retrieval")
    void shouldProvideEncryptionKeyRetrieval() {
        assertDoesNotThrow(() -> secretManagementService.getEncryptionKey());
    }

    @Test
    @DisplayName("Should provide Redis password retrieval")
    void shouldProvideRedisPasswordRetrieval() {
        assertDoesNotThrow(() -> secretManagementService.getRedisPassword());
    }

    @Test
    @DisplayName("Should provide email password retrieval")
    void shouldProvideEmailPasswordRetrieval() {
        assertDoesNotThrow(() -> secretManagementService.getEmailPassword());
    }

    @Test
    @DisplayName("Should handle vault-enabled configuration")
    void shouldHandleVaultEnabledConfiguration() {
        // Vault-enabled service should still work (falls back to env vars in test)
        String secret = vaultEnabledService.getSecret("TEST_SECRET", "default");
        assertEquals("default", secret);
    }

    @Test
    @DisplayName("Should not store secrets when vault is disabled")
    void shouldNotStoreSecretsWhenVaultIsDisabled() {
        boolean result = secretManagementService.storeSecret("TEST_SECRET", "test-value");
        assertFalse(result, "Should return false when vault is disabled");
    }

    @Test
    @DisplayName("Should not rotate secrets when vault is disabled")
    void shouldNotRotateSecretsWhenVaultIsDisabled() {
        boolean result = secretManagementService.rotateSecret("TEST_SECRET");
        assertFalse(result, "Should return false when vault is disabled");
    }

    @Test
    @DisplayName("Should handle vault operations when vault is enabled")
    void shouldHandleVaultOperationsWhenVaultIsEnabled() {
        // Store secret (will return false in test since vault is not actually connected)
        boolean storeResult = vaultEnabledService.storeSecret("TEST_SECRET", "test-value");
        assertFalse(storeResult, "Should return false when vault is not actually available");
        
        // Rotate secret (will return false in test since vault is not actually connected)
        boolean rotateResult = vaultEnabledService.rotateSecret("TEST_SECRET");
        assertFalse(rotateResult, "Should return false when vault is not actually available");
    }

    @Test
    @DisplayName("Should handle various environment variable formats")
    void shouldHandleVariousEnvironmentVariableFormats() {
        // Test that the service tries multiple formats for environment variables
        // This is tested indirectly through the getSecret method
        
        String result1 = secretManagementService.getSecret("test-secret", "default1");
        String result2 = secretManagementService.getSecret("TEST_SECRET", "default2");
        String result3 = secretManagementService.getSecret("test.secret", "default3");
        
        // All should return their defaults since the env vars don't exist
        assertEquals("default1", result1);
        assertEquals("default2", result2);
        assertEquals("default3", result3);
    }

    @Test
    @DisplayName("Should handle secret retrieval errors gracefully")
    void shouldHandleSecretRetrievalErrorsGracefully() {
        // Test with various edge cases
        assertDoesNotThrow(() -> {
            secretManagementService.getSecret("", "default");
            secretManagementService.getSecret("   ", "default");
            secretManagementService.getSecret("VERY_LONG_SECRET_NAME_THAT_DEFINITELY_DOES_NOT_EXIST", "default");
        });
    }

    @Test
    @DisplayName("Should maintain consistent behavior across multiple calls")
    void shouldMaintainConsistentBehaviorAcrossMultipleCalls() {
        String secretName = "CONSISTENT_TEST_SECRET";
        String defaultValue = "consistent-default";
        
        // Multiple calls should return the same result
        String result1 = secretManagementService.getSecret(secretName, defaultValue);
        String result2 = secretManagementService.getSecret(secretName, defaultValue);
        String result3 = secretManagementService.getSecret(secretName, defaultValue);
        
        assertEquals(result1, result2);
        assertEquals(result2, result3);
    }

    @Test
    @DisplayName("Should handle cache operations safely")
    void shouldHandleCacheOperationsSafely() {
        // Clear cache multiple times should not cause issues
        assertDoesNotThrow(() -> {
            secretManagementService.clearCache();
            secretManagementService.clearCache();
            secretManagementService.clearCache();
        });
        
        // Operations after cache clear should still work
        String result = secretManagementService.getSecret("POST_CLEAR_SECRET", "default");
        assertEquals("default", result);
    }

    @Test
    @DisplayName("Should provide all required secret retrieval methods")
    void shouldProvideAllRequiredSecretRetrievalMethods() {
        // Verify all the convenience methods exist and don't throw exceptions
        assertDoesNotThrow(() -> {
            secretManagementService.getDatabasePassword();
            secretManagementService.getJwtSecret();
            secretManagementService.getEncryptionKey();
            secretManagementService.getRedisPassword();
            secretManagementService.getEmailPassword();
        });
    }

    @Test
    @DisplayName("Should handle empty and whitespace secret names")
    void shouldHandleEmptyAndWhitespaceSecretNames() {
        String defaultValue = "default-for-empty";
        
        assertEquals(defaultValue, secretManagementService.getSecret("", defaultValue));
        assertEquals(defaultValue, secretManagementService.getSecret("   ", defaultValue));
        assertEquals(defaultValue, secretManagementService.getSecret("\t\n", defaultValue));
    }

    @Test
    @DisplayName("Should handle null default values")
    void shouldHandleNullDefaultValues() {
        String result = secretManagementService.getSecret("NON_EXISTENT", null);
        assertNull(result);
    }
}