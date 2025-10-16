package com.kk.cibaria.recipe;


import com.kk.cibaria.user.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;


public interface RecipeRepository extends JpaRepository<Recipe, Integer> {
    @Query("SELECT u FROM Recipe u WHERE u.recipeName ILIKE %:query%")
    List<Recipe> findByRecipeNameQuery(@Param("query") String query);

    List<Recipe> findByUser(UserEntity user);
}
