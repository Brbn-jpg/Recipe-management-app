package com.kk.cibaria.rating;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RatingRepository extends JpaRepository<Rating,Integer> {
    Optional<Rating> findByRecipeIdAndUserId(int recipeId, int userId);
}

