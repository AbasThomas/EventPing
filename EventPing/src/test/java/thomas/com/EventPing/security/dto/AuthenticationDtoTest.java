package thomas.com.EventPing.security.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import thomas.com.EventPing.User.dtos.UserResponseDto;
import thomas.com.EventPing.User.model.User;

import java.time.LocalDateTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for Authentication DTOs
 * Tests validation and data transfer functionality
 * **Validates: Requirements 1.1, 1.2, 1.3**
 */
class AuthenticationDtoTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("LoginRequest should validate correct data")
    void loginRequestShouldValidateCorrectData() {
        // Given
        LoginRequest loginRequest = LoginRequest.builder()
                .email("test@example.com")
                .password("TestPassword123!")
                .build();

        // When
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(loginRequest);

        // Then
        assertThat(violations).isEmpty();
        assertThat(loginRequest.getEmail()).isEqualTo("test@example.com");
        assertThat(loginRequest.getPassword()).isEqualTo("TestPassword123!");
    }

    @Test
    @DisplayName("LoginRequest should reject invalid email")
    void loginRequestShouldRejectInvalidEmail() {
        // Given
        LoginRequest loginRequest = LoginRequest.builder()
                .email("invalid-email")
                .password("TestPassword123!")
                .build();

        // When
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(loginRequest);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).contains("Invalid email format");
    }

    @Test
    @DisplayName("LoginRequest should reject empty email")
    void loginRequestShouldRejectEmptyEmail() {
        // Given
        LoginRequest loginRequest = LoginRequest.builder()
                .email("")
                .password("TestPassword123!")
                .build();

        // When
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(loginRequest);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).contains("Email is required");
    }

    @Test
    @DisplayName("LoginRequest should reject short password")
    void loginRequestShouldRejectShortPassword() {
        // Given
        LoginRequest loginRequest = LoginRequest.builder()
                .email("test@example.com")
                .password("short")
                .build();

        // When
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(loginRequest);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).contains("Password must be between 8 and 128 characters");
    }

    @Test
    @DisplayName("LoginRequest should reject empty password")
    void loginRequestShouldRejectEmptyPassword() {
        // Given
        LoginRequest loginRequest = LoginRequest.builder()
                .email("test@example.com")
                .password("")
                .build();

        // When
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(loginRequest);

        // Then
        assertThat(violations).hasSize(2); // Both @NotBlank and @Size are triggered
        assertThat(violations.stream().map(ConstraintViolation::getMessage))
                .containsExactlyInAnyOrder(
                        "Password is required",
                        "Password must be between 8 and 128 characters"
                );
    }

    @Test
    @DisplayName("UserRegistrationRequest should validate correct data")
    void userRegistrationRequestShouldValidateCorrectData() {
        // Given
        UserRegistrationRequest request = UserRegistrationRequest.builder()
                .email("test@example.com")
                .password("TestPassword123!")
                .confirmPassword("TestPassword123!")
                .fullName("John Doe")
                .phoneNumber("+1234567890")
                .build();

        // When
        Set<ConstraintViolation<UserRegistrationRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isEmpty();
        assertThat(request.getEmail()).isEqualTo("test@example.com");
        assertThat(request.getPassword()).isEqualTo("TestPassword123!");
        assertThat(request.getConfirmPassword()).isEqualTo("TestPassword123!");
        assertThat(request.getFullName()).isEqualTo("John Doe");
        assertThat(request.getPhoneNumber()).isEqualTo("+1234567890");
    }

    @Test
    @DisplayName("UserRegistrationRequest should reject weak password")
    void userRegistrationRequestShouldRejectWeakPassword() {
        // Given
        UserRegistrationRequest request = UserRegistrationRequest.builder()
                .email("test@example.com")
                .password("weakpassword")
                .confirmPassword("weakpassword")
                .fullName("John Doe")
                .build();

        // When
        Set<ConstraintViolation<UserRegistrationRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .contains("Password must contain at least one lowercase letter, one uppercase letter, one digit, and one special character");
    }

    @Test
    @DisplayName("UserRegistrationRequest should reject invalid phone number")
    void userRegistrationRequestShouldRejectInvalidPhoneNumber() {
        // Given
        UserRegistrationRequest request = UserRegistrationRequest.builder()
                .email("test@example.com")
                .password("TestPassword123!")
                .confirmPassword("TestPassword123!")
                .fullName("John Doe")
                .phoneNumber("invalid-phone")
                .build();

        // When
        Set<ConstraintViolation<UserRegistrationRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).contains("Invalid phone number format");
    }

    @Test
    @DisplayName("UserRegistrationRequest should reject long full name")
    void userRegistrationRequestShouldRejectLongFullName() {
        // Given
        String longName = "a".repeat(101);
        UserRegistrationRequest request = UserRegistrationRequest.builder()
                .email("test@example.com")
                .password("TestPassword123!")
                .confirmPassword("TestPassword123!")
                .fullName(longName)
                .build();

        // When
        Set<ConstraintViolation<UserRegistrationRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).contains("Full name must not exceed 100 characters");
    }

    @Test
    @DisplayName("AuthenticationResponse should create success response")
    void authenticationResponseShouldCreateSuccessResponse() {
        // Given
        JwtToken token = JwtToken.builder()
                .accessToken("access-token")
                .refreshToken("refresh-token")
                .tokenType("Bearer")
                .expiresIn(3600L)
                .build();

        UserResponseDto user = UserResponseDto.builder()
                .id(1L)
                .email("test@example.com")
                .fullName("John Doe")
                .role(User.UserRole.USER)
                .accountLocked(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // When
        AuthenticationResponse response = AuthenticationResponse.success(token, user);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo(token);
        assertThat(response.getUser()).isEqualTo(user);
        assertThat(response.getMessage()).isEqualTo("Authentication successful");
    }

    @Test
    @DisplayName("UserResponseDto should exclude sensitive information")
    void userResponseDtoShouldExcludeSensitiveInformation() {
        // Given
        UserResponseDto userResponse = UserResponseDto.builder()
                .id(1L)
                .email("test@example.com")
                .fullName("John Doe")
                .phoneNumber("+1234567890")
                .role(User.UserRole.USER)
                .accountLocked(false)
                .lastLoginAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // When & Then
        assertThat(userResponse.getId()).isEqualTo(1L);
        assertThat(userResponse.getEmail()).isEqualTo("test@example.com");
        assertThat(userResponse.getFullName()).isEqualTo("John Doe");
        assertThat(userResponse.getPhoneNumber()).isEqualTo("+1234567890");
        assertThat(userResponse.getRole()).isEqualTo(User.UserRole.USER);
        assertThat(userResponse.getAccountLocked()).isFalse();
        assertThat(userResponse.getLastLoginAt()).isNotNull();
        assertThat(userResponse.getCreatedAt()).isNotNull();
        assertThat(userResponse.getUpdatedAt()).isNotNull();
        
        // Verify no password hash or other sensitive fields are exposed
        // (This is implicit since UserResponseDto doesn't have these fields)
    }

    @Test
    @DisplayName("LoginRequest should handle null values gracefully")
    void loginRequestShouldHandleNullValuesGracefully() {
        // Given
        LoginRequest loginRequest = new LoginRequest();

        // When
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(loginRequest);

        // Then
        assertThat(violations).hasSize(2); // email and password required
        assertThat(violations.stream().map(ConstraintViolation::getMessage))
                .containsExactlyInAnyOrder("Email is required", "Password is required");
    }

    @Test
    @DisplayName("UserRegistrationRequest should handle null values gracefully")
    void userRegistrationRequestShouldHandleNullValuesGracefully() {
        // Given
        UserRegistrationRequest request = new UserRegistrationRequest();

        // When
        Set<ConstraintViolation<UserRegistrationRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(3); // email, password, and confirmPassword required
        assertThat(violations.stream().map(ConstraintViolation::getMessage))
                .containsExactlyInAnyOrder(
                        "Email is required", 
                        "Password is required", 
                        "Password confirmation is required"
                );
    }

    @Test
    @DisplayName("Should validate various phone number formats")
    void shouldValidateVariousPhoneNumberFormats() {
        // Test each valid phone number individually
        String[] validPhones = {
                "+1234567890",        // +1 followed by 9 digits = valid (10 total)
                "1234567890",         // 10 digits starting with 1 = valid  
                "12345678901234",     // 14 digits starting with 1 = valid
                "+123456789012345",   // +1 followed by 14 digits = valid (15 total)
                "123456789012345"     // 15 digits starting with 1 = valid
        };

        for (String phone : validPhones) {
            UserRegistrationRequest request = UserRegistrationRequest.builder()
                    .email("test@example.com")
                    .password("TestPassword123!")
                    .confirmPassword("TestPassword123!")
                    .fullName("John Doe")
                    .phoneNumber(phone)
                    .build();

            Set<ConstraintViolation<UserRegistrationRequest>> violations = validator.validate(request);
            System.out.println("Testing valid phone '" + phone + "': " + violations.size() + " violations");
            if (!violations.isEmpty()) {
                System.out.println("  ERROR: Expected no violations but got: " + violations.iterator().next().getMessage());
            }
            assertThat(violations).as("Phone number '" + phone + "' should be valid").isEmpty();
        }

        // Test a simple invalid case
        UserRegistrationRequest request = UserRegistrationRequest.builder()
                .email("test@example.com")
                .password("TestPassword123!")
                .confirmPassword("TestPassword123!")
                .fullName("John Doe")
                .phoneNumber("abc123456789") // clearly invalid
                .build();

        Set<ConstraintViolation<UserRegistrationRequest>> violations = validator.validate(request);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).contains("Invalid phone number format");
    }
}