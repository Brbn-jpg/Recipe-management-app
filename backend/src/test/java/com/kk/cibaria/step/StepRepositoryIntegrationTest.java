package com.kk.cibaria.step;

import com.kk.cibaria.recipe.Recipe;
import com.kk.cibaria.user.UserEntity;
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
class StepRepositoryIntegrationTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private StepRepository stepRepository;

    private Recipe testRecipe;
    private UserEntity testUser;

    @BeforeEach
    void setup() {
        testUser = new UserEntity();
        testUser.setUsername("testuser");
        testUser.setEmail("test@test.com");
        testUser.setPassword("password123");
        testUser.setRole("USER");
        testUser = entityManager.persist(testUser);

        testRecipe = new Recipe();
        testRecipe.setRecipeName("Test Recipe");
        testRecipe.setCategory("Breakfast");
        testRecipe.setDifficulty(1);
        testRecipe.setPrepareTime(15);
        testRecipe.setServings(2);
        testRecipe.setIsPublic(true);
        testRecipe.setLanguage("en");
        testRecipe.setUser(testUser);
        testRecipe.setIngredients(new ArrayList<>());
        testRecipe.setSteps(new ArrayList<>());
        testRecipe.setRatings(new ArrayList<>());
        testRecipe.setImages(new ArrayList<>());
        testRecipe.setFavouriteByUsers(new ArrayList<>());
        testRecipe = entityManager.persist(testRecipe);

        entityManager.flush();
    }

    @Test
    void testSaveAndFindById() {
        Step step = new Step("Mix all ingredients together", testRecipe);
        
        Step savedStep = entityManager.persistAndFlush(step);

        Optional<Step> foundStep = stepRepository.findById(savedStep.getId());

        assertTrue(foundStep.isPresent());
        assertEquals("Mix all ingredients together", foundStep.get().getContent());
        assertEquals(testRecipe.getId(), foundStep.get().getRecipe().getId());
    }

    @Test
    void testSaveStepUsingRepository() {
        Step step = new Step("Preheat oven to 180°C", testRecipe);

        Step savedStep = stepRepository.save(step);

        assertNotNull(savedStep.getId());
        assertEquals("Preheat oven to 180°C", savedStep.getContent());
        assertEquals(testRecipe, savedStep.getRecipe());
    }

    @Test
    void testFindAll() {
        Step step1 = new Step("Step 1: Prepare ingredients", testRecipe);
        Step step2 = new Step("Step 2: Mix everything", testRecipe);

        entityManager.persist(step1);
        entityManager.persist(step2);
        entityManager.flush();

        List<Step> steps = stepRepository.findAll();

        assertEquals(2, steps.size());
        assertTrue(steps.stream().anyMatch(s -> "Step 1: Prepare ingredients".equals(s.getContent())));
        assertTrue(steps.stream().anyMatch(s -> "Step 2: Mix everything".equals(s.getContent())));
    }

    @Test
    void testDeleteStep() {
        Step step = new Step("Step to be deleted", testRecipe);
        Step savedStep = entityManager.persistAndFlush(step);
        Long stepId = savedStep.getId();

        stepRepository.delete(savedStep);
        entityManager.flush();

        Optional<Step> foundStep = stepRepository.findById(stepId);
        assertFalse(foundStep.isPresent());
    }

    @Test
    void testDeleteById() {
        Step step = new Step("Another step to be deleted", testRecipe);
        Step savedStep = entityManager.persistAndFlush(step);
        Long stepId = savedStep.getId();

        stepRepository.deleteById(stepId);
        entityManager.flush();

        Optional<Step> foundStep = stepRepository.findById(stepId);
        assertFalse(foundStep.isPresent());
    }

    @Test
    void testUpdateStep() {
        Step step = new Step("Original step content", testRecipe);
        Step savedStep = entityManager.persistAndFlush(step);

        savedStep.setContent("Updated step content");
        Step updatedStep = stepRepository.save(savedStep);
        entityManager.flush();
        entityManager.clear();

        Optional<Step> foundStep = stepRepository.findById(updatedStep.getId());
        assertTrue(foundStep.isPresent());
        assertEquals("Updated step content", foundStep.get().getContent());
    }

    @Test
    void testStepWithNullContent() {
        Step step = new Step(null, testRecipe);

        Step savedStep = stepRepository.save(step);

        assertNotNull(savedStep.getId());
        assertNull(savedStep.getContent());
        assertEquals(testRecipe, savedStep.getRecipe());
    }

    @Test
    void testStepWithEmptyContent() {
        Step step = new Step("", testRecipe);

        Step savedStep = stepRepository.save(step);

        assertNotNull(savedStep.getId());
        assertEquals("", savedStep.getContent());
        assertEquals(testRecipe, savedStep.getRecipe());
    }

    @Test
    void testStepWithLongContent() {
        // Keep content under 255 characters limit
        String longContent = "This is a long step instruction that contains multiple sentences " +
                "and detailed explanations of how to perform this cooking step. " +
                "It includes specific temperatures and times.";

        Step step = new Step(longContent, testRecipe);

        Step savedStep = stepRepository.save(step);

        assertNotNull(savedStep.getId());
        assertEquals(longContent, savedStep.getContent());
        assertTrue(savedStep.getContent().length() > 100);
        assertTrue(savedStep.getContent().length() < 255);
    }

    @Test
    void testStepRecipeRelationship() {
        Step step = new Step("Test relationship", testRecipe);
        Step savedStep = entityManager.persistAndFlush(step);
        entityManager.clear();

        Optional<Step> foundStep = stepRepository.findById(savedStep.getId());

        assertTrue(foundStep.isPresent());
        assertNotNull(foundStep.get().getRecipe());
        assertEquals(testRecipe.getId(), foundStep.get().getRecipe().getId());
        assertEquals("Test Recipe", foundStep.get().getRecipe().getRecipeName());
    }

    @Test
    void testMultipleStepsForSameRecipe() {
        Step step1 = new Step("Step 1: Preparation", testRecipe);
        Step step2 = new Step("Step 2: Cooking", testRecipe);
        Step step3 = new Step("Step 3: Serving", testRecipe);

        stepRepository.save(step1);
        stepRepository.save(step2);
        stepRepository.save(step3);
        entityManager.flush();

        List<Step> allSteps = stepRepository.findAll();

        long stepsForRecipe = allSteps.stream()
                .filter(s -> s.getRecipe().getId() == testRecipe.getId())
                .count();

        assertEquals(3, stepsForRecipe);
    }

    @Test
    void testStepContentWithSpecialCharacters() {
        String specialContent = "Heat to 180°C, add 2½ cups flour & mix @ medium speed for 5-10 min.";
        Step step = new Step(specialContent, testRecipe);

        Step savedStep = stepRepository.save(step);
        entityManager.flush();
        entityManager.clear();

        Optional<Step> foundStep = stepRepository.findById(savedStep.getId());

        assertTrue(foundStep.isPresent());
        assertEquals(specialContent, foundStep.get().getContent());
    }

    @Test
    void testStepCount() {
        assertEquals(0, stepRepository.count());

        stepRepository.save(new Step("Step 1", testRecipe));
        stepRepository.save(new Step("Step 2", testRecipe));
        entityManager.flush();

        assertEquals(2, stepRepository.count());
    }

    @Test
    void testExistsById() {
        Step step = new Step("Test existence", testRecipe);
        Step savedStep = stepRepository.save(step);
        entityManager.flush();

        assertTrue(stepRepository.existsById(savedStep.getId()));
        assertFalse(stepRepository.existsById(99999L));
    }
}