package com.kk.cibaria.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kk.cibaria.dto.auth.LoginFormDto;
import com.kk.cibaria.dto.auth.RegisterDto;
import com.kk.cibaria.dto.auth.TokenResponseDto;
import com.kk.cibaria.user.UserEntity;
import com.kk.cibaria.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties", properties = "spring.profiles.active=test")
@Transactional
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    private UserEntity testUser;

    @BeforeEach
    void setup() {
        userRepository.deleteAll();
        
        testUser = new UserEntity();
        testUser.setUsername("testuser");
        testUser.setEmail("test@test.com");
        testUser.setPassword(passwordEncoder.encode("password123"));
        testUser.setRole("USER");
        testUser = userRepository.save(testUser);
    }

    @Test
    void authenticate_ValidCredentials_ReturnsToken() throws Exception {
        LoginFormDto loginDto = new LoginFormDto("test@test.com", "password123");

        MvcResult result = mockMvc.perform(post("/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDto)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.type").value("Bearer"))
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        TokenResponseDto response = objectMapper.readValue(responseBody, TokenResponseDto.class);
        
        assertNotNull(response.getToken());
        assertFalse(response.getToken().isEmpty());
        assertEquals("Bearer", response.getType());
    }

    @Test
    void authenticate_InvalidEmail_ReturnsNotFound() throws Exception {
        LoginFormDto loginDto = new LoginFormDto("nonexistent@example.com", "password123");

        mockMvc.perform(post("/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void authenticate_InvalidPassword_ReturnsNotFound() throws Exception {
        LoginFormDto loginDto = new LoginFormDto("test@test.com", "wrongpassword");

        mockMvc.perform(post("/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void authenticate_EmptyEmail_ReturnsBadRequest() throws Exception {
        LoginFormDto loginDto = new LoginFormDto("   ", "password123");

        mockMvc.perform(post("/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void authenticate_EmptyPassword_ReturnsBadRequest() throws Exception {
        LoginFormDto loginDto = new LoginFormDto("test@test.com", "   ");

        mockMvc.perform(post("/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_ValidRegistration_ReturnsToken() throws Exception {
        RegisterDto registerDto = new RegisterDto();
        registerDto.setUsername("newuser");
        registerDto.setEmail("newuser@example.com");
        registerDto.setPassword("newpassword123");

        MvcResult result = mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerDto)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.type").value("Bearer"))
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        TokenResponseDto response = objectMapper.readValue(responseBody, TokenResponseDto.class);
        
        assertNotNull(response.getToken());
        assertFalse(response.getToken().isEmpty());
        assertEquals("Bearer", response.getType());

        assertTrue(userRepository.findByEmail("newuser@example.com").isPresent());
    }

    @Test
    void register_ExistingEmail_ReturnsConflict() throws Exception {
        RegisterDto registerDto = new RegisterDto();
        registerDto.setUsername("anotheruser");
        registerDto.setEmail("test@test.com"); // This email already exists
        registerDto.setPassword("password123");

        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerDto)))
                .andExpect(status().isConflict());
    }

    @Test
    void register_EmptyUsername_ReturnsBadRequest() throws Exception {
        RegisterDto registerDto = new RegisterDto();
        registerDto.setUsername("   ");
        registerDto.setEmail("newuser@example.com");
        registerDto.setPassword("password123");

        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_EmptyEmail_ReturnsBadRequest() throws Exception {
        RegisterDto registerDto = new RegisterDto();
        registerDto.setUsername("newuser");
        registerDto.setEmail("   ");
        registerDto.setPassword("password123");

        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_EmptyPassword_ReturnsBadRequest() throws Exception {
        RegisterDto registerDto = new RegisterDto();
        registerDto.setUsername("newuser");
        registerDto.setEmail("newuser@example.com");
        registerDto.setPassword("   ");

        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_InvalidEmailFormat_ReturnsBadRequest() throws Exception {
        RegisterDto registerDto = new RegisterDto();
        registerDto.setUsername("newuser");
        registerDto.setEmail("invalid-email");
        registerDto.setPassword("password123");

        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void authenticate_MalformedJson_ReturnsBadRequest() throws Exception {
        String malformedJson = "{\"email\":\"test@test.com\",\"password\":}";

        mockMvc.perform(post("/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(malformedJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_MalformedJson_ReturnsBadRequest() throws Exception {
        String malformedJson = "{\"username\":\"test\",\"email\":\"test@test.com\",\"password\":}";

        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(malformedJson))
                .andExpect(status().isBadRequest());
    }
}