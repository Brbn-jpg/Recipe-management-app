package com.kk.cibaria.cloudinary;

import com.cloudinary.Cloudinary;
import com.cloudinary.Uploader;
import com.kk.cibaria.exception.ImageErrorException;
import com.kk.cibaria.image.Image;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class CloudinaryServiceTest {

    @Mock
    private Cloudinary cloudinary;

    @Mock
    private Uploader uploader;

    @Mock
    private MultipartFile file;

    private CloudinaryService service;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        service = new CloudinaryService(cloudinary);
        when(cloudinary.uploader()).thenReturn(uploader);
    }

    @Test
    void testAddPhoto() throws Exception {
        // setup test data
        byte[] fileData = "test image data".getBytes();
        when(file.getBytes()).thenReturn(fileData);
        
        Map<String, Object> uploadResult = new HashMap<>();
        uploadResult.put("public_id", "cibaria/test_image_123");
        uploadResult.put("url", "https://res.cloudinary.com/test/image/upload/cibaria/test_image_123.jpg");
        
        when(uploader.upload(eq(fileData), any(Map.class))).thenReturn(uploadResult);

        // upload photo
        Image result = service.addPhoto(file);

        // check result
        assertNotNull(result);
        assertEquals("cibaria/test_image_123", result.getPublicId());
        assertEquals("https://res.cloudinary.com/test/image/upload/cibaria/test_image_123.jpg", result.getImageUrl());
        
        // verify folder was set to "cibaria"
        verify(uploader).upload(eq(fileData), argThat(options -> 
            options instanceof Map && "cibaria".equals(((Map<?, ?>) options).get("folder"))
        ));
    }

    @Test
    void testAddPhotoFails() throws Exception {
        // setup failing upload
        byte[] fileData = "test image data".getBytes();
        when(file.getBytes()).thenReturn(fileData);
        when(uploader.upload(any(byte[].class), any(Map.class)))
            .thenThrow(new RuntimeException("Upload failed"));

        // try to upload and expect error
        ImageErrorException error = assertThrows(ImageErrorException.class, 
            () -> service.addPhoto(file));
        
        assertEquals("Upload failed", error.getMessage());
    }

    @Test
    void testFileReadError() throws Exception {
        // setup file read error
        when(file.getBytes()).thenThrow(new RuntimeException("File read error"));

        // try to upload and expect error
        ImageErrorException error = assertThrows(ImageErrorException.class, 
            () -> service.addPhoto(file));
        
        assertEquals("File read error", error.getMessage());
    }

    @Test
    void testRemovePhoto() throws Exception {
        // setup successful delete
        String publicId = "cibaria/test_image_123";
        when(uploader.destroy(eq(publicId), any(HashMap.class))).thenReturn(new HashMap<>());

        // remove photo - should not throw error
        assertDoesNotThrow(() -> service.removePhoto(publicId));
        
        verify(uploader).destroy(eq(publicId), any(HashMap.class));
    }

    @Test
    void testRemovePhotoFails() throws Exception {
        // setup failing delete
        String publicId = "cibaria/test_image_123";
        when(uploader.destroy(eq(publicId), any(HashMap.class)))
            .thenThrow(new RuntimeException("Delete failed"));

        // try to delete and expect error
        ImageErrorException error = assertThrows(ImageErrorException.class, 
            () -> service.removePhoto(publicId));
        
        assertEquals("Delete failed", error.getMessage());
    }

    @Test
    void testNullPublicId() throws Exception {
        // setup upload with null public_id
        byte[] fileData = "test image data".getBytes();
        when(file.getBytes()).thenReturn(fileData);
        
        Map<String, Object> uploadResult = new HashMap<>();
        uploadResult.put("public_id", null);
        uploadResult.put("url", "https://res.cloudinary.com/test/image/upload/test.jpg");
        
        when(uploader.upload(eq(fileData), any(Map.class))).thenReturn(uploadResult);

        // should fail with error
        assertThrows(ImageErrorException.class, 
            () -> service.addPhoto(file));
    }

    @Test
    void testNullUrl() throws Exception {
        // setup upload with null url
        byte[] fileData = "test image data".getBytes();
        when(file.getBytes()).thenReturn(fileData);
        
        Map<String, Object> uploadResult = new HashMap<>();
        uploadResult.put("public_id", "cibaria/test_image_123");
        uploadResult.put("url", null);
        
        when(uploader.upload(eq(fileData), any(Map.class))).thenReturn(uploadResult);

        // should fail with error
        assertThrows(ImageErrorException.class, 
            () -> service.addPhoto(file));
    }

    @Test
    void testConstructor() {
        // create service with mock cloudinary
        Cloudinary testCloudinary = mock(Cloudinary.class);
        CloudinaryService testService = new CloudinaryService(testCloudinary);
        
        assertNotNull(testService);
    }
}