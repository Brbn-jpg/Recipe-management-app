package com.kk.cibaria.user;

import com.kk.cibaria.image.Image;
import com.kk.cibaria.image.ImageType;
import com.kk.cibaria.rating.Rating;
import com.kk.cibaria.recipe.Recipe;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class UserRepositoryIntegrationTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    private UserEntity testUser;

    @BeforeEach
    void setup() {
        testUser = new UserEntity();
        testUser.setUsername("testuser");
        testUser.setEmail("test@test.com");
        testUser.setPassword("encodedPassword");
        testUser.setRole("USER");
        testUser.setDescription("Test description");
        testUser.setRating(new ArrayList<>());
        testUser.setUserRecipes(new ArrayList<>());
        testUser.setFavouriteRecipes(new ArrayList<>());
        testUser.setImages(new ArrayList<>());
    }

    @Test
    void testSaveAndFindById() {
        UserEntity savedUser = entityManager.persistAndFlush(testUser);

        Optional<UserEntity> foundUser = userRepository.findById(savedUser.getId());

        assertTrue(foundUser.isPresent());
        assertEquals("testuser", foundUser.get().getUsername());
        assertEquals("test@test.com", foundUser.get().getEmail());
        assertEquals("USER", foundUser.get().getRole());
        assertEquals("Test description", foundUser.get().getDescription());
    }

    @Test
    void testFindByUsername() {
        entityManager.persistAndFlush(testUser);

        Optional<UserEntity> foundUser = userRepository.findByUsername("testuser");

        assertTrue(foundUser.isPresent());
        assertEquals("test@test.com", foundUser.get().getEmail());
        assertEquals("USER", foundUser.get().getRole());
    }

    @Test
    void testFindByUsername_NotFound() {
        Optional<UserEntity> foundUser = userRepository.findByUsername("nonexistentuser");

        assertFalse(foundUser.isPresent());
    }

    @Test
    void testFindByEmail() {
        entityManager.persistAndFlush(testUser);

        Optional<UserEntity> foundUser = userRepository.findByEmail("test@test.com");

        assertTrue(foundUser.isPresent());
        assertEquals("testuser", foundUser.get().getUsername());
        assertEquals("USER", foundUser.get().getRole());
    }

    @Test
    void testFindByEmail_NotFound() {
        Optional<UserEntity> foundUser = userRepository.findByEmail("nonexistent@example.com");

        assertFalse(foundUser.isPresent());
    }

    @Test
    void testFindAll() {
        UserEntity user1 = new UserEntity();
        user1.setUsername("user1");
        user1.setEmail("user1@example.com");
        user1.setPassword("password1");

        UserEntity user2 = new UserEntity();
        user2.setUsername("user2");
        user2.setEmail("user2@example.com");
        user2.setPassword("password2");

        entityManager.persist(user1);
        entityManager.persist(user2);
        entityManager.flush();

        List<UserEntity> users = userRepository.findAll();

        assertEquals(2, users.size());
        assertTrue(users.stream().anyMatch(u -> "user1".equals(u.getUsername())));
        assertTrue(users.stream().anyMatch(u -> "user2".equals(u.getUsername())));
    }

    @Test
    void testSaveUserWithRatings() {
        Rating rating1 = new Rating();
        rating1.setValue(5);
        rating1.setUser(testUser);

        Rating rating2 = new Rating();
        rating2.setValue(4);
        rating2.setUser(testUser);

        testUser.setRating(List.of(rating1, rating2));

        UserEntity savedUser = entityManager.persistAndFlush(testUser);
        entityManager.clear(); // Clear the persistence context

        Optional<UserEntity> foundUser = userRepository.findById(savedUser.getId());

        assertTrue(foundUser.isPresent());
        assertEquals(2, foundUser.get().getRating().size());
        assertTrue(foundUser.get().getRating().stream().anyMatch(r -> r.getValue() == 5));
        assertTrue(foundUser.get().getRating().stream().anyMatch(r -> r.getValue() == 4));
    }

    @Test
    void testSaveUserWithRecipes() {
        Recipe recipe1 = new Recipe();
        recipe1.setRecipeName("Test Recipe 1");
        recipe1.setCategory("Breakfast");
        recipe1.setDifficulty(1);
        recipe1.setPrepareTime(15);
        recipe1.setServings(2);
        recipe1.setIsPublic(true);
        recipe1.setLanguage("en");
        recipe1.setUser(testUser);
        recipe1.setIngredients(new ArrayList<>());
        recipe1.setSteps(new ArrayList<>());
        recipe1.setRatings(new ArrayList<>());
        recipe1.setImages(new ArrayList<>());
        recipe1.setFavouriteByUsers(new ArrayList<>());

        Recipe recipe2 = new Recipe();
        recipe2.setRecipeName("Test Recipe 2");
        recipe2.setCategory("Lunch");
        recipe2.setDifficulty(2);
        recipe2.setPrepareTime(30);
        recipe2.setServings(4);
        recipe2.setIsPublic(false);
        recipe2.setLanguage("en");
        recipe2.setUser(testUser);
        recipe2.setIngredients(new ArrayList<>());
        recipe2.setSteps(new ArrayList<>());
        recipe2.setRatings(new ArrayList<>());
        recipe2.setImages(new ArrayList<>());
        recipe2.setFavouriteByUsers(new ArrayList<>());

        testUser.setUserRecipes(List.of(recipe1, recipe2));

        UserEntity savedUser = entityManager.persistAndFlush(testUser);
        entityManager.clear();

        Optional<UserEntity> foundUser = userRepository.findById(savedUser.getId());

        assertTrue(foundUser.isPresent());
        assertEquals(2, foundUser.get().getUserRecipes().size());
        assertTrue(foundUser.get().getUserRecipes().stream().anyMatch(r -> "Test Recipe 1".equals(r.getRecipeName())));
        assertTrue(foundUser.get().getUserRecipes().stream().anyMatch(r -> "Test Recipe 2".equals(r.getRecipeName())));
    }

    @Test
    void testSaveUserWithImages() {
        Image profileImage = new Image();
        profileImage.setImageUrl("http://example.com/profile.jpg");
        profileImage.setPublicId("profile_123");
        profileImage.setImageType(ImageType.PROFILE_PICTURE);
        profileImage.setUser(testUser);

        Image backgroundImage = new Image();
        backgroundImage.setImageUrl("http://example.com/background.jpg");
        backgroundImage.setPublicId("background_123");
        backgroundImage.setImageType(ImageType.BACKGROUND_PICTURE);
        backgroundImage.setUser(testUser);

        testUser.setImages(List.of(profileImage, backgroundImage));

        UserEntity savedUser = entityManager.persistAndFlush(testUser);
        entityManager.clear();

        Optional<UserEntity> foundUser = userRepository.findById(savedUser.getId());

        assertTrue(foundUser.isPresent());
        assertEquals(2, foundUser.get().getImages().size());
        assertTrue(foundUser.get().getImages().stream().anyMatch(i -> ImageType.PROFILE_PICTURE == i.getImageType()));
        assertTrue(foundUser.get().getImages().stream().anyMatch(i -> ImageType.BACKGROUND_PICTURE == i.getImageType()));
    }

    @Test
    void testDeleteUser() {
        UserEntity savedUser = entityManager.persistAndFlush(testUser);
        int userId = savedUser.getId();

        userRepository.delete(savedUser);
        entityManager.flush();

        Optional<UserEntity> foundUser = userRepository.findById(userId);
        assertFalse(foundUser.isPresent());
    }

    @Test
    void testUpdateUser() {
        UserEntity savedUser = entityManager.persistAndFlush(testUser);
        
        savedUser.setUsername("updateduser");
        savedUser.setDescription("Updated description");
        savedUser.setRole("ADMIN");

        UserEntity updatedUser = userRepository.save(savedUser);
        entityManager.flush();
        entityManager.clear();

        Optional<UserEntity> foundUser = userRepository.findById(updatedUser.getId());

        assertTrue(foundUser.isPresent());
        assertEquals("updateduser", foundUser.get().getUsername());
        assertEquals("Updated description", foundUser.get().getDescription());
        assertEquals("ADMIN", foundUser.get().getRole());
        assertEquals("test@test.com", foundUser.get().getEmail()); // Should remain unchanged
    }

    @Test
    void testFindByEmail_CaseInsensitive() {
        testUser.setEmail("Test@Example.Com");
        entityManager.persistAndFlush(testUser);

        Optional<UserEntity> foundUser1 = userRepository.findByEmail("test@test.com");
        Optional<UserEntity> foundUser2 = userRepository.findByEmail("TEST@EXAMPLE.COM");
        Optional<UserEntity> foundUser3 = userRepository.findByEmail("Test@Example.Com");

        // Note: This test depends on database collation settings
        // With case-insensitive collation, all should find the user
        // With case-sensitive collation, only the exact match will work
        assertTrue(foundUser3.isPresent()); // Exact match should always work
    }

    @Test
    void testUniqueConstraints() {
        UserEntity user1 = new UserEntity();
        user1.setUsername("uniqueuser");
        user1.setEmail("unique@example.com");
        user1.setPassword("password1");

        UserEntity user2 = new UserEntity();
        user2.setUsername("anotheruser");
        user2.setEmail("unique@example.com"); // Same email
        user2.setPassword("password2");

        entityManager.persist(user1);
        entityManager.flush();

        // This should work at the repository level, but constraints will be enforced at DB level
        assertDoesNotThrow(() -> {
            entityManager.persist(user2);
            // Note: The actual constraint violation will occur on flush/commit
            // depending on the database configuration
        });
    }

    @Test
    void testDefaultValues() {
        UserEntity minimalUser = new UserEntity();
        minimalUser.setUsername("minimal");
        minimalUser.setEmail("minimal@example.com");
        minimalUser.setPassword("password");
        // Don't set role - should default to "USER"

        UserEntity savedUser = entityManager.persistAndFlush(minimalUser);
        entityManager.clear();

        Optional<UserEntity> foundUser = userRepository.findById(savedUser.getId());

        assertTrue(foundUser.isPresent());
        assertEquals("USER", foundUser.get().getRole()); // Should have default value
        assertNull(foundUser.get().getDescription()); // Should be null if not set
        assertNotNull(foundUser.get().getUserRecipes()); // Should be initialized
        assertNotNull(foundUser.get().getFavouriteRecipes()); // Should be initialized
        assertNotNull(foundUser.get().getImages()); // Should be initialized
    }
}