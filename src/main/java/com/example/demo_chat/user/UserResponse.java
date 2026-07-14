package com.example.demo_chat.user;

import java.util.UUID;

/** API representation of a {@link User}. Deliberately omits the password. */
public record UserResponse(
    UUID id, String firstName, String lastName, String email, String phone, String login) {

  /** Maps a persisted {@link User} to its API representation. */
  public static UserResponse from(User user) {
    return new UserResponse(
        user.getId(),
        user.getFirstName(),
        user.getLastName(),
        user.getEmail(),
        user.getPhone(),
        user.getLogin());
  }
}
