package com.kk.cibaria.recipe;

import com.kk.cibaria.cloudinary.CloudinaryService;
import com.kk.cibaria.dto.RecipeAddDto;
import com.kk.cibaria.dto.RecipeRequestDto;
import com.kk.cibaria.exception.*;
import com.kk.cibaria.image.Image;
import com.kk.cibaria.image.ImageService;
import com.kk.cibaria.image.ImageType;
import com.kk.cibaria.ingredient.IngredientService;
import com.kk.cibaria.rating.Rating;
import com.kk.cibaria.rating.RatingRepository;
import com.kk.cibaria.security.jwt.JwtService;
import com.kk.cibaria.user.UserEntity;
import com.kk.cibaria.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecipeServiceImplTest {

    @Mock
    private RecipeRepository recipeRepository;
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private JwtService jwtService;
    
    @Mock
    private ImageService imageService;
    
    @Mock
    private CloudinaryService cloudinaryService;
    
    @Mock
    private RatingRepository ratingRepository;
    
    @Mock
    private IngredientService ingredientService;

    @InjectMocks
    private RecipeServiceImpl recipeService;

    private UserEntity testUser;
    private Recipe testRecipe;
    private RecipeAddDto testRecipeAddDto;
    private String testToken;

    @BeforeEach
    void setup() {
        testUser = new UserEntity();
        testUser.setId(1);
        testUser.setUsername("testuser");

        testRecipe = new Recipe();
        testRecipe.setId(1);
        testRecipe.setRecipeName("Test Recipe");
        testRecipe.setDifficulty(3);
        testRecipe.setPrepareTime(30);
        testRecipe.setServings(4);
        testRecipe.setCategory("Main Course");
        testRecipe.setIsPublic(true);
        testRecipe.setLanguage("en");
        testRecipe.setUser(testUser);
        testRecipe.setIngredients(new ArrayList<>());
        testRecipe.setSteps(new ArrayList<>());

        testRecipeAddDto = new RecipeAddDto();
        testRecipeAddDto.setRecipeName("Test Recipe");
        testRecipeAddDto.setDifficulty(3);
        testRecipeAddDto.setPrepareTime(30);
        testRecipeAddDto.setServings(4);
        testRecipeAddDto.setCategory("Main Course");
        testRecipeAddDto.setIsPublic(true);
        testRecipeAddDto.setLanguage("en");
        testRecipeAddDto.setIngredients(new ArrayList<>());
        testRecipeAddDto.setSteps(new ArrayList<>());

        testToken = "Bearer testtoken123";
    }

    @Test
    void testGetAll() {
        List<Recipe> recipes = List.of(testRecipe);
        when(recipeRepository.findAll()).thenReturn(recipes);

        List<Recipe> result = recipeService.getAll();

        assertEquals(1, result.size());
        assertEquals(testRecipe, result.get(0));
        verify(recipeRepository).findAll();
    }

    @Test
    void testGetById_Success() {
        when(recipeRepository.findById(1)).thenReturn(Optional.of(testRecipe));

        Recipe result = recipeService.getById(1);

        assertEquals(testRecipe, result);
        verify(recipeRepository).findById(1);
    }

    @Test
    void testGetById_NotFound() {
        when(recipeRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(RecipeNotFoundException.class, () -> recipeService.getById(1));
        verify(recipeRepository).findById(1);
    }

    @Test
    void testSaveRecipeWithoutPhoto_Success() throws IOException {
        when(jwtService.extractId("testtoken123")).thenReturn(1);
        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(recipeRepository.save(any(Recipe.class))).thenReturn(testRecipe);

        Recipe result = recipeService.saveRecipeWithoutPhoto(testRecipeAddDto, testToken);

        assertNotNull(result);
        verify(jwtService).extractId("testtoken123");
        verify(userRepository).findById(1);
        verify(recipeRepository).save(any(Recipe.class));
    }

    @Test
    void testSaveRecipeWithoutPhoto_UserNotFound() {
        when(jwtService.extractId("testtoken123")).thenReturn(1);
        when(userRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, 
            () -> recipeService.saveRecipeWithoutPhoto(testRecipeAddDto, testToken));
    }

    @Test
    void testSaveRecipeWithPhotos_Success() throws IOException {
        MultipartFile mockFile = mock(MultipartFile.class);
        List<MultipartFile> images = List.of(mockFile);
        Image mockImage = new Image();
        
        when(jwtService.extractId("testtoken123")).thenReturn(1);
        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(imageService.createPhoto(mockFile, ImageType.RECIPE)).thenReturn(mockImage);
        when(recipeRepository.save(any(Recipe.class))).thenReturn(testRecipe);

        Recipe result = recipeService.saveRecipeWithPhotos(testRecipeAddDto, images, testToken);

        assertNotNull(result);
        verify(imageService).createPhoto(mockFile, ImageType.RECIPE);
        verify(recipeRepository).save(any(Recipe.class));
    }

    @Test
    void testSaveRecipeWithPhotos_ImageError() throws IOException {
        MultipartFile mockFile = mock(MultipartFile.class);
        List<MultipartFile> images = List.of(mockFile);
        
        when(jwtService.extractId("testtoken123")).thenReturn(1);
        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(imageService.createPhoto(mockFile, ImageType.RECIPE)).thenThrow(new IOException("Image error"));

        assertThrows(ImageErrorException.class, 
            () -> recipeService.saveRecipeWithPhotos(testRecipeAddDto, images, testToken));
    }

    @Test
    void testUpdateRecipeWithoutPhotos_Success() {
        Recipe updatedRecipe = new Recipe();
        updatedRecipe.setRecipeName("Updated Recipe");
        updatedRecipe.setIngredients(new ArrayList<>());
        updatedRecipe.setSteps(new ArrayList<>());

        when(recipeRepository.findById(1)).thenReturn(Optional.of(testRecipe));
        when(jwtService.extractId("testtoken123")).thenReturn(1);
        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(recipeRepository.save(any(Recipe.class))).thenReturn(testRecipe);

        Recipe result = recipeService.updateRecipeWithoutPhotos(1, updatedRecipe, testToken, true);

        assertNotNull(result);
        verify(recipeRepository).findById(1);
        verify(recipeRepository).save(any(Recipe.class));
    }

    @Test
    void testUpdateRecipeWithoutPhotos_UnauthorizedException() {
        UserEntity differentUser = new UserEntity();
        differentUser.setId(2);
        
        when(recipeRepository.findById(1)).thenReturn(Optional.of(testRecipe));
        when(jwtService.extractId("testtoken123")).thenReturn(2);
        when(userRepository.findById(2)).thenReturn(Optional.of(differentUser));
        when(jwtService.hasRole("testtoken123", "ADMIN")).thenReturn(false);

        Recipe updatedRecipe = new Recipe();
        updatedRecipe.setIngredients(new ArrayList<>());
        updatedRecipe.setSteps(new ArrayList<>());

        assertThrows(UnauthorizedException.class, 
            () -> recipeService.updateRecipeWithoutPhotos(1, updatedRecipe, testToken, true));
    }

    @Test
    void testIsOwner_True() {
        when(jwtService.extractId("testtoken123")).thenReturn(1);
        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(recipeRepository.findById(1)).thenReturn(Optional.of(testRecipe));

        boolean result = recipeService.isOwner(1, testToken);

        assertTrue(result);
    }

    @Test
    void testIsOwner_False() {
        UserEntity differentUser = new UserEntity();
        differentUser.setId(2);
        
        when(jwtService.extractId("testtoken123")).thenReturn(2);
        when(userRepository.findById(2)).thenReturn(Optional.of(differentUser));
        when(recipeRepository.findById(1)).thenReturn(Optional.of(testRecipe));

        boolean result = recipeService.isOwner(1, testToken);

        assertFalse(result);
    }

    @Test
    void testGetRecipeByPage() {
        List<Recipe> recipes = List.of(testRecipe);
        when(recipeRepository.findAll()).thenReturn(recipes);

        RecipeRequestDto result = recipeService.getRecipeByPage(1, 10, null, null, 
            null, null, true, "en", null);

        assertNotNull(result);
        assertNotNull(result.getContent());
        verify(recipeRepository).findAll();
    }

    @Test
    void testAddRecipeToFavourites_Success() {
        testUser.setFavouriteRecipes(new ArrayList<>());
        
        when(jwtService.extractId("testtoken123")).thenReturn(1);
        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(recipeRepository.findById(1)).thenReturn(Optional.of(testRecipe));

        assertDoesNotThrow(() -> recipeService.addRecipeToFavourites(testToken, 1));
        verify(userRepository).save(testUser);
    }

    @Test
    void testAddRecipeToFavourites_AlreadyExists() {
        testUser.setFavouriteRecipes(List.of(testRecipe));
        
        when(jwtService.extractId("testtoken123")).thenReturn(1);
        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(recipeRepository.findById(1)).thenReturn(Optional.of(testRecipe));

        assertThrows(RecipeErrorException.class, 
            () -> recipeService.addRecipeToFavourites(testToken, 1));
    }

    @Test
    void testDeleteRiceFromFavourites_Success() {
        List<Recipe> favourites = new ArrayList<>(List.of(testRecipe));
        testUser.setFavouriteRecipes(favourites);
        
        when(jwtService.extractId("testtoken123")).thenReturn(1);
        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(recipeRepository.findById(1)).thenReturn(Optional.of(testRecipe));

        assertDoesNotThrow(() -> recipeService.deleteRiceFromFavourites(testToken, 1));
        verify(userRepository).save(testUser);
    }

    @Test
    void testDeleteRiceFromFavourites_NotInFavourites() {
        testUser.setFavouriteRecipes(new ArrayList<>());
        
        when(jwtService.extractId("testtoken123")).thenReturn(1);
        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(recipeRepository.findById(1)).thenReturn(Optional.of(testRecipe));

        assertThrows(RecipeErrorException.class, 
            () -> recipeService.deleteRiceFromFavourites(testToken, 1));
    }

    @Test
    void testDelete_Success() {
        testRecipe.setFavouriteByUsers(new ArrayList<>());
        testRecipe.setImages(new ArrayList<>());
        
        when(jwtService.extractId("testtoken123")).thenReturn(1);
        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(recipeRepository.findById(1)).thenReturn(Optional.of(testRecipe));

        assertDoesNotThrow(() -> recipeService.delete(testToken, 1));
        verify(recipeRepository).delete(testRecipe);
    }

    @Test
    void testDelete_UnauthorizedException() {
        UserEntity differentUser = new UserEntity();
        differentUser.setId(2);
        
        when(jwtService.extractId("testtoken123")).thenReturn(2);
        when(userRepository.findById(2)).thenReturn(Optional.of(differentUser));
        when(recipeRepository.findById(1)).thenReturn(Optional.of(testRecipe));
        when(jwtService.hasRole("testtoken123", "ADMIN")).thenReturn(false);

        assertThrows(UnauthorizedException.class, 
            () -> recipeService.delete(testToken, 1));
    }

    @Test
    void testSearchRecipes() {
        List<Recipe> recipes = List.of(testRecipe);
        when(recipeRepository.findByRecipeNameQuery("test")).thenReturn(recipes);

        List<Recipe> result = recipeService.searchRecipes("test");

        assertEquals(1, result.size());
        assertEquals(testRecipe, result.get(0));
        verify(recipeRepository).findByRecipeNameQuery("test");
    }

    @Test
    void testRating_Success() {
        when(jwtService.extractId("testtoken123")).thenReturn(1);
        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(recipeRepository.findById(1)).thenReturn(Optional.of(testRecipe));
        when(ratingRepository.findByRecipeIdAndUserId(1, 1)).thenReturn(Optional.empty());

        Recipe result = recipeService.rating(1, testToken, 4);

        assertEquals(testRecipe, result);
        verify(ratingRepository).save(any(Rating.class));
    }

    @Test
    void testRating_UpdateExisting() {
        Rating existingRating = new Rating();
        existingRating.setValue(3);
        
        when(jwtService.extractId("testtoken123")).thenReturn(1);
        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(recipeRepository.findById(1)).thenReturn(Optional.of(testRecipe));
        when(ratingRepository.findByRecipeIdAndUserId(1, 1)).thenReturn(Optional.of(existingRating));

        Recipe result = recipeService.rating(1, testToken, 5);

        assertEquals(testRecipe, result);
        assertEquals(5, existingRating.getValue());
        verify(ratingRepository).save(existingRating);
    }

    @Test
    void testRating_InvalidRating() {
        assertThrows(IllegalArgumentException.class, 
            () -> recipeService.rating(1, testToken, 6));
        assertThrows(IllegalArgumentException.class, 
            () -> recipeService.rating(1, testToken, 0));
    }

    @Test
    void testGetUserRating_WithRating() {
        Rating userRating = new Rating();
        userRating.setValue(4);
        
        when(jwtService.extractId("testtoken123")).thenReturn(1);
        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(ratingRepository.findByRecipeIdAndUserId(1, 1)).thenReturn(Optional.of(userRating));

        int result = recipeService.getUserRating(1, testToken);

        assertEquals(4, result);
    }

    @Test
    void testGetUserRating_NoRating() {
        when(jwtService.extractId("testtoken123")).thenReturn(1);
        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(ratingRepository.findByRecipeIdAndUserId(1, 1)).thenReturn(Optional.empty());

        int result = recipeService.getUserRating(1, testToken);

        assertEquals(0, result);
    }

    @Test
    void testIsRecipeFavourite_True() {
        testUser.setFavouriteRecipes(List.of(testRecipe));
        
        when(jwtService.extractId("testtoken123")).thenReturn(1);
        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(recipeRepository.findById(1)).thenReturn(Optional.of(testRecipe));

        boolean result = recipeService.isRecipeFavourite(testToken, 1);

        assertTrue(result);
    }

    @Test
    void testIsRecipeFavourite_False() {
        testUser.setFavouriteRecipes(new ArrayList<>());
        
        when(jwtService.extractId("testtoken123")).thenReturn(1);
        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(recipeRepository.findById(1)).thenReturn(Optional.of(testRecipe));

        boolean result = recipeService.isRecipeFavourite(testToken, 1);

        assertFalse(result);
    }
}