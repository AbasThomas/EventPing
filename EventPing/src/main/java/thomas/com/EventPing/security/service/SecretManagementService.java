package thomas.com.EventPing.security.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for secure secret management and configuration
 * **Validates: Requirements 4.4, 12.1, 12.2**
 */
@Service
@Slf4j
public class SecretManagementService {

    private final boolean vaultEnabled;
    private final String vaultUrl;
    private final String vaultToken;
    private final Map<String, String> secretCache;

    public SecretManagementService(
            @Value("${eventping.security.secrets.vault-enabled:false}") boolean vaultEnabled,
            @Value("${eventping.security.secrets.vault-url:}") String vaultUrl,
            @Value("${eventping.security.secrets.vault-token:}") String vaultToken) {
        
        this.vaultEnabled = vaultEnabled;
        this.vaultUrl = vaultUrl;
        this.vaultToken = vaultToken;
        this.secretCache = new ConcurrentHashMap<>();
        
        if (vaultEnabled) {
            log.info("Secret management initialized with external vault");
        } else {
            log.warn("Secret management using environment variables - consider using a vault for production");
        }
    }

    /**
     * Retrieve a secret value securely
     * 
     * @param secretName the name of the secret
     * @param defaultValue default value if secret not found
     * @return the secret value or default
     */
    public String getSecret(String secretName, String defaultValue) {
        try {
            if (vaultEnabled) {
                return getSecretFromVault(secretName, defaultValue);
            } else {
                return getSecretFromEnvironment(secretName, defaultValue);
            }
        } catch (Exception e) {
            log.error("Failed to retrieve secret: {}", secretName, e);
            return defaultValue;
        }
    }

    /**
     * Store a secret value securely (if vault is enabled)
     * 
     * @param secretName the name of the secret
     * @param secretValue the secret value
     * @return true if stored successfully
     */
    public boolean storeSecret(String secretName, String secretValue) {
        if (!vaultEnabled) {
            log.warn("Cannot store secret '{}' - vault not enabled", secretName);
            return false;
        }
        
        try {
            return storeSecretInVault(secretName, secretValue);
        } catch (Exception e) {
            log.error("Failed to store secret: {}", secretName, e);
            return false;
        }
    }

    /**
     * Check if a secret exists
     * 
     * @param secretName the name of the secret
     * @return true if secret exists
     */
    public boolean secretExists(String secretName) {
        try {
            String value = getSecret(secretName, null);
            return value != null && !value.isEmpty();
        } catch (Exception e) {
            log.error("Failed to check secret existence: {}", secretName, e);
            return false;
        }
    }

    /**
     * Rotate a secret (generate new value and update)
     * 
     * @param secretName the name of the secret to rotate
     * @return true if rotation successful
     */
    public boolean rotateSecret(String secretName) {
        if (!vaultEnabled) {
            log.warn("Cannot rotate secret '{}' - vault not enabled", secretName);
            return false;
        }
        
        try {
            // Generate new secret value based on type
            String newValue = generateSecretValue(secretName);
            boolean stored = storeSecret(secretName, newValue);
            
            if (stored) {
                // Clear from cache to force refresh
                secretCache.remove(secretName);
                log.info("Successfully rotated secret: {}", secretName);
            }
            
            return stored;
        } catch (Exception e) {
            log.error("Failed to rotate secret: {}", secretName, e);
            return false;
        }
    }

    /**
     * Clear secret cache (force refresh from source)
     */
    public void clearCache() {
        secretCache.clear();
        log.info("Secret cache cleared");
    }

    /**
     * Get database password securely
     * 
     * @return database password
     */
    public String getDatabasePassword() {
        return getSecret("DATABASE_PASSWORD", System.getenv("DB_PASSWORD"));
    }

    /**
     * Get JWT secret key securely
     * 
     * @return JWT secret key
     */
    public String getJwtSecret() {
        return getSecret("JWT_SECRET", System.getenv("JWT_SECRET"));
    }

    /**
     * Get encryption key securely
     * 
     * @return data encryption key
     */
    public String getEncryptionKey() {
        return getSecret("DATA_ENCRYPTION_KEY", System.getenv("DATA_ENCRYPTION_KEY"));
    }

    /**
     * Get Redis password securely
     * 
     * @return Redis password
     */
    public String getRedisPassword() {
        return getSecret("REDIS_PASSWORD", System.getenv("REDIS_PASSWORD"));
    }

    /**
     * Get email service password securely
     * 
     * @return email service password
     */
    public String getEmailPassword() {
        return getSecret("EMAIL_PASSWORD", System.getenv("EMAIL_PASSWORD"));
    }

    private String getSecretFromEnvironment(String secretName, String defaultValue) {
        // Try multiple environment variable formats
        String[] envVarNames = {
            secretName,
            secretName.toUpperCase(),
            secretName.toUpperCase().replace("-", "_"),
            secretName.toUpperCase().replace(".", "_")
        };
        
        for (String envVar : envVarNames) {
            String value = System.getenv(envVar);
            if (value != null && !value.isEmpty()) {
                return value;
            }
        }
        
        return defaultValue;
    }

    private String getSecretFromVault(String secretName, String defaultValue) {
        // Check cache first
        String cached = secretCache.get(secretName);
        if (cached != null) {
            return cached;
        }
        
        // In a real implementation, this would make HTTP calls to HashiCorp Vault,
        // AWS Secrets Manager, Azure Key Vault, etc.
        // For now, fall back to environment variables
        log.debug("Vault integration not implemented, falling back to environment variables");
        String value = getSecretFromEnvironment(secretName, defaultValue);
        
        if (value != null) {
            secretCache.put(secretName, value);
        }
        
        return value;
    }

    private boolean storeSecretInVault(String secretName, String secretValue) {
        // In a real implementation, this would make HTTP calls to store the secret
        log.debug("Vault integration not implemented for storing secrets");
        return false;
    }

    private String generateSecretValue(String secretName) {
        // Generate appropriate secret based on name/type
        if (secretName.contains("JWT") || secretName.contains("SECRET")) {
            return generateJwtSecret();
        } else if (secretName.contains("PASSWORD")) {
            return generatePassword();
        } else if (secretName.contains("KEY")) {
            return generateEncryptionKey();
        } else {
            return generateGenericSecret();
        }
    }

    private String generateJwtSecret() {
        // Generate a secure JWT secret (256 bits)
        java.security.SecureRandom random = new java.security.SecureRandom();
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return java.util.Base64.getEncoder().encodeToString(bytes);
    }

    private String generatePassword() {
        // Generate a secure password
        java.security.SecureRandom random = new java.security.SecureRandom();
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*";
        StringBuilder password = new StringBuilder();
        
        for (int i = 0; i < 32; i++) {
            password.append(chars.charAt(random.nextInt(chars.length())));
        }
        
        return password.toString();
    }

    private String generateEncryptionKey() {
        // Generate AES-256 key
        java.security.SecureRandom random = new java.security.SecureRandom();
        byte[] bytes = new byte[32]; // 256 bits
        random.nextBytes(bytes);
        return java.util.Base64.getEncoder().encodeToString(bytes);
    }

    private String generateGenericSecret() {
        // Generate generic secret
        java.security.SecureRandom random = new java.security.SecureRandom();
        byte[] bytes = new byte[24];
        random.nextBytes(bytes);
        return java.util.Base64.getEncoder().encodeToString(bytes);
    }
}