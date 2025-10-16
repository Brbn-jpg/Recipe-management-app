package com.kk.cibaria.recipe;

import com.kk.cibaria.ingredient.Ingredient;
import com.kk.cibaria.rating.Rating;
import com.kk.cibaria.step.Step;
import com.kk.cibaria.user.UserEntity;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RecipeTest {

    @Test
    void testNewRecipe() {
        // new recipe should have default values
        Recipe recipe = new Recipe();
        assertNotNull(recipe);
        assertNull(recipe.getRecipeName());
        assertEquals(0, recipe.getId());
        assertEquals(0, recipe.getDifficulty());
        assertEquals(0, recipe.getPrepareTime());
        assertEquals(0, recipe.getServings());
        assertNull(recipe.getCategory());
        assertFalse(recipe.getIsPublic());
        assertNull(recipe.getLanguage());
    }

    @Test
    void testRecipeAllArgsConstructor() {
        List<Ingredient> ingredients = new ArrayList<>();
        List<Rating> ratings = new ArrayList<>();
        UserEntity user = new UserEntity();
        List<Step> steps = new ArrayList<>();

        Recipe recipe = new Recipe(1, "Test Recipe", 3, ingredients, 30, 4, 
                                 "Main Course", true, "en", ratings, 
                                 new ArrayList<>(), user, new ArrayList<>(), steps);

        assertEquals(1, recipe.getId());
        assertEquals("Test Recipe", recipe.getRecipeName());
        assertEquals(3, recipe.getDifficulty());
        assertEquals(30, recipe.getPrepareTime());
        assertEquals(4, recipe.getServings());
        assertEquals("Main Course", recipe.getCategory());
        assertTrue(recipe.getIsPublic());
        assertEquals("en", recipe.getLanguage());
        assertNotNull(recipe.getIngredients());
        assertNotNull(recipe.getRatings());
        assertNotNull(recipe.getSteps());
        assertEquals(user, recipe.getUser());
    }

    @Test
    void testRecipeSettersAndGetters() {
        Recipe recipe = new Recipe();
        UserEntity user = new UserEntity();
        List<Ingredient> ingredients = new ArrayList<>();
        List<Rating> ratings = new ArrayList<>();
        List<Step> steps = new ArrayList<>();

        recipe.setId(1);
        recipe.setRecipeName("Pasta Recipe");
        recipe.setDifficulty(2);
        recipe.setPrepareTime(25);
        recipe.setServings(2);
        recipe.setCategory("Italian");
        recipe.setIsPublic(true);
        recipe.setLanguage("pl");
        recipe.setUser(user);
        recipe.setIngredients(ingredients);
        recipe.setRatings(ratings);
        recipe.setSteps(steps);

        assertEquals(1, recipe.getId());
        assertEquals("Pasta Recipe", recipe.getRecipeName());
        assertEquals(2, recipe.getDifficulty());
        assertEquals(25, recipe.getPrepareTime());
        assertEquals(2, recipe.getServings());
        assertEquals("Italian", recipe.getCategory());
        assertTrue(recipe.getIsPublic());
        assertEquals("pl", recipe.getLanguage());
        assertEquals(user, recipe.getUser());
        assertEquals(ingredients, recipe.getIngredients());
        assertEquals(ratings, recipe.getRatings());
        assertEquals(steps, recipe.getSteps());
    }

    @Test
    void testRecipeDefaultValues() {
        Recipe recipe = new Recipe();
        
        assertFalse(recipe.getIsPublic());
        assertNotNull(recipe.getRatings());
        assertTrue(recipe.getRatings().isEmpty());
        assertNotNull(recipe.getFavouriteByUsers());
        assertTrue(recipe.getFavouriteByUsers().isEmpty());
        assertNotNull(recipe.getImages());
        assertTrue(recipe.getImages().isEmpty());
    }

    @Test
    void testRecipeRelationships() {
        Recipe recipe = new Recipe();
        UserEntity user = new UserEntity();
        Ingredient ingredient = new Ingredient();
        Rating rating = new Rating();
        Step step = new Step();

        recipe.setUser(user);
        recipe.setIngredients(List.of(ingredient));
        recipe.setRatings(List.of(rating));
        recipe.setSteps(List.of(step));

        assertEquals(user, recipe.getUser());
        assertTrue(recipe.getIngredients().contains(ingredient));
        assertTrue(recipe.getRatings().contains(rating));
        assertTrue(recipe.getSteps().contains(step));
    }

    @Test
    void testRecipeEqualsAndHashCode() {
        Recipe recipe1 = new Recipe();
        recipe1.setId(1);
        recipe1.setRecipeName("Test Recipe");

        Recipe recipe2 = new Recipe();
        recipe2.setId(1);
        recipe2.setRecipeName("Test Recipe");

        assertEquals(recipe1, recipe2);
        assertEquals(recipe1.hashCode(), recipe2.hashCode());
    }

    @Test
    void testRecipeToString() {
        Recipe recipe = new Recipe();
        recipe.setRecipeName("Test Recipe");
        recipe.setDifficulty(2);

        String recipeString = recipe.toString();
        assertNotNull(recipeString);
        assertTrue(recipeString.contains("Test Recipe"));
        assertTrue(recipeString.contains("difficulty=2"));
    }
}