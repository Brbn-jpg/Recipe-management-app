package com.kk.cibaria.user;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.kk.cibaria.image.Image;
import com.kk.cibaria.rating.Rating;
import com.kk.cibaria.recipe.Recipe;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
public class UserEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private int id;

  @Column(name = "username")
  private String username;

  @Column(name = "password")
  private String password;

  @Column(name = "email")
  private String email;

  @Column(name = "role")
  private String role = "USER";

  @Column(name = "description")
  private String description;

  @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
  @JsonManagedReference("user")
  private List<Rating> rating;

  @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
  @JsonManagedReference("user-recipes")
  private List<Recipe> userRecipes = new ArrayList<>();
 
  @ManyToMany
  @JoinTable(name = "favourite_recipes",
  joinColumns = @JoinColumn(name = "user_id"),
  inverseJoinColumns = @JoinColumn(name = "recipe_id"))
  private List<Recipe> favouriteRecipes = new ArrayList<>();

  @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
  @JsonManagedReference("user-images")
  private List<Image> images = new ArrayList<>();
}
