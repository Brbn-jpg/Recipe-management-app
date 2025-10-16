package com.kk.cibaria.auth;

import com.kk.cibaria.dto.auth.TokenResponseDto;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.kk.cibaria.exception.UserNotFoundException;
import com.kk.cibaria.security.UserDetailService;
import com.kk.cibaria.security.jwt.JwtService;
import com.kk.cibaria.dto.auth.LoginFormDto;
import jakarta.validation.Valid;

@RestController
public class LoginController {

  private final AuthenticationManager authenticationManager;
  private final JwtService jwtService;
  private final UserDetailService userDetailService;

  public LoginController(AuthenticationManager authenticationManager, JwtService jwtService,
      UserDetailService userDetailService) {
    this.authenticationManager = authenticationManager;
    this.jwtService = jwtService;
    this.userDetailService = userDetailService;
  }

  @PostMapping("/authenticate")
  public TokenResponseDto authenticate(@Valid @RequestBody LoginFormDto loginFormDto) {
    try{
      authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginFormDto.email(),
              loginFormDto.password()));
      TokenResponseDto token = new TokenResponseDto();
      token.setToken(jwtService.generateToken(userDetailService.loadUserByUsername(loginFormDto.email())));
      return token;
    }catch (BadCredentialsException ex) {
      throw new UserNotFoundException("Invalid credentials");
    }
  }
}
