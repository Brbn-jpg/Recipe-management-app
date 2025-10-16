package com.kk.cibaria.dto.myProfile;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UpdatePasswordDtoTest {

    @Test
    void testDefaultConstructor() {
        UpdatePasswordDto dto = new UpdatePasswordDto();
        
        assertNull(dto.getCurrentPassword());
        assertNull(dto.getNewPassword());
    }

    @Test
    void testSettersAndGetters() {
        UpdatePasswordDto dto = new UpdatePasswordDto();
        
        dto.setCurrentPassword("currentPass123");
        dto.setNewPassword("newPass456");
        
        assertEquals("currentPass123", dto.getCurrentPassword());
        assertEquals("newPass456", dto.getNewPassword());
    }

    @Test
    void testLombokGeneratedMethods() {
        UpdatePasswordDto dto1 = new UpdatePasswordDto();
        dto1.setCurrentPassword("currentPass123");
        dto1.setNewPassword("newPass456");
        
        UpdatePasswordDto dto2 = new UpdatePasswordDto();
        dto2.setCurrentPassword("currentPass123");
        dto2.setNewPassword("newPass456");
        
        UpdatePasswordDto dto3 = new UpdatePasswordDto();
        dto3.setCurrentPassword("differentPass");
        dto3.setNewPassword("newPass456");
        
        // Test equals
        assertEquals(dto1, dto2);
        assertNotEquals(dto1, dto3);
        
        // Test hashCode
        assertEquals(dto1.hashCode(), dto2.hashCode());
        
        // Test toString
        String toString = dto1.toString();
        assertTrue(toString.contains("currentPass123"));
        assertTrue(toString.contains("newPass456"));
    }

    @Test
    void testWithNullValues() {
        UpdatePasswordDto dto = new UpdatePasswordDto();
        dto.setCurrentPassword(null);
        dto.setNewPassword(null);
        
        assertNull(dto.getCurrentPassword());
        assertNull(dto.getNewPassword());
    }

    @Test
    void testWithEmptyValues() {
        UpdatePasswordDto dto = new UpdatePasswordDto();
        dto.setCurrentPassword("");
        dto.setNewPassword("");
        
        assertEquals("", dto.getCurrentPassword());
        assertEquals("", dto.getNewPassword());
    }

    @Test
    void testEqualsWithNullValues() {
        UpdatePasswordDto dto1 = new UpdatePasswordDto();
        UpdatePasswordDto dto2 = new UpdatePasswordDto();
        
        assertEquals(dto1, dto2);
        
        dto1.setCurrentPassword("password");
        assertNotEquals(dto1, dto2);
        
        dto2.setCurrentPassword("password");
        assertEquals(dto1, dto2);
    }

    @Test
    void testHashCodeConsistency() {
        UpdatePasswordDto dto = new UpdatePasswordDto();
        dto.setCurrentPassword("currentPass123");
        dto.setNewPassword("newPass456");
        
        int firstHashCode = dto.hashCode();
        int secondHashCode = dto.hashCode();
        
        assertEquals(firstHashCode, secondHashCode);
    }
}