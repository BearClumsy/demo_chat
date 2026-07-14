package com.example.demo_chat.user;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * Bridges the blocking {@link UserRepository} (JPA/JDBC) onto reactive {@link Mono}s by running
 * each call on {@link Schedulers#boundedElastic()}, so it doesn't block the WebFlux event loop.
 */
@Service
@RequiredArgsConstructor
public class UserService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  /**
   * @param id the user id
   * @return the user, or an empty {@link Mono} if none exists with this id
   */
  public Mono<UserResponse> getUserById(UUID id) {
    return Mono.fromCallable(() -> userRepository.findById(id))
        .subscribeOn(Schedulers.boundedElastic())
        .flatMap(Mono::justOrEmpty)
        .map(UserResponse::from);
  }

  /**
   * Hashes the password and persists a new user.
   *
   * @param request the new user's details
   * @return the created user
   */
  public Mono<UserResponse> createUser(CreateUserRequest request) {
    return Mono.fromCallable(() -> userRepository.save(toNewUser(request)))
        .subscribeOn(Schedulers.boundedElastic())
        .map(UserResponse::from);
  }

  private User toNewUser(CreateUserRequest request) {
    return User.builder()
        .firstName(request.firstName())
        .lastName(request.lastName())
        .email(request.email())
        .phone(request.phone())
        .login(request.login())
        .password(passwordEncoder.encode(request.password()))
        .build();
  }
}
