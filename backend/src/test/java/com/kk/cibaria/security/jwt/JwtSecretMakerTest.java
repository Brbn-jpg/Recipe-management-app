package com.kk.cibaria.security.jwt;

import javax.crypto.SecretKey;

import org.junit.jupiter.api.Test;

import io.jsonwebtoken.Jwts;
import jakarta.xml.bind.DatatypeConverter;

public class JwtSecretMakerTest {

  @Test
  public void getSecretKey() {
    SecretKey key = Jwts.SIG.HS512.key().build();
    String decodedKey = DatatypeConverter.printHexBinary(key.getEncoded());
    System.out.println(decodedKey);
  }

}
