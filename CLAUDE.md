# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project status

This project has moved past scaffolding. Phases 1 and 2 of the roadmap (see
`docs/wiki/Plan/roadmap.md`) are implemented: user management + Spring Security auth (Postgres via
R2DBC), authenticated chat creation with participant validation and message history (Cassandra), and an
8-stage RAG pipeline — normalize → semantic-cache check → retrieve → classify intent → slot-fill →
generate → output-side groundedness guardrail — orchestrated by `ChatPipelineService`. Retrieval and the
semantic cache are both backed by Qdrant (`support_kb` and `semantic_cache` collections). Replies are
available either as a plain JSON response or SSE-streamed (buffer-then-chunk, not live token generation)
via `POST /api/chats/{chatId}/messages/stream`. See `README.md` for a fuller overview.

A React chat MVP now exists under `modules/client` (auth + chat screens against the endpoints above), but
it is not wired into the Gradle build yet — see "Module layout" below. Phase 3a is also done: Spring
Profiles (`local`/`staging`/`prod`), Dockerfiles for both modules, and GitHub Actions CI (`backend-ci`,
`frontend-ci`, `knowledge-base-lint` under `.github/workflows/`). Still outstanding: the AWS half of
Phase 3 — Terraform, ECR, and the deploy workflows. Bedrock/Qdrant calls remain on `Schedulers.boundedElastic()` bridging (a deliberate, accepted
scope boundary — neither has a reactive-native client in this Spring AI version); only Postgres/JPA had a
real reactive alternative (R2DBC) and has been migrated. Test coverage now includes an R2DBC repository
slice test, a `ChatService` unit test, RAG pipeline unit tests (guardrail, semantic cache, text chunking,
groundedness validation), and a `ChatController` SSE slice test, alongside the original context-load test.

## Module layout

This is a multi-module Gradle build, with modules under `modules/`:

- `modules/server` — the Spring Boot backend (all Java source, `application.properties`, Flyway
  migrations, the local docker-compose file). This is what most of this document describes.
  Paths quoted below (`src/main/...`) are relative to `modules/server/`.
- `modules/client` — React 19 + TypeScript + Vite frontend (package-by-feature: `src/features/auth`,
  `src/features/chat/{api,components,hooks,types}`, shared app-level state in `src/app/AuthContext.tsx`).
  Talks to the backend's `/api/**` endpoints; the Vite dev server proxies `/api` to
  `http://localhost:8080` (see `vite.config.ts`). It is a real app now, but its `build.gradle` is still
  just a placeholder comment — it is **not** wired into the Gradle multi-module build, so build/run/test
  it directly with `npm`, not `./gradlew`.

The root `build.gradle` no longer exists — plugins/dependencies live in each module's own
`build.gradle`, wired together via the root `settings.gradle`.

## Commands

### Server

Use the Gradle wrapper (`./gradlew`), not a system-installed Gradle. Backend tasks run against the
`:server` module.

- Build: `./gradlew :server:build`
- Run the app: `./gradlew :server:bootRun`
- Run all tests: `./gradlew :server:test`
- Run a single test class: `./gradlew :server:test --tests "com.example.demo_chat.DemoChatApplicationTests"`
- Run a single test method: `./gradlew :server:test --tests "com.example.demo_chat.DemoChatApplicationTests.contextLoads"`
- Check formatting (Spotless + Google Java Format): `./gradlew :server:spotlessCheck`
- Apply formatting: `./gradlew :server:spotlessApply`
- Clean build output: `./gradlew clean`

- Build the container image: `docker build -f modules/server/Dockerfile -t demo-chat-server .` (build
  context is the **repository root**, not `modules/server`, because the Gradle wrapper lives there)

Tests use JUnit 5 (`useJUnitPlatform()` is configured in `modules/server/build.gradle`). They require a
running Docker daemon — `DemoChatApplicationTests` and `UserRepositoryTest` start Postgres, Cassandra,
and Qdrant via Testcontainers — but **not** AWS credentials: `application-test.properties` sets
`spring.ai.model.{chat,embedding}=none` and the test supplies stub Bedrock beans. If Docker isn't at
the default socket (e.g. Colima), export `DOCKER_HOST` and
`TESTCONTAINERS_DOCKER_SOCKET_OVERRIDE=/var/run/docker.sock`.

### Client

Not a Gradle task — run these from `modules/client/` directly (`npm install` first if `node_modules`
is missing):

- Run the dev server (proxies `/api` to the backend on `:8080`): `npm run dev`
- Lint (ESLint 9 flat config in `eslint.config.js`): `npm run lint`
- Type-check and build: `npm run build`
- Preview a production build: `npm run preview`
- Build the container image (nginx + static build): `docker build -t demo-chat-client modules/client`

No test runner is configured for the client yet.

### Knowledge base

- Validate the intent JSON files the way CI does: `node scripts/validate-intents.mjs`. Its rules mirror
  the `IntentDefinition` record, so keep the two in sync when adding a field.

## Toolchain

- Java 26 (via Gradle toolchain in `modules/server/build.gradle` — do not assume the system JDK
  matches; let the wrapper provision it).
- Spring Boot 4.0.7, with dependency versions managed via `io.spring.dependency-management`.
- Spring AI 2.0.0, imported as a BOM (`springAiVersion` in `modules/server/build.gradle`).
- Server code is formatted with Spotless + Google Java Format (`modules/server/build.gradle`) — see
  `spotlessCheck`/`spotlessApply` under Commands.
- Root package: `com.example.demo_chat`.
- Client: React 19, TypeScript, Vite 6 (`modules/client/package.json`).

## Architecture (from declared dependencies)

The dependency set backs a RAG-style chat application with the following shape. Most of this is now
implemented (see "Project status" above); where a piece is still pending, it's called out below.

- **Reactive web layer**: `spring-boot-starter-webflux` + `spring-boot-starter-webclient` — endpoints and
  outbound HTTP calls are expected to be reactive (`Mono`/`Flux`), not servlet-based.
- **LLM access**: `spring-ai-starter-model-bedrock` — chat completions go through Amazon Bedrock.
- **Vector search**: `spring-ai-starter-vector-store-qdrant` + `spring-ai-vector-store-advisor` — retrieval-
  augmented generation is backed by a Qdrant vector store, wired in via a Spring AI advisor rather than
  manual retrieval calls.
- **Document ingestion**: `spring-ai-markdown-document-reader` — source documents for the vector store are
  expected to be Markdown, chunked/loaded through Spring AI's ETL pipeline.
- **Chat memory**: `spring-ai-starter-model-chat-memory-repository-cassandra` — conversation history is
  persisted in Cassandra, accessed reactively (`spring-boot-starter-data-cassandra-reactive`).
- **Relational persistence**: `spring-boot-starter-data-r2dbc` + `r2dbc-postgresql` for the app's
  reactive Postgres access, plus `spring-boot-starter-jdbc` + `postgresql` driver + `spring-boot-starter-
  flyway` (with `flyway-database-postgresql`) purely so Flyway has a blocking `DataSource` to run
  migrations with — any relational schema (e.g. app/user data distinct from vector or chat-memory
  storage) is Postgres, version-controlled via Flyway migrations. Migration files belong under
  `src/main/resources/db/migration` following Flyway's `V<version>__description.sql` naming.
- **Messaging**: `spring-boot-starter-kafka` — expect async event production/consumption alongside the
  synchronous chat API. Not yet wired up.
- **Observability**: `spring-boot-starter-actuator` — health/metrics endpoints are expected to be enabled.
- Lombok is available (`compileOnly` + annotation processor) for reducing boilerplate on entities/DTOs.

Because R2DBC/Postgres, Cassandra, and Qdrant are all present, expect three distinct data stores serving
different purposes (transactional data, chat memory/dialogue state, vector embeddings — including a
second Qdrant collection, `semantic_cache`, alongside the knowledge-base `support_kb` collection) rather
than a single database — don't default to putting new persistent state in R2DBC/Postgres without checking
whether it's chat memory or vector data instead.

## Configuration

Configuration is split by Spring Profile under `src/main/resources/`:

- `application.properties` — environment-independent only: Bedrock model IDs, Qdrant collection names,
  and the `demo-chat.rag.*`, `demo-chat.guardrail.*`, `demo-chat.cache.*`, `demo-chat.streaming.*`
  tuning properties (kebab-case, `@Value("${...:default}")` injection, no `@ConfigurationProperties`
  class). It also sets `spring.profiles.default=local`, so `bootRun` behaves as it always has.
- `application-local.properties` — the `localhost` connection details matching
  `src/main/resources/local/docker-compose.yml`.
- `application-staging.properties` / `application-prod.properties` — the same keys bound to environment
  variables. Secrets (`${POSTGRES_PASSWORD}`, `${CASSANDRA_PASSWORD}`, `${QDRANT_API_KEY}`) have **no
  default** so a missing one fails startup instead of falling back to a dev value. These two files
  duplicate each other on purpose: `spring.config.import` of a shared file does not take effect from a
  profile-specific document, and it fails silently by falling back to Boot's defaults.

When adding a connection setting, add it to all three environment files, not just `application.properties`.

Bedrock credentials are never in these files — they come from the AWS credential chain. Note that
startup itself needs Bedrock unless the Qdrant collections already exist, because creating a collection
calls the embedding model for its dimensions; `spring.ai.vectorstore.qdrant.initialize-schema` governs
this for both the `support_kb` and `semantic_cache` stores.

## Knowledge Sources

This project has two navigable knowledge sources — prefer them over raw Read/grep:

- `graphify-out/` — auto-generated code graph (god nodes, communities, cross-file relationships)
- `docs/wiki/` — Obsidian vault of manually/AI-curated project knowledge (requirements, decisions,
  notes), distinct from graphify's auto-generated graph

### graphify

- Use `graphify query "<question>"` when `graphify-out/graph.json` exists. Use
  `graphify path "<A>" "<B>"` for relationships and `graphify explain "<concept>"` for focused
  concepts. These return a scoped subgraph, usually much smaller than GRAPH_REPORT.md or raw grep
  output.
- Read `graphify-out/GRAPH_REPORT.md` only for broad architecture review or when
  query/path/explain do not surface enough context.
- After modifying code, run `graphify update .` to keep the graph current (AST-only, no API cost).

### Working with this Vault

This is an Obsidian vault. For reading/searching/writing notes, use the Obsidian CLI
(`obsidian ...`) rather than directly reading `.md` files through Read — the CLI works through the
Obsidian runtime and correctly updates links, front matter, and indexes.

Structure: `Features/<name>.md`, `Infrastructure/{Kafka,Postgres,Cassandra,Qdrant}/<resource>.md`,
`Daily/<YYYY-MM-DD>.md` — each of these folders has a `_template.md` to copy from. `Plan/<topic>.md` is
architecture/roadmap-level documentation (system overview, RAG pipeline design, phased roadmap) rather
than per-resource notes, so it has no template; `Plan/README.md` is its own sub-index and
`Plan/roadmap.md` tracks what's implemented per phase. `index.md` (vault root) is the overall MOC
entry point.

Before manually grepping files, first try:
- `graphify query "<question>"` — broad context/connections on a topic
- `obsidian search query="<term>"` — exact search by headings/tags
- `obsidian links <note>` / `obsidian backlinks <note>` — link graph of a single note
