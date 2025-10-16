package com.kk.cibaria.dto.admin;

import lombok.Data;

@Data
public class UpdateUserDto {
    private String role;
    private String email;
    private String username;
}