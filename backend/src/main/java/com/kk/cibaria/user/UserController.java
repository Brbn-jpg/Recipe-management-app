package com.kk.cibaria.user;

import java.io.IOException;
import java.util.List;

import com.kk.cibaria.dto.myProfile.MyProfileDto;
import com.kk.cibaria.dto.myProfile.UpdateEmailDto;
import com.kk.cibaria.dto.myProfile.UpdatePasswordDto;
import com.kk.cibaria.exception.UnauthorizedException;
import com.kk.cibaria.security.jwt.JwtService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


@RestController
@RequestMapping("/users")
public class UserController {

  private final UserService userService;
  private final JwtService jwtService;

  public UserController(UserService userService, JwtService jwtService) {
    this.userService = userService;
    this.jwtService = jwtService;
  }

  @GetMapping
  public List<UserEntity> getAll() {
    return userService.getAll();
  }

  @GetMapping("/{id}")
  public UserEntity getById(@PathVariable int id) {
    return userService.getById(id);
  }

  @PutMapping("/{id}") 
  public UserEntity update(@PathVariable int id, @RequestBody UserEntity user, @RequestHeader("Authorization") String token) {
    return userService.update(id, user);
  }

  @PutMapping("/{id}/profile")
  public UserEntity updateProfile(@PathVariable int id, @RequestBody MyProfileDto profileDto, @RequestHeader("Authorization") String token) {
    return userService.updateProfile(id, profileDto, token);
  }

  @PutMapping("/{id}/email")
  public UserEntity updateEmail(@PathVariable int id, @RequestBody UpdateEmailDto updateEmailDto, @RequestHeader("Authorization") String token) {
    return userService.updateEmail(id, updateEmailDto, token);
  } 

  @PutMapping("/{id}/password")
  public UserEntity updatePassword(@PathVariable int id, @RequestBody UpdatePasswordDto updatePasswordDto, @RequestHeader("Authorization") String token) {
    return userService.updatePassword(id, updatePasswordDto, token);
  }

  @GetMapping("/aboutme")
  public MyProfileDto getMyProfile(@RequestHeader("Authorization") String token){
    return userService.getMyProfile(token);
  }

  @GetMapping("/recipes")
  public MyProfileDto getUserRecipes(@RequestHeader("Authorization") String token){
    return userService.getUserRecipes(token);
  }

  @GetMapping("/favourites")
  public MyProfileDto getFavouriteRecipes(@RequestHeader("Authorization") String token){
    return userService.getFavouriteRecipes(token);
  }
  

  @PutMapping("/{id}/profile-picture")
  public ResponseEntity<String> updateProfilePicture(
          @PathVariable int id,
          @RequestParam("file") MultipartFile file, @RequestHeader("Authorization") String token) throws IOException {
        
      String imageUrl = userService.updateProfilePicture(id, file, token);
      return ResponseEntity.ok(imageUrl);
  }

  @PutMapping("/{id}/background-picture")
  public ResponseEntity<String> updateBackgroundPicture(
          @PathVariable int id,
          @RequestParam("file") MultipartFile file, @RequestHeader("Authorization") String token) throws IOException {
        
      String imageUrl = userService.updateBackgroundPicture(id, file, token);
      return ResponseEntity.ok(imageUrl);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<?> delete(@PathVariable int id, @RequestHeader("Authorization") String authHeader) {
      if (authHeader == null || !authHeader.startsWith("Bearer ")) {
          throw new UnauthorizedException("Invalid authorization header");
      }
      String token = authHeader.substring(7);

      if (!jwtService.isTokenValid(token)) {
          throw new UnauthorizedException("Invalid or expired token");
      }

      int currentUserId = jwtService.extractId(token);
      boolean isAdmin = jwtService.hasRole(token, "ADMIN");
        
      if (currentUserId == id || isAdmin) {
          userService.delete(id);
          return ResponseEntity.noContent().build();
      } else {
        throw new UnauthorizedException("You don't have permission to delete this user");
      }
    }
}
