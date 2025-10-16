package com.kk.cibaria.helper;

import com.kk.cibaria.ingredient.Ingredient;
import com.kk.cibaria.ingredient.IngredientService;
import com.kk.cibaria.recipe.Recipe;

import java.util.List;
import java.util.stream.Collectors;

public class RecipeFilter {

    private final IngredientService ingredientService;

    public RecipeFilter(IngredientService ingredientService) {
        this.ingredientService = ingredientService;
    }

    public List<Recipe> filterByParams(List<String> category, Integer difficulty,
                                       String servings, String prepareTime,
                                       List<Recipe> recipes, String language, List<String> ingredients) {
    
        List<Recipe> filteredRecipes = recipes;
        if (category != null) {
            filteredRecipes = filteredRecipes.stream()
                    .filter(recipe -> category.stream().anyMatch(c -> c.equals(recipe.getCategory())))
                    .toList();
        }

        if(difficulty != null)
        {
            filteredRecipes = filteredRecipes.stream()
                    .filter(recipe -> recipe.getDifficulty()==difficulty).toList();
        }

        if(servings!=null)
        {
            int from = Integer.parseInt(servings.split("-")[0]);
            int to = Integer.parseInt(servings.split("-")[1]);

            filteredRecipes = filteredRecipes.stream()
                    .filter(recipe -> recipe.getServings()>=from && recipe.getServings()<=to).toList();
        }

        if(prepareTime!=null)
        {

            int from = Integer.parseInt(prepareTime.split("-")[0]);
            int to = Integer.parseInt(prepareTime.split("-")[1]);

            filteredRecipes = filteredRecipes.stream()
                    .filter(recipe -> recipe.getPrepareTime()>=from && recipe.getPrepareTime()<=to).toList();
        }

        if (language != null && !language.isEmpty()) {
            filteredRecipes = filteredRecipes.stream()
              .filter(recipe -> recipe.getLanguage() != null &&
            recipe.getLanguage().equalsIgnoreCase(language))
              .toList();
        }

        if(ingredients != null && !ingredients.isEmpty()) {
            filteredRecipes = filteredRecipes.stream()
                    .filter(recipe -> recipe.getIngredients() != null &&
                            ingredients.stream()
                                    .allMatch(selectedIngredient -> recipe.getIngredients().stream()
                                            .anyMatch(recipeIngredient -> recipeIngredient.getIngredientName()
                                                    .equalsIgnoreCase(selectedIngredient))))
                    .toList();
        }

        return filteredRecipes.stream()
            .peek(recipe -> {
                if (language != null && !language.isEmpty() && recipe.getIngredients() != null) {
                    List<Ingredient> languageFiltered = ingredientService.filterByLanguage(recipe.getIngredients(), language);
                    recipe.setIngredients(languageFiltered);
                }
            })
            .collect(Collectors.toList());

    }
}
