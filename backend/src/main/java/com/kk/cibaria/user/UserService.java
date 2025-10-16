package com.kk.cibaria.user;

import java.io.IOException;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.kk.cibaria.dto.auth.RegisterDto;
import com.kk.cibaria.dto.auth.TokenResponseDto;
import com.kk.cibaria.dto.myProfile.MyProfileDto;
import com.kk.cibaria.dto.myProfile.UpdateEmailDto;
import com.kk.cibaria.dto.myProfile.UpdatePasswordDto;

public interface UserService {
   List<UserEntity> getAll();

   UserEntity getById(int id);

   TokenResponseDto save(RegisterDto user);

   UserEntity update(int id, UserEntity user);

   void delete(int id);

   MyProfileDto getMyProfile(String token);

   String updateProfilePicture(int id, MultipartFile file, String token) throws IOException;
   
   String updateBackgroundPicture(int id, MultipartFile file, String token) throws IOException;

   UserEntity updateProfile(int id, MyProfileDto profileDto, String token);

   UserEntity updateEmail(int id, UpdateEmailDto updateEmailDto, String token);

   UserEntity updatePassword(int id, UpdatePasswordDto updatePasswordDto, String token);

   MyProfileDto getUserRecipes (String token);
   MyProfileDto getFavouriteRecipes (String token);

   UserEntity updateUser(int id, String role, String email, String username);

}
