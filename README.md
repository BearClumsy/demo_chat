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

Phase 3 is half done. Spring Profiles (`local`/`staging`/`prod`), Dockerfiles for both modules, and
GitHub Actions CI (`backend-ci`, `frontend-ci`, `knowledge-base-lint`) are in place; the AWS side —
Terraform, ECR, and the deploy workflows — is still outstanding and needs an account. See
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
  solely for Flyway migrations under `modules/server/src/main/resources/db/migration`.
- **Messaging**: Kafka.
- **Security**: Spring Security.
- **Observability**: Actuator.

## Project structure

Multi-module Gradle build, split under `modules/`:

- `modules/server` — the Spring Boot backend described in this README (package-by-feature under
  `com.example.demo_chat`)
- `modules/client` — React 19 + TypeScript + Vite frontend (auth + chat screens against the endpoints
  above). Built with `npm`, not Gradle — its `build.gradle` is still a placeholder.

```
modules/server/src/main/java/com/example/demo_chat/
├── DemoChatApplication.java
├── chat/     # ChatController (incl. SSE /messages/stream), ChatService, ChatHistory (Cassandra), DTOs
├── user/     # UserController, UserService, User (Postgres/R2DBC), auth principal & repository
├── rag/      # RAG pipeline: QueryNormalizationService, KnowledgeRetrievalService,
│             # IntentClassificationService, SlotFillingService, PromptBuilder,
│             # AnswerGenerationService, ResponseValidator, SemanticCacheService, TextChunker,
│             # orchestrated by ChatPipelineService
├── config/   # SecurityConfig, PasswordEncoderConfig, ChatClientConfig, SemanticCacheVectorStoreConfig
└── common/   # ValidationExceptionHandler

modules/server/src/main/resources/
├── application.properties            # environment-independent: model ids, collection names, demo-chat.* tuning
├── application-local.properties      # the docker-compose stack (default profile)
├── application-staging.properties    # AWS staging, every value from an env var
├── application-prod.properties       # AWS prod
├── db/migration/                     # Flyway migrations (V1__create_users_table.sql)
├── knowledge-base/intents/           # RAG knowledge base source docs (per-intent JSON)
└── local/docker-compose.yml          # Postgres, Cassandra, Qdrant, Kafka for local dev
```

## Getting started

Start local dependencies (Postgres, Cassandra, Qdrant, Kafka):

```bash
make up   # or: docker compose -f modules/server/src/main/resources/local/docker-compose.yml up -d
```

You'll also need AWS credentials with Bedrock access for chat completions and embeddings (the app
doesn't provision or mock Bedrock locally). Startup itself needs them too, unless the Qdrant
collections already exist: creating a collection asks the embedding model for its dimensions, which is
a live Bedrock call.

Run the app (no profile set → `local`):

```bash
make run   # or: ./gradlew :server:bootRun
```

To run against a deployed environment's configuration instead, set `SPRING_PROFILES_ACTIVE=staging`
(or `prod`) and supply `POSTGRES_*`, `CASSANDRA_*`, `QDRANT_*`, and `KAFKA_BOOTSTRAP_SERVERS` — those
profiles read every host and credential from the environment and fail fast if a secret is missing.

## Commands

The root `Makefile` wraps all of these — `make help` lists them (`make up`, `make run`, `make test`,
`make client-dev`, `make docker`, `make ci`, …). It's a thin alias layer over the tools below, so
either form works.

Use the Gradle wrapper (`./gradlew`), not a system-installed Gradle. Backend tasks live under the
`:server` module.

- Build: `./gradlew :server:build`
- Run the app: `./gradlew :server:bootRun`
- Run all tests: `./gradlew :server:test`
- Run a single test class: `./gradlew :server:test --tests "com.example.demo_chat.DemoChatApplicationTests"`
- Check/apply formatting: `./gradlew :server:spotlessCheck` / `./gradlew :server:spotlessApply`
- Clean build output: `./gradlew clean`

Frontend (from `modules/client/`): `npm run dev`, `npm run lint`, `npm run build`.

Knowledge base: `node scripts/validate-intents.mjs` checks the intent JSON files the same way CI does.

Container images:

```bash
docker build -f modules/server/Dockerfile -t demo-chat-server .   # context is the repo root
docker build -t demo-chat-client modules/client
```

Tests use JUnit 5: a full-context wiring test, an R2DBC repository slice test, a `ChatService` unit
test, RAG pipeline unit tests (guardrail, semantic cache, chunking), and a `ChatController` SSE slice
test. They need **Docker** (Testcontainers starts Postgres, Cassandra, and Qdrant) but **not** AWS
credentials — Bedrock is stubbed. If your Docker daemon isn't at the default socket (Colima, Rancher),
export `DOCKER_HOST` and `TESTCONTAINERS_DOCKER_SOCKET_OVERRIDE=/var/run/docker.sock` first.

## Documentation

- [`docs/wiki/`](docs/wiki/index.md) — Obsidian vault of curated project knowledge: feature notes
  (`Features/`), infrastructure notes (`Infrastructure/`), and the architecture/roadmap plan
  (`docs/wiki/Plan/`).
- [`graphify-out/`](graphify-out/GRAPH_REPORT.md) — auto-generated code graph for navigating
  cross-file relationships (`graphify query "<question>"`).
