# Java Backend: Project Structure

[← Back to README](README.md) · [Architecture overview](overview.md)

Stack: **Spring Boot 4.0.7 + Spring WebFlux + Spring AI 2.0.0 + Spring Security + Spring Data JPA
(Postgres/Flyway) + Spring Data Cassandra (reactive) + Kafka**.
Build tool: Gradle, **Groovy DSL** (`build.gradle`, not Kotlin DSL). Java toolchain: **26**. Lombok is
used throughout for entity/DTO boilerplate.

This is a single Gradle module at the repo root — not a `backend/` subdirectory (there's no separate
frontend or infra module in this repo; see [README.md](README.md)).

## Actual package tree

```
src/
├── main/
│   ├── java/com/example/demo_chat/
│   │   ├── DemoChatApplication.java
│   │   │
│   │   ├── chat/                              # chat feature (package-by-feature)
│   │   │   ├── ChatController.java             # POST /api/chats, POST /api/chats/{id}/participants
│   │   │   ├── ChatService.java                 # startChat(), addParticipant()
│   │   │   ├── ChatHistory.java                  # Cassandra @Table("chat_history"), PK = user_id
│   │   │   ├── ChatMessage.java                   # Cassandra @UserDefinedType, frozen list on ChatHistory
│   │   │   ├── ChatHistoryRepository.java          # ReactiveCassandraRepository<ChatHistory, UUID>
│   │   │   ├── MessageRequest.java                  # record: userId, message, datetime
│   │   │   ├── ParticipantRequest.java               # record: userId
│   │   │   └── StartChatRequest.java                  # record: currentUserId, participantIds, title, message
│   │   │
│   │   ├── user/                               # user feature (package-by-feature)
│   │   │   ├── UserController.java              # GET /api/users/{id}, POST /api/users
│   │   │   ├── UserService.java                  # blocking JPA bridged via Schedulers.boundedElastic()
│   │   │   ├── User.java                          # JPA @Entity, table demo_chat.users, UUID PK
│   │   │   ├── UserRepository.java                  # Spring Data JPA repository
│   │   │   ├── UserPrincipal.java                    # Spring Security UserDetails adapter over User
│   │   │   ├── SecurityUserDetailsService.java        # loads UserPrincipal by login
│   │   │   ├── CreateUserRequest.java                  # record: firstName, lastName, email, phone, login, password
│   │   │   └── UserResponse.java                        # response DTO, excludes password
│   │   │
│   │   ├── common/
│   │   │   └── ValidationExceptionHandler.java   # @RestControllerAdvice — app-wide error mapping
│   │   │                                          # (bean validation -> 400, IllegalArgumentException -> 400,
│   │   │                                          #  AccessDeniedException -> 403)
│   │   │
│   │   └── config/
│   │       ├── SecurityConfig.java               # @EnableWebFluxSecurity, HTTP Basic, permits POST /api/users
│   │       └── PasswordEncoderConfig.java          # BCryptPasswordEncoder bean
│   │
│   └── resources/
│       ├── application.properties                # single properties file — no Spring Profiles yet
│       ├── db/migration/
│       │   └── V1__create_users_table.sql         # Flyway migration for demo_chat.users
│       └── local/
│           └── docker-compose.yml                 # postgres, cassandra, qdrant, kafka (local dev stack)
│
└── test/
    └── java/com/example/demo_chat/
        └── DemoChatApplicationTests.java          # context-load smoke test only
```

Not present yet (kept as planned design, not current structure): an `api`/`service`/`orchestration`/
`domain`/`infrastructure` layered split, RAG pipeline services (normalization, retrieval, scope filter,
slot filling, prompt builder, generation, guardrail), a `ChatOrchestrator`, `IntentDefinitionRegistry`,
`DialogueState`/`DialogueStateService`, or a `knowledge-base/` resource tree. See
[rag-pipeline.md](rag-pipeline.md) for that target design once the RAG feature is built.

## Rationale for the current layers

| Package | Responsibility |
|---|---|
| `chat` | Chat creation and participant management — controller, service, Cassandra entity/repository, request DTOs |
| `user` | User identity — controller, service, JPA entity/repository, Spring Security integration, request/response DTOs |
| `common` | Cross-cutting concerns not specific to one feature (currently just validation/error handling) |
| `config` | Spring beans that aren't tied to one feature (security filter chain, password encoder) |

Package-by-feature (`chat/`, `user/`) was a deliberate choice over package-by-layer
(`controller/`, `service/`, `repository/`) — see the Decisions section of the `User` and `Chat` feature
notes in the Obsidian vault (`Features/User.md`, `Features/Chat.md`) for the reasoning.

## Reactive boundaries

- `chat/*` is reactive end-to-end: `ReactiveCassandraRepository` + `Mono`/`Flux` throughout
  `ChatController`/`ChatService`.
- `user/*` sits on **blocking** JPA/JDBC (no R2DBC driver in this project). `UserService` bridges each
  repository call with `Mono.fromCallable(...).subscribeOn(Schedulers.boundedElastic())` so it doesn't
  block the WebFlux event loop.
- `ChatService.validateParticipantIds()` similarly bridges a blocking JPA lookup (`UserRepository`) onto
  `Mono` the same way, since chat participant validation depends on the (blocking) user store.

## Related documents

- [Architecture overview](overview.md)
- [RAG pipeline](rag-pipeline.md) — planned, describes the target service layer
- [Local ↔ AWS mapping](local-vs-aws.md)