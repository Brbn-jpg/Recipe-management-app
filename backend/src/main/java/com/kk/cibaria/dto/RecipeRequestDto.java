package com.kk.cibaria.dto;

import com.kk.cibaria.recipe.Recipe;
import lombok.Data;

import java.util.List;

@Data
public class RecipeRequestDto {
    private List<Recipe> content;
    private int totalPages;
}
