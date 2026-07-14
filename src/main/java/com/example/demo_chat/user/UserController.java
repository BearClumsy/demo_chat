package com.example.demo_chat.user;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/** REST endpoints for reading and creating {@link User}s. */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

  private final UserService userService;

  /**
   * @param id the user id
   * @return 200 with the user, or 404 if no user has this id
   */
  @GetMapping("/{id}")
  public Mono<ResponseEntity<UserResponse>> getUserById(@PathVariable UUID id) {
    return userService
        .getUserById(id)
        .map(ResponseEntity::ok)
        .defaultIfEmpty(ResponseEntity.notFound().build());
  }

  /**
   * @param request the new user's details
   * @return 201 with the created user and a {@code Location} header, or 409 if the email or login
   *     is already taken
   */
  @PostMapping
  public Mono<ResponseEntity<UserResponse>> createUser(
      @Valid @RequestBody CreateUserRequest request) {
    return userService
        .createUser(request)
        .map(
            response ->
                ResponseEntity.created(URI.create("/api/users/" + response.id())).body(response));
  }

  /** Maps a unique-constraint violation (duplicate email/login) to 409 Conflict. */
  @ExceptionHandler(DataIntegrityViolationException.class)
  @ResponseStatus(HttpStatus.CONFLICT)
  public void handleDuplicateUser() {}
}
