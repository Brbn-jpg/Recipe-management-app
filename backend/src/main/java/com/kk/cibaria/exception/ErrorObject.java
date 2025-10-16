package com.kk.cibaria.exception;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
public class ErrorObject {
  private int statusCode;
  private String message;
  private Date timestamp = new Date();
}
