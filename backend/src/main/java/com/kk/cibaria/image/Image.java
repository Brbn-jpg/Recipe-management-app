package com.kk.cibaria.image;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.kk.cibaria.recipe.Recipe;
import com.kk.cibaria.user.UserEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Image {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String imageUrl;

    private String publicId;

    @ManyToOne
    @JoinColumn(name = "recipe_id")
    @JsonBackReference("recipe-images")
    private Recipe recipe;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonBackReference("user-images")
    private UserEntity user;

    @Enumerated(EnumType.STRING)
    private ImageType imageType; // RECIPE , PROFILE_PICTURE, BACKGROUND_PICTURE
}
