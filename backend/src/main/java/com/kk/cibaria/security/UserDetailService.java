package com.kk.cibaria.security;

import java.util.Optional;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.kk.cibaria.user.UserEntity;
import com.kk.cibaria.user.UserRepository;

@Service
public class UserDetailService implements UserDetailsService {

  private final UserRepository userRepository;

  public UserDetailService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Override
  public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
    Optional<UserEntity> user = userRepository.findByEmail(email);
    if (user.isPresent()) {
      var userObj = user.get();
      return User.builder()
          .username(userObj.getEmail())
          .password(userObj.getPassword())
          .roles(getRoles(userObj))
          .build();

    } else {
      throw new UsernameNotFoundException(email);
    }

  }

  private String[] getRoles(UserEntity userObj) {
    if (userObj.getRole().isEmpty()) {
      return new String[] { "USER" };
    } else {
      return userObj.getRole().split(",");
    }
  }

}
