package com.kk.cibaria.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kk.cibaria.dto.admin.UpdateUserDto;
import com.kk.cibaria.recipe.Recipe;
import com.kk.cibaria.recipe.RecipeService;
import com.kk.cibaria.user.UserEntity;
import com.kk.cibaria.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

// AdminController unit tests
@ExtendWith(MockitoExtension.class)
class AdminControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private RecipeService recipeService;

    @InjectMocks
    private AdminController adminController;

    private UserEntity adminUser;
    private UserEntity regularUser;
    private Recipe publicRecipe;
    private Recipe privateRecipe;
    private UpdateUserDto updateUserDto;

    @BeforeEach
    void setup() {
        adminUser = new UserEntity();
        adminUser.setId(1);
        adminUser.setUsername("admin");
        adminUser.setEmail("admin@test.com");
        adminUser.setRole("ADMIN");

        regularUser = new UserEntity();
        regularUser.setId(2);
        regularUser.setUsername("user");
        regularUser.setEmail("user@test.com");
        regularUser.setRole("USER");

        publicRecipe = new Recipe();
        publicRecipe.setId(1);
        publicRecipe.setRecipeName("Public Recipe");
        publicRecipe.setIsPublic(true);

        privateRecipe = new Recipe();
        privateRecipe.setId(2);
        privateRecipe.setRecipeName("Private Recipe");
        privateRecipe.setIsPublic(false);

        updateUserDto = new UpdateUserDto();
        updateUserDto.setRole("USER");
        updateUserDto.setEmail("updated@test.com");
        updateUserDto.setUsername("nupdatedUser");
    }

    @Test
    void getAllUsers_ShouldReturnAllUsers() throws Exception {
        List<UserEntity> users = Arrays.asList(adminUser, regularUser);
        when(userService.getAll()).thenReturn(users);

        List<UserEntity> result = adminController.getAllUsers();
        
        assertEquals(2, result.size());
        assertEquals("admin", result.get(0).getUsername());
        assertEquals("user", result.get(1).getUsername());
        verify(userService).getAll();
    }

    @Test
    void deleteUser_ShouldDeleteUser() throws Exception {
        doNothing().when(userService).delete(1);

        adminController.deleteUser(1);

        verify(userService).delete(1);
    }

    @Test
    void getAllRecipes_ShouldReturnAllRecipes() throws Exception {
        List<Recipe> recipes = Arrays.asList(publicRecipe, privateRecipe);
        when(recipeService.getAll()).thenReturn(recipes);

        List<Recipe> result = adminController.getAllRecipes();
        
        assertEquals(2, result.size());
        verify(recipeService).getAll();
    }

    @Test
    void getStats_ShouldReturnStats() throws Exception {
        List<UserEntity> users = Arrays.asList(adminUser, regularUser);
        List<Recipe> recipes = Arrays.asList(publicRecipe, privateRecipe);
        
        when(userService.getAll()).thenReturn(users);
        when(recipeService.getAll()).thenReturn(recipes);

        var stats = adminController.getStats();
        
        assertEquals(2, stats.getTotalUsers());
        assertEquals(2, stats.getTotalRecipes());
        verify(userService).getAll();
        verify(recipeService).getAll();
    }

    @Test
    void updateUserRole_ShouldUpdateUserRole() {
        UpdateUserDto updateDto = new UpdateUserDto();
        updateDto.setRole("USER");
        updateDto.setEmail("updated@test.com");
        updateDto.setUsername("updatedUser");
        
        UserEntity updatedUser = new UserEntity();
        updatedUser.setRole("USER");
        updatedUser.setEmail("updated@test.com");
        updatedUser.setUsername("updatedUser");
        
        when(userService.updateUser(1, "USER", "updated@test.com", "updatedUser"))
                .thenReturn(updatedUser);
        
        ResponseEntity<UserEntity> result = adminController.updateUserRole(1, updateDto);
        
        UserEntity resultUser = result.getBody();
        assertNotNull(resultUser);
        assertEquals("USER", resultUser.getRole());
        assertEquals("updated@test.com", resultUser.getEmail());
        assertEquals("updatedUser", resultUser.getUsername());
        verify(userService).updateUser(1, "USER", "updated@test.com", "updatedUser");
    }

    @Test
    void deleteRecipe_ShouldDeleteRecipe() {
        doNothing().when(recipeService).delete("Bearer token", 1);
        
        ResponseEntity<Void> result = adminController.deleteRecipe(1, "Bearer token");
        
        assertNotNull(result);
        assertEquals(200, result.getStatusCode().value());
        verify(recipeService).delete("Bearer token", 1);
    }

    private Recipe createTestRecipe(int id) {
        Recipe recipe = new Recipe();
        recipe.setId(id);
        recipe.setRecipeName("Updated Recipe");
        recipe.setDifficulty(3);
        recipe.setIsPublic(true);
        recipe.setLanguage("en");
        return recipe;
    }

    private String serializeRecipe(Recipe recipe) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writeValueAsString(recipe);
    }

    private void assertRecipeEquals(Recipe expected, Recipe actual) {
        assertNotNull(actual);
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getRecipeName(), actual.getRecipeName());
        assertEquals(expected.getDifficulty(), actual.getDifficulty());
        assertEquals(expected.getIsPublic(), actual.getIsPublic());
        assertEquals(expected.getLanguage(), actual.getLanguage());
    }

    @Test
    void updateRecipe_WithoutImages_KeepExisting() throws Exception {
        int recipeId = 1;
        String authToken = "Bearer token";
        Recipe testRecipe = createTestRecipe(0);
        Recipe resultRecipe = createTestRecipe(recipeId);
        String recipeJson = serializeRecipe(testRecipe);
        
        when(recipeService.updateRecipeWithoutPhotos(eq(recipeId), any(Recipe.class), eq(authToken), eq(true)))
                .thenReturn(resultRecipe);
        
        Recipe result = adminController.updateRecipe(recipeId, recipeJson, authToken, null, "true");
        
        assertRecipeEquals(resultRecipe, result);
        verify(recipeService).updateRecipeWithoutPhotos(eq(recipeId), any(Recipe.class), eq(authToken), eq(true));
        verify(recipeService, never()).updateRecipeWithPhotos(anyInt(), any(Recipe.class), any(), anyString());
    }

    @Test
    void updateRecipe_WithImages() throws Exception {
        int recipeId = 1;
        String authToken = "Bearer token";
        Recipe testRecipe = createTestRecipe(0);
        Recipe resultRecipe = createTestRecipe(recipeId);
        String recipeJson = serializeRecipe(testRecipe);
        
        MockMultipartFile image1 = new MockMultipartFile("image1", "test1.jpg", "image/jpeg", "content1".getBytes());
        MockMultipartFile image2 = new MockMultipartFile("image2", "test2.jpg", "image/jpeg", "content2".getBytes());
        List<MultipartFile> images = Arrays.asList(image1, image2);
        
        when(recipeService.updateRecipeWithPhotos(eq(recipeId), any(Recipe.class), eq(images), eq(authToken)))
                .thenReturn(resultRecipe);
        
        Recipe result = adminController.updateRecipe(recipeId, recipeJson, authToken, images, null);
        
        assertRecipeEquals(resultRecipe, result);
        verify(recipeService).updateRecipeWithPhotos(eq(recipeId), any(Recipe.class), eq(images), eq(authToken));
        verify(recipeService, never()).updateRecipeWithoutPhotos(anyInt(), any(Recipe.class), anyString(), anyBoolean());
    }

    @Test
    void updateRecipe_WithoutImages_DontKeepExisting() throws Exception {
        int recipeId = 1;
        String authToken = "Bearer token";
        Recipe testRecipe = createTestRecipe(0);
        Recipe resultRecipe = createTestRecipe(recipeId);
        String recipeJson = serializeRecipe(testRecipe);
        
        when(recipeService.updateRecipeWithoutPhotos(eq(recipeId), any(Recipe.class), eq(authToken), eq(false)))
                .thenReturn(resultRecipe);
        
        Recipe result = adminController.updateRecipe(recipeId, recipeJson, authToken, null, "false");
        
        assertRecipeEquals(resultRecipe, result);
        verify(recipeService).updateRecipeWithoutPhotos(eq(recipeId), any(Recipe.class), eq(authToken), eq(false));
    }

    @Test
    void getStats_ShouldReturnDetailedStats() {
        List<UserEntity> users = Arrays.asList(adminUser, regularUser);
        List<Recipe> recipes = Arrays.asList(publicRecipe, privateRecipe);
        
        when(userService.getAll()).thenReturn(users);
        when(recipeService.getAll()).thenReturn(recipes);
        
        var stats = adminController.getStats();
        
        assertEquals(2, stats.getTotalUsers());
        assertEquals(1, stats.getAdminUsers());
        assertEquals(1, stats.getRegularUsers());
        assertEquals(2, stats.getTotalRecipes());
        assertEquals(1, stats.getPublicRecipes());
        assertEquals(1, stats.getPrivateRecipes());
        verify(userService).getAll();
        verify(recipeService).getAll();
    }

    @Test
    void getStats_ShouldHandleEmptyData() {
        when(userService.getAll()).thenReturn(Arrays.asList());
        when(recipeService.getAll()).thenReturn(Arrays.asList());
        
        var stats = adminController.getStats();
        
        assertEquals(0, stats.getTotalUsers());
        assertEquals(0, stats.getAdminUsers());
        assertEquals(0, stats.getRegularUsers());
        assertEquals(0, stats.getTotalRecipes());
        assertEquals(0, stats.getPublicRecipes());
        assertEquals(0, stats.getPrivateRecipes());
    }

    @Test
    void deleteUser_ShouldReturnOkResponse() {
        doNothing().when(userService).delete(1);
        
        ResponseEntity<Void> result = adminController.deleteUser(1);
        
        assertNotNull(result);
        assertEquals(200, result.getStatusCode().value());
        verify(userService).delete(1);
    }

    @Test
    void updateUserRole_ShouldHandleNullValues() {
        UpdateUserDto partialDto = new UpdateUserDto();
        partialDto.setRole("USER");
        // email and username are null
        
        UserEntity updatedUser = new UserEntity();
        updatedUser.setRole("USER");
        
        when(userService.updateUser(1, "USER", null, null))
                .thenReturn(updatedUser);
        
        ResponseEntity<UserEntity> result = adminController.updateUserRole(1, partialDto);
        
        UserEntity resultUser = result.getBody();
        assertNotNull(resultUser);
        assertEquals("USER", resultUser.getRole());
        verify(userService).updateUser(1, "USER", null, null);
    }
}