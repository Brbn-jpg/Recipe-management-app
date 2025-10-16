package com.kk.cibaria.security;

import com.kk.cibaria.user.UserEntity;
import com.kk.cibaria.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserDetailServiceTest {

    @Mock
    private UserRepository userRepository;

    private UserDetailService userDetailService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        userDetailService = new UserDetailService(userRepository);
    }

    @Test
    void testLoadUser() {
        // create test user
        UserEntity user = new UserEntity();
        user.setEmail("test@test.com");
        user.setPassword("$2a$10$encodedPassword");
        user.setRole("USER");
        
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));

        // load user details
        UserDetails userDetails = userDetailService.loadUserByUsername("test@test.com");

        // check user details
        assertNotNull(userDetails);
        assertEquals("test@test.com", userDetails.getUsername());
        assertEquals("$2a$10$encodedPassword", userDetails.getPassword());
        assertTrue(userDetails.getAuthorities().stream()
            .anyMatch(auth -> auth.getAuthority().equals("ROLE_USER")));
    }

    @Test
    void testLoadUserByUsername_UserNotFound() {
        // given
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // when & then
        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class,
            () -> userDetailService.loadUserByUsername("nonexistent@example.com"));
        
        assertEquals("nonexistent@example.com", exception.getMessage());
    }

    @Test
    void testLoadUserByUsername_WithAdminRole() {
        // given
        UserEntity adminUser = new UserEntity();
        adminUser.setEmail("admin@example.com");
        adminUser.setPassword("$2a$10$adminPassword");
        adminUser.setRole("ADMIN");
        
        when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(adminUser));

        // when
        UserDetails userDetails = userDetailService.loadUserByUsername("admin@example.com");

        // then
        assertNotNull(userDetails);
        assertEquals("admin@example.com", userDetails.getUsername());
        assertTrue(userDetails.getAuthorities().stream()
            .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN")));
    }

    @Test
    void testLoadUserByUsername_WithMultipleRoles() {
        // given
        UserEntity user = new UserEntity();
        user.setEmail("multiuser@example.com");
        user.setPassword("$2a$10$multiPassword");
        user.setRole("USER,ADMIN");
        
        when(userRepository.findByEmail("multiuser@example.com")).thenReturn(Optional.of(user));

        // when
        UserDetails userDetails = userDetailService.loadUserByUsername("multiuser@example.com");

        // then
        assertNotNull(userDetails);
        assertEquals(2, userDetails.getAuthorities().size());
        assertTrue(userDetails.getAuthorities().stream()
            .anyMatch(auth -> auth.getAuthority().equals("ROLE_USER")));
        assertTrue(userDetails.getAuthorities().stream()
            .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN")));
    }

    @Test
    void testLoadUserByUsername_WithEmptyRole() {
        // given
        UserEntity user = new UserEntity();
        user.setEmail("emptyRole@example.com");
        user.setPassword("$2a$10$emptyRolePassword");
        user.setRole(""); // empty role defaults to USER
        
        when(userRepository.findByEmail("emptyRole@example.com")).thenReturn(Optional.of(user));

        // when
        UserDetails userDetails = userDetailService.loadUserByUsername("emptyRole@example.com");

        // then
        assertNotNull(userDetails);
        assertEquals(1, userDetails.getAuthorities().size());
        assertTrue(userDetails.getAuthorities().stream()
            .anyMatch(auth -> auth.getAuthority().equals("ROLE_USER")));
    }

    @Test
    void testConstructor() {
        UserDetailService service = new UserDetailService(userRepository);
        
        assertNotNull(service);
    }
}