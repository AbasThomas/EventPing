package thomas.com.EventPing.security.validation;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import lombok.Data;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for custom validation annotations
 * **Validates: Requirements 2.2, 2.3, 2.6**
 */
@SpringBootTest
@ActiveProfiles("test")
class ValidationAnnotationsUnitTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("NoSqlInjection annotation should detect SQL injection patterns")
    void noSqlInjectionAnnotationShouldDetectSqlInjectionPatterns() {
        // Clean input should pass validation
        TestEntity cleanEntity = new TestEntity();
        cleanEntity.setSqlField("John Doe");
        
        Set<ConstraintViolation<TestEntity>> violations = validator.validate(cleanEntity);
        assertThat(violations).isEmpty();
        
        // SQL injection should fail validation
        TestEntity maliciousEntity = new TestEntity();
        maliciousEntity.setSqlField("SELECT * FROM users");
        
        violations = validator.validate(maliciousEntity);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).contains("SQL injection");
        
        // Another SQL injection pattern
        maliciousEntity.setSqlField("' OR '1'='1");
        violations = validator.validate(maliciousEntity);
        assertThat(violations).hasSize(1);
    }

    @Test
    @DisplayName("NoXss annotation should detect XSS patterns")
    void noXssAnnotationShouldDetectXssPatterns() {
        // Clean input should pass validation
        TestEntity cleanEntity = new TestEntity();
        cleanEntity.setXssField("Hello World");
        
        Set<ConstraintViolation<TestEntity>> violations = validator.validate(cleanEntity);
        assertThat(violations).isEmpty();
        
        // XSS should fail validation
        TestEntity maliciousEntity = new TestEntity();
        maliciousEntity.setXssField("<script>alert('xss')</script>");
        
        violations = validator.validate(maliciousEntity);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).contains("XSS");
        
        // Another XSS pattern
        maliciousEntity.setXssField("<img src=x onerror=alert('xss')>");
        violations = validator.validate(maliciousEntity);
        assertThat(violations).hasSize(1);
    }

    @Test
    @DisplayName("Both annotations should work together")
    void bothAnnotationsShouldWorkTogether() {
        TestEntity entity = new TestEntity();
        entity.setSqlField("SELECT * FROM users");
        entity.setXssField("<script>alert('xss')</script>");
        
        Set<ConstraintViolation<TestEntity>> violations = validator.validate(entity);
        assertThat(violations).hasSize(2);
        
        // Check that both violations are present
        boolean hasSqlViolation = violations.stream()
            .anyMatch(v -> v.getMessage().contains("SQL injection"));
        boolean hasXssViolation = violations.stream()
            .anyMatch(v -> v.getMessage().contains("XSS"));
        
        assertThat(hasSqlViolation).isTrue();
        assertThat(hasXssViolation).isTrue();
    }

    @Test
    @DisplayName("Annotations should handle null values gracefully")
    void annotationsShouldHandleNullValuesGracefully() {
        TestEntity entity = new TestEntity();
        entity.setSqlField(null);
        entity.setXssField(null);
        
        Set<ConstraintViolation<TestEntity>> violations = validator.validate(entity);
        assertThat(violations).isEmpty(); // Null values should pass (let @NotNull handle null validation)
    }

    @Test
    @DisplayName("Annotations should handle empty strings")
    void annotationsShouldHandleEmptyStrings() {
        TestEntity entity = new TestEntity();
        entity.setSqlField("");
        entity.setXssField("");
        
        Set<ConstraintViolation<TestEntity>> violations = validator.validate(entity);
        assertThat(violations).isEmpty(); // Empty strings should pass
    }

    @Test
    @DisplayName("Annotations should detect encoded attack patterns")
    void annotationsShouldDetectEncodedAttackPatterns() {
        TestEntity entity = new TestEntity();
        
        // Encoded SQL injection
        entity.setSqlField("\\x27 OR \\x31\\x3D\\x31");
        Set<ConstraintViolation<TestEntity>> violations = validator.validate(entity);
        assertThat(violations).hasSize(1);
        
        // Encoded XSS
        entity.setSqlField("Clean input");
        entity.setXssField("&#60;script&#62;alert('xss')&#60;/script&#62;");
        violations = validator.validate(entity);
        assertThat(violations).hasSize(1);
    }

    @Test
    @DisplayName("Annotations should work with case variations")
    void annotationsShouldWorkWithCaseVariations() {
        TestEntity entity = new TestEntity();
        
        // Case variation SQL injection
        entity.setSqlField("SeLeCt * FrOm UsErS");
        Set<ConstraintViolation<TestEntity>> violations = validator.validate(entity);
        assertThat(violations).hasSize(1);
        
        // Case variation XSS
        entity.setSqlField("Clean input");
        entity.setXssField("<ScRiPt>alert('xss')</ScRiPt>");
        violations = validator.validate(entity);
        assertThat(violations).hasSize(1);
    }

    // Test entity for validation testing
    @Data
    static class TestEntity {
        @NoSqlInjection
        private String sqlField;
        
        @NoXss
        private String xssField;
    }
}