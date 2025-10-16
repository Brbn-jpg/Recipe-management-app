package com.kk.cibaria.rating;

import com.kk.cibaria.recipe.Recipe;
import com.kk.cibaria.user.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class RatingRepositoryIntegrationTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private RatingRepository ratingRepository;

    private UserEntity testUser;
    private Recipe testRecipe;
    private Rating testRating;

    @BeforeEach
    void setup() {
        testUser = new UserEntity();
        testUser.setEmail("test@test.com");
        testUser.setUsername("testuser");
        testUser.setPassword("password");
        testUser.setRole("USER");
        entityManager.persistAndFlush(testUser);

        testRecipe = new Recipe();
        testRecipe.setRecipeName("Test Recipe");
        testRecipe.setDifficulty(2);
        testRecipe.setServings(4);
        testRecipe.setPrepareTime(30);
        testRecipe.setCategory("dinner");
        testRecipe.setIsPublic(true);
        testRecipe.setUser(testUser);
        entityManager.persistAndFlush(testRecipe);

        testRating = new Rating();
        testRating.setUser(testUser);
        testRating.setRecipe(testRecipe);
        testRating.setValue(5);
    }

    @Test
    void testSaveRating() {
        Rating savedRating = ratingRepository.save(testRating);

        assertNotNull(savedRating.getRatingId());
        assertEquals(5, savedRating.getValue());
        assertEquals(testUser.getId(), savedRating.getUser().getId());
        assertEquals(testRecipe.getId(), savedRating.getRecipe().getId());
    }

    @Test
    void testFindByRecipeIdAndUserId_Found() {
        Rating savedRating = entityManager.persistAndFlush(testRating);

        Optional<Rating> foundRating = ratingRepository.findByRecipeIdAndUserId(
            testRecipe.getId(), testUser.getId());

        assertTrue(foundRating.isPresent());
        assertEquals(savedRating.getRatingId(), foundRating.get().getRatingId());
        assertEquals(5, foundRating.get().getValue());
    }

    @Test
    void testFindByRecipeIdAndUserId_NotFound() {
        Optional<Rating> foundRating = ratingRepository.findByRecipeIdAndUserId(999, 999);

        assertFalse(foundRating.isPresent());
    }

    @Test
    void testUpdateRating() {
        Rating savedRating = entityManager.persistAndFlush(testRating);
        
        // update rating value
        savedRating.setValue(3);
        Rating updatedRating = ratingRepository.save(savedRating);

        assertEquals(3, updatedRating.getValue());
        assertEquals(savedRating.getRatingId(), updatedRating.getRatingId());
    }

    @Test
    void testDeleteRating() {
        Rating savedRating = entityManager.persistAndFlush(testRating);
        int ratingId = savedRating.getRatingId();

        ratingRepository.delete(savedRating);

        assertFalse(ratingRepository.existsById(ratingId));
    }

    @Test
    void testFindById() {
        Rating savedRating = entityManager.persistAndFlush(testRating);

        Optional<Rating> foundRating = ratingRepository.findById(savedRating.getRatingId());

        assertTrue(foundRating.isPresent());
        assertEquals(savedRating.getRatingId(), foundRating.get().getRatingId());
    }

    @Test
    void testMultipleRatingsForDifferentRecipes() {
        // first rating
        Rating savedRating1 = entityManager.persistAndFlush(testRating);

        // second recipe and rating
        Recipe recipe2 = new Recipe();
        recipe2.setRecipeName("Another Recipe");
        recipe2.setDifficulty(1);
        recipe2.setServings(2);
        recipe2.setPrepareTime(15);
        recipe2.setCategory("breakfast");
        recipe2.setIsPublic(true);
        recipe2.setUser(testUser);
        entityManager.persistAndFlush(recipe2);

        Rating rating2 = new Rating();
        rating2.setUser(testUser);
        rating2.setRecipe(recipe2);
        rating2.setValue(4);
        Rating savedRating2 = entityManager.persistAndFlush(rating2);

        // verify both ratings exist
        Optional<Rating> foundRating1 = ratingRepository.findByRecipeIdAndUserId(
            testRecipe.getId(), testUser.getId());
        Optional<Rating> foundRating2 = ratingRepository.findByRecipeIdAndUserId(
            recipe2.getId(), testUser.getId());

        assertTrue(foundRating1.isPresent());
        assertTrue(foundRating2.isPresent());
        assertEquals(5, foundRating1.get().getValue());
        assertEquals(4, foundRating2.get().getValue());
    }
}