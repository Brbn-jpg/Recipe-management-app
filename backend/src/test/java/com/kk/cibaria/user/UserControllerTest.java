package com.kk.cibaria.user;

import com.kk.cibaria.dto.myProfile.MyProfileDto;
import com.kk.cibaria.dto.myProfile.UpdateEmailDto;
import com.kk.cibaria.dto.myProfile.UpdatePasswordDto;
import com.kk.cibaria.security.jwt.JwtService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private UserController userController;

    private UserEntity testUser;
    private String testToken;

    @BeforeEach
    void setup() {
        testUser = new UserEntity();
        testUser.setId(1);
        testUser.setUsername("testuser");
        testUser.setEmail("test@test.com");
        testUser.setPassword("password123");
        testUser.setRole("USER");
        testUser.setDescription("Test description");
        testUser.setRating(new ArrayList<>());
        testUser.setUserRecipes(new ArrayList<>());
        testUser.setFavouriteRecipes(new ArrayList<>());
        testUser.setImages(new ArrayList<>());

        testToken = "Bearer testtoken123";
    }

    @Test
    void testGetAll() {
        List<UserEntity> users = List.of(testUser);
        when(userService.getAll()).thenReturn(users);

        List<UserEntity> result = userController.getAll();

        assertEquals(1, result.size());
        assertEquals(testUser, result.get(0));
        verify(userService).getAll();
    }

    @Test
    void testGetById() {
        when(userService.getById(1)).thenReturn(testUser);

        UserEntity result = userController.getById(1);

        assertEquals(testUser, result);
        verify(userService).getById(1);
    }

    @Test
    void testUpdate() {
        UserEntity updatedUser = new UserEntity();
        updatedUser.setUsername("updateduser");
        
        when(userService.update(1, updatedUser)).thenReturn(testUser);

        UserEntity result = userController.update(1, updatedUser, testToken);

        assertEquals(testUser, result);
        verify(userService).update(1, updatedUser);
    }

    @Test
    void testUpdateProfile() {
        MyProfileDto profileDto = new MyProfileDto();
        profileDto.setUsername("updateduser");
        profileDto.setDescription("Updated description");

        when(userService.updateProfile(1, profileDto, testToken)).thenReturn(testUser);

        UserEntity result = userController.updateProfile(1, profileDto, testToken);

        assertEquals(testUser, result);
        verify(userService).updateProfile(1, profileDto, testToken);
    }

    @Test
    void testUpdateEmail() {
        UpdateEmailDto updateEmailDto = new UpdateEmailDto();
        updateEmailDto.setNewEmail("newemail@example.com");
        updateEmailDto.setPassword("password123");

        when(userService.updateEmail(1, updateEmailDto, testToken)).thenReturn(testUser);

        UserEntity result = userController.updateEmail(1, updateEmailDto, testToken);

        assertEquals(testUser, result);
        verify(userService).updateEmail(1, updateEmailDto, testToken);
    }

    @Test
    void testUpdatePassword() {
        UpdatePasswordDto updatePasswordDto = new UpdatePasswordDto();
        updatePasswordDto.setCurrentPassword("currentPassword");
        updatePasswordDto.setNewPassword("NewPassword123");

        when(userService.updatePassword(1, updatePasswordDto, testToken)).thenReturn(testUser);

        UserEntity result = userController.updatePassword(1, updatePasswordDto, testToken);

        assertEquals(testUser, result);
        verify(userService).updatePassword(1, updatePasswordDto, testToken);
    }

    @Test
    void testGetMyProfile() {
        MyProfileDto profileDto = new MyProfileDto();
        profileDto.setId(1);
        profileDto.setUsername("testuser");
        profileDto.setDescription("Test description");

        when(userService.getMyProfile(testToken)).thenReturn(profileDto);

        MyProfileDto result = userController.getMyProfile(testToken);

        assertEquals(profileDto, result);
        verify(userService).getMyProfile(testToken);
    }

    @Test
    void testGetUserRecipes() {
        MyProfileDto profileDto = new MyProfileDto();
        profileDto.setUserRecipes(new ArrayList<>());

        when(userService.getUserRecipes(testToken)).thenReturn(profileDto);

        MyProfileDto result = userController.getUserRecipes(testToken);

        assertEquals(profileDto, result);
        verify(userService).getUserRecipes(testToken);
    }

    @Test
    void testGetFavouriteRecipes() {
        MyProfileDto profileDto = new MyProfileDto();
        profileDto.setFavourites(new ArrayList<>());

        when(userService.getFavouriteRecipes(testToken)).thenReturn(profileDto);

        MyProfileDto result = userController.getFavouriteRecipes(testToken);

        assertEquals(profileDto, result);
        verify(userService).getFavouriteRecipes(testToken);
    }

    @Test
    void testUpdateProfilePicture() throws IOException {
        MockMultipartFile file = new MockMultipartFile("image", "test.jpg", "image/jpeg", "test data".getBytes());
        String imageUrl = "http://test.com/image.jpg";

        when(userService.updateProfilePicture(1, file, testToken)).thenReturn(imageUrl);

        ResponseEntity<String> result = userController.updateProfilePicture(1, file, testToken);

        assertEquals(200, result.getStatusCode().value());
        assertEquals(imageUrl, result.getBody());
        verify(userService).updateProfilePicture(1, file, testToken);
    }

    @Test
    void testUpdateBackgroundPicture() throws IOException {
        MockMultipartFile file = new MockMultipartFile("image", "background.jpg", "image/jpeg", "test data".getBytes());
        String imageUrl = "http://test.com/background.jpg";

        when(userService.updateBackgroundPicture(1, file, testToken)).thenReturn(imageUrl);

        ResponseEntity<String> result = userController.updateBackgroundPicture(1, file, testToken);

        assertEquals(200, result.getStatusCode().value());
        assertEquals(imageUrl, result.getBody());
        verify(userService).updateBackgroundPicture(1, file, testToken);
    }

    @Test
    void testDelete() {
    when(jwtService.isTokenValid("testtoken123")).thenReturn(true);
    when(jwtService.extractId("testtoken123")).thenReturn(1);
    when(jwtService.hasRole("testtoken123", "ADMIN")).thenReturn(false); 
    
    doNothing().when(userService).delete(1);

    assertDoesNotThrow(() -> userController.delete(1, testToken));
    verify(userService).delete(1);
    verify(jwtService).isTokenValid("testtoken123");
    verify(jwtService).extractId("testtoken123");
    }
    
    @Test
    void testUpdateProfilePicture_IOException() throws IOException {
        MockMultipartFile file = new MockMultipartFile("image", "test.jpg", "image/jpeg", "test data".getBytes());
        
        when(userService.updateProfilePicture(1, file, testToken)).thenThrow(new IOException("File upload failed"));

        assertThrows(IOException.class, () -> userController.updateProfilePicture(1, file, testToken));
        verify(userService).updateProfilePicture(1, file, testToken);
    }

    @Test
    void testUpdateBackgroundPicture_IOException() throws IOException {
        MockMultipartFile file = new MockMultipartFile("image", "background.jpg", "image/jpeg", "test data".getBytes());
        
        when(userService.updateBackgroundPicture(1, file, testToken)).thenThrow(new IOException("File upload failed"));

        assertThrows(IOException.class, () -> userController.updateBackgroundPicture(1, file, testToken));
        verify(userService).updateBackgroundPicture(1, file, testToken);
    }
}