package thomas.com.EventPing.security.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.regex.Pattern;

/**
 * Service for encrypting and decrypting sensitive data (PII)
 * **Validates: Requirements 4.1, 4.3, 4.4**
 */
@Service
@Slf4j
public class SensitiveDataEncryptionService {

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 16;
    private static final String ENCRYPTED_PREFIX = "ENC:";
    
    // Patterns for detecting sensitive data
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");
    private static final Pattern PHONE_PATTERN = Pattern.compile(
        "^\\+?[1-9]\\d{1,14}$");
    private static final Pattern SSN_PATTERN = Pattern.compile(
        "^\\d{3}-?\\d{2}-?\\d{4}$");
    
    private final SecretKey encryptionKey;
    private final SecureRandom secureRandom;
    private final boolean encryptionEnabled;

    public SensitiveDataEncryptionService(
            @Value("${eventping.security.encryption.key:}") String encryptionKeyBase64,
            @Value("${eventping.security.encryption.enabled:true}") boolean encryptionEnabled) {
        
        this.encryptionEnabled = encryptionEnabled;
        this.secureRandom = new SecureRandom();
        
        if (encryptionEnabled) {
            if (encryptionKeyBase64 != null && !encryptionKeyBase64.isEmpty()) {
                // Use provided key
                byte[] keyBytes = Base64.getDecoder().decode(encryptionKeyBase64);
                this.encryptionKey = new SecretKeySpec(keyBytes, ALGORITHM);
                log.info("Sensitive data encryption initialized with provided key");
            } else {
                // Generate new key (for development/testing)
                this.encryptionKey = generateNewEncryptionKey();
                log.warn("Sensitive data encryption initialized with generated key - not suitable for production");
            }
        } else {
            this.encryptionKey = null;
            log.warn("Sensitive data encryption is DISABLED - not recommended for production");
        }
    }

    /**
     * Encrypt sensitive data for database storage
     * 
     * @param plainText the sensitive data to encrypt
     * @return encrypted data with prefix, or original data if encryption disabled
     */
    public String encryptSensitiveData(String plainText) {
        if (!encryptionEnabled || plainText == null || plainText.isEmpty()) {
            return plainText;
        }
        
        // Don't double-encrypt
        if (isEncrypted(plainText)) {
            return plainText;
        }
        
        try {
            String encrypted = encrypt(plainText);
            return ENCRYPTED_PREFIX + encrypted;
        } catch (Exception e) {
            log.error("Failed to encrypt sensitive data", e);
            throw new RuntimeException("Encryption failed", e);
        }
    }

    /**
     * Decrypt sensitive data from database storage
     * 
     * @param encryptedData the encrypted data with prefix
     * @return decrypted plain text, or original data if not encrypted
     */
    public String decryptSensitiveData(String encryptedData) {
        if (!encryptionEnabled || encryptedData == null || encryptedData.isEmpty()) {
            return encryptedData;
        }
        
        if (!isEncrypted(encryptedData)) {
            return encryptedData;
        }
        
        try {
            String encryptedContent = encryptedData.substring(ENCRYPTED_PREFIX.length());
            return decrypt(encryptedContent);
        } catch (Exception e) {
            log.error("Failed to decrypt sensitive data", e);
            throw new RuntimeException("Decryption failed", e);
        }
    }

    /**
     * Check if data is encrypted (has encryption prefix)
     * 
     * @param data the data to check
     * @return true if data appears to be encrypted
     */
    public boolean isEncrypted(String data) {
        return data != null && data.startsWith(ENCRYPTED_PREFIX);
    }

    /**
     * Encrypt email addresses for storage
     * 
     * @param email the email address
     * @return encrypted email or original if encryption disabled
     */
    public String encryptEmail(String email) {
        if (email != null && EMAIL_PATTERN.matcher(email).matches()) {
            return encryptSensitiveData(email);
        }
        return email;
    }

    /**
     * Decrypt email addresses from storage
     * 
     * @param encryptedEmail the encrypted email
     * @return decrypted email or original if not encrypted
     */
    public String decryptEmail(String encryptedEmail) {
        return decryptSensitiveData(encryptedEmail);
    }

    /**
     * Encrypt phone numbers for storage
     * 
     * @param phoneNumber the phone number
     * @return encrypted phone number or original if encryption disabled
     */
    public String encryptPhoneNumber(String phoneNumber) {
        if (phoneNumber != null && PHONE_PATTERN.matcher(phoneNumber).matches()) {
            return encryptSensitiveData(phoneNumber);
        }
        return phoneNumber;
    }

    /**
     * Decrypt phone numbers from storage
     * 
     * @param encryptedPhoneNumber the encrypted phone number
     * @return decrypted phone number or original if not encrypted
     */
    public String decryptPhoneNumber(String encryptedPhoneNumber) {
        return decryptSensitiveData(encryptedPhoneNumber);
    }

    /**
     * Encrypt personal information (names, addresses, etc.)
     * 
     * @param personalInfo the personal information
     * @return encrypted personal info or original if encryption disabled
     */
    public String encryptPersonalInfo(String personalInfo) {
        if (personalInfo != null && !personalInfo.trim().isEmpty()) {
            return encryptSensitiveData(personalInfo);
        }
        return personalInfo;
    }

    /**
     * Decrypt personal information from storage
     * 
     * @param encryptedPersonalInfo the encrypted personal information
     * @return decrypted personal info or original if not encrypted
     */
    public String decryptPersonalInfo(String encryptedPersonalInfo) {
        return decryptSensitiveData(encryptedPersonalInfo);
    }

    /**
     * Detect if a string contains sensitive data patterns
     * 
     * @param data the data to analyze
     * @return true if sensitive patterns are detected
     */
    public boolean containsSensitiveData(String data) {
        if (data == null || data.isEmpty()) {
            return false;
        }
        
        return EMAIL_PATTERN.matcher(data).find() ||
               PHONE_PATTERN.matcher(data).find() ||
               SSN_PATTERN.matcher(data).find() ||
               containsPersonalNames(data);
    }

    /**
     * Mask sensitive data for logging (show only first and last characters)
     * 
     * @param sensitiveData the sensitive data to mask
     * @return masked version safe for logging
     */
    public String maskForLogging(String sensitiveData) {
        if (sensitiveData == null || sensitiveData.length() <= 2) {
            return "***";
        }
        
        if (sensitiveData.length() <= 4) {
            return sensitiveData.charAt(0) + "***";
        }
        
        return sensitiveData.charAt(0) + 
               "*".repeat(sensitiveData.length() - 2) + 
               sensitiveData.charAt(sensitiveData.length() - 1);
    }

    /**
     * Generate a new encryption key for development/testing
     * 
     * @return new AES encryption key
     */
    public SecretKey generateNewEncryptionKey() {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance(ALGORITHM);
            keyGenerator.init(256);
            SecretKey key = keyGenerator.generateKey();
            
            String keyBase64 = Base64.getEncoder().encodeToString(key.getEncoded());
            log.info("Generated new encryption key (Base64): {}", keyBase64);
            
            return key;
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate encryption key", e);
        }
    }

    private String encrypt(String plainText) throws Exception {
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        
        // Generate random IV
        byte[] iv = new byte[GCM_IV_LENGTH];
        secureRandom.nextBytes(iv);
        
        GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
        cipher.init(Cipher.ENCRYPT_MODE, encryptionKey, parameterSpec);
        
        byte[] encryptedData = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
        
        // Combine IV and encrypted data
        byte[] encryptedWithIv = new byte[GCM_IV_LENGTH + encryptedData.length];
        System.arraycopy(iv, 0, encryptedWithIv, 0, GCM_IV_LENGTH);
        System.arraycopy(encryptedData, 0, encryptedWithIv, GCM_IV_LENGTH, encryptedData.length);
        
        return Base64.getEncoder().encodeToString(encryptedWithIv);
    }

    private String decrypt(String encryptedData) throws Exception {
        byte[] decodedData = Base64.getDecoder().decode(encryptedData);
        
        // Extract IV and encrypted data
        byte[] iv = new byte[GCM_IV_LENGTH];
        byte[] encrypted = new byte[decodedData.length - GCM_IV_LENGTH];
        
        System.arraycopy(decodedData, 0, iv, 0, GCM_IV_LENGTH);
        System.arraycopy(decodedData, GCM_IV_LENGTH, encrypted, 0, encrypted.length);
        
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
        cipher.init(Cipher.DECRYPT_MODE, encryptionKey, parameterSpec);
        
        byte[] decryptedData = cipher.doFinal(encrypted);
        return new String(decryptedData, StandardCharsets.UTF_8);
    }

    private boolean containsPersonalNames(String data) {
        // Simple heuristic for detecting personal names
        // In production, this could be more sophisticated
        String[] commonNamePrefixes = {"Mr.", "Mrs.", "Ms.", "Dr.", "Prof."};
        String lowerData = data.toLowerCase();
        
        for (String prefix : commonNamePrefixes) {
            if (lowerData.contains(prefix.toLowerCase())) {
                return true;
            }
        }
        
        // Check for patterns like "First Last" (two capitalized words)
        return data.matches(".*\\b[A-Z][a-z]+ [A-Z][a-z]+\\b.*");
    }
}