package com.example.demo_chat.common;

import java.util.LinkedHashMap;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;

@RestControllerAdvice
public class ValidationExceptionHandler {

  @ExceptionHandler(WebExchangeBindException.class)
  public ProblemDetail handleValidationException(WebExchangeBindException ex) {
    var fieldErrors = new LinkedHashMap<String, String>();
    for (var fieldError : ex.getFieldErrors()) {
      fieldErrors.put(fieldError.getField(), fieldError.getDefaultMessage());
    }

    var problemDetail =
        ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Validation failed");
    problemDetail.setProperty("errors", fieldErrors);
    return problemDetail;
  }

  /** Maps invalid chat participants (self-chat or unknown user ids) to 400 Bad Request. */
  @ExceptionHandler(IllegalArgumentException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public void handleInvalidChatParticipants() {}

  /** Maps a {@code currentUserId} that doesn't match the authenticated user to 403 Forbidden. */
  @ExceptionHandler(AccessDeniedException.class)
  @ResponseStatus(HttpStatus.FORBIDDEN)
  public void handleCurrentUserMismatch() {}
}
