package com.kk.cibaria.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AdminStatsDto {
    private long totalUsers;
    private long adminUsers;
    private long regularUsers;
    private long totalRecipes;
    private long publicRecipes;
    private long privateRecipes;
}