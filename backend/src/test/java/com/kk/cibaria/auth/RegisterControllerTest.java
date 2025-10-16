package com.kk.cibaria.auth;

import com.kk.cibaria.dto.auth.RegisterDto;
import com.kk.cibaria.dto.auth.TokenResponseDto;
import com.kk.cibaria.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegisterControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private RegisterController registerController;

    private RegisterDto registerDto;
    private TokenResponseDto expectedResponse;

    @BeforeEach
    void setup() {
        registerDto = new RegisterDto();
        registerDto.setUsername("testuser");
        registerDto.setEmail("test@test.com");
        registerDto.setPassword("password123");

        expectedResponse = new TokenResponseDto();
        expectedResponse.setToken("jwt.token.here");
        expectedResponse.setType("Bearer");
    }

    @Test
    void registerUser_ValidRegistration_ReturnsTokenResponse() {
        when(userService.save(any(RegisterDto.class)))
                .thenReturn(expectedResponse);

        TokenResponseDto response = registerController.RegisterUser(registerDto);

        assertNotNull(response);
        assertEquals(expectedResponse.getToken(), response.getToken());
        assertEquals(expectedResponse.getType(), response.getType());
        
        verify(userService).save(registerDto);
    }

    @Test
    void registerUser_CallsUserServiceWithCorrectDto() {
        when(userService.save(any(RegisterDto.class)))
                .thenReturn(expectedResponse);

        registerController.RegisterUser(registerDto);

        verify(userService).save(argThat(dto -> 
                dto.getUsername().equals("testuser") &&
                dto.getEmail().equals("test@test.com") &&
                dto.getPassword().equals("password123")
        ));
    }

    @Test
    void registerUser_UserServiceReturnsNull_ReturnsNull() {
        when(userService.save(any(RegisterDto.class)))
                .thenReturn(null);

        TokenResponseDto response = registerController.RegisterUser(registerDto);

        assertNull(response);
        verify(userService).save(registerDto);
    }

    @Test
    void registerUser_UserServiceThrowsException_ExceptionPropagated() {
        RuntimeException expectedException = new RuntimeException("User registration failed");
        when(userService.save(any(RegisterDto.class)))
                .thenThrow(expectedException);

        RuntimeException actualException = assertThrows(RuntimeException.class, 
                () -> registerController.RegisterUser(registerDto));

        assertEquals("User registration failed", actualException.getMessage());
        verify(userService).save(registerDto);
    }

    @Test
    void registerUser_EmptyDto_PassedToUserService() {
        RegisterDto emptyDto = new RegisterDto();
        when(userService.save(any(RegisterDto.class)))
                .thenReturn(expectedResponse);

        TokenResponseDto response = registerController.RegisterUser(emptyDto);

        assertNotNull(response);
        verify(userService).save(emptyDto);
    }
}