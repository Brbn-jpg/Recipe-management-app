package com.kk.cibaria.recipe;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kk.cibaria.dto.FavouriteRequest;
import com.kk.cibaria.dto.RecipeAddDto;
import com.kk.cibaria.dto.RecipeRequestDto;
import com.kk.cibaria.image.ImageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecipeControllerTest {

    @Mock
    private RecipeService recipeService;

    @Mock
    private ImageService imageService;

    @InjectMocks
    private RecipeController recipeController;

    private Recipe testRecipe;
    private RecipeAddDto testRecipeAddDto;
    private RecipeRequestDto testRecipeRequestDto;
    private String testToken;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        testRecipe = new Recipe();
        testRecipe.setId(1);
        testRecipe.setRecipeName("Test Recipe");
        testRecipe.setDifficulty(3);
        testRecipe.setPrepareTime(30);
        testRecipe.setServings(4);
        testRecipe.setCategory("Dinner");
        testRecipe.setIsPublic(true);
        testRecipe.setLanguage("en");

        testRecipeAddDto = new RecipeAddDto();
        testRecipeAddDto.setRecipeName("Test Recipe");
        testRecipeAddDto.setDifficulty(3);
        testRecipeAddDto.setPrepareTime(30);
        testRecipeAddDto.setServings(4);
        testRecipeAddDto.setCategory("Dinner");
        testRecipeAddDto.setIsPublic(true);
        testRecipeAddDto.setLanguage("en");
        testRecipeAddDto.setIngredients(new ArrayList<>());
        testRecipeAddDto.setSteps(new ArrayList<>());

        testRecipeRequestDto = new RecipeRequestDto();
        testRecipeRequestDto.setContent(List.of(testRecipe));
        testRecipeRequestDto.setTotalPages(1);

        testToken = "Bearer testtoken123";
        objectMapper = new ObjectMapper();
    }

    @Test
    void testGetRecipesByPage() {
        when(recipeService.getRecipeByPage(1, 10, null, null, null, null, true, null, null))
            .thenReturn(testRecipeRequestDto);

        RecipeRequestDto result = recipeController.getRecipesByPage(1, 10, null, null, 
            null, null, true, null, null);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(testRecipe, result.getContent().get(0));
        assertEquals(1, result.getTotalPages());
        verify(recipeService).getRecipeByPage(1, 10, null, null, null, null, true, null, null);
    }

    @Test
    void testGetRecipesByPage_WithFilters() {
        List<String> categories = List.of("Breakfast");
        List<String> ingredients = List.of("tomato");
        
        when(recipeService.getRecipeByPage(1, 5, categories, 2, "4", "30", false, "en", ingredients))
            .thenReturn(testRecipeRequestDto);

        RecipeRequestDto result = recipeController.getRecipesByPage(1, 5, categories, 2, 
            "4", "30", false, "en", ingredients);

        assertNotNull(result);
        verify(recipeService).getRecipeByPage(1, 5, categories, 2, "4", "30", false, "en", ingredients);
    }

    @Test
    void testGetById() {
        when(recipeService.getById(1)).thenReturn(testRecipe);

        Recipe result = recipeController.getById(1);

        assertEquals(testRecipe, result);
        verify(recipeService).getById(1);
    }

    @Test
    void testSave_WithoutImages() throws IOException {
        String recipeJson = objectMapper.writeValueAsString(testRecipeAddDto);
        
        when(recipeService.saveRecipeWithoutPhoto(any(RecipeAddDto.class), eq(testToken)))
            .thenReturn(testRecipe);

        Recipe result = recipeController.save(recipeJson, testToken, Optional.empty());

        assertEquals(testRecipe, result);
        verify(recipeService).saveRecipeWithoutPhoto(any(RecipeAddDto.class), eq(testToken));
        verify(recipeService, never()).saveRecipeWithPhotos(any(), any(), any());
    }

    @Test
    void testSave_WithImages() throws IOException {
        String recipeJson = objectMapper.writeValueAsString(testRecipeAddDto);
        List<MultipartFile> images = List.of(new MockMultipartFile("image", "test.jpg", 
            "image/jpeg", "test data".getBytes()));
        
        when(recipeService.saveRecipeWithPhotos(any(RecipeAddDto.class), eq(images), eq(testToken)))
            .thenReturn(testRecipe);

        Recipe result = recipeController.save(recipeJson, testToken, Optional.of(images));

        assertEquals(testRecipe, result);
        verify(recipeService).saveRecipeWithPhotos(any(RecipeAddDto.class), eq(images), eq(testToken));
        verify(recipeService, never()).saveRecipeWithoutPhoto(any(), any());
    }

    @Test
    void testUpdate_WithImages() throws IOException {
        String recipeJson = objectMapper.writeValueAsString(testRecipe);
        List<MultipartFile> images = List.of(new MockMultipartFile("image", "test.jpg", 
            "image/jpeg", "test data".getBytes()));
        
        when(recipeService.updateRecipeWithPhotos(eq(1), any(Recipe.class), eq(images), eq(testToken)))
            .thenReturn(testRecipe);

        Recipe result = recipeController.update(1, recipeJson, testToken, images, null);

        assertEquals(testRecipe, result);
        verify(recipeService).updateRecipeWithPhotos(eq(1), any(Recipe.class), eq(images), eq(testToken));
        verify(recipeService, never()).updateRecipeWithoutPhotos(anyInt(), any(), any(), anyBoolean());
    }

    @Test
    void testUpdate_WithoutImages_KeepExisting() throws IOException {
        String recipeJson = objectMapper.writeValueAsString(testRecipe);
        
        when(recipeService.updateRecipeWithoutPhotos(eq(1), any(Recipe.class), eq(testToken), eq(true)))
            .thenReturn(testRecipe);

        Recipe result = recipeController.update(1, recipeJson, testToken, null, "true");

        assertEquals(testRecipe, result);
        verify(recipeService).updateRecipeWithoutPhotos(eq(1), any(Recipe.class), eq(testToken), eq(true));
        verify(recipeService, never()).updateRecipeWithPhotos(anyInt(), any(), any(), any());
    }

    @Test
    void testUpdate_WithoutImages_DontKeepExisting() throws IOException {
        String recipeJson = objectMapper.writeValueAsString(testRecipe);
        
        when(recipeService.updateRecipeWithoutPhotos(eq(1), any(Recipe.class), eq(testToken), eq(false)))
            .thenReturn(testRecipe);

        Recipe result = recipeController.update(1, recipeJson, testToken, null, "false");

        assertEquals(testRecipe, result);
        verify(recipeService).updateRecipeWithoutPhotos(eq(1), any(Recipe.class), eq(testToken), eq(false));
    }

    @Test
    void testDelete() {
        doNothing().when(recipeService).delete(testToken, 1);

        assertDoesNotThrow(() -> recipeController.delete(1, testToken));
        verify(recipeService).delete(testToken, 1);
    }

    @Test
    void testRecipeRating() {
        when(recipeService.rating(1, testToken, 4)).thenReturn(testRecipe);

        assertDoesNotThrow(() -> recipeController.recipeRating(1, testToken, 4));
        verify(recipeService).rating(1, testToken, 4);
    }

    @Test
    void testGetUserRating_Success() {
        when(recipeService.getUserRating(1, testToken)).thenReturn(4);

        ResponseEntity<Integer> result = recipeController.getUserRating(1, testToken);

        assertEquals(200, result.getStatusCode().value());
        assertEquals(4, result.getBody());
        verify(recipeService).getUserRating(1, testToken);
    }

    @Test
    void testGetUserRating_Exception() {
        when(recipeService.getUserRating(1, testToken)).thenThrow(new RuntimeException("Test error"));

        ResponseEntity<Integer> result = recipeController.getUserRating(1, testToken);

        assertEquals(200, result.getStatusCode().value());
        assertEquals(0, result.getBody());
        verify(recipeService).getUserRating(1, testToken);
    }

    @Test
    void testIsRecipeFavourite() {
        when(recipeService.isRecipeFavourite(testToken, 1)).thenReturn(true);

        boolean result = recipeController.isRecipeFavourite(testToken, 1);

        assertTrue(result);
        verify(recipeService).isRecipeFavourite(testToken, 1);
    }

    @Test
    void testIsOwner() {
        when(recipeService.isOwner(1, testToken)).thenReturn(true);

        boolean result = recipeController.isOwner(1, testToken);

        assertTrue(result);
        verify(recipeService).isOwner(1, testToken);
    }

    @Test
    void testAddRecipeToFavourites() {
        FavouriteRequest request = new FavouriteRequest();
        request.setRecipeId(1);
        
        doNothing().when(recipeService).addRecipeToFavourites(testToken, 1);

        assertDoesNotThrow(() -> recipeController.addRecipeToFavourites(testToken, request));
        verify(recipeService).addRecipeToFavourites(testToken, 1);
    }

    @Test
    void testDeleteRecipeFromFavourites() {
        FavouriteRequest request = new FavouriteRequest();
        request.setRecipeId(1);
        
        doNothing().when(recipeService).deleteRiceFromFavourites(testToken, 1);

        assertDoesNotThrow(() -> recipeController.deleteRecipeFromFavourites(testToken, request));
        verify(recipeService).deleteRiceFromFavourites(testToken, 1);
    }

    @Test
    void testSearchRecipes() {
        List<Recipe> recipes = List.of(testRecipe);
        when(recipeService.searchRecipes("test")).thenReturn(recipes);

        List<Recipe> result = recipeController.search("test");

        assertEquals(1, result.size());
        assertEquals(testRecipe, result.get(0));
        verify(recipeService).searchRecipes("test");
    }
}