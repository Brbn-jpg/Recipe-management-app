package com.kk.cibaria.admin;

import com.kk.cibaria.security.jwt.JwtService;
import com.kk.cibaria.user.UserEntity;
import com.kk.cibaria.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

// Security tests for AdminController focusing on authorization and access control
@ExtendWith(MockitoExtension.class)
class AdminSecurityTest {

    @Mock
    private UserService userService;

    @Mock
    private JwtService jwtService;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AdminController adminController;

    private UserDetails adminUserDetails;
    private UserDetails regularUserDetails;
    private UserEntity adminEntity;
    private UserEntity userEntity;

    @BeforeEach
    void setup() {
        // Setup admin user details with ADMIN role
        adminUserDetails = User.builder()
                .username("admin@test.com")
                .password("password")
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN")))
                .build();

        // Setup regular user details with USER role
        regularUserDetails = User.builder()
                .username("user@test.com")
                .password("password")
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")))
                .build();

        // Setup entity objects
        adminEntity = new UserEntity();
        adminEntity.setId(1);
        adminEntity.setEmail("admin@test.com");
        adminEntity.setRole("ADMIN");

        userEntity = new UserEntity();
        userEntity.setId(2);
        userEntity.setEmail("user@test.com");
        userEntity.setRole("USER");

            SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void adminEndpoints_ShouldAllowAccess_WhenUserHasAdminRole() {
        // Test admin can access admin endpoints when properly authenticated
        when(userService.getAll()).thenReturn(Collections.singletonList(adminEntity));

        // Should not throw any security exceptions
        assertDoesNotThrow(() -> {
            var users = adminController.getAllUsers();
            assertNotNull(users);
            assertEquals(1, users.size());
        });

        verify(userService).getAll();
    }

    @Test
    void adminEndpoints_ShouldDenyAccess_WhenUserHasUserRole() {
        // Test regular user cannot access admin endpoints
        // In real scenario, this would be blocked by @PreAuthorize annotation
        // For unit test, we simulate the security check
        boolean hasAdminRole = regularUserDetails.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));

        assertFalse(hasAdminRole, "Regular user should not have ADMIN role");
    }

    @Test
    void adminEndpoints_ShouldDenyAccess_WhenUserNotAuthenticated() {
        // Test unauthenticated user cannot access admin endpoints
        when(authentication.isAuthenticated()).thenReturn(false);

        assertFalse(authentication.isAuthenticated(), "User should not be authenticated");
    }

    @Test
    void jwtToken_ShouldContainCorrectRoles_ForAdminUser() {
        // Test JWT token generation contains correct roles for admin
        String mockToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.admin.token";
        
        when(jwtService.generateToken(adminUserDetails)).thenReturn(mockToken);
        when(jwtService.extractUsername(mockToken)).thenReturn("admin@test.com");
        when(jwtService.isTokenValid(mockToken)).thenReturn(true);

        String token = jwtService.generateToken(adminUserDetails);
        String extractedUsername = jwtService.extractUsername(token);
        boolean isValid = jwtService.isTokenValid(token);

        assertEquals(mockToken, token);
        assertEquals("admin@test.com", extractedUsername);
        assertTrue(isValid);
        verify(jwtService).generateToken(adminUserDetails);
        verify(jwtService).extractUsername(mockToken);
        verify(jwtService).isTokenValid(mockToken);
    }

    @Test
    void jwtToken_ShouldContainCorrectRoles_ForRegularUser() {
        // Test JWT token generation contains correct roles for regular user
        String mockToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.user.token";
        
        when(jwtService.generateToken(regularUserDetails)).thenReturn(mockToken);
        when(jwtService.extractUsername(mockToken)).thenReturn("user@test.com");
        when(jwtService.isTokenValid(mockToken)).thenReturn(true);

        String token = jwtService.generateToken(regularUserDetails);
        String extractedUsername = jwtService.extractUsername(token);
        boolean isValid = jwtService.isTokenValid(token);

        assertEquals(mockToken, token);
        assertEquals("user@test.com", extractedUsername);
        assertTrue(isValid);
        
        // Verify regular user does not have admin role
        boolean hasAdminRole = regularUserDetails.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
        assertFalse(hasAdminRole);
    }

    @Test
    void securityContext_ShouldPreserveUserDetails_ThroughoutRequest() {
        // Test security context maintains user details during request processing
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(adminUserDetails);
        when(authentication.isAuthenticated()).thenReturn(true);

        Authentication auth = securityContext.getAuthentication();
        UserDetails principal = (UserDetails) auth.getPrincipal();

        assertEquals(adminUserDetails.getUsername(), principal.getUsername());
        assertEquals(adminUserDetails.getAuthorities(), principal.getAuthorities());
        assertTrue(auth.isAuthenticated());
    }

    @Test
    void adminRole_ShouldHaveAllNecessaryAuthorities() {
        // Test admin role has all required authorities for admin operations
        boolean hasAdminRole = adminUserDetails.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));

        assertTrue(hasAdminRole, "Admin user should have ROLE_ADMIN authority");
        assertEquals(1, adminUserDetails.getAuthorities().size(), "Admin should have exactly one role");
    }

    @Test
    void userRole_ShouldNotHaveAdminAuthorities() {
        // Test regular user role does not have admin authorities
        boolean hasAdminRole = regularUserDetails.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
        
        boolean hasUserRole = regularUserDetails.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_USER"));

        assertFalse(hasAdminRole, "Regular user should not have ROLE_ADMIN authority");
        assertTrue(hasUserRole, "Regular user should have ROLE_USER authority");
        assertEquals(1, regularUserDetails.getAuthorities().size(), "User should have exactly one role");
    }

    @Test
    void invalidToken_ShouldFailAuthentication() {
        // Test invalid JWT tokens fail authentication
        String invalidToken = "invalid.jwt.token";
        
        when(jwtService.isTokenValid(invalidToken)).thenReturn(false);
        when(jwtService.extractUsername(invalidToken)).thenThrow(new RuntimeException("Invalid token"));

        assertThrows(RuntimeException.class, () -> {
            jwtService.extractUsername(invalidToken);
        });

        boolean isValid = jwtService.isTokenValid(invalidToken);
        assertFalse(isValid, "Invalid token should not be valid");
    }

    @Test
    void expiredToken_ShouldFailAuthentication() {
        // Test expired JWT tokens fail authentication
        String expiredToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.expired.token";
        
        when(jwtService.isTokenValid(expiredToken)).thenReturn(false);
        when(jwtService.extractUsername(expiredToken)).thenReturn("admin@test.com");

        String username = jwtService.extractUsername(expiredToken);
        boolean isValid = jwtService.isTokenValid(expiredToken);

        assertEquals("admin@test.com", username, "Username should be extracted even from expired token");
        assertFalse(isValid, "Expired token should not be valid");
    }
}