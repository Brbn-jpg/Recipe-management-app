package com.kk.cibaria.recipe;

import com.kk.cibaria.user.UserEntity;
import com.kk.cibaria.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class RecipeRepositoryIntegrationTest {

    @Autowired
    private RecipeRepository recipeRepository;

    @Autowired
    private UserRepository userRepository;

    private UserEntity testUser;
    private Recipe testRecipe1;
    private Recipe testRecipe2;

    @BeforeEach
    void setup() {
        testUser = new UserEntity();
        testUser.setUsername("testuser");
        testUser.setEmail("test@test.com");
        testUser.setPassword("password");
        testUser = userRepository.save(testUser);

        testRecipe1 = new Recipe();
        testRecipe1.setRecipeName("Chocolate Cake");
        testRecipe1.setDifficulty(3);
        testRecipe1.setPrepareTime(60);
        testRecipe1.setServings(8);
        testRecipe1.setCategory("Dessert");
        testRecipe1.setIsPublic(true);
        testRecipe1.setLanguage("en");
        testRecipe1.setUser(testUser);

        testRecipe2 = new Recipe();
        testRecipe2.setRecipeName("Vanilla Cake");
        testRecipe2.setDifficulty(2);
        testRecipe2.setPrepareTime(45);
        testRecipe2.setServings(6);
        testRecipe2.setCategory("Dessert");
        testRecipe2.setIsPublic(false);
        testRecipe2.setLanguage("en");
        testRecipe2.setUser(testUser);

        recipeRepository.save(testRecipe1);
        recipeRepository.save(testRecipe2);
    }

    @Test
    void testFindAll() {
        List<Recipe> recipes = recipeRepository.findAll();
        
        assertTrue(recipes.size() >= 2);
        assertTrue(recipes.stream().anyMatch(r -> r.getRecipeName().equals("Chocolate Cake")));
        assertTrue(recipes.stream().anyMatch(r -> r.getRecipeName().equals("Vanilla Cake")));
    }

    @Test
    void testFindById() {
        Recipe savedRecipe = recipeRepository.save(testRecipe1);
        Optional<Recipe> foundRecipe = recipeRepository.findById(savedRecipe.getId());
        
        assertTrue(foundRecipe.isPresent());
        assertEquals("Chocolate Cake", foundRecipe.get().getRecipeName());
        assertEquals(testUser.getId(), foundRecipe.get().getUser().getId());
    }

    @Test
    void testFindByRecipeNameQuery_CaseInsensitive() {
        List<Recipe> results = recipeRepository.findByRecipeNameQuery("chocolate");
        
        assertEquals(1, results.size());
        assertEquals("Chocolate Cake", results.get(0).getRecipeName());
    }

    @Test
    void testFindByRecipeNameQuery_PartialMatch() {
        List<Recipe> results = recipeRepository.findByRecipeNameQuery("cake");
        
        assertEquals(2, results.size());
        assertTrue(results.stream().anyMatch(r -> r.getRecipeName().equals("Chocolate Cake")));
        assertTrue(results.stream().anyMatch(r -> r.getRecipeName().equals("Vanilla Cake")));
    }

    @Test
    void testFindByRecipeNameQuery_NoResults() {
        List<Recipe> results = recipeRepository.findByRecipeNameQuery("nonexistent");
        
        assertTrue(results.isEmpty());
    }

    @Test
    void testFindByUser() {
        List<Recipe> userRecipes = recipeRepository.findByUser(testUser);
        
        assertEquals(2, userRecipes.size());
        assertTrue(userRecipes.stream().allMatch(r -> r.getUser().getId() == testUser.getId()));
    }

    @Test
    void testFindByUser_NoRecipes() {
        UserEntity anotherUser = new UserEntity();
        anotherUser.setUsername("anotheruser");
        anotherUser.setEmail("another@example.com");
        anotherUser.setPassword("password");
        anotherUser = userRepository.save(anotherUser);

        List<Recipe> userRecipes = recipeRepository.findByUser(anotherUser);
        
        assertTrue(userRecipes.isEmpty());
    }

    @Test
    void testSaveRecipe() {
        Recipe newRecipe = new Recipe();
        newRecipe.setRecipeName("Apple Pie");
        newRecipe.setDifficulty(4);
        newRecipe.setPrepareTime(90);
        newRecipe.setServings(8);
        newRecipe.setCategory("Dessert");
        newRecipe.setIsPublic(true);
        newRecipe.setLanguage("en");
        newRecipe.setUser(testUser);

        Recipe savedRecipe = recipeRepository.save(newRecipe);
        
        assertNotNull(savedRecipe.getId());
        assertTrue(savedRecipe.getId() > 0);
        assertEquals("Apple Pie", savedRecipe.getRecipeName());
        assertEquals(testUser.getId(), savedRecipe.getUser().getId());
    }

    @Test
    void testUpdateRecipe() {
        Recipe savedRecipe = recipeRepository.save(testRecipe1);
        savedRecipe.setRecipeName("Updated Chocolate Cake");
        savedRecipe.setDifficulty(4);
        
        Recipe updatedRecipe = recipeRepository.save(savedRecipe);
        
        assertEquals(savedRecipe.getId(), updatedRecipe.getId());
        assertEquals("Updated Chocolate Cake", updatedRecipe.getRecipeName());
        assertEquals(4, updatedRecipe.getDifficulty());
    }

    @Test
    void testDeleteRecipe() {
        Recipe savedRecipe = recipeRepository.save(testRecipe1);
        Integer recipeId = savedRecipe.getId();
        
        recipeRepository.delete(savedRecipe);
        
        Optional<Recipe> deletedRecipe = recipeRepository.findById(recipeId);
        assertFalse(deletedRecipe.isPresent());
    }

    @Test
    void testRecipeWithRelationships() {
        Recipe savedRecipe = recipeRepository.save(testRecipe1);
        Optional<Recipe> foundRecipe = recipeRepository.findById(savedRecipe.getId());
        
        assertTrue(foundRecipe.isPresent());
        Recipe recipe = foundRecipe.get();
        
        assertNotNull(recipe.getUser());
        assertEquals(testUser.getId(), (Integer) recipe.getUser().getId());
    }
}