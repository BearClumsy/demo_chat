package com.example.demo_chat.user;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * Bridges the blocking {@link UserRepository} onto a {@link Mono} for Spring Security's reactive
 * authentication manager, the same pattern {@link UserService} uses for request handling.
 */
@Service
@RequiredArgsConstructor
public class SecurityUserDetailsService implements ReactiveUserDetailsService {

  private final UserRepository userRepository;

  @Override
  public Mono<UserDetails> findByUsername(String login) {
    return Mono.fromCallable(() -> userRepository.findByLogin(login))
        .subscribeOn(Schedulers.boundedElastic())
        .flatMap(Mono::justOrEmpty)
        .map(UserPrincipal::new);
  }
}
