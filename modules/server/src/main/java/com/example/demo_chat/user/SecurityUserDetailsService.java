package com.example.demo_chat.user;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * Calls the reactive {@link UserRepository} (R2DBC) directly for Spring Security's reactive
 * authentication manager - no blocking bridge needed.
 */
@Service
@RequiredArgsConstructor
public class SecurityUserDetailsService implements ReactiveUserDetailsService {

  private final UserRepository userRepository;

  @Override
  public Mono<UserDetails> findByUsername(String login) {
    return userRepository.findByLogin(login).map(UserPrincipal::new);
  }
}
