package com.kk.cibaria.rating;

import com.kk.cibaria.recipe.Recipe;
import com.kk.cibaria.user.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RatingTest {

    private Rating rating;
    private UserEntity user;
    private Recipe recipe;

    @BeforeEach
    void setup() {
        // create fresh objects for each test
        rating = new Rating();
        
        user = new UserEntity();
        user.setId(1);
        user.setEmail("test@test.com");
        
        recipe = new Recipe();
        recipe.setId(1);
        recipe.setRecipeName("Test Recipe");
    }

    @Test
    void testNewRating() {
        // new rating should be empty
        Rating newRating = new Rating();
        
        assertEquals(0, newRating.getRatingId());
        assertNull(newRating.getUser());
        assertNull(newRating.getRecipe());
        assertEquals(0, newRating.getValue());
    }

    @Test
    void testSetAndGet() {
        // set all fields
        rating.setRatingId(5);
        rating.setUser(user);
        rating.setRecipe(recipe);
        rating.setValue(4);

        // check all fields
        assertEquals(5, rating.getRatingId());
        assertEquals(user, rating.getUser());
        assertEquals(recipe, rating.getRecipe());
        assertEquals(4, rating.getValue());
    }

    @Test
    void testDifferentRatingValues() {
        // test different rating values
        rating.setValue(1);
        assertEquals(1, rating.getValue());
        
        rating.setValue(5);
        assertEquals(5, rating.getValue());
        
        rating.setValue(0);
        assertEquals(0, rating.getValue());
    }

    @Test
    void testUserConnection() {
        // connect rating to user
        rating.setUser(user);
        
        assertNotNull(rating.getUser());
        assertEquals("test@test.com", rating.getUser().getEmail());
        assertEquals(1, rating.getUser().getId());
    }

    @Test
    void testRecipeConnection() {
        // connect rating to recipe
        rating.setRecipe(recipe);
        
        assertNotNull(rating.getRecipe());
        assertEquals("Test Recipe", rating.getRecipe().getRecipeName());
        assertEquals(1, rating.getRecipe().getId());
    }

    @Test
    void testEqualRatings() {
        // create two same ratings
        Rating rating1 = new Rating();
        rating1.setRatingId(1);
        rating1.setValue(5);
        rating1.setUser(user);
        rating1.setRecipe(recipe);
        
        Rating rating2 = new Rating();
        rating2.setRatingId(1);
        rating2.setValue(5);
        rating2.setUser(user);
        rating2.setRecipe(recipe);
        
        // they should be equal
        assertEquals(rating1, rating2);
        assertEquals(rating1.hashCode(), rating2.hashCode());
        
        String text = rating1.toString();
        assertTrue(text.contains("5"));
    }

    @Test
    void testNullValues() {
        // set null values
        rating.setUser(null);
        rating.setRecipe(null);
        
        assertNull(rating.getUser());
        assertNull(rating.getRecipe());
    }

    @Test
    void testBadRatingValue() {
        // test weird rating values
        rating.setValue(-1);
        assertEquals(-1, rating.getValue());
    }

    @Test
    void testBigRatingValue() {
        // test high rating value
        rating.setValue(10);
        assertEquals(10, rating.getValue());
    }
}