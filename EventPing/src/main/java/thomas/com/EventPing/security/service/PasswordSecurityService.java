package thomas.com.EventPing.security.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.regex.Pattern;

/**
 * Service for password security operations including hashing, verification, and validation
 * **Validates: Requirements 4.3, 4.4**
 */
@Service
@Slf4j
public class PasswordSecurityService {

    private static final Pattern SPECIAL_CHARS_PATTERN = Pattern.compile("[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]");
    private static final Pattern UPPERCASE_PATTERN = Pattern.compile("[A-Z]");
    private static final Pattern LOWERCASE_PATTERN = Pattern.compile("[a-z]");
    private static final Pattern DIGIT_PATTERN = Pattern.compile("[0-9]");
    
    private final int bcryptRounds;
    private final int minPasswordLength;
    private final boolean requireSpecialChars;
    private final SecureRandom secureRandom;

    public PasswordSecurityService(
            @Value("${eventping.security.password.bcrypt-rounds:12}") int bcryptRounds,
            @Value("${eventping.security.password.min-length:8}") int minPasswordLength,
            @Value("${eventping.security.password.require-special-chars:true}") boolean requireSpecialChars) {
        
        this.bcryptRounds = Math.max(12, bcryptRounds); // Ensure minimum 12 rounds
        this.minPasswordLength = Math.max(8, minPasswordLength); // Ensure minimum 8 characters
        this.requireSpecialChars = requireSpecialChars;
        this.secureRandom = new SecureRandom();
        
        log.info("Password security initialized with {} BCrypt rounds, min length {}, special chars required: {}", 
                this.bcryptRounds, this.minPasswordLength, this.requireSpecialChars);
    }

    /**
     * Hash a password using BCrypt with configured rounds
     * 
     * @param plainPassword the plain text password
     * @return the BCrypt hashed password
     * @throws IllegalArgumentException if password is invalid
     */
    public String hashPassword(String plainPassword) {
        if (plainPassword == null || plainPassword.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }
        
        // Validate password meets security requirements
        validatePasswordStrength(plainPassword);
        
        // Generate salt and hash password
        String salt = BCrypt.gensalt(bcryptRounds, secureRandom);
        String hashedPassword = BCrypt.hashpw(plainPassword, salt);
        
        log.debug("Password hashed successfully with {} rounds", bcryptRounds);
        return hashedPassword;
    }

    /**
     * Verify a password against its hash
     * 
     * @param plainPassword the plain text password
     * @param hashedPassword the BCrypt hashed password
     * @return true if password matches, false otherwise
     */
    public boolean verifyPassword(String plainPassword, String hashedPassword) {
        if (plainPassword == null || hashedPassword == null) {
            return false;
        }
        
        try {
            boolean matches = BCrypt.checkpw(plainPassword, hashedPassword);
            log.debug("Password verification result: {}", matches);
            return matches;
        } catch (Exception e) {
            log.warn("Password verification failed due to exception", e);
            return false;
        }
    }

    /**
     * Validate password strength according to security policy
     * 
     * @param password the password to validate
     * @throws IllegalArgumentException if password doesn't meet requirements
     */
    public void validatePasswordStrength(String password) {
        if (password == null) {
            throw new IllegalArgumentException("Password cannot be null");
        }
        
        if (password.length() < minPasswordLength) {
            throw new IllegalArgumentException(
                String.format("Password must be at least %d characters long", minPasswordLength));
        }
        
        if (password.length() > 72) {
            throw new IllegalArgumentException("Password cannot exceed 72 characters (BCrypt limitation)");
        }
        
        if (!UPPERCASE_PATTERN.matcher(password).find()) {
            throw new IllegalArgumentException("Password must contain at least one uppercase letter");
        }
        
        if (!LOWERCASE_PATTERN.matcher(password).find()) {
            throw new IllegalArgumentException("Password must contain at least one lowercase letter");
        }
        
        if (!DIGIT_PATTERN.matcher(password).find()) {
            throw new IllegalArgumentException("Password must contain at least one digit");
        }
        
        if (requireSpecialChars && !SPECIAL_CHARS_PATTERN.matcher(password).find()) {
            throw new IllegalArgumentException("Password must contain at least one special character");
        }
        
        // Check for common weak patterns
        if (isCommonWeakPassword(password)) {
            throw new IllegalArgumentException("Password is too common or weak");
        }
    }

    /**
     * Generate a secure random password
     * 
     * @param length the desired password length
     * @return a secure random password
     */
    public String generateSecurePassword(int length) {
        if (length < minPasswordLength) {
            length = minPasswordLength;
        }
        if (length > 72) {
            length = 72;
        }
        
        String uppercase = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String lowercase = "abcdefghijklmnopqrstuvwxyz";
        String digits = "0123456789";
        String special = "!@#$%^&*()_+-=[]{}|;:,.<>?";
        
        StringBuilder password = new StringBuilder();
        
        // Ensure at least one character from each required category
        password.append(uppercase.charAt(secureRandom.nextInt(uppercase.length())));
        password.append(lowercase.charAt(secureRandom.nextInt(lowercase.length())));
        password.append(digits.charAt(secureRandom.nextInt(digits.length())));
        
        if (requireSpecialChars) {
            password.append(special.charAt(secureRandom.nextInt(special.length())));
        }
        
        // Fill remaining length with random characters from all categories
        String allChars = uppercase + lowercase + digits + (requireSpecialChars ? special : "");
        for (int i = password.length(); i < length; i++) {
            password.append(allChars.charAt(secureRandom.nextInt(allChars.length())));
        }
        
        // Shuffle the password to avoid predictable patterns
        return shuffleString(password.toString());
    }

    /**
     * Check if password needs to be rehashed (e.g., due to changed security parameters)
     * 
     * @param hashedPassword the current hashed password
     * @return true if password should be rehashed
     */
    public boolean needsRehash(String hashedPassword) {
        if (hashedPassword == null || !hashedPassword.startsWith("$2")) {
            return true;
        }
        
        try {
            String[] parts = hashedPassword.split("\\$");
            if (parts.length < 4) {
                return true;
            }
            
            int currentRounds = Integer.parseInt(parts[2]);
            return currentRounds < bcryptRounds;
        } catch (Exception e) {
            log.warn("Failed to parse BCrypt hash, recommending rehash", e);
            return true;
        }
    }

    /**
     * Get password strength score (0-100)
     * 
     * @param password the password to evaluate
     * @return strength score from 0 (weakest) to 100 (strongest)
     */
    public int getPasswordStrength(String password) {
        if (password == null || password.isEmpty()) {
            return 0;
        }
        
        int score = 0;
        
        // Length scoring
        if (password.length() >= 8) score += 20;
        if (password.length() >= 12) score += 10;
        if (password.length() >= 16) score += 10;
        
        // Character variety scoring
        if (UPPERCASE_PATTERN.matcher(password).find()) score += 15;
        if (LOWERCASE_PATTERN.matcher(password).find()) score += 15;
        if (DIGIT_PATTERN.matcher(password).find()) score += 15;
        if (SPECIAL_CHARS_PATTERN.matcher(password).find()) score += 15;
        
        // Penalty for common patterns
        if (isCommonWeakPassword(password)) score -= 30;
        if (hasRepeatingCharacters(password)) score -= 10;
        if (hasSequentialCharacters(password)) score -= 10;
        
        return Math.max(0, Math.min(100, score));
    }

    private boolean isCommonWeakPassword(String password) {
        String lower = password.toLowerCase();
        
        // Common weak passwords
        String[] commonPasswords = {
            "password", "123456", "password123", "admin", "qwerty", 
            "letmein", "welcome", "monkey", "dragon", "master",
            "password1", "123456789", "12345678", "1234567890"
        };
        
        for (String common : commonPasswords) {
            if (lower.contains(common)) {
                return true;
            }
        }
        
        // Sequential patterns
        if (lower.matches(".*(?:abc|bcd|cde|def|efg|fgh|ghi|hij|ijk|jkl|klm|lmn|mno|nop|opq|pqr|qrs|rst|stu|tuv|uvw|vwx|wxy|xyz).*")) {
            return true;
        }
        
        if (lower.matches(".*(?:123|234|345|456|567|678|789|890).*")) {
            return true;
        }
        
        return false;
    }

    private boolean hasRepeatingCharacters(String password) {
        for (int i = 0; i < password.length() - 2; i++) {
            if (password.charAt(i) == password.charAt(i + 1) && 
                password.charAt(i) == password.charAt(i + 2)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasSequentialCharacters(String password) {
        for (int i = 0; i < password.length() - 2; i++) {
            char c1 = password.charAt(i);
            char c2 = password.charAt(i + 1);
            char c3 = password.charAt(i + 2);
            
            if ((c2 == c1 + 1 && c3 == c2 + 1) || (c2 == c1 - 1 && c3 == c2 - 1)) {
                return true;
            }
        }
        return false;
    }

    private String shuffleString(String input) {
        char[] chars = input.toCharArray();
        for (int i = chars.length - 1; i > 0; i--) {
            int j = secureRandom.nextInt(i + 1);
            char temp = chars[i];
            chars[i] = chars[j];
            chars[j] = temp;
        }
        return new String(chars);
    }
}