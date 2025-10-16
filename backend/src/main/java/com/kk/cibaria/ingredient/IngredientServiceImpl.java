package com.kk.cibaria.ingredient;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

@Service
public class IngredientServiceImpl implements IngredientService{

    @Override
    public List<Ingredient> filterByLanguage(List<Ingredient> ingredients, String language) {
        String targetLanguage = language != null ? language : "en";
        return ingredients.stream()
                .filter(ingredient -> targetLanguage.equalsIgnoreCase(ingredient.getLanguage()))
                .collect(Collectors.toList());
    }
}
