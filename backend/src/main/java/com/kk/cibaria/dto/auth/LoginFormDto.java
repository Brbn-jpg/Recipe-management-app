package com.kk.cibaria.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;

public record LoginFormDto(
    @NotBlank(message = "Email is required") 
    @Email(message = "Email should be valid") 
    String email, 
    
    @NotBlank(message = "Password is required") 
    String password
) {

}
