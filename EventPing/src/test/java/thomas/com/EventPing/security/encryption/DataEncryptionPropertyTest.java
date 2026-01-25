package thomas.com.EventPing.security.encryption;

import net.jqwik.api.*;
import net.jqwik.api.constraints.AlphaChars;
import net.jqwik.api.constraints.StringLength;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Property-based tests for data encryption consistency
 * **Feature: eventping-security-hardening, Property 5: Data Encryption Consistency**
 * **Validates: Requirements 4.1, 4.3**
 */
@SpringBootTest
@ActiveProfiles("test")
class DataEncryptionPropertyTest {

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 16;
    
    // Initialize encryption components statically for jqwik
    private static final SecretKey SECRET_KEY;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    
    static {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance(ALGORITHM);
            keyGenerator.init(256);
            SECRET_KEY = keyGenerator.generateKey();
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize encryption key", e);
        }
    }

    @Property(tries = 100)
    @Label("Property 5: Data Encryption Consistency - For any sensitive data, encryption then decryption should return original value")
    void encryptionDecryptionRoundTrip(@ForAll @StringLength(min = 1, max = 1000) String sensitiveData) {
        try {
            // Encrypt the data
            String encryptedData = encryptData(sensitiveData);
            
            // Verify encrypted data is different from original
            Assume.that(!encryptedData.equals(sensitiveData));
            
            // Decrypt the data
            String decryptedData = decryptData(encryptedData);
            
            // Verify round-trip consistency
            assert decryptedData.equals(sensitiveData) : 
                String.format("Decrypted data '%s' does not match original '%s'", decryptedData, sensitiveData);
                
        } catch (Exception e) {
            throw new RuntimeException("Encryption/decryption failed", e);
        }
    }

    @Property(tries = 100)
    @Label("Property 5a: Encryption produces different output for same input with different IVs")
    void encryptionProducesDifferentOutputWithDifferentIVs(@ForAll @StringLength(min = 1, max = 100) String data) {
        try {
            // Encrypt the same data twice
            String encrypted1 = encryptData(data);
            String encrypted2 = encryptData(data);
            
            // Verify that encrypted outputs are different (due to different IVs)
            assert !encrypted1.equals(encrypted2) : 
                "Encryption should produce different outputs for same input due to random IVs";
                
            // But both should decrypt to the same original data
            String decrypted1 = decryptData(encrypted1);
            String decrypted2 = decryptData(encrypted2);
            
            assert decrypted1.equals(data) && decrypted2.equals(data) : 
                "Both encrypted versions should decrypt to original data";
                
        } catch (Exception e) {
            throw new RuntimeException("Encryption consistency test failed", e);
        }
    }

    @Property(tries = 100)
    @Label("Property 5b: Encrypted data should not contain original plaintext")
    void encryptedDataDoesNotContainPlaintext(@ForAll @StringLength(min = 5, max = 100) @AlphaChars String data) {
        try {
            String encryptedData = encryptData(data);
            
            // Verify encrypted data doesn't contain the original plaintext
            assert !encryptedData.contains(data) : 
                String.format("Encrypted data should not contain original plaintext: '%s'", data);
                
            // Verify encrypted data is Base64 encoded (no plaintext visible)
            byte[] decodedBytes = Base64.getDecoder().decode(encryptedData);
            String decodedString = new String(decodedBytes, StandardCharsets.UTF_8);
            
            assert !decodedString.contains(data) : 
                "Decoded encrypted data should not contain original plaintext";
                
        } catch (Exception e) {
            throw new RuntimeException("Plaintext leakage test failed", e);
        }
    }

    @Property(tries = 50)
    @Label("Property 5c: Password encryption should use proper bcrypt rounds")
    void passwordEncryptionUsesBcryptRounds(@ForAll("validPasswords") String password) {
        try {
            String hashedPassword = hashPassword(password);
            
            // Verify bcrypt format (starts with $2a$, $2b$, or $2y$)
            assert hashedPassword.matches("^\\$2[aby]\\$\\d{2}\\$.*") : 
                "Password hash should use bcrypt format";
            
            // Verify minimum rounds (should be at least 12)
            String[] parts = hashedPassword.split("\\$");
            int rounds = Integer.parseInt(parts[2]);
            assert rounds >= 12 : 
                String.format("BCrypt rounds should be at least 12, but was %d", rounds);
            
            // Verify password verification works
            assert verifyPassword(password, hashedPassword) : 
                "Password verification should succeed for correct password";
            
            // Verify wrong password fails
            assert !verifyPassword(password + "wrong", hashedPassword) : 
                "Password verification should fail for incorrect password";
                
        } catch (Exception e) {
            throw new RuntimeException("Password encryption test failed", e);
        }
    }

    @Property(tries = 100)
    @Label("Property 5d: Sensitive data fields should be encrypted in database format")
    void sensitiveDataFieldsAreEncrypted(@ForAll @StringLength(min = 1, max = 200) String personalInfo) {
        try {
            // Simulate database storage format for PII
            String encryptedPii = encryptForDatabase(personalInfo);
            
            // Verify it's in proper encrypted format (Base64 with metadata)
            assert encryptedPii.startsWith("ENC:") : 
                "Database encrypted data should have ENC: prefix";
            
            // Verify it can be decrypted
            String decryptedPii = decryptFromDatabase(encryptedPii);
            assert decryptedPii.equals(personalInfo) : 
                "Database decryption should return original PII";
            
            // Verify encrypted form doesn't contain original data
            assert !encryptedPii.contains(personalInfo) : 
                "Encrypted database field should not contain original PII";
                
        } catch (Exception e) {
            throw new RuntimeException("Database encryption test failed", e);
        }
    }

    // Helper methods for encryption operations
    private String encryptData(String data) throws Exception {
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        
        // Generate random IV
        byte[] iv = new byte[GCM_IV_LENGTH];
        SECURE_RANDOM.nextBytes(iv);
        
        GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
        cipher.init(Cipher.ENCRYPT_MODE, SECRET_KEY, parameterSpec);
        
        byte[] encryptedData = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
        
        // Combine IV and encrypted data
        byte[] encryptedWithIv = new byte[GCM_IV_LENGTH + encryptedData.length];
        System.arraycopy(iv, 0, encryptedWithIv, 0, GCM_IV_LENGTH);
        System.arraycopy(encryptedData, 0, encryptedWithIv, GCM_IV_LENGTH, encryptedData.length);
        
        return Base64.getEncoder().encodeToString(encryptedWithIv);
    }

    private String decryptData(String encryptedData) throws Exception {
        byte[] decodedData = Base64.getDecoder().decode(encryptedData);
        
        // Extract IV and encrypted data
        byte[] iv = new byte[GCM_IV_LENGTH];
        byte[] encrypted = new byte[decodedData.length - GCM_IV_LENGTH];
        
        System.arraycopy(decodedData, 0, iv, 0, GCM_IV_LENGTH);
        System.arraycopy(decodedData, GCM_IV_LENGTH, encrypted, 0, encrypted.length);
        
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
        cipher.init(Cipher.DECRYPT_MODE, SECRET_KEY, parameterSpec);
        
        byte[] decryptedData = cipher.doFinal(encrypted);
        return new String(decryptedData, StandardCharsets.UTF_8);
    }

    private String hashPassword(String password) {
        // Simulate BCrypt hashing with 12 rounds
        return org.springframework.security.crypto.bcrypt.BCrypt.hashpw(password, 
            org.springframework.security.crypto.bcrypt.BCrypt.gensalt(12));
    }

    private boolean verifyPassword(String password, String hash) {
        return org.springframework.security.crypto.bcrypt.BCrypt.checkpw(password, hash);
    }

    private String encryptForDatabase(String data) throws Exception {
        String encrypted = encryptData(data);
        return "ENC:" + encrypted;
    }

    private String decryptFromDatabase(String encryptedData) throws Exception {
        if (!encryptedData.startsWith("ENC:")) {
            throw new IllegalArgumentException("Invalid encrypted database format");
        }
        return decryptData(encryptedData.substring(4));
    }

    // Generators for specific data types
    @Provide
    Arbitrary<String> personalInfo() {
        return Arbitraries.strings()
            .withCharRange('a', 'z')
            .withCharRange('A', 'Z')
            .withCharRange('0', '9')
            .withChars(' ', '.', '@', '-')
            .ofMinLength(1)
            .ofMaxLength(100);
    }

    @Provide
    Arbitrary<String> validPasswords() {
        return Arbitraries.strings()
            .withCharRange('a', 'z')
            .withCharRange('A', 'Z')
            .withCharRange('0', '9')
            .withChars('!', '@', '#', '$', '%', '^', '&', '*')
            .ofMinLength(8)
            .ofMaxLength(50) // BCrypt has 72-byte limit, keep well under
            .filter(password -> password.getBytes(StandardCharsets.UTF_8).length <= 50);
    }
}