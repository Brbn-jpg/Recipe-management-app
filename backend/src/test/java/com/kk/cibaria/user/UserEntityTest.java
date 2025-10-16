package com.kk.cibaria.user;

import com.kk.cibaria.image.Image;
import com.kk.cibaria.rating.Rating;
import com.kk.cibaria.recipe.Recipe;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UserEntityTest {

    private UserEntity user;

    @BeforeEach
    void setup() {
        user = new UserEntity();
    }

    @Test
    void testNewUser() {
        // new user should have default values
        assertNotNull(user);
        assertEquals(0, user.getId());
        assertNull(user.getUsername());
        assertNull(user.getPassword());
        assertNull(user.getEmail());
        assertEquals("USER", user.getRole());
        assertNull(user.getDescription());
        assertNotNull(user.getFavouriteRecipes());
        assertTrue(user.getFavouriteRecipes().isEmpty());
        assertNotNull(user.getUserRecipes());
        assertTrue(user.getUserRecipes().isEmpty());
        assertNotNull(user.getImages());
        assertTrue(user.getImages().isEmpty());
    }

    @Test
    void testUserEntitySettersAndGetters() {
        user.setId(1);
        user.setUsername("testuser");
        user.setPassword("password123");
        user.setEmail("test@test.com");
        user.setRole("ADMIN");
        user.setDescription("Test description");

        assertEquals(1, user.getId());
        assertEquals("testuser", user.getUsername());
        assertEquals("password123", user.getPassword());
        assertEquals("test@test.com", user.getEmail());
        assertEquals("ADMIN", user.getRole());
        assertEquals("Test description", user.getDescription());
    }

    @Test
    void testDefaultRole() {
        UserEntity newUser = new UserEntity();
        assertEquals("USER", newUser.getRole());
    }

    @Test
    void testRatingsRelationship() {
        List<Rating> ratings = new ArrayList<>();
        Rating rating1 = new Rating();
        rating1.setValue(5);
        Rating rating2 = new Rating();
        rating2.setValue(4);
        
        ratings.add(rating1);
        ratings.add(rating2);
        
        user.setRating(ratings);
        
        assertEquals(2, user.getRating().size());
        assertTrue(user.getRating().contains(rating1));
        assertTrue(user.getRating().contains(rating2));
    }

    @Test
    void testUserRecipesRelationship() {
        List<Recipe> recipes = new ArrayList<>();
        Recipe recipe1 = new Recipe();
        recipe1.setRecipeName("Test Recipe 1");
        Recipe recipe2 = new Recipe();
        recipe2.setRecipeName("Test Recipe 2");
        
        recipes.add(recipe1);
        recipes.add(recipe2);
        
        user.setUserRecipes(recipes);
        
        assertEquals(2, user.getUserRecipes().size());
        assertTrue(user.getUserRecipes().contains(recipe1));
        assertTrue(user.getUserRecipes().contains(recipe2));
    }

    @Test
    void testFavouriteRecipesRelationship() {
        List<Recipe> favourites = new ArrayList<>();
        Recipe recipe1 = new Recipe();
        recipe1.setRecipeName("Favourite Recipe 1");
        Recipe recipe2 = new Recipe();
        recipe2.setRecipeName("Favourite Recipe 2");
        
        favourites.add(recipe1);
        favourites.add(recipe2);
        
        user.setFavouriteRecipes(favourites);
        
        assertEquals(2, user.getFavouriteRecipes().size());
        assertTrue(user.getFavouriteRecipes().contains(recipe1));
        assertTrue(user.getFavouriteRecipes().contains(recipe2));
    }

    @Test
    void testImagesRelationship() {
        List<Image> images = new ArrayList<>();
        Image image1 = new Image();
        image1.setImageUrl("http://example.com/image1.jpg");
        Image image2 = new Image();
        image2.setImageUrl("http://example.com/image2.jpg");
        
        images.add(image1);
        images.add(image2);
        
        user.setImages(images);
        
        assertEquals(2, user.getImages().size());
        assertTrue(user.getImages().contains(image1));
        assertTrue(user.getImages().contains(image2));
    }

    @Test
    void testEqualsAndHashCode() {
        UserEntity user1 = new UserEntity();
        user1.setId(1);
        user1.setUsername("testuser");
        user1.setEmail("test@test.com");

        UserEntity user2 = new UserEntity();
        user2.setId(1);
        user2.setUsername("testuser");
        user2.setEmail("test@test.com");

        UserEntity user3 = new UserEntity();
        user3.setId(2);
        user3.setUsername("anotheruser");
        user3.setEmail("another@example.com");

        assertEquals(user1, user2);
        assertNotEquals(user1, user3);
        assertEquals(user1.hashCode(), user2.hashCode());
    }

    @Test
    void testToString() {
        user.setId(1);
        user.setUsername("testuser");
        user.setEmail("test@test.com");
        user.setRole("USER");

        String toString = user.toString();
        
        assertNotNull(toString);
        assertTrue(toString.contains("testuser"));
        assertTrue(toString.contains("test@test.com"));
        assertTrue(toString.contains("USER"));
    }

    @Test
    void testListInitialization() {
        UserEntity newUser = new UserEntity();
        
        // Test that collections are initialized and not null
        assertNotNull(newUser.getUserRecipes());
        assertNotNull(newUser.getFavouriteRecipes());
        assertNotNull(newUser.getImages());
        
        // Test that we can add to collections without null pointer exception
        assertDoesNotThrow(() -> {
            Recipe recipe = new Recipe();
            newUser.getUserRecipes().add(recipe);
            newUser.getFavouriteRecipes().add(recipe);
            
            Image image = new Image();
            newUser.getImages().add(image);
        });
    }
}