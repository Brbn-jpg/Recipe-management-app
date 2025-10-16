package com.kk.cibaria.dto.auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class LoginFormDtoTest {

    private Validator validator;

    @BeforeEach
    void setup() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testValidLogin() {
        // create valid login form
        LoginFormDto loginForm = new LoginFormDto("test@test.com", "password123");
        
        Set<ConstraintViolation<LoginFormDto>> errors = validator.validate(loginForm);
        
        assertTrue(errors.isEmpty()); // no validation errors
        assertEquals("test@test.com", loginForm.email());
        assertEquals("password123", loginForm.password());
    }

    @Test
    void testEmptyEmail() {
        // empty email should fail
        LoginFormDto loginForm = new LoginFormDto("", "password123");
        
        Set<ConstraintViolation<LoginFormDto>> errors = validator.validate(loginForm);
        
        assertEquals(1, errors.size());
        assertEquals("Email is required", errors.iterator().next().getMessage());
    }

    @Test
    void testNullEmail() {
        // null email should fail
        LoginFormDto loginForm = new LoginFormDto(null, "password123");
        
        Set<ConstraintViolation<LoginFormDto>> errors = validator.validate(loginForm);
        
        assertEquals(1, errors.size());
        assertEquals("Email is required", errors.iterator().next().getMessage());
    }

    @Test
    void testBadEmailFormat() {
        // bad email format should fail
        LoginFormDto loginForm = new LoginFormDto("bad-email", "password123");
        
        Set<ConstraintViolation<LoginFormDto>> errors = validator.validate(loginForm);
        
        assertEquals(1, errors.size());
        assertEquals("Email should be valid", errors.iterator().next().getMessage());
    }

    @Test
    void testEmptyPassword() {
        // empty password should fail
        LoginFormDto loginForm = new LoginFormDto("test@test.com", "");
        
        Set<ConstraintViolation<LoginFormDto>> errors = validator.validate(loginForm);
        
        assertEquals(1, errors.size());
        assertEquals("Password is required", errors.iterator().next().getMessage());
    }

    @Test
    void testNullPassword() {
        // null password should fail
        LoginFormDto loginForm = new LoginFormDto("test@test.com", null);
        
        Set<ConstraintViolation<LoginFormDto>> errors = validator.validate(loginForm);
        
        assertEquals(1, errors.size());
        assertEquals("Password is required", errors.iterator().next().getMessage());
    }

    @Test
    void testBothFieldsBad() {
        // both fields bad should give 2 errors
        LoginFormDto loginForm = new LoginFormDto("", "");
        
        Set<ConstraintViolation<LoginFormDto>> errors = validator.validate(loginForm);
        
        assertEquals(2, errors.size());
    }

    @Test
    void testToStringWorks() {
        // toString should contain both fields
        LoginFormDto loginForm = new LoginFormDto("test@test.com", "password123");
        
        String text = loginForm.toString();
        
        assertTrue(text.contains("test@test.com"));
        assertTrue(text.contains("password123"));
    }

    @Test
    void testEqualsWorks() {
        // same data should be equal
        LoginFormDto form1 = new LoginFormDto("test@test.com", "password123");
        LoginFormDto form2 = new LoginFormDto("test@test.com", "password123");
        LoginFormDto form3 = new LoginFormDto("other@test.com", "password123");
        
        assertEquals(form1, form2);
        assertNotEquals(form1, form3);
        assertEquals(form1.hashCode(), form2.hashCode());
    }
}