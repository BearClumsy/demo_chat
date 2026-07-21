package com.example.demo_chat.user;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/** Calls the reactive {@link UserRepository} (R2DBC) directly - no blocking bridge needed. */
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
    return userRepository.findById(id).map(UserResponse::from);
  }

  /**
   * Hashes the password and persists a new user.
   *
   * @param request the new user's details
   * @return the created user
   */
  public Mono<UserResponse> createUser(CreateUserRequest request) {
    return userRepository.save(toNewUser(request)).map(UserResponse::from);
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
