package com.kk.cibaria.security.jwt;

import com.kk.cibaria.security.UserDetailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtAuthenticationFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private UserDetailService userDetailService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    private JwtAuthenticationFilter filter;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        filter = new JwtAuthenticationFilter(jwtService, userDetailService);
        SecurityContextHolder.clearContext(); // clean slate
    }

    @Test
    void testNoAuthHeader() throws Exception {
        // no Authorization header in request
        when(request.getHeader("Authorization")).thenReturn(null);

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication()); // not authenticated
    }

    @Test
    void testBadAuthHeader() throws Exception {
        // wrong header format (not "Bearer token")
        when(request.getHeader("Authorization")).thenReturn("BadHeader");

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication()); // still not authenticated
    }

    @Test
    void testGoodToken() throws Exception {
        // valid JWT token flow
        String token = "goodToken";
        UserDetails user = User.builder()
            .username("test@test.com")
            .password("pass")
            .authorities(Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")))
            .build();

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtService.extractUsername(token)).thenReturn("test@test.com");
        when(userDetailService.loadUserByUsername("test@test.com")).thenReturn(user);
        when(jwtService.isTokenValid(token)).thenReturn(true);

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNotNull(SecurityContextHolder.getContext().getAuthentication()); // now authenticated
        assertEquals("test@test.com", SecurityContextHolder.getContext().getAuthentication().getName());
    }

    @Test
    void testBadToken() throws Exception {
        // expired/invalid token
        String token = "badToken";
        UserDetails user = User.builder()
            .username("test@test.com")
            .password("pass")
            .authorities(Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")))
            .build();

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtService.extractUsername(token)).thenReturn("test@test.com");
        when(userDetailService.loadUserByUsername("test@test.com")).thenReturn(user);
        when(jwtService.isTokenValid(token)).thenReturn(false); // token is bad

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication()); // not authenticated
    }

    @Test
    void testUserNotFound() throws Exception {
        // token is ok but user doesn't exist in DB
        String token = "goodToken";
        
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtService.extractUsername(token)).thenReturn("test@test.com");
        when(userDetailService.loadUserByUsername("test@test.com")).thenReturn(null); // user not found

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication()); // not authenticated
    }

    @Test
    void testConstructor() {
        JwtAuthenticationFilter newFilter = new JwtAuthenticationFilter(jwtService, userDetailService);
        
        assertNotNull(newFilter);
    }
}