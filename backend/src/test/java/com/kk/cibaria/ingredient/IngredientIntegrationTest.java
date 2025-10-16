package com.kk.cibaria.ingredient;

import com.kk.cibaria.recipe.Recipe;
import com.kk.cibaria.recipe.RecipeRepository;
import com.kk.cibaria.user.UserEntity;
import com.kk.cibaria.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@TestPropertySource(locations = "classpath:application-test.properties", properties = "spring.profiles.active=test")
@Transactional
class IngredientIntegrationTest {

    @Autowired
    private IngredientRepository ingredientRepository;

    @Autowired
    private IngredientService ingredientService;

    @Autowired
    private RecipeRepository recipeRepository;

    @Autowired
    private UserRepository userRepository;

    private Recipe testRecipe;
    private UserEntity testUser;

    @BeforeEach
    void setup() {
        // Create test user
        testUser = new UserEntity();
        testUser.setUsername("ingredientTestUser");
        testUser.setEmail("ingredient@test.com");
        testUser.setPassword("password");
        testUser.setRole("USER");
        testUser = userRepository.save(testUser);

        // Create test recipe
        testRecipe = new Recipe();
        testRecipe.setRecipeName("Test Recipe for Ingredients");
        testRecipe.setDifficulty(2);
        testRecipe.setIsPublic(true);
        testRecipe.setLanguage("pl");
        testRecipe.setUser(testUser);
        testRecipe = recipeRepository.save(testRecipe);

        // Create test ingredients
        Ingredient ingredient1 = new Ingredient();
        ingredient1.setIngredientName("Mąka");
        ingredient1.setQuantity(500.0f);
        ingredient1.setUnit("g");
        ingredient1.setIsOptional(false);
        ingredient1.setLanguage("pl");
        ingredient1.setRecipe(testRecipe);

        Ingredient ingredient2 = new Ingredient();
        ingredient2.setIngredientName("Flour");
        ingredient2.setQuantity(500.0f);
        ingredient2.setUnit("g");
        ingredient2.setIsOptional(false);
        ingredient2.setLanguage("en");
        ingredient2.setRecipe(testRecipe);

        Ingredient ingredient3 = new Ingredient();
        ingredient3.setIngredientName("Cukier");
        ingredient3.setQuantity(200.0f);
        ingredient3.setUnit("g");
        ingredient3.setIsOptional(true);
        ingredient3.setLanguage("pl");
        ingredient3.setRecipe(testRecipe);

        ingredientRepository.saveAll(Arrays.asList(ingredient1, ingredient2, ingredient3));
        ingredientRepository.flush();
    }

    @Test
    void ingredientRepository_ShouldSaveAndRetrieveIngredients() {
        List<Ingredient> allIngredients = ingredientRepository.findAll();

        assertEquals(3, allIngredients.size());

        Ingredient polishFlour = allIngredients.stream()
                .filter(i -> "Mąka".equals(i.getIngredientName()))
                .findFirst()
                .orElse(null);

        assertNotNull(polishFlour);
        assertEquals("Mąka", polishFlour.getIngredientName());
        assertEquals(500.0f, polishFlour.getQuantity());
        assertEquals("g", polishFlour.getUnit());
        assertFalse(polishFlour.getIsOptional());
        assertEquals("pl", polishFlour.getLanguage());
        assertNotNull(polishFlour.getRecipe());
        assertEquals(testRecipe.getId(), polishFlour.getRecipe().getId());
    }

    @Test
    void ingredientRepository_ShouldFindById() {
        List<Ingredient> savedIngredients = ingredientRepository.findAll();
        int firstIngredientId = savedIngredients.get(0).getId();

        Optional<Ingredient> found = ingredientRepository.findById(firstIngredientId);

        assertTrue(found.isPresent());
        assertEquals(firstIngredientId, found.get().getId());
    }

    @Test
    void ingredientRepository_ShouldUpdateIngredient() {
        List<Ingredient> savedIngredients = ingredientRepository.findAll();
        Ingredient ingredientToUpdate = savedIngredients.get(0);

        String originalName = ingredientToUpdate.getIngredientName();
        ingredientToUpdate.setIngredientName("Updated Name");
        ingredientToUpdate.setQuantity(999.0f);

        Ingredient updatedIngredient = ingredientRepository.save(ingredientToUpdate);

        assertNotEquals(originalName, updatedIngredient.getIngredientName());
        assertEquals("Updated Name", updatedIngredient.getIngredientName());
        assertEquals(999.0f, updatedIngredient.getQuantity());
    }

    @Test
    void ingredientRepository_ShouldDeleteIngredient() {
        List<Ingredient> savedIngredients = ingredientRepository.findAll();
        assertEquals(3, savedIngredients.size());

        Ingredient ingredientToDelete = savedIngredients.get(0);
        int ingredientId = ingredientToDelete.getId();

        ingredientRepository.delete(ingredientToDelete);
        ingredientRepository.flush();

        assertFalse(ingredientRepository.findById(ingredientId).isPresent());
        assertEquals(2, ingredientRepository.findAll().size());
    }

    @Test
    void ingredientService_ShouldFilterByLanguageWithDatabaseData() {
        List<Ingredient> allIngredients = ingredientRepository.findAll();

        List<Ingredient> polishIngredients = ingredientService.filterByLanguage(allIngredients, "pl");
        List<Ingredient> englishIngredients = ingredientService.filterByLanguage(allIngredients, "en");

        assertEquals(2, polishIngredients.size());
        assertEquals(1, englishIngredients.size());

        assertTrue(polishIngredients.stream()
                .allMatch(ingredient -> "pl".equals(ingredient.getLanguage())));
        assertTrue(englishIngredients.stream()
                .allMatch(ingredient -> "en".equals(ingredient.getLanguage())));
    }

    @Test
    void ingredient_ShouldMaintainRecipeRelationship() {
        List<Ingredient> ingredients = ingredientRepository.findAll();

        for (Ingredient ingredient : ingredients) {
            assertNotNull(ingredient.getRecipe());
            assertEquals(testRecipe.getId(), ingredient.getRecipe().getId());
            assertEquals("Test Recipe for Ingredients", ingredient.getRecipe().getRecipeName());
        }
    }

    @Test
    void ingredient_ShouldHandleOptionalProperty() {
        List<Ingredient> ingredients = ingredientRepository.findAll();

        // Find sugar (Cukier) which should be optional
        Ingredient sugar = ingredients.stream()
                .filter(i -> "Cukier".equals(i.getIngredientName()))
                .findFirst()
                .orElse(null);

        assertNotNull(sugar);
        assertTrue(sugar.getIsOptional());

        // Find flour (Mąka) which should not be optional
        Ingredient flour = ingredients.stream()
                .filter(i -> "Mąka".equals(i.getIngredientName()))
                .findFirst()
                .orElse(null);

        assertNotNull(flour);
        assertFalse(flour.getIsOptional());
    }

    @Test
    void ingredient_ShouldHandleDifferentUnitsAndQuantities() {
        // Add ingredient with different unit
        Ingredient liquidIngredient = new Ingredient();
        liquidIngredient.setIngredientName("Mleko");
        liquidIngredient.setQuantity(250.0f);
        liquidIngredient.setUnit("ml");
        liquidIngredient.setIsOptional(false);
        liquidIngredient.setLanguage("pl");
        liquidIngredient.setRecipe(testRecipe);

        Ingredient savedLiquid = ingredientRepository.save(liquidIngredient);

        assertNotNull(savedLiquid.getId());
        assertEquals("Mleko", savedLiquid.getIngredientName());
        assertEquals(250.0f, savedLiquid.getQuantity());
        assertEquals("ml", savedLiquid.getUnit());
    }

    @Test
    void ingredientService_ShouldWorkWithEmptyDatabase() {
        // Clear all ingredients
        ingredientRepository.deleteAll();
        ingredientRepository.flush();

        List<Ingredient> emptyList = ingredientRepository.findAll();
        assertTrue(emptyList.isEmpty());

        List<Ingredient> filtered = ingredientService.filterByLanguage(emptyList, "pl");
        assertTrue(filtered.isEmpty());
    }

    @Test
    void ingredient_ShouldPersistFloatQuantitiesCorrectly() {
        Ingredient preciseIngredient = new Ingredient();
        preciseIngredient.setIngredientName("Sól");
        preciseIngredient.setQuantity(2.5f);
        preciseIngredient.setUnit("tsp");
        preciseIngredient.setIsOptional(true);
        preciseIngredient.setLanguage("pl");
        preciseIngredient.setRecipe(testRecipe);

        Ingredient saved = ingredientRepository.save(preciseIngredient);

        assertEquals(2.5f, saved.getQuantity(), 0.001f);
        assertEquals("tsp", saved.getUnit());
    }
}