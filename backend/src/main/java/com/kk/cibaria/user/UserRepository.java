package com.kk.cibaria.user;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<UserEntity, Integer> {
  Optional<UserEntity> findByUsername(String username);
  Optional<UserEntity> findByEmail(String email);
}
