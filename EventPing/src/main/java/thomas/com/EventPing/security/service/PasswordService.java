package thomas.com.EventPing.security.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Service for password hashing and verification using BCrypt
 */
@Service
@Slf4j
public class PasswordService {
    
    private final PasswordEncoder passwordEncoder;
    
    public PasswordService() {
        // Use BCrypt with strength 12 for production security
        this.passwordEncoder = new BCryptPasswordEncoder(12);
    }
    
    /**
     * Hash a plain text password
     * 
     * @param plainPassword the plain text password
     * @return the hashed password
     */
    public String hashPassword(String plainPassword) {
        if (plainPassword == null || plainPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }
        
        try {
            String hashedPassword = passwordEncoder.encode(plainPassword);
            log.debug("Password hashed successfully");
            return hashedPassword;
        } catch (Exception e) {
            log.error("Error hashing password", e);
            throw new RuntimeException("Failed to hash password", e);
        }
    }
    
    /**
     * Verify a plain text password against a hashed password
     * 
     * @param plainPassword the plain text password
     * @param hashedPassword the hashed password
     * @return true if the password matches, false otherwise
     */
    public boolean verifyPassword(String plainPassword, String hashedPassword) {
        if (plainPassword == null || hashedPassword == null) {
            log.warn("Attempted to verify null password or hash");
            return false;
        }
        
        try {
            boolean matches = passwordEncoder.matches(plainPassword, hashedPassword);
            log.debug("Password verification result: {}", matches);
            return matches;
        } catch (Exception e) {
            log.error("Error verifying password", e);
            return false;
        }
    }
    
    /**
     * Check if a password needs to be rehashed (for security upgrades)
     * 
     * @param hashedPassword the current hashed password
     * @return true if the password should be rehashed
     */
    public boolean needsRehash(String hashedPassword) {
        if (hashedPassword == null) {
            return true;
        }
        
        try {
            // BCrypt hashes start with $2a$, $2b$, or $2y$ followed by the cost
            // We want to ensure we're using cost 12
            return !hashedPassword.startsWith("$2") || 
                   !hashedPassword.substring(4, 6).equals("12");
        } catch (Exception e) {
            log.warn("Error checking if password needs rehash", e);
            return true;
        }
    }
}