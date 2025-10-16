package com.kk.cibaria.dto.myProfile;

import java.util.List;

import com.kk.cibaria.image.Image;
import com.kk.cibaria.ingredient.Ingredient;

import lombok.Data;

@Data
public class MyProfileRecipeDto {
    private int id;
    private List<Image> imageUrl;
    private String recipeName;
    private int servings;
    private int difficulty;
    private int prepareTime;
    private String category;
    private Long avgRating;
    private String language;
    private List<Ingredient> ingredients;
}
