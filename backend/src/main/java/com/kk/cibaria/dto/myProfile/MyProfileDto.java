package com.kk.cibaria.dto.myProfile;

import lombok.Data;

import java.util.List;

@Data
public class MyProfileDto {
    private int id;
    private String photoUrl;
    private String backgroundUrl;
    private String username;
    private String description;
    private List<MyProfileRecipeDto> favourites;
    private List<MyProfileRecipeDto> UserRecipes;
}
