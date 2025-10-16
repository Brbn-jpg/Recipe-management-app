package com.kk.cibaria.user;

import com.kk.cibaria.cloudinary.CloudinaryService;
import com.kk.cibaria.dto.auth.RegisterDto;
import com.kk.cibaria.dto.auth.TokenResponseDto;
import com.kk.cibaria.dto.myProfile.MyProfileDto;
import com.kk.cibaria.dto.myProfile.MyProfileRecipeDto;
import com.kk.cibaria.dto.myProfile.UpdateEmailDto;
import com.kk.cibaria.dto.myProfile.UpdatePasswordDto;
import com.kk.cibaria.exception.*;
import com.kk.cibaria.image.Image;
import com.kk.cibaria.image.ImageRepository;
import com.kk.cibaria.image.ImageService;
import com.kk.cibaria.image.ImageType;
import com.kk.cibaria.rating.Rating;
import com.kk.cibaria.recipe.Recipe;
import com.kk.cibaria.security.UserDetailService;
import com.kk.cibaria.security.jwt.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private UserDetailService userDetailService;

    @Mock
    private ImageService imageService;

    @Mock
    private CloudinaryService cloudinaryService;

    @Mock
    private ImageRepository imageRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private UserEntity user;
    private String token;

    @BeforeEach
    void setup() {
        // create test user
        user = new UserEntity();
        user.setId(1);
        user.setUsername("testuser");
        user.setEmail("test@test.com");
        user.setPassword("encodedPassword");
        user.setRole("USER");
        user.setDescription("Test description");
        user.setRating(new ArrayList<>());
        user.setUserRecipes(new ArrayList<>());
        user.setFavouriteRecipes(new ArrayList<>());
        user.setImages(new ArrayList<>());

        token = "Bearer testtoken123";
    }

    @Test
    void testGetAll() {
        List<UserEntity> users = List.of(user);
        when(userRepository.findAll()).thenReturn(users);

        List<UserEntity> result = userService.getAll();

        assertEquals(1, result.size());
        assertEquals(user, result.get(0));
        verify(userRepository).findAll();
    }

    @Test
    void testGetById_Success() {
        when(userRepository.findById(1)).thenReturn(Optional.of(user));

        UserEntity result = userService.getById(1);

        assertEquals(user, result);
        verify(userRepository).findById(1);
    }

    @Test
    void testGetById_UserNotFound() {
        when(userRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.getById(1));
        verify(userRepository).findById(1);
    }

    @Test
    void testSave_Success() {
        RegisterDto registerDto = new RegisterDto();
        registerDto.setEmail("new@example.com");
        registerDto.setPassword("password123");
        registerDto.setUsername("newuser");

        UserDetails userDetails = mock(UserDetails.class);
        when(userRepository.findByEmail("new@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(UserEntity.class))).thenReturn(user);
        when(userDetailService.loadUserByUsername("test@test.com")).thenReturn(userDetails);
        when(jwtService.generateToken(userDetails)).thenReturn("generatedToken");

        TokenResponseDto result = userService.save(registerDto);

        assertNotNull(result);
        assertEquals("generatedToken", result.getToken());
        verify(userRepository).findByEmail("new@example.com");
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(any(UserEntity.class));
    }

    @Test
    void testSave_EmailAlreadyExists() {
        RegisterDto registerDto = new RegisterDto();
        registerDto.setEmail("existing@example.com");

        when(userRepository.findByEmail("existing@example.com")).thenReturn(Optional.of(user));

        assertThrows(UserEmailAlreadyExistException.class, () -> userService.save(registerDto));
        verify(userRepository).findByEmail("existing@example.com");
        verify(userRepository, never()).save(any());
    }

    @Test
    void testUpdate_Success() {
        UserEntity updatedUser = new UserEntity();
        updatedUser.setUsername("updateduser");
        updatedUser.setDescription("Updated description");
        updatedUser.setPassword("newPassword");
        updatedUser.setEmail("updated@example.com");
        updatedUser.setRating(new ArrayList<>());

        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(userRepository.save(any(UserEntity.class))).thenReturn(user);

        UserEntity result = userService.update(1, updatedUser);

        assertEquals(user, result);
        verify(userRepository).findById(1);
        verify(userRepository).save(user);
    }

    @Test
    void testUpdateProfile_Success() {
        MyProfileDto profileDto = new MyProfileDto();
        profileDto.setUsername("updateduser");
        profileDto.setDescription("Updated description");

        when(jwtService.extractId("testtoken123")).thenReturn(1);
        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        UserEntity result = userService.updateProfile(1, profileDto, token);

        assertEquals("updateduser", result.getUsername());
        assertEquals("Updated description", result.getDescription());
        verify(userRepository).save(user);
    }

    @Test
    void testUpdateProfile_Unauthorized() {
        MyProfileDto profileDto = new MyProfileDto();
        
        when(jwtService.extractId("testtoken123")).thenReturn(2); // Different user ID
        when(userRepository.findById(2)).thenReturn(Optional.of(new UserEntity()));

        assertThrows(UnauthorizedException.class, 
            () -> userService.updateProfile(1, profileDto, token));
    }

    @Test
    void testUpdateEmail_Success() {
        UpdateEmailDto updateEmailDto = new UpdateEmailDto();
        updateEmailDto.setNewEmail("newemail@example.com");
        updateEmailDto.setPassword("password123");

        when(jwtService.extractId("testtoken123")).thenReturn(1);
        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(true);
        when(userRepository.findByEmail("newemail@example.com")).thenReturn(Optional.empty());
        when(userRepository.save(user)).thenReturn(user);

        UserEntity result = userService.updateEmail(1, updateEmailDto, token);

        assertEquals("newemail@example.com", result.getEmail());
        verify(userRepository).save(user);
    }

    @Test
    void testUpdateEmail_InvalidFormat() {
        UpdateEmailDto updateEmailDto = new UpdateEmailDto();
        updateEmailDto.setNewEmail("invalid-email");
        updateEmailDto.setPassword("password123");

        when(jwtService.extractId("testtoken123")).thenReturn(1);
        when(userRepository.findById(1)).thenReturn(Optional.of(user));

        assertThrows(InvalidEmailFormatException.class, 
            () -> userService.updateEmail(1, updateEmailDto, token));
    }

    @Test
    void testUpdateEmail_WrongPassword() {
        UpdateEmailDto updateEmailDto = new UpdateEmailDto();
        updateEmailDto.setNewEmail("newemail@example.com");
        updateEmailDto.setPassword("wrongpassword");

        when(jwtService.extractId("testtoken123")).thenReturn(1);
        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongpassword", "encodedPassword")).thenReturn(false);

        assertThrows(InvalidPasswordException.class, 
            () -> userService.updateEmail(1, updateEmailDto, token));
    }

    @Test
    void testUpdatePassword_Success() {
        UpdatePasswordDto updatePasswordDto = new UpdatePasswordDto();
        updatePasswordDto.setCurrentPassword("currentPassword");
        updatePasswordDto.setNewPassword("NewPassword123");

        when(jwtService.extractId("testtoken123")).thenReturn(1);
        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("currentPassword", "encodedPassword")).thenReturn(true);
        when(passwordEncoder.matches("NewPassword123", "encodedPassword")).thenReturn(false);
        when(passwordEncoder.encode("NewPassword123")).thenReturn("newEncodedPassword");
        when(userRepository.save(user)).thenReturn(user);

        UserEntity result = userService.updatePassword(1, updatePasswordDto, token);

        assertEquals("newEncodedPassword", result.getPassword());
        verify(userRepository).save(user);
    }

    @Test
    void testUpdatePassword_WeakPassword() {
        UpdatePasswordDto updatePasswordDto = new UpdatePasswordDto();
        updatePasswordDto.setCurrentPassword("currentPassword");
        updatePasswordDto.setNewPassword("weak");

        when(jwtService.extractId("testtoken123")).thenReturn(1);
        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("currentPassword", "encodedPassword")).thenReturn(true);

        assertThrows(WeakPasswordException.class, 
            () -> userService.updatePassword(1, updatePasswordDto, token));
    }

    @Test
    void testUpdateProfilePicture_Success() throws IOException {
        MockMultipartFile file = new MockMultipartFile("image", "test.jpg", "image/jpeg", "test data".getBytes());
        Image mockImage = new Image();
        mockImage.setImageUrl("http://test.com/image.jpg");

        when(jwtService.extractId("testtoken123")).thenReturn(1);
        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(imageService.createPhoto(file, ImageType.PROFILE_PICTURE)).thenReturn(mockImage);
        when(imageRepository.save(mockImage)).thenReturn(mockImage);
        when(userRepository.save(user)).thenReturn(user);

        String result = userService.updateProfilePicture(1, file, token);

        assertEquals("http://test.com/image.jpg", result);
        verify(imageService).createPhoto(file, ImageType.PROFILE_PICTURE);
    }

    @Test
    void testDelete_Success() {
        when(userRepository.findById(1)).thenReturn(Optional.of(user));

        assertDoesNotThrow(() -> userService.delete(1));
        verify(userRepository).delete(user);
    }

    @Test
    void testGetMyProfile_Success() {
        Image profileImage = new Image();
        profileImage.setImageType(ImageType.PROFILE_PICTURE);
        profileImage.setImageUrl("http://profile.jpg");
        
        Image backgroundImage = new Image();
        backgroundImage.setImageType(ImageType.BACKGROUND_PICTURE);
        backgroundImage.setImageUrl("http://background.jpg");
        
        user.getImages().add(profileImage);
        user.getImages().add(backgroundImage);

        when(jwtService.extractId("testtoken123")).thenReturn(1);
        when(userRepository.findById(1)).thenReturn(Optional.of(user));

        MyProfileDto result = userService.getMyProfile(token);

        assertEquals(1, result.getId());
        assertEquals("testuser", result.getUsername());
        assertEquals("Test description", result.getDescription());
        assertEquals("http://profile.jpg", result.getPhotoUrl());
        assertEquals("http://background.jpg", result.getBackgroundUrl());
    }

    @Test
    void testGetFavouriteRecipes_Success() {
        Recipe recipe = new Recipe();
        recipe.setId(1);
        recipe.setRecipeName("Test Recipe");
        recipe.setCategory("Breakfast");
        recipe.setServings(2);
        recipe.setDifficulty(1);
        recipe.setPrepareTime(15);
        recipe.setLanguage("en");
        recipe.setImages(new ArrayList<>());
        recipe.setIngredients(new ArrayList<>());
        recipe.setRatings(new ArrayList<>());
        
        user.getFavouriteRecipes().add(recipe);

        when(jwtService.extractId("testtoken123")).thenReturn(1);
        when(userRepository.findById(1)).thenReturn(Optional.of(user));

        MyProfileDto result = userService.getFavouriteRecipes(token);

        assertNotNull(result.getFavourites());
        assertEquals(1, result.getFavourites().size());
        assertEquals("Test Recipe", result.getFavourites().get(0).getRecipeName());
    }

    @Test
    void testGetUserRecipes_Success() {
        Recipe recipe = new Recipe();
        recipe.setId(1);
        recipe.setRecipeName("User Recipe");
        recipe.setCategory("Lunch");
        recipe.setServings(4);
        recipe.setDifficulty(2);
        recipe.setPrepareTime(30);
        recipe.setLanguage("en");
        recipe.setImages(new ArrayList<>());
        recipe.setIngredients(new ArrayList<>());
        
        Rating rating = new Rating();
        rating.setValue(5);
        List<Rating> ratings = List.of(rating);
        recipe.setRatings(ratings);
        
        user.getUserRecipes().add(recipe);

        when(jwtService.extractId("testtoken123")).thenReturn(1);
        when(userRepository.findById(1)).thenReturn(Optional.of(user));

        MyProfileDto result = userService.getUserRecipes(token);

        assertNotNull(result.getUserRecipes());
        assertEquals(1, result.getUserRecipes().size());
        MyProfileRecipeDto recipeDto = result.getUserRecipes().get(0);
        assertEquals("User Recipe", recipeDto.getRecipeName());
        assertEquals(5L, recipeDto.getAvgRating());
    }

    @Test
    void testUpdateUser_Success() {
        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(userRepository.findByEmail("new@example.com")).thenReturn(Optional.empty());
        when(userRepository.save(user)).thenReturn(user);

        UserEntity result = userService.updateUser(1, "ADMIN", "new@example.com", "newusername");

        assertEquals("ADMIN", result.getRole());
        assertEquals("new@example.com", result.getEmail());
        assertEquals("newusername", result.getUsername());
        verify(userRepository).save(user);
    }

    @Test
    void testUpdateUser_EmailAlreadyExists() {
        UserEntity anotherUser = new UserEntity();
        anotherUser.setId(2);
        
        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(userRepository.findByEmail("existing@example.com")).thenReturn(Optional.of(anotherUser));

        assertThrows(UserEmailAlreadyExistException.class, 
            () -> userService.updateUser(1, "USER", "existing@example.com", "username"));
    }

    @Test
    void testGetProfilePicture() {
        Image profileImage = new Image();
        profileImage.setImageType(ImageType.PROFILE_PICTURE);
        profileImage.setImageUrl("http://profile.jpg");
        
        Image otherImage = new Image();
        otherImage.setImageType(ImageType.BACKGROUND_PICTURE);
        otherImage.setImageUrl("http://background.jpg");
        
        user.getImages().add(profileImage);
        user.getImages().add(otherImage);

        String result = userService.getProfilePicture(user);

        assertEquals("http://profile.jpg", result);
    }

    @Test
    void testGetProfilePicture_NoImage() {
        String result = userService.getProfilePicture(user);

        assertNull(result);
    }

    @Test
    void testGetBackgroundPicture() {
        Image backgroundImage = new Image();
        backgroundImage.setImageType(ImageType.BACKGROUND_PICTURE);
        backgroundImage.setImageUrl("http://background.jpg");
        
        user.getImages().add(backgroundImage);

        String result = userService.getBackgroundPicture(user);

        assertEquals("http://background.jpg", result);
    }
}