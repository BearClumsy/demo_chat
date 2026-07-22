package com.example.demo_chat.user;

import static org.assertj.core.api.Assertions.assertThat;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.r2dbc.test.autoconfigure.DataR2dbcTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.test.StepVerifier;

@Testcontainers
@DataR2dbcTest
class UserRepositoryTest {

  @Container
  static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

  @DynamicPropertySource
  static void r2dbcProperties(DynamicPropertyRegistry registry) {
    registry.add(
        "spring.r2dbc.url",
        () ->
            "r2dbc:postgresql://%s:%d/%s?schema=demo_chat"
                .formatted(
                    POSTGRES.getHost(), POSTGRES.getFirstMappedPort(), POSTGRES.getDatabaseName()));
    registry.add("spring.r2dbc.username", POSTGRES::getUsername);
    registry.add("spring.r2dbc.password", POSTGRES::getPassword);
  }

  @BeforeAll
  static void migrate() {
    Flyway.configure()
        .dataSource(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword())
        .schemas("demo_chat")
        .load()
        .migrate();
  }

  @Autowired private UserRepository userRepository;

  @Test
  void savedUserCanBeFoundById() {
    var user = newUser("jane.doe", "jane@example.com");

    userRepository
        .save(user)
        .as(StepVerifier::create)
        .assertNext(saved -> assertThat(saved.getId()).isNotNull())
        .verifyComplete();

    userRepository
        .findByLogin("jane.doe")
        .as(StepVerifier::create)
        .assertNext(found -> assertThat(found.getEmail()).isEqualTo("jane@example.com"))
        .verifyComplete();
  }

  @Test
  void findByLoginReturnsEmptyWhenNoUserMatches() {
    userRepository.findByLogin("nobody").as(StepVerifier::create).verifyComplete();
  }

  private User newUser(String login, String email) {
    return User.builder()
        .firstName("Jane")
        .lastName("Doe")
        .email(email)
        .phone("555-0100")
        .login(login)
        .password("hashed-password")
        .build();
  }
}
