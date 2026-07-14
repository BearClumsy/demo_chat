package com.example.demo_chat.user;

import java.util.List;
import java.util.UUID;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * {@link UserDetails} view of a {@link User}. Exposes the user's id so authenticated request
 * handlers can use it without a second lookup.
 */
public class UserPrincipal implements UserDetails {

  private final User user;

  public UserPrincipal(User user) {
    this.user = user;
  }

  public UUID getId() {
    return user.getId();
  }

  @Override
  public List<GrantedAuthority> getAuthorities() {
    return List.of();
  }

  @Override
  public String getPassword() {
    return user.getPassword();
  }

  @Override
  public String getUsername() {
    return user.getLogin();
  }
}
