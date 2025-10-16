package com.kk.cibaria.security.jwt;

import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.crypto.SecretKey;

import com.kk.cibaria.user.UserEntity;
import com.kk.cibaria.user.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

  @Value("${SECRET_KEY}")
  private String secretKey;
  private static final long EXPIRATIONTIME = TimeUnit.HOURS.toMillis(72);

  private final UserRepository userRepository;

  public JwtService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public String generateToken(UserDetails userDetails) {
    UserEntity user =
            userRepository.findByEmail(userDetails.getUsername()).orElseThrow(()->new UsernameNotFoundException("User not found"));
    Map<String, Object> claims = new HashMap<>();
    claims.put("provider", "kkBackend");
    claims.put("id", String.valueOf(user.getId()));
    claims.put("roles", user.getRole().split(","));

    return Jwts.builder()
        .claims(claims)
        .subject(userDetails.getUsername())
        .issuedAt(Date.from(Instant.now()))
        .expiration(Date.from(Instant.now().plusMillis(EXPIRATIONTIME)))
        .signWith(generateKey())
        .compact();
  }

  public SecretKey generateKey() {
    byte[] key = Base64.getDecoder().decode(secretKey);
    return Keys.hmacShaKeyFor(key);
  }

  public String extractUsername(String jwt) {
    Claims claims = getClaims(jwt);
    return claims.getSubject();
  }

  public int extractId(String jwt){
    Claims claims = getClaims(jwt);
    return Integer.parseInt(claims.get("id").toString());
  }

  private Claims getClaims(String jwt) {
    Claims claims = Jwts.parser()
        .verifyWith(generateKey())
        .build()
        .parseSignedClaims(jwt)
        .getPayload();
    return claims;
  }

  public boolean isTokenValid(String jwt) {
    Claims claims = getClaims(jwt);
    return claims.getExpiration().after(Date.from(Instant.now()));
  }

  public boolean hasRole(String jwt, String role) {
    Claims claims = getClaims(jwt);
    @SuppressWarnings("unchecked")
    java.util.List<String> roles = (java.util.List<String>) claims.get("roles");
    if (roles != null) {
      return roles.contains(role);
    }
    return false;
  }

}
