package com.kk.cibaria.step;

import com.kk.cibaria.recipe.Recipe;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class StepTest {

    private Step step;
    private Recipe recipe;

    @BeforeEach
    void setup() {
        // create test recipe
        recipe = new Recipe();
        recipe.setId(1);
        recipe.setRecipeName("Test Recipe");
        recipe.setIngredients(new ArrayList<>());
        recipe.setSteps(new ArrayList<>());
        recipe.setRatings(new ArrayList<>());
        recipe.setImages(new ArrayList<>());
        recipe.setFavouriteByUsers(new ArrayList<>());

        step = new Step();
    }

    @Test
    void testStepCreation() {
        assertNotNull(step);
        assertNull(step.getId());
        assertNull(step.getContent());
        assertNull(step.getRecipe());
    }

    @Test
    void testStepConstructorWithParameters() {
        Step step = new Step("Mix the ingredients", recipe);

        assertEquals("Mix the ingredients", step.getContent());
        assertEquals(recipe, step.getRecipe());
        assertNull(step.getId()); // ID should be null before persisting
    }

    @Test
    void testStepSettersAndGetters() {
        step.setId(1L);
        step.setContent("Bake for 30 minutes");
        step.setRecipe(recipe);

        assertEquals(1L, step.getId());
        assertEquals("Bake for 30 minutes", step.getContent());
        assertEquals(recipe, step.getRecipe());
    }

    @Test
    void testStepWithNullContent() {
        step.setContent(null);
        step.setRecipe(recipe);

        assertNull(step.getContent());
        assertEquals(recipe, step.getRecipe());
    }

    @Test
    void testStepWithEmptyContent() {
        step.setContent("");
        step.setRecipe(recipe);

        assertEquals("", step.getContent());
        assertEquals(recipe, step.getRecipe());
    }

    @Test
    void testStepWithLongContent() {
        String longContent = "This is a very long step description that contains detailed instructions " +
                "on how to prepare this particular part of the recipe. It includes multiple sentences " +
                "and provides comprehensive guidance for the cooking process.";
        
        step.setContent(longContent);
        step.setRecipe(recipe);

        assertEquals(longContent, step.getContent());
        assertEquals(recipe, step.getRecipe());
    }

    @Test
    void testStepWithNullRecipe() {
        step.setContent("Some cooking step");
        step.setRecipe(null);

        assertEquals("Some cooking step", step.getContent());
        assertNull(step.getRecipe());
    }

    @Test
    void testEqualsAndHashCode() {
        Step step1 = new Step();
        step1.setId(1L);
        step1.setContent("Mix ingredients");
        step1.setRecipe(recipe);

        Step step2 = new Step();
        step2.setId(1L);
        step2.setContent("Mix ingredients");
        step2.setRecipe(recipe);

        Step step3 = new Step();
        step3.setId(2L);
        step3.setContent("Bake mixture");
        step3.setRecipe(recipe);

        assertEquals(step1, step2);
        assertNotEquals(step1, step3);
        assertEquals(step1.hashCode(), step2.hashCode());
    }

    @Test
    void testToString() {
        step.setId(1L);
        step.setContent("Preheat oven to 180°C");
        step.setRecipe(recipe);

        String toString = step.toString();
        
        assertNotNull(toString);
        assertTrue(toString.contains("Preheat oven to 180°C"));
    }

    @Test
    void testStepRecipeRelationship() {
        step.setContent("Add salt to taste");
        step.setRecipe(recipe);

        // Test bidirectional relationship
        assertEquals(recipe, step.getRecipe());
        assertEquals("Test Recipe", step.getRecipe().getRecipeName());
        assertEquals(1, step.getRecipe().getId());
    }

    @Test
    void testStepContentSpecialCharacters() {
        String contentWithSpecialChars = "Add 2½ cups flour & mix @ 180°C for 30 min.";
        
        step.setContent(contentWithSpecialChars);
        step.setRecipe(recipe);

        assertEquals(contentWithSpecialChars, step.getContent());
    }

    @Test
    void testStepContentWithNewlines() {
        String contentWithNewlines = "Step 1:\nPreheat oven\n\nStep 2:\nMix ingredients";
        
        step.setContent(contentWithNewlines);
        step.setRecipe(recipe);

        assertEquals(contentWithNewlines, step.getContent());
        assertTrue(step.getContent().contains("\n"));
    }

    @Test
    void testStepIdTypeAndRange() {
        step.setId(Long.MAX_VALUE);
        assertEquals(Long.MAX_VALUE, step.getId());

        step.setId(0L);
        assertEquals(0L, step.getId());

        step.setId(-1L);
        assertEquals(-1L, step.getId());
    }
}