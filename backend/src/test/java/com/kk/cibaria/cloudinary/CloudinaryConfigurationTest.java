package com.kk.cibaria.cloudinary;

import com.cloudinary.Cloudinary;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class CloudinaryConfigurationTest {

    @Test
    void testCloudinaryBeanCreation() {
        // given
        CloudinaryConfiguration config = new CloudinaryConfiguration();
        ReflectionTestUtils.setField(config, "apiKey", "test_api_key");
        ReflectionTestUtils.setField(config, "secretKey", "test_secret_key");
        ReflectionTestUtils.setField(config, "cloudName", "test_cloud_name");

        // when
        Cloudinary cloudinary = config.cloudinary();

        // then
        assertNotNull(cloudinary);
        assertEquals("test_api_key", cloudinary.config.apiKey);
        assertEquals("test_secret_key", cloudinary.config.apiSecret);
        assertEquals("test_cloud_name", cloudinary.config.cloudName);
    }

    @Test
    void testCloudinaryBeanWithNullValues() {
        // given
        CloudinaryConfiguration config = new CloudinaryConfiguration();
        ReflectionTestUtils.setField(config, "apiKey", null);
        ReflectionTestUtils.setField(config, "secretKey", null);
        ReflectionTestUtils.setField(config, "cloudName", null);

        // when
        Cloudinary cloudinary = config.cloudinary();

        // then
        assertNotNull(cloudinary);
        assertNull(cloudinary.config.apiKey);
        assertNull(cloudinary.config.apiSecret);
        assertNull(cloudinary.config.cloudName);
    }

    @Test
    void testCloudinaryBeanWithEmptyValues() {
        // given
        CloudinaryConfiguration config = new CloudinaryConfiguration();
        ReflectionTestUtils.setField(config, "apiKey", "");
        ReflectionTestUtils.setField(config, "secretKey", "");
        ReflectionTestUtils.setField(config, "cloudName", "");

        // when
        Cloudinary cloudinary = config.cloudinary();

        // then
        assertNotNull(cloudinary);
        assertEquals("", cloudinary.config.apiKey);
        assertEquals("", cloudinary.config.apiSecret);
        assertEquals("", cloudinary.config.cloudName);
    }
}