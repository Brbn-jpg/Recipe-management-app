package com.kk.cibaria.dto.myProfile;

import lombok.Data;

@Data
public class UpdateEmailDto {
    private String newEmail;
    private String password; // Password for verification
}
