package com.kk.cibaria.auth;

import com.kk.cibaria.dto.auth.LoginFormDto;
import com.kk.cibaria.dto.auth.TokenResponseDto;
import com.kk.cibaria.exception.UserNotFoundException;
import com.kk.cibaria.security.UserDetailService;
import com.kk.cibaria.security.jwt.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoginControllerTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @Mock
    private UserDetailService userDetailService;

    @Mock
    private UserDetails userDetails;

    @InjectMocks
    private LoginController loginController;

    private LoginFormDto loginFormDto;

    @BeforeEach
    void setup() {
        loginFormDto = new LoginFormDto("test@test.com", "password123");
    }

    @Test
    void authenticate_ValidCredentials_ReturnsToken() {
        String expectedToken = "jwt.token.here";
        
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(null);
        when(userDetailService.loadUserByUsername("test@test.com"))
                .thenReturn(userDetails);
        when(jwtService.generateToken(userDetails))
                .thenReturn(expectedToken);

        TokenResponseDto response = loginController.authenticate(loginFormDto);

        assertNotNull(response);
        assertEquals(expectedToken, response.getToken());
        assertEquals("Bearer", response.getType());
        
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userDetailService).loadUserByUsername("test@test.com");
        verify(jwtService).generateToken(userDetails);
    }

    @Test
    void authenticate_InvalidCredentials_ThrowsUserNotFoundException() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        UserNotFoundException exception = assertThrows(UserNotFoundException.class, 
                () -> loginController.authenticate(loginFormDto));

        assertEquals("Invalid credentials", exception.getMessage());
        
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userDetailService, never()).loadUserByUsername(anyString());
        verify(jwtService, never()).generateToken(any());
    }

    @Test
    void authenticate_AuthenticationManagerThrowsException_ThrowsUserNotFoundException() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Authentication failed"));

        assertThrows(UserNotFoundException.class, 
                () -> loginController.authenticate(loginFormDto));
        
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userDetailService, never()).loadUserByUsername(anyString());
        verify(jwtService, never()).generateToken(any());
    }

    @Test
    void authenticate_VerifiesCorrectAuthenticationToken() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(null);
        when(userDetailService.loadUserByUsername(anyString()))
                .thenReturn(userDetails);
        when(jwtService.generateToken(any()))
                .thenReturn("token");

        loginController.authenticate(loginFormDto);

        verify(authenticationManager).authenticate(argThat(token -> 
                token instanceof UsernamePasswordAuthenticationToken &&
                token.getPrincipal().equals("test@test.com") &&
                token.getCredentials().equals("password123")
        ));
    }
}