package com.kk.cibaria.image;

import com.kk.cibaria.cloudinary.CloudinaryService;
import com.kk.cibaria.user.UserEntity;
import com.kk.cibaria.user.UserRepository;
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
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.kk.cibaria.exception.ImageErrorException;
import org.mockito.ArgumentMatchers;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.reset;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties", properties = "spring.profiles.active=test")
@Transactional
class ImageControllerIntegrationTest {

    @TestConfiguration
    static class TestConfig {
        
        @Bean
        @Primary
        public CloudinaryService mockCloudinaryService() {
            return mock(CloudinaryService.class);
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ImageRepository imageRepository;

    @Autowired
    private UserRepository userRepository;


    @Autowired
    private CloudinaryService cloudinaryService;

    private UserEntity testUser;
    private Image testImage;

    @BeforeEach
    void setup() {
        // Reset mock before each test
        reset(cloudinaryService);
        
        testUser = new UserEntity();
        testUser.setUsername("imageTestUser");
        testUser.setEmail("imagetest@test.com");
        testUser.setPassword("hashedPassword");
        testUser.setRole("USER");
        testUser = userRepository.save(testUser);

        // userToken not needed for image endpoints (public access)

        testImage = new Image();
        testImage.setImageUrl("https://res.cloudinary.com/test/image/upload/v123/test.jpg");
        testImage.setPublicId("test-public-id");
        testImage.setImageType(ImageType.RECIPE);
        testImage.setUser(testUser);
    }

    @Test
    void addPhoto_ShouldCreateAndSaveImage_WhenValidFile() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test-image.jpg",
                "image/jpeg",
                "test image content".getBytes()
        );

        Image cloudinaryResponse = new Image();
        cloudinaryResponse.setImageUrl("https://res.cloudinary.com/test/upload/new-image.jpg");
        cloudinaryResponse.setPublicId("new-public-id");

        when(cloudinaryService.addPhoto(ArgumentMatchers.any())).thenReturn(cloudinaryResponse);

        mockMvc.perform(multipart("/image/addPhoto")
                        .file(file)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.imageUrl").value("https://res.cloudinary.com/test/upload/new-image.jpg"))
                .andExpect(jsonPath("$.publicId").value("new-public-id"));

        assertEquals(1, imageRepository.count());
        Image savedImage = imageRepository.findAll().get(0);
        assertEquals("https://res.cloudinary.com/test/upload/new-image.jpg", savedImage.getImageUrl());
        assertEquals("new-public-id", savedImage.getPublicId());

        verify(cloudinaryService).addPhoto(ArgumentMatchers.any());
    }

    @Test
    void addPhoto_ShouldReturn400_WhenCloudinaryFails() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test-image.jpg",
                "image/jpeg",
                "test image content".getBytes()
        );

        when(cloudinaryService.addPhoto(ArgumentMatchers.any())).thenThrow(new ImageErrorException("Cloudinary service unavailable"));

        mockMvc.perform(multipart("/image/addPhoto")
                        .file(file)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest());

        assertEquals(0, imageRepository.count());
        verify(cloudinaryService).addPhoto(ArgumentMatchers.any());
    }

    @Test
    void addPhoto_ShouldHandleEmptyFile() throws Exception {
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file",
                "empty.jpg",
                "image/jpeg",
                new byte[0]
        );

        Image cloudinaryResponse = new Image();
        cloudinaryResponse.setImageUrl("https://res.cloudinary.com/test/upload/empty.jpg");
        cloudinaryResponse.setPublicId("empty-id");

        when(cloudinaryService.addPhoto(ArgumentMatchers.any())).thenReturn(cloudinaryResponse);

        mockMvc.perform(multipart("/image/addPhoto")
                        .file(emptyFile)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk());

        verify(cloudinaryService).addPhoto(ArgumentMatchers.any());
    }

    @Test
    void deletePhoto_ShouldCallCloudinaryService() throws Exception {
        String publicId = "test-public-id-to-delete";
        doNothing().when(cloudinaryService).removePhoto(publicId);

        mockMvc.perform(post("/image/deletePhoto")
                        .param("publicId", publicId))
                .andExpect(status().isOk());

        verify(cloudinaryService).removePhoto(publicId);
    }

    @Test
    void deletePhoto_ShouldHandleEmptyPublicId() throws Exception {
        doNothing().when(cloudinaryService).removePhoto("");

        mockMvc.perform(post("/image/deletePhoto")
                        .param("publicId", ""))
                .andExpect(status().isOk());

        verify(cloudinaryService).removePhoto("");
    }

    @Test
    void addPhoto_ShouldPersistCorrectImageType() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "profile.jpg",
                "image/jpeg",
                "profile image content".getBytes()
        );

        Image cloudinaryResponse = new Image();
        cloudinaryResponse.setImageUrl("https://res.cloudinary.com/test/upload/profile.jpg");
        cloudinaryResponse.setPublicId("profile-id");

        when(cloudinaryService.addPhoto(ArgumentMatchers.any())).thenReturn(cloudinaryResponse);

        mockMvc.perform(multipart("/image/addPhoto")
                        .file(file)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk());

        assertEquals(1, imageRepository.count());
        Image savedImage = imageRepository.findAll().get(0);
        assertNull(savedImage.getImageType()); // Service is called with null in controller
        
        verify(cloudinaryService).addPhoto(ArgumentMatchers.any());
    }

    @Test
    void imageEndpoints_ShouldBeAccessibleWithoutAuthentication() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "public-image.jpg",
                "image/jpeg",
                "public image content".getBytes()
        );

        Image cloudinaryResponse = new Image();
        cloudinaryResponse.setImageUrl("https://res.cloudinary.com/test/upload/public.jpg");
        cloudinaryResponse.setPublicId("public-id");

        when(cloudinaryService.addPhoto(ArgumentMatchers.any())).thenReturn(cloudinaryResponse);

        // Test without authorization header
        mockMvc.perform(multipart("/image/addPhoto")
                        .file(file)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk());

        mockMvc.perform(post("/image/deletePhoto")
                        .param("publicId", "test-id"))
                .andExpect(status().isOk());

        verify(cloudinaryService).addPhoto(ArgumentMatchers.any());
        verify(cloudinaryService).removePhoto("test-id");
    }
}