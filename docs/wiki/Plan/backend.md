# Java Backend: Project Structure

[← Back to README](README.md) · [Architecture overview](overview.md)

Stack: **Spring Boot 4.0.7 + Spring WebFlux + Spring AI 2.0.0 + Spring Security + Spring Data R2DBC
(Postgres) + Flyway (JDBC, migrations only) + Spring Data Cassandra (reactive) + Kafka**.
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
│   │   │   ├── ChatController.java             # POST /api/chats, POST /api/chats/{id}/participants,
│   │   │   │                                    #   POST /api/chats/{id}/messages,
│   │   │   │                                    #   POST /api/chats/{id}/messages/stream (SSE)
│   │   │   ├── ChatService.java                 # startChat(), addParticipant(), getChatForParticipant()
│   │   │   ├── ChatHistory.java                  # Cassandra @Table("chat_history"), PK = user_id
│   │   │   ├── ChatMessage.java                   # Cassandra @UserDefinedType, frozen list on ChatHistory
│   │   │   ├── ChatHistoryRepository.java          # ReactiveCassandraRepository<ChatHistory, UUID>
│   │   │   ├── MessageRequest.java                  # record: userId, message, datetime
│   │   │   ├── ParticipantRequest.java               # record: userId
│   │   │   ├── StartChatRequest.java                  # record: currentUserId, participantIds, title, message
│   │   │   ├── SendMessageRequest.java                 # record: message (userId comes from the principal)
│   │   │   └── SendMessageResponse.java                 # record: reply, status
│   │   │
│   │   ├── user/                               # user feature (package-by-feature)
│   │   │   ├── UserController.java              # GET /api/users/{id}, POST /api/users
│   │   │   ├── UserService.java                  # calls the reactive R2DBC repository directly
│   │   │   ├── User.java                          # Spring Data Relational @Table("users"), UUID PK
│   │   │   ├── UserRepository.java                  # R2dbcRepository<User, UUID>
│   │   │   ├── UserPrincipal.java                    # Spring Security UserDetails adapter over User
│   │   │   ├── SecurityUserDetailsService.java        # loads UserPrincipal by login
│   │   │   ├── CreateUserRequest.java                  # record: firstName, lastName, email, phone, login, password
│   │   │   └── UserResponse.java                        # response DTO, excludes password
│   │   │
│   │   ├── rag/                                # RAG pipeline feature (package-by-feature, flat — see
│   │   │   │                                    #   "Rationale" below for why this isn't service.<layer>.X)
│   │   │   ├── ChatPipelineService.java          # orchestrator: normalize → retrieve → classify →
│   │   │   │                                    #   scope-check → slot-fill → generate → guardrail;
│   │   │   │                                    #   persists DialogueState and appends to ChatHistory;
│   │   │   │                                    #   handleMessageStream() for SSE (buffer-then-chunk)
│   │   │   ├── QueryNormalizationService.java     # stage 1: raw message -> clean query (ChatClient)
│   │   │   ├── KnowledgeRetrievalService.java      # stage 2: VectorStore.similaritySearch(), top-K
│   │   │   ├── IntentClassificationService.java     # stage 3: ChatClient .entity(IntentClassification)
│   │   │   ├── IntentClassification.java             # record: intentId, confidence (LLM structured output)
│   │   │   ├── ScopeFilter.java                       # stage 4: threshold + whitelist -> in/out of scope
│   │   │   ├── SlotFillingService.java                 # stage 5: IntentDefinition.requiredSlots() vs slots
│   │   │   ├── PromptBuilder.java                       # stage 6: SYSTEM/CONTEXT/USER STATE/USER MESSAGE
│   │   │   ├── AssembledPrompt.java                      # record: system, user
│   │   │   ├── AnswerGenerationService.java               # stage 7: final answer + clarifying questions
│   │   │   ├── ResponseValidator.java                      # stage 8: output-side groundedness guardrail
│   │   │   ├── GroundednessCheck.java                       # record: grounded, reasoning (LLM structured output)
│   │   │   ├── SemanticCacheService.java                     # semantic cache: lookup()/store() against a
│   │   │   │                                                 #   second Qdrant collection (semantic_cache)
│   │   │   ├── TextChunker.java                              # splits a validated answer into SSE token chunks
│   │   │   ├── IntentDefinition.java                       # record loaded from knowledge-base/intents/*.json
│   │   │   ├── IntentDefinitionRegistry.java                # loads *.json at startup into Map<id, IntentDefinition>
│   │   │   ├── KnowledgeBaseIndexer.java                     # ApplicationRunner: pushes intents into Qdrant
│   │   │   ├── DialogueState.java                             # Cassandra @Table("dialogue_state"), PK = chat_id
│   │   │   ├── DialogueStatus.java                             # enum: NEW/SLOT_FILLING/READY_TO_ANSWER/
│   │   │   │                                                    #   ANSWERED/OUT_OF_SCOPE/ESCALATED
│   │   │   └── DialogueStateRepository.java                     # ReactiveCassandraRepository<DialogueState, UUID>
│   │   │
│   │   ├── common/
│   │   │   └── ValidationExceptionHandler.java   # @RestControllerAdvice — app-wide error mapping
│   │   │                                          # (bean validation -> 400, IllegalArgumentException -> 400,
│   │   │                                          #  AccessDeniedException -> 403)
│   │   │
│   │   └── config/
│   │       ├── SecurityConfig.java               # @EnableWebFluxSecurity, HTTP Basic, permits POST /api/users
│   │       ├── PasswordEncoderConfig.java          # BCryptPasswordEncoder bean
│   │       ├── ChatClientConfig.java                # ChatClient bean over the autoconfigured Bedrock ChatModel
│   │       └── SemanticCacheVectorStoreConfig.java   # second, qualified VectorStore bean for semantic_cache
│   │
│   └── resources/
│       ├── application.properties                # single properties file — no Spring Profiles yet;
│       │                                          #   spring.datasource.* (JDBC, Flyway only) and
│       │                                          #   spring.r2dbc.* (app data access) both point at Postgres
│       ├── db/migration/
│       │   └── V1__create_users_table.sql         # Flyway migration for demo_chat.users
│       ├── knowledge-base/intents/
│       │   └── *.json                              # 4 intents (refund_status, order_status,
│       │                                            #   change_shipping_address, password_reset) — see
│       │                                            #   vector-store-schema.md
│       └── local/
│           └── docker-compose.yml                 # postgres, cassandra, qdrant, kafka (local dev stack)
│
└── test/
    └── java/com/example/demo_chat/
        ├── DemoChatApplicationTests.java          # context-load smoke test
        ├── user/UserRepositoryTest.java            # R2DBC slice test (Testcontainers Postgres)
        ├── chat/ChatServiceValidateParticipantIdsTest.java
        ├── chat/ChatControllerStreamTest.java       # SSE endpoint slice test
        └── rag/                                     # ResponseValidator, SemanticCacheService, TextChunker,
                                                       #   ChatPipelineService unit tests
```

RAG pipeline services (Phase 1 + 2: normalize → retrieve → classify → scope-check → slot-fill → generate
→ guardrail, plus the semantic cache and SSE streaming) are implemented in the `rag` package above. An
`api`/`service`/`orchestration`/`domain`/`infrastructure` layered split was never adopted (see Rationale
below). See [rag-pipeline.md](rag-pipeline.md) for the stage-by-stage design.

## Rationale for the current layers

| Package | Responsibility |
|---|---|
| `chat` | Chat creation, participant management, and sending messages (incl. SSE streaming) — controller, service, Cassandra entity/repository, request DTOs |
| `user` | User identity — controller, service, R2DBC entity/repository, Spring Security integration, request/response DTOs |
| `rag` | RAG pipeline — flat package holding every pipeline stage, the knowledge-base registry/indexer, and dialogue state, rather than the `service.normalization.X`/`service.retrieval.X`/… layered naming in the original design notes (see below) |
| `common` | Cross-cutting concerns not specific to one feature (currently just validation/error handling) |
| `config` | Spring beans that aren't tied to one feature (security filter chain, password encoder, chat client) |

Package-by-feature (`chat/`, `user/`, `rag/`) was a deliberate choice over package-by-layer
(`controller/`, `service/`, `repository/`) — see the Decisions section of the `User` and `Chat` feature
notes in the Obsidian vault (`Features/User.md`, `Features/Chat.md`) for the reasoning. `rag/` follows the
same convention: [rag-pipeline.md](rag-pipeline.md)'s original stage-to-service table used a
`service.<layer>.ClassName` layered naming, but that was never adopted — every RAG class lives flat in
`rag/`, same as `chat/` and `user/`.

## Reactive boundaries

- `chat/*` is reactive end-to-end: `ReactiveCassandraRepository` + `Mono`/`Flux` throughout
  `ChatController`/`ChatService`.
- `user/*` is now reactive end-to-end too (Phase 2): migrated from blocking JPA/JDBC to **R2DBC**
  (`R2dbcRepository`), so `UserService`/`SecurityUserDetailsService` call the repository directly with
  no `Schedulers.boundedElastic()` bridge. Postgres still needs a blocking JDBC `DataSource` for Flyway
  migrations only (`spring-boot-starter-jdbc`), separate from the R2DBC `ConnectionFactory` the app uses.
- `ChatService.validateParticipantIds()` was rewritten for `Flux<User>`/`Set` semantics against the
  reactive `UserRepository.findAllById(...)` — no bridging needed either.
- `rag/*`'s Bedrock/Qdrant calls **remain** on the `Schedulers.boundedElastic()` bridging pattern: the
  Bedrock `ChatClient` calls (`QueryNormalizationService`, `IntentClassificationService`,
  `AnswerGenerationService`, `ResponseValidator`) and the Qdrant `VectorStore` calls
  (`KnowledgeRetrievalService`, `SemanticCacheService`) are all blocking under the hood — neither has a
  reactive-native client in this Spring AI version, so every call site wraps them in
  `Mono.fromCallable(...).subscribeOn(Schedulers.boundedElastic())`. This is an accepted, deliberate
  scope boundary (see [roadmap.md](roadmap.md) Phase 2), not an oversight.

## Related documents

- [Architecture overview](overview.md)
- [RAG pipeline](rag-pipeline.md) — implemented (Phase 1 + 2); describes the actual service layer
- [Local ↔ AWS mapping](local-vs-aws.md)