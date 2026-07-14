package com.example.demo_chat.user;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** Spring Data JPA repository for {@link User}. */
public interface UserRepository extends JpaRepository<User, UUID> {

  Optional<User> findByLogin(String login);
}
