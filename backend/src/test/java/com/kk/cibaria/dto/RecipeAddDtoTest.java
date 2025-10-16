package com.kk.cibaria.dto;

import com.kk.cibaria.ingredient.Ingredient;
import com.kk.cibaria.step.Step;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RecipeAddDtoTest {

    @Test
    void testDefaultConstructor() {
        RecipeAddDto dto = new RecipeAddDto();
        
        assertNull(dto.getRecipeName());
        assertEquals(0, dto.getDifficulty());
        assertNull(dto.getIngredients());
        assertEquals(0, dto.getPrepareTime());
        assertEquals(0, dto.getServings());
        assertNull(dto.getCategory());
        assertNull(dto.getIsPublic());
        assertNull(dto.getSteps());
        assertNull(dto.getLanguage());
    }

    @Test
    void testSettersAndGetters() {
        RecipeAddDto dto = new RecipeAddDto();
        List<Ingredient> ingredients = new ArrayList<>();
        List<Step> steps = new ArrayList<>();
        
        dto.setRecipeName("Test Recipe");
        dto.setDifficulty(3);
        dto.setIngredients(ingredients);
        dto.setPrepareTime(60);
        dto.setServings(4);
        dto.setCategory("dessert");
        dto.setIsPublic(true);
        dto.setSteps(steps);
        dto.setLanguage("english");
        
        assertEquals("Test Recipe", dto.getRecipeName());
        assertEquals(3, dto.getDifficulty());
        assertEquals(ingredients, dto.getIngredients());
        assertEquals(60, dto.getPrepareTime());
        assertEquals(4, dto.getServings());
        assertEquals("dessert", dto.getCategory());
        assertTrue(dto.getIsPublic());
        assertEquals(steps, dto.getSteps());
        assertEquals("english", dto.getLanguage());
    }

    @Test
    void testWithIngredientsAndSteps() {
        RecipeAddDto dto = new RecipeAddDto();
        
        Ingredient ingredient1 = new Ingredient();
        ingredient1.setIngredientName("Flour");
        ingredient1.setQuantity(2.0f);
        ingredient1.setUnit("cups");
        
        Ingredient ingredient2 = new Ingredient();
        ingredient2.setIngredientName("Sugar");
        ingredient2.setQuantity(1.0f);
        ingredient2.setUnit("cup");
        
        Step step1 = new Step();
        step1.setContent("Mix flour and sugar");
        
        Step step2 = new Step();
        step2.setContent("Bake for 30 minutes");
        
        List<Ingredient> ingredients = Arrays.asList(ingredient1, ingredient2);
        List<Step> steps = Arrays.asList(step1, step2);
        
        dto.setIngredients(ingredients);
        dto.setSteps(steps);
        
        assertEquals(2, dto.getIngredients().size());
        assertEquals(2, dto.getSteps().size());
        assertEquals("Flour", dto.getIngredients().get(0).getIngredientName());
        assertEquals("Mix flour and sugar", dto.getSteps().get(0).getContent());
    }

    @Test
    void testDifficultyRange() {
        RecipeAddDto dto = new RecipeAddDto();
        
        // Test different difficulty levels
        dto.setDifficulty(1);
        assertEquals(1, dto.getDifficulty());
        
        dto.setDifficulty(2);
        assertEquals(2, dto.getDifficulty());
        
        dto.setDifficulty(3);
        assertEquals(3, dto.getDifficulty());
    }

    @Test
    void testBooleanIsPublic() {
        RecipeAddDto dto = new RecipeAddDto();
        
        dto.setIsPublic(true);
        assertTrue(dto.getIsPublic());
        
        dto.setIsPublic(false);
        assertFalse(dto.getIsPublic());
        
        dto.setIsPublic(null);
        assertNull(dto.getIsPublic());
    }

    @Test
    void testLanguageOptions() {
        RecipeAddDto dto = new RecipeAddDto();
        
        dto.setLanguage("english");
        assertEquals("english", dto.getLanguage());
        
        dto.setLanguage("polish");
        assertEquals("polish", dto.getLanguage());
    }

    @Test
    void testCategoryOptions() {
        RecipeAddDto dto = new RecipeAddDto();
        
        String[] categories = {"breakfast", "lunch", "dinner", "dessert", "snack", "drink"};
        
        for (String category : categories) {
            dto.setCategory(category);
            assertEquals(category, dto.getCategory());
        }
    }

    @Test
    void testNumericalFields() {
        RecipeAddDto dto = new RecipeAddDto();
        
        dto.setPrepareTime(45);
        assertEquals(45, dto.getPrepareTime());
        
        dto.setServings(6);
        assertEquals(6, dto.getServings());
        
        dto.setDifficulty(2);
        assertEquals(2, dto.getDifficulty());
        
        // Test zero values
        dto.setPrepareTime(0);
        assertEquals(0, dto.getPrepareTime());
        
        dto.setServings(0);
        assertEquals(0, dto.getServings());
    }

    @Test
    void testLombokGeneratedMethods() {
        RecipeAddDto dto1 = new RecipeAddDto();
        dto1.setRecipeName("Test Recipe");
        dto1.setDifficulty(2);
        dto1.setPrepareTime(30);
        dto1.setServings(4);
        dto1.setCategory("dinner");
        dto1.setIsPublic(true);
        dto1.setLanguage("english");
        
        RecipeAddDto dto2 = new RecipeAddDto();
        dto2.setRecipeName("Test Recipe");
        dto2.setDifficulty(2);
        dto2.setPrepareTime(30);
        dto2.setServings(4);
        dto2.setCategory("dinner");
        dto2.setIsPublic(true);
        dto2.setLanguage("english");
        
        // Test equals
        assertEquals(dto1, dto2);
        
        // Test hashCode
        assertEquals(dto1.hashCode(), dto2.hashCode());
        
        // Test toString
        String toString = dto1.toString();
        assertTrue(toString.contains("Test Recipe"));
        assertTrue(toString.contains("dinner"));
        assertTrue(toString.contains("english"));
    }

    @Test
    void testWithEmptyCollections() {
        RecipeAddDto dto = new RecipeAddDto();
        
        dto.setIngredients(new ArrayList<>());
        dto.setSteps(new ArrayList<>());
        
        assertTrue(dto.getIngredients().isEmpty());
        assertTrue(dto.getSteps().isEmpty());
    }

    @Test
    void testCompleteRecipeData() {
        RecipeAddDto dto = new RecipeAddDto();
        
        // Set up complete recipe
        dto.setRecipeName("Chocolate Cake");
        dto.setDifficulty(3);
        dto.setPrepareTime(120);
        dto.setServings(8);
        dto.setCategory("dessert");
        dto.setIsPublic(true);
        dto.setLanguage("english");
        
        Ingredient flour = new Ingredient();
        flour.setIngredientName("Flour");
        flour.setQuantity(2.0f);
        flour.setUnit("cups");
        
        dto.setIngredients(Arrays.asList(flour));
        
        Step mixStep = new Step();
        mixStep.setContent("Mix all ingredients");
        
        dto.setSteps(Arrays.asList(mixStep));
        
        // Verify all fields are set correctly
        assertEquals("Chocolate Cake", dto.getRecipeName());
        assertEquals(3, dto.getDifficulty());
        assertEquals(120, dto.getPrepareTime());
        assertEquals(8, dto.getServings());
        assertEquals("dessert", dto.getCategory());
        assertTrue(dto.getIsPublic());
        assertEquals("english", dto.getLanguage());
        assertEquals(1, dto.getIngredients().size());
        assertEquals(1, dto.getSteps().size());
    }
}