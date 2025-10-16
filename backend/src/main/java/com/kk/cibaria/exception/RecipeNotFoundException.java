package com.kk.cibaria.exception;

public class RecipeNotFoundException extends RuntimeException {
  public RecipeNotFoundException(String message) {
    super(message);
  }
}
