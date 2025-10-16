package com.kk.cibaria.dto.myProfile;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UpdateEmailDtoTest {

    @Test
    void testDefaultConstructor() {
        UpdateEmailDto dto = new UpdateEmailDto();
        
        assertNull(dto.getNewEmail());
        assertNull(dto.getPassword());
    }

    @Test
    void testSettersAndGetters() {
        UpdateEmailDto dto = new UpdateEmailDto();
        
        dto.setNewEmail("newemail@example.com");
        dto.setPassword("password123");
        
        assertEquals("newemail@example.com", dto.getNewEmail());
        assertEquals("password123", dto.getPassword());
    }

    @Test
    void testLombokGeneratedMethods() {
        UpdateEmailDto dto1 = new UpdateEmailDto();
        dto1.setNewEmail("newemail@example.com");
        dto1.setPassword("password123");
        
        UpdateEmailDto dto2 = new UpdateEmailDto();
        dto2.setNewEmail("newemail@example.com");
        dto2.setPassword("password123");
        
        UpdateEmailDto dto3 = new UpdateEmailDto();
        dto3.setNewEmail("different@example.com");
        dto3.setPassword("password123");
        
        // Test equals
        assertEquals(dto1, dto2);
        assertNotEquals(dto1, dto3);
        
        // Test hashCode
        assertEquals(dto1.hashCode(), dto2.hashCode());
        
        // Test toString
        String toString = dto1.toString();
        assertTrue(toString.contains("newemail@example.com"));
        assertTrue(toString.contains("password123"));
    }

    @Test
    void testWithNullValues() {
        UpdateEmailDto dto = new UpdateEmailDto();
        dto.setNewEmail(null);
        dto.setPassword(null);
        
        assertNull(dto.getNewEmail());
        assertNull(dto.getPassword());
    }

    @Test
    void testWithEmptyValues() {
        UpdateEmailDto dto = new UpdateEmailDto();
        dto.setNewEmail("");
        dto.setPassword("");
        
        assertEquals("", dto.getNewEmail());
        assertEquals("", dto.getPassword());
    }

    @Test
    void testEqualsWithNullValues() {
        UpdateEmailDto dto1 = new UpdateEmailDto();
        UpdateEmailDto dto2 = new UpdateEmailDto();
        
        assertEquals(dto1, dto2);
        
        dto1.setNewEmail("email@example.com");
        assertNotEquals(dto1, dto2);
        
        dto2.setNewEmail("email@example.com");
        assertEquals(dto1, dto2);
    }

    @Test
    void testHashCodeConsistency() {
        UpdateEmailDto dto = new UpdateEmailDto();
        dto.setNewEmail("newemail@example.com");
        dto.setPassword("password123");
        
        int firstHashCode = dto.hashCode();
        int secondHashCode = dto.hashCode();
        
        assertEquals(firstHashCode, secondHashCode);
    }

    @Test
    void testFieldsIndependence() {
        UpdateEmailDto dto = new UpdateEmailDto();
        
        dto.setNewEmail("email@example.com");
        assertEquals("email@example.com", dto.getNewEmail());
        assertNull(dto.getPassword());
        
        dto.setPassword("mypassword");
        assertEquals("email@example.com", dto.getNewEmail());
        assertEquals("mypassword", dto.getPassword());
    }
}