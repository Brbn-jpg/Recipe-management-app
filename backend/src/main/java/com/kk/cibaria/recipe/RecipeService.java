package com.kk.cibaria.recipe;

import java.io.IOException;
import java.util.List;

import com.kk.cibaria.dto.RecipeAddDto;
import com.kk.cibaria.dto.RecipeRequestDto;

import org.springframework.web.multipart.MultipartFile;

public interface RecipeService {
  List<Recipe> getAll();

  Recipe getById(int id);

  Recipe saveRecipeWithoutPhoto(RecipeAddDto recipe, String token) throws IOException;

  Recipe saveRecipeWithPhotos(RecipeAddDto recipe, List<MultipartFile> images, String token);

  Recipe updateRecipeWithoutPhotos(int id, Recipe recipe, String token, boolean keepExistingImages);

  Recipe updateRecipeWithPhotos(int id, Recipe recipe, List<MultipartFile> images, String token);

  RecipeRequestDto getRecipeByPage(int page, int size, List<String> category, Integer difficulty, String servings,
                                   String prepareTime, Boolean isPublic, String language, List<String> ingredients);

  void addRecipeToFavourites(String token, int recipeId);

  void deleteRiceFromFavourites(String token, int recipeId);

  void delete(String token, int id);

  List<Recipe> searchRecipes(String query);

  boolean isRecipeFavourite(String token, int recipeId);

  boolean isOwner(int id, String token);

  Recipe rating(int id, String token, int rating);

  int getUserRating(int recipeId, String token);
}
