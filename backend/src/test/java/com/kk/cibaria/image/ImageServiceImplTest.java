package com.kk.cibaria.image;

import com.kk.cibaria.cloudinary.CloudinaryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import com.kk.cibaria.exception.ImageErrorException;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ImageServiceImplTest {

    @Mock
    private CloudinaryService cloudinaryService;

    @Mock
    private ImageRepository imageRepository;

    @InjectMocks
    private ImageServiceImpl imageService;

    private MockMultipartFile testFile;
    private Image cloudinaryImage;
    private Image savedImage;

    @BeforeEach
    void setup() {
        testFile = new MockMultipartFile(
                "file",
                "test-image.jpg",
                "image/jpeg",
                "test image content".getBytes()
        );

        cloudinaryImage = new Image();
        cloudinaryImage.setImageUrl("https://res.cloudinary.com/test/image/upload/v123/test.jpg");
        cloudinaryImage.setPublicId("test-public-id");

        savedImage = new Image();
        savedImage.setId(1L);
        savedImage.setImageUrl("https://res.cloudinary.com/test/image/upload/v123/test.jpg");
        savedImage.setPublicId("test-public-id");
        savedImage.setImageType(ImageType.RECIPE);
    }

    @Test
    void createPhoto_ShouldReturnSavedImage_WhenValidFileAndImageType() throws IOException {
        when(cloudinaryService.addPhoto(testFile)).thenReturn(cloudinaryImage);
        when(imageRepository.save(any(Image.class))).thenReturn(savedImage);

        Image result = imageService.createPhoto(testFile, ImageType.RECIPE);

        assertNotNull(result);
        assertEquals(savedImage.getId(), result.getId());
        assertEquals(savedImage.getImageUrl(), result.getImageUrl());
        assertEquals(savedImage.getPublicId(), result.getPublicId());
        assertEquals(ImageType.RECIPE, result.getImageType());
        
        verify(cloudinaryService).addPhoto(testFile);
        verify(imageRepository).save(any(Image.class));
    }

    @Test
    void createPhoto_ShouldSetImageType_WhenImageTypeProvided() throws IOException {
        when(cloudinaryService.addPhoto(testFile)).thenReturn(cloudinaryImage);
        when(imageRepository.save(any(Image.class))).thenAnswer(invocation -> {
            Image imageToSave = invocation.getArgument(0);
            assertEquals(ImageType.PROFILE_PICTURE, imageToSave.getImageType());
            return savedImage;
        });

        Image result = imageService.createPhoto(testFile, ImageType.PROFILE_PICTURE);

        assertNotNull(result);
        verify(cloudinaryService).addPhoto(testFile);
        verify(imageRepository).save(any(Image.class));
    }

    @Test
    void createPhoto_ShouldHandleNullImageType() throws IOException {
        when(cloudinaryService.addPhoto(testFile)).thenReturn(cloudinaryImage);
        when(imageRepository.save(any(Image.class))).thenAnswer(invocation -> {
            Image imageToSave = invocation.getArgument(0);
            assertNull(imageToSave.getImageType());
            return savedImage;
        });

        Image result = imageService.createPhoto(testFile, null);

        assertNotNull(result);
        verify(cloudinaryService).addPhoto(testFile);
        verify(imageRepository).save(any(Image.class));
    }

    @Test
    void createPhoto_ShouldThrowIOException_WhenCloudinaryFails() throws IOException {
        when(cloudinaryService.addPhoto(testFile)).thenThrow(new ImageErrorException("Cloudinary upload failed"));

        assertThrows(ImageErrorException.class, () -> imageService.createPhoto(testFile, ImageType.RECIPE));
        
        verify(cloudinaryService).addPhoto(testFile);
        verify(imageRepository, never()).save(any(Image.class));
    }

    @Test
    void createPhoto_ShouldPreserveCloudinaryData() throws IOException {
        String expectedUrl = "https://res.cloudinary.com/test/upload/custom-url.jpg";
        String expectedPublicId = "custom-public-id";
        
        cloudinaryImage.setImageUrl(expectedUrl);
        cloudinaryImage.setPublicId(expectedPublicId);
        
        when(cloudinaryService.addPhoto(testFile)).thenReturn(cloudinaryImage);
        when(imageRepository.save(any(Image.class))).thenAnswer(invocation -> {
            Image imageToSave = invocation.getArgument(0);
            assertEquals(expectedUrl, imageToSave.getImageUrl());
            assertEquals(expectedPublicId, imageToSave.getPublicId());
            return imageToSave;
        });

        imageService.createPhoto(testFile, ImageType.BACKGROUND_PICTURE);

        verify(cloudinaryService).addPhoto(testFile);
        verify(imageRepository).save(any(Image.class));
    }

    @Test
    void deletePhoto_ShouldCallCloudinaryService() {
        String publicId = "test-public-id";
        doNothing().when(cloudinaryService).removePhoto(publicId);

        imageService.deletePhoto(publicId);

        verify(cloudinaryService).removePhoto(publicId);
    }

    @Test
    void deletePhoto_ShouldNotThrow_WhenCloudinaryServiceCompletes() {
        String publicId = "test-public-id";
        doNothing().when(cloudinaryService).removePhoto(publicId);

        assertDoesNotThrow(() -> imageService.deletePhoto(publicId));
        verify(cloudinaryService).removePhoto(publicId);
    }

    @Test
    void deletePhoto_ShouldHandleEmptyPublicId() {
        String emptyPublicId = "";
        doNothing().when(cloudinaryService).removePhoto(emptyPublicId);

        imageService.deletePhoto(emptyPublicId);

        verify(cloudinaryService).removePhoto(emptyPublicId);
    }

    @Test
    void deletePhoto_ShouldHandleNullPublicId() {
        doNothing().when(cloudinaryService).removePhoto(null);

        imageService.deletePhoto(null);

        verify(cloudinaryService).removePhoto(null);
    }
}