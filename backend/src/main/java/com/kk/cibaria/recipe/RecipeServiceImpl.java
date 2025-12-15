package com.kk.cibaria.recipe;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.kk.cibaria.dto.RecipeAddDto;
import com.kk.cibaria.dto.RecipeRequestDto;
import com.kk.cibaria.exception.ImageErrorException;
import com.kk.cibaria.exception.RecipeErrorException;
import com.kk.cibaria.exception.UserNotFoundException;
import com.kk.cibaria.helper.Pagination;
import com.kk.cibaria.helper.RecipeFilter;
import com.kk.cibaria.cloudinary.CloudinaryService;
import com.kk.cibaria.image.Image;
import com.kk.cibaria.image.ImageService;
import com.kk.cibaria.image.ImageType;
import com.kk.cibaria.ingredient.Ingredient;
import com.kk.cibaria.ingredient.IngredientService;
import com.kk.cibaria.rating.Rating;
import com.kk.cibaria.rating.RatingRepository;
import com.kk.cibaria.security.jwt.JwtService;
import com.kk.cibaria.step.Step;
import com.kk.cibaria.step.StepRepository;
import com.kk.cibaria.user.UserEntity;

import org.springframework.stereotype.Service;

import com.kk.cibaria.exception.RecipeNotFoundException;
import com.kk.cibaria.exception.UnauthorizedException;
import com.kk.cibaria.user.UserRepository;

import jakarta.transaction.Transactional;

import org.springframework.web.multipart.MultipartFile;

@Service
public class RecipeServiceImpl implements RecipeService {

  private final RatingRepository ratingRepository;
  private final RecipeRepository recipeRepository;
  private final UserRepository userRepository;
  private final JwtService jwtService;
  private final ImageService imageService;
  private final CloudinaryService cloudinaryService;
  private final IngredientService ingredientService;

  public RecipeServiceImpl(RecipeRepository recipeRepository, UserRepository userRepository, JwtService jwtService, ImageService imageService, CloudinaryService cloudinaryService, StepRepository stepRepository, RatingRepository ratingRepository, IngredientService ingredientService) {
    this.recipeRepository = recipeRepository;
    this.userRepository = userRepository;
    this.jwtService = jwtService;
    this.imageService = imageService;
    this.cloudinaryService = cloudinaryService;
    this.ratingRepository = ratingRepository;
    this.ingredientService = ingredientService;
  }

  @Override
  public List<Recipe> getAll() {
    return recipeRepository.findAll();
  }

  @Override
  public Recipe getById(int id) {
    return recipeRepository.findById(id).orElseThrow(
        () -> new RecipeNotFoundException(String.format("Recipe with id: %s does not exist in the database", id)));
  }

  @Override
  public Recipe saveRecipeWithoutPhoto(RecipeAddDto recipe, String token) throws IOException {
    Recipe newRecipe = createRecipe(recipe, token);
    return recipeRepository.save(newRecipe);
  }

  @Override
  public Recipe saveRecipeWithPhotos(RecipeAddDto recipe, List<MultipartFile> images, String token) {
      Recipe newRecipe = createRecipe(recipe, token);
      List<Image> imagesSaved = new ArrayList<>();
      images.forEach(image->{
        try {
          imagesSaved.add(imageService.createPhoto(image, ImageType.RECIPE));
        } catch (IOException e) {
          throw new ImageErrorException(e.getMessage());
        }
      });
      imagesSaved.forEach(image->{
        image.setRecipe(newRecipe);
      });
      newRecipe.setImages(imagesSaved);
      return recipeRepository.save(newRecipe);
  }

  private Recipe createRecipe(RecipeAddDto recipe, String token){
    int userId = jwtService.extractId(token.substring(7));
    UserEntity user = userRepository.findById(userId).orElseThrow(
      () -> new UserNotFoundException(String.format("User with id: %s does not exist in the database", userId)));

    Recipe newRecipe = new Recipe();
    newRecipe.setRecipeName(recipe.getRecipeName());
    newRecipe.setDifficulty(recipe.getDifficulty());
    newRecipe.setUser(user);
    newRecipe.setLanguage(recipe.getLanguage());

    List<Ingredient> newIngredients = recipe.getIngredients().stream().map(i -> {
      Ingredient ingredient = new Ingredient();
      ingredient.setRecipe(newRecipe);
      ingredient.setIngredientName(i.getIngredientName());
      ingredient.setUnit(i.getUnit());
      ingredient.setQuantity(i.getQuantity());
      ingredient.setIsOptional(i.getIsOptional());
      ingredient.setLanguage(recipe.getLanguage());
      return ingredient;
    }).toList();

    List<Step> newSteps = recipe.getSteps().stream().map(s->{
      Step step = new Step();
      step.setContent(s.getContent());
      step.setRecipe(newRecipe);
      return step;
    }).toList();
    newRecipe.setSteps(newSteps);
    newRecipe.setIngredients(newIngredients);
    newRecipe.setPrepareTime(recipe.getPrepareTime());
    newRecipe.setServings(recipe.getServings());
    newRecipe.setCategory(recipe.getCategory());
    newRecipe.setIsPublic(recipe.getIsPublic());

    return newRecipe;
  }

  @Transactional
  @Override
  public Recipe updateRecipeWithPhotos(int id, Recipe recipe, List<MultipartFile> images, String token) {
    Recipe recipeFound = recipeRepository.findById(id).orElseThrow(
      () -> new RecipeNotFoundException(String.format("Recipe with id: %s does not exist in the database", id)));
   
    int userId = jwtService.extractId(token.substring(7));
    UserEntity currentUser = userRepository.findById(userId).orElseThrow(
      () -> new UserNotFoundException(String.format("User with id: %s does not exist in the database", userId)));
   
    // Check if user is owner or admin
    boolean isOwner = recipeFound.getUser().getId() == currentUser.getId();
    boolean isAdmin = jwtService.hasRole(token.substring(7), "ADMIN");
    
    if (!isOwner && !isAdmin) {
       throw new UnauthorizedException("You can edit only your own recipes!");
    }

    recipeFound.setRecipeName(recipe.getRecipeName());
    recipeFound.setDifficulty(recipe.getDifficulty());
    recipeFound.setPrepareTime(recipe.getPrepareTime());
    recipeFound.setServings(recipe.getServings());
    recipeFound.setCategory(recipe.getCategory());
    recipeFound.setIsPublic(recipe.getIsPublic());
    recipeFound.setLanguage(recipe.getLanguage());

    recipeFound.getIngredients().clear();
    for (Ingredient ingredient : recipe.getIngredients()) {
       ingredient.setRecipe(recipeFound);
       recipeFound.getIngredients().add(ingredient);
    }

    recipeFound.getSteps().clear();
    for (Step step : recipe.getSteps()) {
       step.setRecipe(recipeFound);
       recipeFound.getSteps().add(step);
    }

    if (images != null && !images.isEmpty()) {
      // Remove old images from Cloudinary before replacing them
      if (recipeFound.getImages() != null && !recipeFound.getImages().isEmpty()) {
        recipeFound.getImages().forEach(oldImage -> {
          try {
            cloudinaryService.removePhoto(oldImage.getPublicId());
          } catch (Exception e) {
            System.err.println("Failed to delete old image from Cloudinary: " + oldImage.getPublicId() + ", Error: " + e.getMessage());
          }
        });
      }

      List<Image> newImages = new ArrayList<>();
      images.forEach(image -> {
          try {
              newImages.add(imageService.createPhoto(image, ImageType.RECIPE));
          } catch (IOException e) {
              throw new ImageErrorException(e.getMessage());
          }
        });
       
        newImages.forEach(image -> {
          image.setRecipe(recipeFound);
        });
       
       recipeFound.getImages().clear();
       recipeFound.getImages().addAll(newImages);
       
    }
  return recipeRepository.save(recipeFound);
  }

  @Transactional
  @Override
  public Recipe updateRecipeWithoutPhotos(int id, Recipe recipe, String token, boolean keepExistingImages) {
    Recipe recipeFound = recipeRepository.findById(id).orElseThrow(
        () -> new RecipeNotFoundException(String.format("Recipe with id: %s does not exist in the database", id)));

    int userId = jwtService.extractId(token.substring(7));
    UserEntity currentUser = userRepository.findById(userId).orElseThrow(
      () -> new UserNotFoundException(String.format("User with id: %s does not exist in the database", userId)));

    // Check if user is owner or admin
    boolean isOwner = recipeFound.getUser().getId() == currentUser.getId();
    boolean isAdmin = jwtService.hasRole(token.substring(7), "ADMIN");
    
    if (!isOwner && !isAdmin){
      throw new UnauthorizedException("You can edit only your own recipes!");
    }
    recipeFound.setRecipeName(recipe.getRecipeName());
    recipeFound.setDifficulty(recipe.getDifficulty());

    recipeFound.getIngredients().clear();
    for (Ingredient ingredient : recipe.getIngredients()) {
      ingredient.setRecipe(recipeFound);
      recipeFound.getIngredients().add(ingredient);
    }

    recipeFound.getSteps().clear();
    for (Step step : recipe.getSteps()) {
      step.setRecipe(recipeFound);
      recipeFound.getSteps().add(step);
    }

    recipeFound.setPrepareTime(recipe.getPrepareTime());
    recipeFound.setServings(recipe.getServings());
    recipeFound.setCategory(recipe.getCategory());
    recipeFound.setIsPublic(recipe.getIsPublic());
    recipeFound.setLanguage(recipe.getLanguage());

    // Handle image deletion if not keeping existing images
    if (!keepExistingImages) {
      // Remove images from Cloudinary
      if (recipeFound.getImages() != null && !recipeFound.getImages().isEmpty()) {
        recipeFound.getImages().forEach(image -> {
          try {
            cloudinaryService.removePhoto(image.getPublicId());
          } catch (Exception e) {
            System.err.println("Failed to delete image from Cloudinary: " + image.getPublicId() + ", Error: " + e.getMessage());
          }
        });
      }
      recipeFound.getImages().clear();
    }

    return recipeRepository.save(recipeFound);
  }

  @Override
  public boolean isOwner(int id, String token){
    int userId = jwtService.extractId(token.substring(7));
    UserEntity user = userRepository.findById(userId).orElseThrow(
      () -> new UserNotFoundException(String.format("User with id: %s does not exist in the database", userId)));

    Recipe recipe = recipeRepository.findById(id).orElseThrow(
        () -> new RecipeNotFoundException(String.format("Recipe with id: %s does not exist in the database", id)));

    return recipe.getUser().getId() == user.getId();
  }

  @Override
  public RecipeRequestDto getRecipeByPage(int page, int size, List<String> category,
                                          Integer difficulty, String servings, String prepareTime, Boolean isPublic, String language, List<String> ingredients) {
    Pagination pagination = new Pagination();
    RecipeFilter filter = new RecipeFilter(ingredientService);
    List<Recipe> recipes = recipeRepository.findAll();

    if(isPublic != null && isPublic){
      recipes = recipes.stream()
                .filter(recipe -> Boolean.TRUE.equals(recipe.getIsPublic()) == true)
                .collect(Collectors.toList());
    }

    List<Recipe> filteredRecipes = filter.filterByParams(category,difficulty,servings,prepareTime,recipes, language, ingredients);
    List<Recipe> paginatedRecipes = pagination.paginate(page,size,filteredRecipes);
    RecipeRequestDto recipeRequestDto = new RecipeRequestDto();
    recipeRequestDto.setContent(paginatedRecipes);
    recipeRequestDto.setTotalPages(pagination.getTotalPages(size,filteredRecipes));
    return recipeRequestDto;
  }

  @Override
  public boolean isRecipeFavourite(String token, int recipeId) {
    int userId = jwtService.extractId(token.substring(7));
    UserEntity user = userRepository.findById(userId).orElseThrow(
      () -> new UserNotFoundException(String.format("User with id: %s does not exist in the database", userId)));

    Recipe recipe = recipeRepository.findById(recipeId).orElseThrow(
        () -> new RecipeNotFoundException(String.format("Recipe with id: %s does not exist in the database", recipeId)));

    return user.getFavouriteRecipes().contains(recipe);
  }

  @Override
  public void addRecipeToFavourites(String token, int recipeId) {
    int userId = jwtService.extractId(token.substring(7));
    UserEntity user = userRepository.findById(userId).orElseThrow(
      () -> new UserNotFoundException(String.format("User with id: %s does not exist in the database", userId)));
    Recipe recipe = recipeRepository.findById(recipeId).orElseThrow(
        () -> new RecipeNotFoundException(String.format("Recipe with id: %s does not exist in the database", recipeId)));

    List<Recipe> favRecipes = user.getFavouriteRecipes();

    if(favRecipes.contains(recipe)){
      throw new RecipeErrorException("Recipe is already added to favourites!");
    }

    favRecipes.add(recipe);
    userRepository.save(user);
  }

  @Override
  public void deleteRiceFromFavourites(String token, int recipeId) {
    int userId = jwtService.extractId(token.substring(7));
    UserEntity user = userRepository.findById(userId).orElseThrow(
      () -> new UserNotFoundException(String.format("User with id: %s does not exist in the database", userId)));
    Recipe recipe = recipeRepository.findById(recipeId).orElseThrow(
        () -> new RecipeNotFoundException(String.format("Recipe with id: %s does not exist in the database", recipeId)));

    List<Recipe> favRecipes = user.getFavouriteRecipes();

    if(!favRecipes.contains(recipe)){
      throw new RecipeErrorException("Recipe is not located in your favourites!");
    }

    favRecipes.remove(recipe);
    userRepository.save(user);
  }

  @Transactional
  @Override
  public void delete(String token, int id) {
    int userId = jwtService.extractId(token.substring(7));
    UserEntity user = userRepository.findById(userId).orElseThrow(
      () -> new UserNotFoundException(String.format("User with id: %s does not exist in the database", userId)));
    Recipe recipe = recipeRepository.findById(id).orElseThrow(
        () -> new RecipeNotFoundException(String.format("Recipe with id: %s does not exist in the database", id)));

    // Check if user is owner or admin
    boolean isOwner = recipe.getUser() != null && recipe.getUser().getId() == user.getId();
    boolean isAdmin = jwtService.hasRole(token.substring(7), "ADMIN");
    
    if (!isOwner && !isAdmin) {
       throw new UnauthorizedException("You can delete only your own recipes!");
    }

    // Remove images from Cloudinary before deleting recipe
    if (recipe.getImages() != null && !recipe.getImages().isEmpty()) {
      recipe.getImages().forEach(image -> {
        try {
          cloudinaryService.removePhoto(image.getPublicId());
        } catch (Exception e) {
          System.err.println("Failed to delete image from Cloudinary: " + image.getPublicId() + ", Error: " + e.getMessage());
        }
      });
    }

    recipe.getFavouriteByUsers().forEach(favUser -> {
        favUser.getFavouriteRecipes().remove(recipe);
    });
    recipe.getFavouriteByUsers().clear();
    
    recipeRepository.save(recipe);
    recipeRepository.delete(recipe);
  }

  @Override
  public List<Recipe> searchRecipes(String query) {
   return recipeRepository.findByRecipeNameQuery(query);
  }
  
  @Transactional
  @Override
  public Recipe rating(int id, String token, int rating){
    if (rating < 1 || rating > 5){
      throw new IllegalArgumentException("Rating has to be from 1 to 5");
    }
    int userId = jwtService.extractId(token.substring(7));
    UserEntity user = userRepository.findById(userId).orElseThrow(
      () -> new UserNotFoundException(String.format("User with id: %s does not exist in the database", userId)));

    Recipe recipe = recipeRepository.findById(id).orElseThrow(
        () -> new RecipeNotFoundException(String.format("Recipe with id: %s does not exist in the database", id)));

    Optional<Rating> existingRating = ratingRepository.findByRecipeIdAndUserId(id, userId);
    
    if (existingRating.isPresent()) {
        Rating rating1 = existingRating.get();
        rating1.setValue(rating);
        ratingRepository.save(rating1);
    } else {
        Rating newRating = new Rating();
        newRating.setRecipe(recipe);
        newRating.setValue(rating);
        newRating.setUser(user);
        ratingRepository.save(newRating);
    }
    return recipe;
  }

  @Override
  public int getUserRating(int recipeId, String token){
    int userId = jwtService.extractId(token.substring(7));
    userRepository.findById(userId).orElseThrow(
      () -> new UserNotFoundException(String.format("User with id: %s does not exist in the database", userId)));
    
    Optional<Rating> userRating = ratingRepository.findByRecipeIdAndUserId(recipeId, userId);

    return userRating.map(Rating::getValue).orElse(0);
  }
}
