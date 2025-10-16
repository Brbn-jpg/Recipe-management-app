package com.kk.cibaria.dto.auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class RegisterDtoTest {

    private Validator validator;

    @BeforeEach
    void setup() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testValidRegister() {
        // create valid register data
        RegisterDto dto = new RegisterDto();
        dto.setUsername("testuser");
        dto.setEmail("test@test.com");
        dto.setPassword("password123");
        
        Set<ConstraintViolation<RegisterDto>> errors = validator.validate(dto);
        
        assertTrue(errors.isEmpty()); // no validation errors
        assertEquals("testuser", dto.getUsername());
        assertEquals("test@test.com", dto.getEmail());
        assertEquals("password123", dto.getPassword());
    }

    @Test
    void testUsernameNotBlank() {
        RegisterDto registerDto = new RegisterDto();
        registerDto.setUsername("");
        registerDto.setEmail("test@test.com");
        registerDto.setPassword("password123");
        
        Set<ConstraintViolation<RegisterDto>> violations = validator.validate(registerDto);
        
        assertEquals(2, violations.size()); // @NotBlank + @Size
    }

    @Test
    void testUsernameNull() {
        RegisterDto registerDto = new RegisterDto();
        registerDto.setUsername(null);
        registerDto.setEmail("test@test.com");
        registerDto.setPassword("password123");
        
        Set<ConstraintViolation<RegisterDto>> violations = validator.validate(registerDto);
        
        assertEquals(1, violations.size());
        assertEquals("Username is required", violations.iterator().next().getMessage());
    }

    @Test
    void testUsernameTooShort() {
        RegisterDto registerDto = new RegisterDto();
        registerDto.setUsername("a");
        registerDto.setEmail("test@test.com");
        registerDto.setPassword("password123");
        
        Set<ConstraintViolation<RegisterDto>> violations = validator.validate(registerDto);
        
        assertEquals(1, violations.size());
        assertEquals("Username must be between 2 and 16 characters", violations.iterator().next().getMessage());
    }

    @Test
    void testUsernameTooLong() {
        RegisterDto registerDto = new RegisterDto();
        registerDto.setUsername("verylongusernamethathasmorethan16characters");
        registerDto.setEmail("test@test.com");
        registerDto.setPassword("password123");
        
        Set<ConstraintViolation<RegisterDto>> violations = validator.validate(registerDto);
        
        assertEquals(1, violations.size());
        assertEquals("Username must be between 2 and 16 characters", violations.iterator().next().getMessage());
    }

    @Test
    void testUsernameAtBoundaries() {
        // min length = 2
        RegisterDto registerDto1 = new RegisterDto();
        registerDto1.setUsername("ab");
        registerDto1.setEmail("test@test.com");
        registerDto1.setPassword("password123");
        
        Set<ConstraintViolation<RegisterDto>> violations1 = validator.validate(registerDto1);
        assertTrue(violations1.isEmpty());
        
        // max length = 16  
        RegisterDto registerDto2 = new RegisterDto();
        registerDto2.setUsername("1234567890123456");
        registerDto2.setEmail("test@test.com");
        registerDto2.setPassword("password123");
        
        Set<ConstraintViolation<RegisterDto>> violations2 = validator.validate(registerDto2);
        assertTrue(violations2.isEmpty());
    }

    @Test
    void testEmailNotBlank() {
        RegisterDto registerDto = new RegisterDto();
        registerDto.setUsername("testuser");
        registerDto.setEmail("");
        registerDto.setPassword("password123");
        
        Set<ConstraintViolation<RegisterDto>> violations = validator.validate(registerDto);
        
        assertEquals(1, violations.size());
        assertEquals("Email is required", violations.iterator().next().getMessage());
    }

    @Test
    void testEmailNull() {
        RegisterDto registerDto = new RegisterDto();
        registerDto.setUsername("testuser");
        registerDto.setEmail(null);
        registerDto.setPassword("password123");
        
        Set<ConstraintViolation<RegisterDto>> violations = validator.validate(registerDto);
        
        assertEquals(1, violations.size());
        assertEquals("Email is required", violations.iterator().next().getMessage());
    }

    @Test
    void testInvalidEmailFormat() {
        RegisterDto registerDto = new RegisterDto();
        registerDto.setUsername("testuser");
        registerDto.setEmail("invalid-email");
        registerDto.setPassword("password123");
        
        Set<ConstraintViolation<RegisterDto>> violations = validator.validate(registerDto);
        
        assertEquals(1, violations.size());
        assertEquals("Email should be valid", violations.iterator().next().getMessage());
    }

    @Test
    void testPasswordNotBlank() {
        RegisterDto registerDto = new RegisterDto();
        registerDto.setUsername("testuser");
        registerDto.setEmail("test@test.com");
        registerDto.setPassword("");
        
        Set<ConstraintViolation<RegisterDto>> violations = validator.validate(registerDto);
        
        assertEquals(2, violations.size()); // @NotBlank + @Size
    }

    @Test
    void testPasswordNull() {
        RegisterDto registerDto = new RegisterDto();
        registerDto.setUsername("testuser");
        registerDto.setEmail("test@test.com");
        registerDto.setPassword(null);
        
        Set<ConstraintViolation<RegisterDto>> violations = validator.validate(registerDto);
        
        assertEquals(1, violations.size());
        assertEquals("Password is required", violations.iterator().next().getMessage());
    }

    @Test
    void testPasswordTooShort() {
        RegisterDto registerDto = new RegisterDto();
        registerDto.setUsername("testuser");
        registerDto.setEmail("test@test.com");
        registerDto.setPassword("12345");
        
        Set<ConstraintViolation<RegisterDto>> violations = validator.validate(registerDto);
        
        assertEquals(1, violations.size());
        assertEquals("Password must be at least 6 characters long", violations.iterator().next().getMessage());
    }

    @Test
    void testPasswordAtMinimumLength() {
        RegisterDto registerDto = new RegisterDto();
        registerDto.setUsername("testuser");
        registerDto.setEmail("test@test.com");
        registerDto.setPassword("123456"); // min length = 6
        
        Set<ConstraintViolation<RegisterDto>> violations = validator.validate(registerDto);
        
        assertTrue(violations.isEmpty());
    }

    @Test
    void testAllFieldsInvalid() {
        RegisterDto registerDto = new RegisterDto();
        registerDto.setUsername("");
        registerDto.setEmail("invalid");
        registerDto.setPassword("123");
        
        Set<ConstraintViolation<RegisterDto>> violations = validator.validate(registerDto);
        
        assertEquals(4, violations.size()); // username: @NotBlank + @Size, email: @Email, password: @Size
    }

    @Test
    void testLombokGeneratedMethods() {
        RegisterDto registerDto1 = new RegisterDto();
        registerDto1.setUsername("testuser");
        registerDto1.setEmail("test@test.com");
        registerDto1.setPassword("password123");
        
        RegisterDto registerDto2 = new RegisterDto();
        registerDto2.setUsername("testuser");
        registerDto2.setEmail("test@test.com");
        registerDto2.setPassword("password123");
        
        // Test equals
        assertEquals(registerDto1, registerDto2);
        
        // Test hashCode
        assertEquals(registerDto1.hashCode(), registerDto2.hashCode());
        
        // Test toString
        String toString = registerDto1.toString();
        assertTrue(toString.contains("testuser"));
        assertTrue(toString.contains("test@test.com"));
        assertTrue(toString.contains("password123"));
    }
}