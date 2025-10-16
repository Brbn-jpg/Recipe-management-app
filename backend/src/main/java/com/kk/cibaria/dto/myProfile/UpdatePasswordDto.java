package com.kk.cibaria.dto.myProfile;

import lombok.Data;

@Data
public class UpdatePasswordDto {
    private String currentPassword;
    private String newPassword;
}