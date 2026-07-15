# demo_chat

A reactive Spring Boot backend for a RAG-based support chatbot with scoped intent matching. The
long-term goal (see [`docs/wiki/Plan/`](docs/wiki/Plan/README.md)) is for retrieval-augmented
generation to be the single source of truth for both which topics the bot may answer and the answers
themselves — keeping the model from responding outside an approved knowledge base.

## Current status

Phases 1 and 2 of the roadmap are implemented:

- **User management & auth** — registration and Spring Security integration, backed by Postgres via
  R2DBC (reactive end-to-end, no blocking JPA bridge).
- **Chat** — authenticated chat creation with participant validation, message history persisted to
  Cassandra, and SSE-streamed replies (buffer-then-chunk) alongside the plain JSON endpoint.
- **RAG pipeline (8 stages)** — normalize → semantic-cache check → retrieve → classify intent →
  slot-fill → generate → output-side groundedness guardrail, wired through `ChatPipelineService`.
  Retrieval and the semantic cache are both backed by Qdrant (`support_kb` and `semantic_cache`
  collections); Bedrock/Qdrant calls remain on `Schedulers.boundedElastic()` bridging, a deliberate
  scope boundary since neither has a reactive-native client in this Spring AI version.

Not yet built: the React frontend, AWS deployment, and CI/CD. See
[`docs/wiki/Plan/roadmap.md`](docs/wiki/Plan/roadmap.md) for the phased plan.

## Tech stack

- **Java 26**, provisioned via the Gradle toolchain (don't assume your system JDK matches).
- **Spring Boot 4.0.7** with **Spring AI 2.0.0** (BOM).
- **Reactive web**: WebFlux + WebClient — endpoints and outbound calls use `Mono`/`Flux`, not servlet APIs.
- **LLM**: Amazon Bedrock — `anthropic.claude-3-5-haiku-20241022-v1:0` for chat, `amazon.titan-embed-text-v2:0`
  for embeddings.
- **Vector search**: Qdrant — `support_kb` (knowledge base) and `semantic_cache` (Phase 2 semantic
  cache) collections, via `spring-ai-vector-store-advisor`.
- **Chat history**: Cassandra, accessed reactively.
- **Relational data** (users): Postgres via R2DBC for the app; a blocking JDBC `DataSource` is kept
  solely for Flyway migrations under `src/main/resources/db/migration`.
- **Messaging**: Kafka.
- **Security**: Spring Security.
- **Observability**: Actuator.

## Project structure

Single Gradle module, package-by-feature under `com.example.demo_chat`:

```
src/main/java/com/example/demo_chat/
├── DemoChatApplication.java
├── chat/     # ChatController (incl. SSE /messages/stream), ChatService, ChatHistory (Cassandra), DTOs
├── user/     # UserController, UserService, User (Postgres/R2DBC), auth principal & repository
├── rag/      # RAG pipeline: QueryNormalizationService, KnowledgeRetrievalService,
│             # IntentClassificationService, SlotFillingService, PromptBuilder,
│             # AnswerGenerationService, ResponseValidator, SemanticCacheService, TextChunker,
│             # orchestrated by ChatPipelineService
├── config/   # SecurityConfig, PasswordEncoderConfig, ChatClientConfig, SemanticCacheVectorStoreConfig
└── common/   # ValidationExceptionHandler

src/main/resources/
├── application.properties     # Postgres (JDBC for Flyway + R2DBC for the app), Cassandra, Bedrock, Qdrant, Kafka
├── db/migration/              # Flyway migrations (V1__create_users_table.sql)
├── knowledge-base/intents/     # RAG knowledge base source docs (per-intent JSON)
└── local/docker-compose.yml   # Postgres, Cassandra, Qdrant, Kafka for local dev
```

## Getting started

Start local dependencies (Postgres, Cassandra, Qdrant, Kafka):

```bash
docker compose -f src/main/resources/local/docker-compose.yml up -d
```

You'll also need AWS credentials with Bedrock access for chat completions and embeddings (the app
doesn't provision or mock Bedrock locally).

Run the app:

```bash
./gradlew bootRun
```

## Commands

Use the Gradle wrapper (`./gradlew`), not a system-installed Gradle.

- Build: `./gradlew build`
- Run the app: `./gradlew bootRun`
- Run all tests: `./gradlew test`
- Run a single test class: `./gradlew test --tests "com.example.demo_chat.DemoChatApplicationTests"`
- Clean build output: `./gradlew clean`

Tests use JUnit 5: the generated context-load test, an R2DBC repository slice test (Testcontainers
Postgres), a `ChatService` unit test, RAG pipeline unit tests (guardrail, semantic cache), and a
`ChatController` SSE endpoint slice test. Note: the context-load test needs real (or placeholder)
AWS credentials and reachable Cassandra/Postgres/Qdrant to pass — see "Getting started" above.

## Documentation

- [`docs/wiki/`](docs/wiki/index.md) — Obsidian vault of curated project knowledge: feature notes
  (`Features/`), infrastructure notes (`Infrastructure/`), and the architecture/roadmap plan
  (`docs/wiki/Plan/`).
- [`graphify-out/`](graphify-out/GRAPH_REPORT.md) — auto-generated code graph for navigating
  cross-file relationships (`graphify query "<question>"`).
