package com.kk.cibaria.image;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ImageControllerTest {

    @Mock
    private ImageService imageService;

    @InjectMocks
    private ImageController imageController;

    private MockMultipartFile testFile;
    private Image testImage;

    @BeforeEach
    void setup() {
        testFile = new MockMultipartFile(
                "file",
                "test-image.jpg",
                "image/jpeg",
                "test image content".getBytes()
        );

        testImage = new Image();
        testImage.setId(1L);
        testImage.setImageUrl("https://res.cloudinary.com/test/image/upload/v123/test.jpg");
        testImage.setPublicId("test");
        testImage.setImageType(ImageType.RECIPE);
    }

    @Test
    void createPhoto_ShouldReturnImage_WhenValidFile() throws IOException {
        when(imageService.createPhoto(testFile, null)).thenReturn(testImage);

        Image result = imageController.createPhoto(testFile);

        assertNotNull(result);
        assertEquals(testImage.getId(), result.getId());
        assertEquals(testImage.getImageUrl(), result.getImageUrl());
        assertEquals(testImage.getPublicId(), result.getPublicId());
        assertEquals(testImage.getImageType(), result.getImageType());
        verify(imageService).createPhoto(testFile, null);
    }

    @Test
    void createPhoto_ShouldThrowIOException_WhenServiceThrowsException() throws IOException {
        when(imageService.createPhoto(testFile, null)).thenThrow(new IOException("Upload failed"));

        assertThrows(IOException.class, () -> imageController.createPhoto(testFile));
        verify(imageService).createPhoto(testFile, null);
    }

    @Test
    void deletePhoto_ShouldCallService_WhenValidPublicId() {
        String publicId = "test-public-id";
        doNothing().when(imageService).deletePhoto(publicId);

        imageController.deletePhoto(publicId);

        verify(imageService).deletePhoto(publicId);
    }

    @Test
    void deletePhoto_ShouldNotThrow_WhenServiceCallsComplete() {
        String publicId = "test-public-id";
        doNothing().when(imageService).deletePhoto(publicId);

        assertDoesNotThrow(() -> imageController.deletePhoto(publicId));
        verify(imageService).deletePhoto(publicId);
    }
}