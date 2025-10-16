package com.kk.cibaria.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kk.cibaria.cloudinary.CloudinaryService;
import com.kk.cibaria.dto.auth.LoginFormDto;
import com.kk.cibaria.dto.myProfile.MyProfileDto;
import com.kk.cibaria.dto.myProfile.UpdateEmailDto;
import com.kk.cibaria.dto.myProfile.UpdatePasswordDto;
import com.kk.cibaria.image.Image;
import com.kk.cibaria.image.ImageService;
import com.kk.cibaria.image.ImageType;
import com.kk.cibaria.ingredient.IngredientService;
import com.kk.cibaria.recipe.Recipe;
import com.kk.cibaria.recipe.RecipeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties", properties = "spring.profiles.active=test")
@Transactional
class UserControllerIntegrationTest {

    @TestConfiguration
    static class TestConfig {
        
        @Bean
        @Primary
        public CloudinaryService mockCloudinaryService() {
            return mock(CloudinaryService.class);
        }
        
        @Bean
        @Primary
        public ImageService mockImageService() throws IOException {
            ImageService mockImageService = mock(ImageService.class);
            when(mockImageService.createPhoto(any(), any(ImageType.class))).thenAnswer(invocation -> {
                Image mockImage = new Image();
                // Don't set ID, let JPA handle it
                mockImage.setImageUrl("http://test.com/image.jpg");
                mockImage.setPublicId("test_public_id_" + System.currentTimeMillis());
                return mockImage;
            });
            return mockImageService;
        }
        
        @Bean
        @Primary
        public IngredientService mockIngredientService() {
            IngredientService mockIngredientService = mock(IngredientService.class);
            when(mockIngredientService.filterByLanguage(any(), any())).thenReturn(new ArrayList<>());
            return mockIngredientService;
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RecipeRepository recipeRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private UserEntity testUser;
    private String authToken;

    @BeforeEach
    void setup() throws Exception {
        userRepository.deleteAll();
        recipeRepository.deleteAll();
        
        testUser = new UserEntity();
        testUser.setUsername("testuser");
        testUser.setEmail("test@test.com");
        testUser.setPassword(passwordEncoder.encode("password123"));
        testUser.setRole("USER");
        testUser.setDescription("Test description");
        testUser = userRepository.save(testUser);

        LoginFormDto loginDto = new LoginFormDto("test@test.com", "password123");

        MvcResult loginResult = mockMvc.perform(post("/authenticate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDto)))
                .andExpect(status().isOk())
                .andReturn();

        String loginResponse = loginResult.getResponse().getContentAsString();
        authToken = "Bearer " + objectMapper.readTree(loginResponse).get("token").asText();
    }

    @Test
    void testGetAllUsers() throws Exception {
        mockMvc.perform(get("/users")
                .header("Authorization", authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].username").value("testuser"))
                .andExpect(jsonPath("$[0].email").value("test@test.com"));
    }

    @Test
    void testGetUserById() throws Exception {
        mockMvc.perform(get("/users/{id}", testUser.getId())
                .header("Authorization", authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testUser.getId()))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@test.com"));
    }

    @Test
    void testGetUserById_NotFound() throws Exception {
        mockMvc.perform(get("/users/{id}", 99999)
                .header("Authorization", authToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void testUpdateProfile() throws Exception {
        MyProfileDto profileDto = new MyProfileDto();
        profileDto.setUsername("updateduser");
        profileDto.setDescription("Updated description");

        mockMvc.perform(put("/users/{id}/profile", testUser.getId())
                .header("Authorization", authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(profileDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("updateduser"))
                .andExpect(jsonPath("$.description").value("Updated description"));

        UserEntity updatedUser = userRepository.findById(testUser.getId()).orElseThrow();
        assertEquals("updateduser", updatedUser.getUsername());
        assertEquals("Updated description", updatedUser.getDescription());
    }

    @Test
    void testUpdateProfile_Unauthorized() throws Exception {
        UserEntity anotherUser = new UserEntity();
        anotherUser.setUsername("anotheruser");
        anotherUser.setEmail("another@example.com");
        anotherUser.setPassword(passwordEncoder.encode("password123"));
        anotherUser.setRole("USER");
        anotherUser = userRepository.save(anotherUser);

        MyProfileDto profileDto = new MyProfileDto();
        profileDto.setUsername("hacker");

        mockMvc.perform(put("/users/{id}/profile", anotherUser.getId())
                .header("Authorization", authToken) // Using testUser's token for anotherUser
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(profileDto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testUpdateEmail() throws Exception {
        UpdateEmailDto updateEmailDto = new UpdateEmailDto();
        updateEmailDto.setNewEmail("newemail@example.com");
        updateEmailDto.setPassword("password123");

        mockMvc.perform(put("/users/{id}/email", testUser.getId())
                .header("Authorization", authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateEmailDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("newemail@example.com"));

        UserEntity updatedUser = userRepository.findById(testUser.getId()).orElseThrow();
        assertEquals("newemail@example.com", updatedUser.getEmail());
    }

    @Test
    void testUpdateEmail_WrongPassword() throws Exception {
        UpdateEmailDto updateEmailDto = new UpdateEmailDto();
        updateEmailDto.setNewEmail("newemail@example.com");
        updateEmailDto.setPassword("wrongpassword");

        mockMvc.perform(put("/users/{id}/email", testUser.getId())
                .header("Authorization", authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateEmailDto)))
                .andExpect(status().is4xxClientError()); // InvalidPasswordException should return 4xx
    }

    @Test
    void testUpdatePassword() throws Exception {
        UpdatePasswordDto updatePasswordDto = new UpdatePasswordDto();
        updatePasswordDto.setCurrentPassword("password123");
        updatePasswordDto.setNewPassword("NewPassword123");

        mockMvc.perform(put("/users/{id}/password", testUser.getId())
                .header("Authorization", authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatePasswordDto)))
                .andExpect(status().isOk());

        UserEntity updatedUser = userRepository.findById(testUser.getId()).orElseThrow();
        assertTrue(passwordEncoder.matches("NewPassword123", updatedUser.getPassword()));
    }

    @Test
    void testUpdatePassword_WeakPassword() throws Exception {
        UpdatePasswordDto updatePasswordDto = new UpdatePasswordDto();
        updatePasswordDto.setCurrentPassword("password123");
        updatePasswordDto.setNewPassword("weak");

        mockMvc.perform(put("/users/{id}/password", testUser.getId())
                .header("Authorization", authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatePasswordDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetMyProfile() throws Exception {
        mockMvc.perform(get("/users/aboutme")
                .header("Authorization", authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testUser.getId()))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.description").value("Test description"));
    }

    @Test
    void testGetUserRecipes() throws Exception {
        Recipe recipe = new Recipe();
        recipe.setRecipeName("Test Recipe");
        recipe.setCategory("Breakfast");
        recipe.setDifficulty(1);
        recipe.setPrepareTime(15);
        recipe.setServings(2);
        recipe.setIsPublic(true);
        recipe.setLanguage("en");
        recipe.setUser(testUser);
        recipe.setIngredients(new ArrayList<>());
        recipe.setSteps(new ArrayList<>());
        recipe.setRatings(new ArrayList<>());
        recipe.setImages(new ArrayList<>());
        recipe.setFavouriteByUsers(new ArrayList<>());
        recipeRepository.save(recipe);

        mockMvc.perform(get("/users/recipes")
                .header("Authorization", authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userRecipes").isArray());
    }

    @Test
    void testGetFavouriteRecipes() throws Exception {
        mockMvc.perform(get("/users/favourites")
                .header("Authorization", authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.favourites").isArray());
    }

    @Test
    void testUpdateProfilePicture() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "profile.jpg", "image/jpeg", "test image data".getBytes());

        mockMvc.perform(multipart("/users/{userId}/profile-picture", testUser.getId())
                .file(file)
                .header("Authorization", authToken)
                .with(request -> {
                    request.setMethod("PUT");
                    return request;
                }))
                .andExpect(status().isOk())
                .andExpect(content().string("http://test.com/image.jpg"));
    }

    @Test
    void testUpdateBackgroundPicture() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "background.jpg", "image/jpeg", "test image data".getBytes());

        mockMvc.perform(multipart("/users/{userId}/background-picture", testUser.getId())
                .file(file)
                .header("Authorization", authToken)
                .with(request -> {
                    request.setMethod("PUT");
                    return request;
                }))
                .andExpect(status().isOk())
                .andExpect(content().string("http://test.com/image.jpg"));
    }

    @Test
    void testUpdateProfilePicture_Unauthorized() throws Exception {
        UserEntity anotherUser = new UserEntity();
        anotherUser.setUsername("anotheruser");
        anotherUser.setEmail("another@example.com");
        anotherUser.setPassword(passwordEncoder.encode("password123"));
        anotherUser.setRole("USER");
        anotherUser = userRepository.save(anotherUser);

        MockMultipartFile file = new MockMultipartFile("file", "profile.jpg", "image/jpeg", "test image data".getBytes());

        mockMvc.perform(multipart("/users/{userId}/profile-picture", anotherUser.getId())
                .file(file)
                .header("Authorization", authToken) // Using testUser's token for anotherUser
                .with(request -> {
                    request.setMethod("PUT");
                    return request;
                }))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testGetMyProfile_Unauthorized() throws Exception {
        mockMvc.perform(get("/users/aboutme"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testUpdateProfile_ValidationError() throws Exception {
        MyProfileDto profileDto = new MyProfileDto();
        profileDto.setUsername(""); // Empty username

        mockMvc.perform(put("/users/{id}/profile", testUser.getId())
                .header("Authorization", authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(profileDto)))
                .andExpect(status().isOk()); // UserService handles empty username by not updating it

        UserEntity user = userRepository.findById(testUser.getId()).orElseThrow();
        assertEquals("testuser", user.getUsername()); // Should remain unchanged
    }

    @Test
    void testDeleteUser() throws Exception {
        mockMvc.perform(delete("/users/{id}", testUser.getId())
                .header("Authorization", authToken))
                .andExpect(status().is(204));

        assertFalse(userRepository.findById(testUser.getId()).isPresent());
    }
}