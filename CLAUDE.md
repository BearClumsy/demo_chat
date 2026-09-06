# CLAUDE.md

Guidance for Claude Code (claude.ai/code) working in this repo — an implemented RAG chat app:
reactive Spring Boot backend + React frontend.

## Contents

- [Common tasks](#common-tasks) — the command cheat-sheet
- [Project status](#project-status) — what's built, what's pending
- [Orientation](#orientation) — module layout, package map, request path, API surface, storage
- [Conventions](#conventions) — Java / test / migration / intent / client rules
- [Configuration](#configuration) — the Spring-profile split and its cross-file contract
- [Build & toolchain](#build--toolchain)
- [Infrastructure](#infrastructure) — lint-only, never applied
- [Gotchas](#gotchas) — the non-obvious footguns
- [Knowledge sources](#knowledge-sources) — graphify graph + Obsidian wiki
- [Keeping this file current](#keeping-this-file-current)

## Common tasks

Run from the repo root. The `Makefile` wraps everything (`make help` for the full list); the
underlying commands are in the sections below.

| Task | Command |
|---|---|
| Start local infra (Postgres, Cassandra, Qdrant) | `make up` |
| Run the backend | `./gradlew :server:bootRun` (or `make run`) |
| Run all backend tests | `./gradlew :server:test` |
| Format backend code | `./gradlew :server:spotlessApply` |
| Smoke-test the RAG chat end to end | `make verify-chat` |
| Run the client dev server | `cd modules/client && npm run dev` (or `make client-dev`) |

Tests and Testcontainers need a running Docker daemon; they do **not** need AWS credentials.

## Project status

An implemented RAG chat app. Per-phase status — what's done, what's outstanding — lives in
`docs/wiki/Plan/roadmap.md` (the single source of truth; don't restate it here). In short: user
management + Spring Security auth, authenticated chat with participant validation and Cassandra
message history, and the 8-stage RAG pipeline with a semantic-cache short-circuit are implemented
and wired. Outstanding: the AWS half of Phase 3 — Terraform apply, ECR, and the deploy workflows
(all currently lint-only).

Fuller prose overview: `README.md`. Pipeline design: `docs/wiki/Plan/rag-pipeline.md`.

## Orientation

### Module layout

Multi-module Gradle build, modules under `modules/`. There is **no root `build.gradle`** —
plugins/dependencies live in each module's own `build.gradle`, wired via the root `settings.gradle`.

- `modules/server` — the Spring Boot backend: all Java source, `application*.properties`, Flyway
  migrations, the local `docker-compose.yml`. `src/main/...` paths below are relative to here.
- `modules/client` — React 19 + TypeScript + Vite frontend. Talks to `/api/**`; the Vite dev
  server proxies `/api` to `http://localhost:8080` (`vite.config.ts`). `settings.gradle` does
  `include 'client'` but `modules/client/build.gradle` is a placeholder comment — build/run/lint
  it with `npm`, not `./gradlew`.

### Server package map

Package-by-feature under `com.example.demo_chat`:

- `config/` — `SecurityConfig` (WebFlux security), `ChatClientConfig`, `PasswordEncoderConfig`,
  and `SemanticCacheVectorStoreConfig`, which declares the **second** Qdrant `VectorStore` bean
  (qualified `semanticCacheVectorStore`) alongside the autoconfigured knowledge-base one.
- `common/` — `ValidationExceptionHandler`, the single `@RestControllerAdvice`.
- `user/` — R2DBC `User`/`UserRepository`, `UserService`, `UserController`,
  `SecurityUserDetailsService` + `UserPrincipal`, request/response records.
- `chat/` — `ChatController` (JSON + SSE), `ChatService`, Cassandra `ChatHistory` and the
  `ChatMessage` UDT + `ChatHistoryRepository`, DTO records.
- `rag/` — `ChatPipelineService` plus the eight stage classes, `SemanticCacheService`,
  `IntentDefinitionRegistry`/`IntentDefinition`, `KnowledgeBaseIndexer` (an `ApplicationRunner`),
  Cassandra `DialogueState`/`DialogueStateRepository`/`DialogueStatus`, `TextChunker`, and the
  value records `IntentClassification`, `GroundednessCheck`, `AssembledPrompt`.

The pipeline, each stage numbered in its javadoc: 1 `QueryNormalizationService` →
2 `KnowledgeRetrievalService` → 3 `IntentClassificationService` → 4 `ScopeFilter` →
5 `SlotFillingService` → 6 `PromptBuilder` → 7 `AnswerGenerationService` → 8 `ResponseValidator`
(output-side groundedness guardrail). The semantic-cache lookup is **not** a numbered stage — it
runs between normalization and retrieval and short-circuits the rest of the turn on a hit.

### Request path

`POST /api/chats/{chatId}/messages[/stream]` → `ChatController` →
`ChatService.getChatForParticipant` (participant guard: 404 unknown chat, 403 non-participant,
applied uniformly to *every* `/{chatId}/**` route, including `addParticipant`) →
`ChatPipelineService.handleMessage` / `handleMessageStream` → the numbered stages → persist to
Cassandra `chat_history` + `dialogue_state`. JSON replies return `SendMessageResponse{reply,
status}`; SSE emits `token` events then one `done` event carrying the `DialogueStatus` name.

### API surface

HTTP Basic on everything except `POST /api/users` and `/actuator/health(/**)`. CSRF and formLogin
are disabled (`config/SecurityConfig.java`).

- `POST /api/users` (201; 409 on duplicate email/login) · `GET /api/users/{id}`
- `POST /api/chats` — 403 if the request's `currentUserId` ≠ the authenticated principal
- `POST /api/chats/{chatId}/participants` — caller must already be a participant; takes the
  caller's id as its own parameter rather than trusting the request body
- `POST /api/chats/{chatId}/messages` → `SendMessageResponse{reply, status}`
- `POST /api/chats/{chatId}/messages/stream` → SSE `token` events, then one `done` event

### Storage split

**Both** chat history and dialogue state are Cassandra. Postgres holds only `users`. Qdrant holds
two vector collections (`support_kb` for retrieval, `semantic_cache` for the cache short-circuit).
When adding persistent state, decide whether it's chat memory (Cassandra) or vector data (Qdrant)
before defaulting to Postgres.

## Conventions

### Java

- **`var`** for locals when the right-hand side already names the type (see the global CLAUDE.md
  for the rationale).
- **Jackson 3, not Jackson 2.** The `rag` records import `tools.jackson.databind.*`, not
  `com.fasterxml.jackson.*` — a copied Jackson 2 snippet will not compile.
- DTOs are `record`s. Lombok is available (`compileOnly` + annotation processor).
- Formatted with **Spotless + Google Java Format**: `./gradlew :server:spotlessApply` before
  committing; `:server:spotlessCheck` is what CI runs.
- Reactive `Mono`/`Flux` endpoints and outbound calls. Bedrock and Qdrant calls bridge through
  `Schedulers.boundedElastic()` — neither has a reactive-native client in this Spring AI version.
  That is the correct way to wrap blocking I/O, not a shortcut; only Postgres had a real reactive
  alternative (R2DBC) and was migrated. Don't "fix" the bridging.
- One `@RestControllerAdvice` — `common/ValidationExceptionHandler`.
- Pipeline stage classes carry a numbered javadoc; keep the numbering consistent when adding or
  reordering stages.
- Kafka is on the classpath and `spring.kafka.bootstrap-servers` is set in every profile, but
  there is **zero** producer/consumer/listener code. It's aspirational — not an integration point.

### Tests

- JUnit 5. Require a running Docker daemon (`DemoChatApplicationTests` starts Postgres, Cassandra,
  Qdrant via Testcontainers; `UserRepositoryTest` starts Postgres alone) but **never** AWS
  credentials — `application-test.properties` sets `spring.ai.model.{chat,embedding}=none` and the
  tests supply stub Bedrock beans.
- The stub Bedrock `ChatModel`/`EmbeddingModel` beans live in a nested
  `@TestConfiguration StubBedrockModels` inside `DemoChatApplicationTests`, not in a shared support
  class — a new `@SpringBootTest` must reuse or re-declare them.
- Current set: `DemoChatApplicationTests` (Testcontainers context load + intent registration),
  `ChatPipelineServiceTest` (routing, collaborators mocked), `ResponseValidatorTest`,
  `SemanticCacheServiceTest`, `TextChunkerTest`, `ChatControllerStreamTest` (SSE slice),
  `ChatServiceValidateParticipantIdsTest`, `UserRepositoryTest` (`@DataR2dbcTest` + Postgres).
- Single class / method: `./gradlew :server:test --tests
  "com.example.demo_chat.DemoChatApplicationTests"` /
  `"...DemoChatApplicationTests.contextLoads"`.

### Migrations

- **Postgres**: Flyway, `V<version>__description.sql` under `src/main/resources/db/migration`. The
  app migrates on startup; standalone `./gradlew :server:flywayMigrate` / `:server:flywayInfo`
  also work (`flyway {}` block in `modules/server/build.gradle` — read the comments there before
  targeting staging/prod). **Never edit an applied migration** — a changed checksum fails
  `flyway validate` on startup; add a new one.
- **Cassandra has no migration tool.** `spring.cassandra.schema-action` is `create-if-not-exists`
  locally / in tests but `${CASSANDRA_SCHEMA_ACTION:none}` in staging/prod, so new Cassandra
  tables need DDL applied by hand. `schema-action` does **not** create the keyspace — locally the
  `cassandra-init` one-shot in `local/docker-compose.yml` does (on `make up`); staging/prod need
  it created out of band.

### Intents

Intent JSON under `knowledge-base/intents/*.json`. `node scripts/validate-intents.mjs` enforces
(mirrors the `IntentDefinition` record — keep the two in sync when adding a field):

- `intent_id` must equal the filename stem.
- Every `{placeholder}` in `answer_template` must be listed in `required_slots`.
- No two *different* files may claim the same canonical question (case-insensitively; repeats
  *within* one file are deliberately allowed).
- Unknown fields are rejected.

### Client

- Package-by-feature: `src/features/auth`, `src/features/chat/{api,components,hooks,types}`,
  shared app-level state in `src/app/AuthContext.tsx`.
- **Auth is in-memory only** — `AuthContext.tsx` holds `{userId, login, password}` and builds a
  Basic header; no login endpoint, no persistence, so a refresh logs you out. No router either —
  `App.tsx` switches screens with `useState`.
- SSE behind nginx: `nginx.conf.template` proxies `/api/` to `${BACKEND_URL}` (default
  `http://server:8080`) with `proxy_buffering off` — that flag is what keeps streaming working in
  the container image.

## Configuration

Split by Spring Profile under `src/main/resources/`. Open the relevant file for the actual keys;
what matters here is the cross-file contract:

- `application.properties` — environment-independent only (Bedrock model IDs, Qdrant collection
  names, the `demo-chat.{rag,guardrail,cache,streaming}.*` tuning props — kebab-case,
  `@Value("${...:default}")`, no `@ConfigurationProperties` class). Sets
  `spring.profiles.default=local` and pins `spring.ai.model.{chat,embedding}` explicitly (needed
  once a second model starter is on the classpath).
- `application-local.properties` — `localhost` details matching `local/docker-compose.yml`.
- `application-staging.properties` / `application-prod.properties` — the same keys bound to env
  vars. These two **duplicate each other on purpose**: `spring.config.import` of a shared file
  does not take effect from a profile-specific document and fails silently to Boot's defaults.
  Secrets (`POSTGRES_PASSWORD`, `CASSANDRA_PASSWORD`, `QDRANT_API_KEY`) have **no default** so a
  missing one fails startup instead of falling back to a dev value.
- `application-offline.properties` — deltas only, activated as `local,offline`
  (`make run-offline`); points `spring.ai.model.{chat,embedding}` at a local Ollama server so the
  app runs with no AWS credentials.

When adding a connection setting, add it to **all** the environment files, not just
`application.properties`. Each Terraform env's `container_env` output and the `demo-chat-config`
ConfigMap in `infra/k8s/manifest-*.yaml` must stay in sync with this env-var contract.

Bedrock credentials never live in these files (AWS credential chain). Startup needs Bedrock unless
the Qdrant collections already exist, because creating one calls the embedding model for its
dimensions (`spring.ai.vectorstore.qdrant.initialize-schema` governs this for both stores).

## Build & toolchain

- **Java 25** via the Gradle toolchain (`modules/server/build.gradle`) — let the wrapper
  provision it; the system JDK may not match. Always use `./gradlew`, never a system Gradle.
- Spring Boot 4 and Spring AI 2 as BOMs; exact dependency versions in
  `modules/server/build.gradle` (managed via `io.spring.dependency-management`). Root package
  `com.example.demo_chat`.
- Server tasks: `./gradlew :server:build` · `:server:bootRun` · `:server:test` ·
  `:server:spotlessCheck` / `:server:spotlessApply` · `:server:flywayMigrate` /
  `:server:flywayInfo` · `:server:downloadDependencySources` · `./gradlew clean`.
  `make sources` wraps `downloadDependencySources`. `make run-offline`
  needs `make up-offline` first — the containerised Ollama needs a Docker VM of ≥ ~12 GiB
  (`make colima-offline` sizes it) or `llama3.1` OOM-kills on the first chat turn; a native
  `ollama serve` on the host also works and is faster on Apple Silicon.
- `make verify-chat` wraps `scripts/verify-chat.sh` (health check → `POST /api/chats` →
  `POST /api/chats/{id}/messages` as the seeded `testuser`, asserts a non-empty reply);
  `BASE_URL` / `LOGIN` / `PASSWORD` / `USER_ID` / `MESSAGE` override the defaults.
- Client (from `modules/client/`, `npm install` first): `npm run dev` · `npm run lint`
  (ESLint 10, flat config in `eslint.config.js`) · `npm run build` (type-check + build) ·
  `npm run preview`. No test runner yet.
- Container images — build context is the **repo root** for the server:
  `docker build -f modules/server/Dockerfile -t demo-chat-server .` and
  `docker build -t demo-chat-client modules/client`.

## Infrastructure

`infra/terraform/` (AWS staging/prod IaC) and `infra/k8s/` (app manifests) are **lint-only** —
there is no AWS account, nothing has ever been applied. Deploy target is **Kubernetes (kubeadm on
EC2), not ECS**. Details: `infra/terraform/README.md`, `docs/wiki/Plan/kubernetes.md`.

- Lint the way CI does: `make tf-lint` (needs `terraform`/`tofu` + `tflint`), `make k8s-lint`
  (needs `kubeconform` + `kubectl` + `shellcheck`).
- The `ecs-service`, `alb`, and `bedrock-iam` Terraform modules are **retained as lint-clean
  references** but no longer instantiated — do **not** delete or gut them (`tflint` flags
  `terraform_unused_declarations`).
- `envs/staging` and `envs/prod` are duplicated on purpose (same rationale as the duplicated
  `application-{staging,prod}.properties`).
- `deploy-staging.yml` / `deploy-prod.yml` are skeletons — they reference GitHub Environment
  `vars.*` that only exist once Terraform is applied. The KB bootstrap `Job` runs the server image
  with `--reindex-and-exit` to seed Qdrant `support_kb` (staging/prod keep
  `reindex-on-startup=false`).

## Gotchas

- **`ChatHistory`'s primary key is misnamed.** `@PrimaryKey("user_id")` on the `chat_history`
  table actually holds the **chat** id (`chat/ChatHistory.java`).
- **`demo-chat.rag.reindex-on-startup` is the one `demo-chat.*` key not in
  `application.properties`.** Its code default is `true` (in `rag/KnowledgeBaseIndexer`); it is
  set per-profile instead — `true` in local, `false` in staging, prod, and test.
- **`offline` profile uses 768-dim embeddings** (`nomic-embed-text`) vs Bedrock Titan's 1024.
  Switching a machine between `make run` and `make run-offline` against the same Qdrant volume
  fails on insert (collection dimension mismatch) — `make nuke` then `make up` / `make up-offline`
  between the two.
- **Flyway is configured in two places and they must agree** — Boot's startup migration
  (`spring.flyway.schemas` in the profile properties) and the `flyway {}` block in `build.gradle`
  (backs the standalone `flywayMigrate`/`flywayInfo` tasks). Divergent `schemas` values build
  separate `flyway_schema_history` tables that re-run each other's migrations. Staging/prod resolve
  the schema from `${POSTGRES_SCHEMA}` while the Gradle block defaults to `demo_chat`, so migrating
  against them needs an explicit `FLYWAY_SCHEMAS` / `-Pflyway.schemas`. Full explanation is in the
  `build.gradle` comments.
- **`DialogueStatus.READY_TO_ANSWER` is declared but never assigned** by the pipeline — don't
  route on it.
- **A fixed test user is seeded locally by a Flyway migration outside `db/migration`.**
  `local/db/migration/R__seed_local_test_user.sql` (repeatable, idempotent `INSERT ... ON CONFLICT
  (login) DO NOTHING`) creates login `testuser` / password `password`, id
  `00000000-0000-0000-0000-000000000001`. Picked up **only locally** — `spring.flyway.locations`
  lists `classpath:local/db/migration` in `application-local.properties` but not staging/prod, and
  the `build.gradle` `flyway {}` block adds the `filesystem:` equivalent only when targeting the
  default local DB. Applied on startup with the rest, or via `make migrate`.
- **Client auth / routing** — see [Conventions › Client](#client): in-memory only, no router.
- **Client SSE in the container image** — see [Conventions › Client](#client): the
  `proxy_buffering off` flag in `nginx.conf.template`.

## Knowledge sources

Two navigable sources beyond the code:

- `graphify-out/` — auto-generated code graph (god nodes, communities, cross-file relationships).
  Answers **where** something lives and **what connects to it**, not what the code does. Reach for
  it for architecture / relationship / ripple questions, when the answer spans docs *and* code
  (grep can't link `docs/wiki/Plan/rag-pipeline.md` to `ChatPipelineService.java` — no shared
  string), or when you don't yet know which files matter. Go straight to Grep/Read when you know
  the file, want a literal string / symbol / config key, or are editing specific lines.
  - `graphify query "<question>"`, `graphify path "<A>" "<B>"`, `graphify explain "<concept>"`.
  - `graphify-out/GRAPH_REPORT.md` only for a broad architecture review.
  - **Keep it current:** `graphify update .` after changing code (AST-only, 0 tokens). A full
    `/graphify .` rebuild re-runs semantic extraction over `docs/` and costs real tokens — don't
    rebuild casually; `graphify-out/cost.json` tracks spend per run.
- `docs/wiki/` — an Obsidian vault of curated project knowledge (requirements, decisions, notes),
  distinct from graphify's auto-generated graph. Structure: `Features/<name>.md`,
  `Infrastructure/{Kafka,Postgres,Cassandra,Qdrant}/<resource>.md`, `Daily/<YYYY-MM-DD>.md` (each
  folder has a `_template.md` to copy); `Plan/<topic>.md` is architecture / roadmap-level
  (`Plan/README.md` is its sub-index, `Plan/roadmap.md` tracks per-phase status); `index.md` is
  the MOC entry point. Use the Obsidian CLI (`obsidian ...`) for reading / searching / writing
  notes — it works through the Obsidian runtime and updates links, front matter, and indexes
  correctly. To place a topic you can't yet position: `obsidian search query="<term>"`,
  `obsidian links <note>` / `obsidian backlinks <note>`.

## Keeping this file current

When you change one of these, update the matching section here:

- `modules/server/build.gradle` — Build & toolchain, and the Flyway gotcha.
- `application*.properties` — Configuration.
- `config/SecurityConfig.java` — API surface (auth rules).
- `rag/` stage classes — the pipeline list in the server package map.
- `scripts/validate-intents.mjs` / the `IntentDefinition` record — Conventions › Intents.
- `infra/**` READMEs — Infrastructure.
- Phase / status changes go in `docs/wiki/Plan/roadmap.md`, **not** here.
