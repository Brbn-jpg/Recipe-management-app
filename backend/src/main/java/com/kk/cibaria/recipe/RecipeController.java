package com.kk.cibaria.recipe;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kk.cibaria.dto.FavouriteRequest;
import com.kk.cibaria.dto.RecipeAddDto;
import com.kk.cibaria.dto.RecipeRequestDto;
import com.kk.cibaria.image.ImageService;

import jakarta.validation.constraints.Min;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/recipes")
public class RecipeController {

  private final RecipeService recipeService;

  public RecipeController(RecipeService recipeService, ImageService imageService) {
    this.recipeService = recipeService;
  }

  @GetMapping
  public RecipeRequestDto getRecipesByPage(
          @RequestParam(defaultValue = "1", required = false) @Min(1) int page,
          @RequestParam(defaultValue = "10", required = false) @Min(1) int size,
          @RequestParam(required = false) List<String> category,
          @RequestParam(required = false) Integer difficulty,
          @RequestParam(required = false) String servings,
          @RequestParam(required = false) String prepareTime,
          @RequestParam(defaultValue = "true") Boolean isPublic,
          @RequestParam(required = false) String language,
          @RequestParam(required = false) List<String> ingredients
  )
  {
    return recipeService.getRecipeByPage(page,size,category,difficulty,servings,prepareTime, isPublic, language, ingredients);
  }

  @GetMapping("/{id}")
  public Recipe getById(@PathVariable int id) {
    return recipeService.getById(id);
  }

  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public Recipe save(@RequestParam("recipe") String json,
                     @RequestHeader("Authorization") String token,                   
                     @RequestParam(value = "images", required = false) Optional<List<MultipartFile>> images) throws IOException {

    System.out.println("=== KONTROLER ===");
    System.out.println("JSON: " + json);
    System.out.println("Images count: " + (images.isPresent() && images.get() != null ? images.get().size() : 0));
    
    ObjectMapper objectMapper = new ObjectMapper();
    RecipeAddDto recipe = objectMapper.readValue(json,RecipeAddDto.class);
    if(images.isPresent() && images.get() != null && !images.get().isEmpty()){
     return recipeService.saveRecipeWithPhotos(recipe, images.get(), token);
    }else{
      return recipeService.saveRecipeWithoutPhoto(recipe, token);
    }
  }

  @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public Recipe update(@PathVariable int id,
                      @RequestParam("recipe") String json,
                      @RequestHeader("Authorization") String token,
                      @RequestParam(value = "images", required = false) List<MultipartFile> images,
                      @RequestParam(value = "keepExistingImage", required = false) String keepExistingImage) throws IOException {
      
      ObjectMapper objectMapper = new ObjectMapper();
      Recipe recipe = objectMapper.readValue(json, Recipe.class);
      
      if(images != null && !images.isEmpty()) {
          return recipeService.updateRecipeWithPhotos(id, recipe, images, token);
      } else {
          boolean shouldKeepImages = "true".equals(keepExistingImage);
          return recipeService.updateRecipeWithoutPhotos(id, recipe, token, shouldKeepImages);
      }
  }

  @DeleteMapping("/{id}")
  public void delete(@PathVariable int id, @RequestHeader("Authorization") String token) {
    recipeService.delete(token, id);
  }

  @PostMapping("/{id}")
  public void recipeRating(@PathVariable int id, @RequestHeader("Authorization") String token, @RequestBody int rating) {
      recipeService.rating(id, token, rating);
  }

  @GetMapping("/{id}/rating")
  public ResponseEntity<Integer> getUserRating(@PathVariable int id, @RequestHeader("Authorization") String token) {
    try {
      int userRating = recipeService.getUserRating(id, token);
      return ResponseEntity.ok(userRating);
    } catch (Exception e){
      return ResponseEntity.ok(0);
    }
  }

  @GetMapping("/favourites/isFavourite")
  public boolean isRecipeFavourite(@RequestHeader("Authorization") String token,
                                   @RequestParam int recipeId) {
    return recipeService.isRecipeFavourite(token, recipeId);
  }

  @GetMapping("/{id}/isOwner")
  public boolean isOwner(@PathVariable int id, @RequestHeader("Authorization") String token){
    return recipeService.isOwner(id, token);
  }

  @PostMapping("/favourites/add")
  public void addRecipeToFavourites(@RequestHeader("Authorization") String token,
                                          @RequestBody FavouriteRequest request){
    recipeService.addRecipeToFavourites(token,request.getRecipeId());
  }

  @PostMapping("/favourites/delete")
  public void deleteRecipeFromFavourites(@RequestHeader("Authorization") String token,
                                          @RequestBody FavouriteRequest request){
    recipeService.deleteRiceFromFavourites(token,request.getRecipeId());
  }

  @GetMapping("/search")
  public List<Recipe> search(@RequestParam String query) {
    return recipeService.searchRecipes(query);
  }
}
