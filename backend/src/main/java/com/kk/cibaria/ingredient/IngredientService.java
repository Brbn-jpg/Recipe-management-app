package com.kk.cibaria.ingredient;

import java.util.List;

public interface IngredientService {

    public List<Ingredient> filterByLanguage(List<Ingredient> ingredients, String language);
}
