package com.example.demo_chat.user;

import java.util.UUID;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;

/** Reactive Spring Data R2DBC repository for {@link User}. */
public interface UserRepository extends R2dbcRepository<User, UUID> {

  Mono<User> findByLogin(String login);
}
