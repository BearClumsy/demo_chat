package com.example.demo_chat.common;

import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;

@RestControllerAdvice
public class ValidationExceptionHandler {

  @ExceptionHandler(WebExchangeBindException.class)
  public ProblemDetail handleValidationException(WebExchangeBindException ex) {
    Map<String, String> fieldErrors = new LinkedHashMap<>();
    for (FieldError fieldError : ex.getFieldErrors()) {
      fieldErrors.put(fieldError.getField(), fieldError.getDefaultMessage());
    }

    ProblemDetail problemDetail =
        ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Validation failed");
    problemDetail.setProperty("errors", fieldErrors);
    return problemDetail;
  }
}
