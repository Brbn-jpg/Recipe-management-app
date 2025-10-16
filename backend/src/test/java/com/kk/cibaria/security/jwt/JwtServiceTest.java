package com.kk.cibaria.security.jwt;

import com.kk.cibaria.user.UserEntity;
import com.kk.cibaria.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtServiceTest {

    @Mock
    private UserRepository userRepository;

    private JwtService jwtService;
    
    private UserEntity testUser;
    private UserDetails testUserDetails;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        jwtService = new JwtService(userRepository);
        
        // base64 encoded test secret key
        ReflectionTestUtils.setField(jwtService, "secretKey", "12345678901234567890123456789012345678901234567890123456789012345678901234567890");
        
        testUser = new UserEntity();
        testUser.setId(1);
        testUser.setEmail("test@test.com");
        testUser.setRole("USER");
        
        testUserDetails = User.builder()
            .username("test@test.com")
            .password("encodedPassword")
            .authorities(Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")))
            .build();
    }

    @Test
    void testGenerateToken_Success() {
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(testUser));

        String token = jwtService.generateToken(testUserDetails);

        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.split("\\.").length == 3); // JWT format: header.payload.signature
    }

    @Test
    void testGenerateToken_UserNotFound() {
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, 
            () -> jwtService.generateToken(testUserDetails));
    }

    @Test
    void testExtractUsername() {
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(testUser));
        
        String token = jwtService.generateToken(testUserDetails);
        String extractedUsername = jwtService.extractUsername(token);

        assertEquals("test@test.com", extractedUsername);
    }

    @Test
    void testExtractId() {
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(testUser));
        
        String token = jwtService.generateToken(testUserDetails);
        int extractedId = jwtService.extractId(token);

        assertEquals(1, extractedId);
    }

    @Test
    void testIsTokenValid() {
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(testUser));
        
        String token = jwtService.generateToken(testUserDetails);
        boolean isValid = jwtService.isTokenValid(token);

        assertTrue(isValid); // fresh token should be valid
    }

    @Test
    void testHasRole_UserRole() {
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(testUser));
        
        String token = jwtService.generateToken(testUserDetails);
        boolean hasUserRole = jwtService.hasRole(token, "USER");
        boolean hasAdminRole = jwtService.hasRole(token, "ADMIN");

        assertTrue(hasUserRole);
        assertFalse(hasAdminRole);
    }

    @Test
    void testHasRole_AdminRole() {
        testUser.setRole("ADMIN");
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(testUser));
        
        String token = jwtService.generateToken(testUserDetails);
        boolean hasAdminRole = jwtService.hasRole(token, "ADMIN");

        assertTrue(hasAdminRole);
    }

    @Test
    void testHasRole_MultipleRoles() {
        testUser.setRole("USER,ADMIN");
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(testUser));
        
        String token = jwtService.generateToken(testUserDetails);
        boolean hasUserRole = jwtService.hasRole(token, "USER");
        boolean hasAdminRole = jwtService.hasRole(token, "ADMIN");

        assertTrue(hasUserRole);
        assertTrue(hasAdminRole);
    }

    @Test
    void testGenerateKey() {
        assertNotNull(jwtService.generateKey());
    }

    @Test
    void testConstructor() {
        JwtService service = new JwtService(userRepository);
        
        assertNotNull(service);
    }

    @Test
    void testTokenContainsClaims() {
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(testUser));
        
        String token = jwtService.generateToken(testUserDetails);
        
        // verify token structure and basic claims
        assertNotNull(jwtService.extractUsername(token));
        assertEquals(1, jwtService.extractId(token));
        assertTrue(jwtService.hasRole(token, "USER"));
    }
}