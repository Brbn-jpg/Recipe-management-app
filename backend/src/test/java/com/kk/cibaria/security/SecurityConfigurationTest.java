package com.kk.cibaria.security;

import com.kk.cibaria.security.jwt.JwtAuthenticationFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.cors.CorsConfigurationSource;

import static org.junit.jupiter.api.Assertions.*;

class SecurityConfigurationTest {

    @Mock
    private UserDetailService userDetailService;

    @Mock
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private SecurityConfiguration securityConfiguration;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        securityConfiguration = new SecurityConfiguration(userDetailService, jwtAuthenticationFilter);
    }

    @Test
    void testPasswordEncoder() {
        // create password encoder
        PasswordEncoder encoder = securityConfiguration.passwordEncoder();

        assertNotNull(encoder);
        assertTrue(encoder.encode("password").startsWith("$2a$")); // BCrypt format
        assertTrue(encoder.matches("password", encoder.encode("password")));
    }

    @Test
    void testAuthenticationProviderBean() {
        AuthenticationProvider provider = securityConfiguration.authenticationProvider();

        assertNotNull(provider);
        assertTrue(provider instanceof DaoAuthenticationProvider);
    }

    @Test
    void testAuthenticationManagerBean() {
        AuthenticationManager manager = securityConfiguration.authenticationManager();

        assertNotNull(manager);
    }

    @Test
    void testCorsConfigurationSource() {
        CorsConfigurationSource corsSource = securityConfiguration.corsConfigurationSource();

        assertNotNull(corsSource);
    }

    @Test
    void testConstructor() {
        SecurityConfiguration config = new SecurityConfiguration(userDetailService, jwtAuthenticationFilter);

        assertNotNull(config);
    }

    @Test
    void testPasswordEncoderWithSalt() {
        PasswordEncoder encoder = securityConfiguration.passwordEncoder();
        String password = "testPassword123!";

        String encoded1 = encoder.encode(password);
        String encoded2 = encoder.encode(password);

        assertNotEquals(encoded1, encoded2); // different salt each time
        assertTrue(encoder.matches(password, encoded1));
        assertTrue(encoder.matches(password, encoded2));
        assertFalse(encoder.matches("wrongPassword", encoded1));
    }
}