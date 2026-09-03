# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project status

This project has moved past scaffolding. Phases 1 and 2 of the roadmap (see
`docs/wiki/Plan/roadmap.md`) are implemented: user management + Spring Security auth (Postgres via
R2DBC), authenticated chat creation with participant validation and message history (Cassandra), and an
8-stage RAG pipeline orchestrated by `ChatPipelineService`. Each stage class carries its number in its
javadoc: 1 `QueryNormalizationService` → 2 `KnowledgeRetrievalService` → 3 `IntentClassificationService`
→ 4 `ScopeFilter` → 5 `SlotFillingService` → 6 `PromptBuilder` → 7 `AnswerGenerationService` →
8 `ResponseValidator` (output-side groundedness guardrail). The semantic-cache lookup is not a numbered
stage — it runs between normalization and retrieval and short-circuits the rest of the turn on a hit.
Retrieval and the semantic cache are both backed by Qdrant (`support_kb` and `semantic_cache`
collections). Replies are available either as a plain JSON response or SSE-streamed (buffer-then-chunk,
not live token generation) via `POST /api/chats/{chatId}/messages/stream`. See `README.md` for a fuller
overview.

A React chat MVP now exists under `modules/client` (auth + chat screens against the endpoints above), but
it is not wired into the Gradle build yet — see "Module layout" below. Phase 3a is also done: Spring
Profiles (`local`/`staging`/`prod`), Dockerfiles for both modules, and GitHub Actions CI (`backend-ci`,
`frontend-ci`, `knowledge-base-lint` under `.github/workflows/`). Still outstanding: the AWS half of
Phase 3 — Terraform, ECR, and the deploy workflows. Bedrock/Qdrant calls remain on `Schedulers.boundedElastic()` bridging (a deliberate, accepted
scope boundary — neither has a reactive-native client in this Spring AI version); only Postgres/JPA had a
real reactive alternative (R2DBC) and has been migrated.

The full test set (`modules/server/src/test/java/com/example/demo_chat/`):
`DemoChatApplicationTests` (Testcontainers context load + intent registration), `ChatPipelineServiceTest`
(pipeline routing with all collaborators mocked), `ResponseValidatorTest`, `SemanticCacheServiceTest`,
`TextChunkerTest`, `ChatControllerStreamTest` (SSE slice), `ChatServiceValidateParticipantIdsTest`, and
`UserRepositoryTest` (`@DataR2dbcTest` + Postgres container).

## Module layout

This is a multi-module Gradle build, with modules under `modules/`:

- `modules/server` — the Spring Boot backend (all Java source, `application.properties`, Flyway
  migrations, the local docker-compose file). This is what most of this document describes.
  Paths quoted below (`src/main/...`) are relative to `modules/server/`.
- `modules/client` — React 19 + TypeScript + Vite frontend (package-by-feature: `src/features/auth`,
  `src/features/chat/{api,components,hooks,types}`, shared app-level state in `src/app/AuthContext.tsx`).
  Talks to the backend's `/api/**` endpoints; the Vite dev server proxies `/api` to
  `http://localhost:8080` (see `vite.config.ts`). `settings.gradle` does `include 'client'`, but
  `modules/client/build.gradle` is a one-line placeholder comment — the module is registered with no
  build logic, so build/run/lint it with `npm`, not `./gradlew`.

The root `build.gradle` no longer exists — plugins/dependencies live in each module's own
`build.gradle`, wired together via the root `settings.gradle`.

### Server package map

Server code is package-by-feature under `com.example.demo_chat`:

- `config/` — `SecurityConfig` (WebFlux security), `ChatClientConfig`, `PasswordEncoderConfig`, and
  `SemanticCacheVectorStoreConfig`, which declares the **second** Qdrant `VectorStore` bean (qualified
  `semanticCacheVectorStore`) alongside the autoconfigured knowledge-base one.
- `common/` — `ValidationExceptionHandler`, the single `@RestControllerAdvice`.
- `user/` — R2DBC `User`/`UserRepository`, `UserService`, `UserController`, `SecurityUserDetailsService`
  + `UserPrincipal`, request/response records.
- `chat/` — `ChatController` (JSON + SSE), `ChatService`, Cassandra `ChatHistory` and the `ChatMessage`
  UDT + `ChatHistoryRepository`, DTO records.
- `rag/` — the pipeline: `ChatPipelineService` plus the eight stage classes listed above,
  `SemanticCacheService`, `IntentDefinitionRegistry`/`IntentDefinition`, `KnowledgeBaseIndexer` (an
  `ApplicationRunner`), Cassandra `DialogueState`/`DialogueStateRepository`/`DialogueStatus`,
  `TextChunker`, and the value records `IntentClassification`, `GroundednessCheck`, `AssembledPrompt`.

Note the storage split this implies: **both** chat history and dialogue state are Cassandra. Postgres
holds only `users`.

### API surface

Everything is HTTP Basic authenticated except `POST /api/users` and `/actuator/health(/**)`; CSRF and
formLogin are disabled (`config/SecurityConfig.java`).

- `POST /api/users` (201, 409 on duplicate email/login) · `GET /api/users/{id}`
- `POST /api/chats` — 403 if the request's `currentUserId` doesn't match the authenticated principal
- `POST /api/chats/{chatId}/participants` — the caller must already be a participant
- `POST /api/chats/{chatId}/messages` → `SendMessageResponse{reply, status}`
- `POST /api/chats/{chatId}/messages/stream` → SSE `token` events, then one `done` event carrying the
  `DialogueStatus` name

Every `/{chatId}/**` endpoint routes through `ChatService.getChatForParticipant`, so non-participants
get 403 (and unknown chat ids 404) uniformly — including `addParticipant`, which takes the caller's id
as its own parameter rather than trusting the request body.

## Commands

The root `Makefile` wraps everything below in one entry point — run `make help` for the list (e.g.
`make up`, `make run`, `make test`, `make client-dev`, `make ci`, `make verify-chat`). It is a thin
alias layer only; the
underlying commands are still the ones documented here, and new commands should be added to both.

### Server

Use the Gradle wrapper (`./gradlew`), not a system-installed Gradle. Backend tasks run against the
`:server` module.

- Build: `./gradlew :server:build`
- Run the app: `./gradlew :server:bootRun`
- Run the app with no AWS: `make run-offline` (profile `local,offline` — chat + embeddings come from
  Ollama on `:11434`; run `make up-offline` first, which starts the Ollama container and pulls its
  models). On Apple Silicon the container is CPU-only; a native `ollama serve` on the host also works
  and is faster. The containerised path needs a Docker VM of **≥ ~12 GiB** — the default 2 GiB Colima
  VM OOM-kills `llama3.1` on the first chat turn (`ggml_aligned_malloc: insufficient memory` at
  `QueryNormalizationService`); fix with `colima stop && colima start --cpu 6 --memory 16`, or the
  `make colima-offline` alias for the same.
- Smoke-test the RAG chat end to end against a running backend: `make verify-chat` (wraps
  `scripts/verify-chat.sh` — health check, then `POST /api/chats` + `POST /api/chats/{id}/messages`
  as the seeded `testuser`, asserting a non-empty reply). `BASE_URL` / `LOGIN` / `PASSWORD` /
  `USER_ID` / `MESSAGE` env vars override the defaults.
- Run all tests: `./gradlew :server:test`
- Run a single test class: `./gradlew :server:test --tests "com.example.demo_chat.DemoChatApplicationTests"`
- Run a single test method: `./gradlew :server:test --tests "com.example.demo_chat.DemoChatApplicationTests.contextLoads"`
- Check formatting (Spotless + Google Java Format): `./gradlew :server:spotlessCheck`
- Apply formatting: `./gradlew :server:spotlessApply`
- Clean build output: `./gradlew clean`
- Download dependency source/javadoc jars into the Gradle cache: `./gradlew :server:downloadDependencySources`
  (or `make sources`). IDE-driven fetch is already on via the `eclipse` plugin's `downloadSources`.
- Apply Flyway migrations without starting the app: `./gradlew :server:flywayMigrate`
- Show applied/pending migration state: `./gradlew :server:flywayInfo`

The app migrates on startup too, so `bootRun` covers the normal case; the two `flyway*` tasks exist
for applying/inspecting the schema on its own. They default to the local Postgres and are overridden
with `-Pflyway.url=…` / `-Pflyway.user=…` / `-Pflyway.password=…` / `-Pflyway.schemas=…` or the
matching `FLYWAY_*` environment variables (`flyway {}` block in `modules/server/build.gradle`).
Staging and prod set `spring.flyway.schemas` from `POSTGRES_SCHEMA`, so pass
`FLYWAY_SCHEMAS=$POSTGRES_SCHEMA` when targeting them — see the Gotchas entry below.

- Build the container image: `docker build -f modules/server/Dockerfile -t demo-chat-server .` (build
  context is the **repository root**, not `modules/server`, because the Gradle wrapper lives there)

Tests use JUnit 5 (`useJUnitPlatform()` is configured in `modules/server/build.gradle`). They require a
running Docker daemon — `DemoChatApplicationTests` starts Postgres, Cassandra, and Qdrant via
Testcontainers, and `UserRepositoryTest` starts Postgres alone — but **not** AWS credentials: `application-test.properties` sets
`spring.ai.model.{chat,embedding}=none` and the test supplies stub Bedrock beans. If Docker isn't at
the default socket (e.g. Colima), export `DOCKER_HOST` and
`TESTCONTAINERS_DOCKER_SOCKET_OVERRIDE=/var/run/docker.sock`.

### Client

Not a Gradle task — run these from `modules/client/` directly (`npm install` first if `node_modules`
is missing):

- Run the dev server (proxies `/api` to the backend on `:8080`): `npm run dev`
- Lint (ESLint 10, flat config in `eslint.config.js`): `npm run lint`
- Type-check and build: `npm run build`
- Preview a production build: `npm run preview`
- Build the container image (nginx + static build): `docker build -t demo-chat-client modules/client`

No test runner is configured for the client yet.

### Knowledge base

- Validate the intent JSON files the way CI does: `node scripts/validate-intents.mjs`. Its rules mirror
  the `IntentDefinition` record, so keep the two in sync when adding a field.

### Infrastructure (Terraform)

- `infra/terraform/` holds the AWS staging/prod IaC. It is **lint-only** — there is no AWS account
  yet, so `terraform plan`/`apply` have never run; resource shapes and the env-var wiring are meant
  to be right but AWS-specific values (AMI ids, cert/secret ARNs, `github_org`, `admin_cidr`) are
  `TODO` variables.
- Lint it the way CI does: `make tf-lint` (needs `terraform` or `tofu` + `tflint` on PATH). It runs
  `fmt -check`, `validate` on `envs/staging` and `envs/prod` with `init -backend=false`, and
  `tflint --recursive`. No AWS credentials are used. The `terraform-lint` GitHub workflow
  (`.github/workflows/terraform-lint.yml`, path-filtered to `infra/**`) runs the same steps.
- `envs/staging` and `envs/prod` are duplicated on purpose (same rationale as the duplicated
  `application-{staging,prod}.properties`). Each env's `container_env` output must stay in sync with
  the matching `application-*.properties` env-var contract **and** the `demo-chat-config` ConfigMap
  in `infra/k8s/manifest-*.yaml` — see `infra/terraform/README.md`.
- **Deploy target is Kubernetes (kubeadm on EC2), not ECS.** `envs/*` call `k8s-cluster`,
  `alb-k8s`, `ecr`, `github-oidc` (plus `vpc`/`rds-postgres`/`keyspaces`/`qdrant-ec2`/`msk`). The
  `ecs-service`, `alb` and `bedrock-iam` modules are **retained as lint-clean references** but no
  longer instantiated (each has a README saying so) — do not delete or gut them (`tflint` would
  flag `terraform_unused_declarations`).

### Infrastructure (Kubernetes)

- `infra/k8s/` holds the app manifests — one consolidated multi-doc file per env
  (`manifest-{staging,prod}.yaml`, deliberately duplicated) plus `addons/` (pinned Calico /
  metrics-server / ingress-nginx / node-termination-handler installs). Also **lint-only** — no
  cluster. See `docs/wiki/Plan/kubernetes.md`.
- Lint the way CI does: `make k8s-lint` (needs `kubeconform` + `kubectl` + `shellcheck`). The
  `manifests-lint` workflow (`.github/workflows/manifests-lint.yml`, path `infra/k8s/**`) runs
  `kubeconform` + `kubectl --dry-run=client` + `shellcheck` on the add-on/user-data scripts +
  `actionlint` on the deploy workflows. No AWS credentials.
- `deploy-staging.yml` (push to `main`) and `deploy-prod.yml` (tag `v*`, `environment: production`
  approval) build+push images to ECR via GitHub OIDC, render the manifest + Secret, stage them in
  S3, and `kubectl apply` on a control-plane node through **SSM Run Command** — CI never opens a
  connection to kube-apiserver. Both workflows are skeletons: they reference GitHub Environment
  `vars.*` that only exist once Terraform is applied.
- Image placeholders `IMAGE_PLACEHOLDER_SERVER` / `_CLIENT` and ConfigMap `REPLACE_*` values are
  substituted at deploy time. The `Secret` object ships with `REPLACE_AT_DEPLOY` placeholders on
  purpose — a deploy that skips the secret-render step then fails fast.
- The client image switched to `nginxinc/nginx-unprivileged` (listens on 8080) and the server
  Dockerfile pins uid 10001, so the `demo-chat` namespace can run PodSecurity `restricted`.
- The KB bootstrap `Job` runs the server image with `--reindex-and-exit` (new one-shot mode in
  `KnowledgeBaseIndexer`) to seed Qdrant `support_kb`, since staging/prod keep
  `reindex-on-startup=false`.

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
- **Messaging**: `spring-boot-starter-kafka` — the starter is on the classpath and
  `spring.kafka.bootstrap-servers` is set in all three profiles, but there is **zero** producer,
  consumer, or listener code in `src/main/java`. Treat Kafka as aspirational, not as an existing
  integration point.
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
- `application-offline.properties` — deltas only, activated as `local,offline` (`make run-offline`).
  Points `spring.ai.model.{chat,embedding}` at a local Ollama server instead of Bedrock, so the app
  runs with no AWS credentials. `application.properties` now pins both selectors to the Bedrock values
  explicitly (needed once a second model starter is on the classpath).

When adding a connection setting, add it to all three environment files, not just `application.properties`.

Bedrock credentials are never in these files — they come from the AWS credential chain. Note that
startup itself needs Bedrock unless the Qdrant collections already exist, because creating a collection
calls the embedding model for its dimensions; `spring.ai.vectorstore.qdrant.initialize-schema` governs
this for both the `support_kb` and `semantic_cache` stores.

## Gotchas

- **Jackson 3, not Jackson 2.** The `rag` records import `tools.jackson.databind.*`, not
  `com.fasterxml.jackson.*`. A copied Jackson 2 snippet will not compile.
- **`ChatHistory`'s primary key is misnamed.** `@PrimaryKey("user_id")` on the `chat_history` table
  actually holds the **chat** id (`modules/server/src/main/java/com/example/demo_chat/chat/ChatHistory.java:22`).
- **`demo-chat.rag.reindex-on-startup` is the one `demo-chat.*` key not in `application.properties`.**
  Its code default is `true` (`rag/KnowledgeBaseIndexer.java:27`); it is set per-profile instead —
  `true` in local, `false` in staging, prod, and test.
- **Cassandra has no migration tool.** `spring.cassandra.schema-action` is `create-if-not-exists`
  locally and in tests but defaults to `none` in staging/prod (`${CASSANDRA_SCHEMA_ACTION:none}`), so
  new Cassandra tables need DDL applied by hand outside the app. Flyway covers Postgres only (currently
  a single `V1__create_users_table.sql`). `schema-action` also does **not** create the *keyspace* —
  locally the `cassandra-init` one-shot service in `local/docker-compose.yml` does that on `make up`;
  staging/prod need it created out of band.
- **`offline` profile uses 768-dim embeddings** (`nomic-embed-text`) vs Bedrock Titan's 1024. Switching
  a machine between `make run` and `make run-offline` against the same Qdrant volume fails on insert
  (collection dimension mismatch) — `make nuke` then `make up` / `make up-offline` between the two, or
  the KB reindex errors.
- **Flyway is configured in two places and they must agree.** Boot migrates on startup from
  `spring.flyway.schemas` in the profile properties; the `flyway {}` block in
  `modules/server/build.gradle` backs the standalone `flywayMigrate`/`flywayInfo` tasks. If the
  `schemas` value diverges, the two build *separate* `flyway_schema_history` tables and each re-runs
  the other's migrations. Staging/prod resolve `spring.flyway.schemas` from `${POSTGRES_SCHEMA}`
  while the Gradle block defaults to `demo_chat`, so migrating against them needs an explicit
  `FLYWAY_SCHEMAS` / `-Pflyway.schemas`. In practice `POSTGRES_SCHEMA` is pinned anyway:
  `V1__create_users_table.sql` hardcodes `demo_chat.users`, so any other value produces a schema
  the app cannot read. Changing it needs a new migration, not an edit to V1 — V1 is already applied
  everywhere and rewriting it changes its checksum and fails `flyway validate` on startup.
  The plugin version is pinned to the `flyway-core` version the Boot BOM
  resolves (11.14.1) for the same reason, and its Postgres support comes from the `buildscript`
  classpath at the top of that file — without those entries the tasks fail with "No Flyway database
  plugin found to handle jdbc:postgresql://…".
- **`DialogueStatus.READY_TO_ANSWER` is declared but never assigned** by the pipeline — don't route on it.
- **A fixed test user is seeded locally by a Flyway migration outside `db/migration`.**
  `local/db/migration/R__seed_local_test_user.sql` (repeatable, idempotent `INSERT ... ON CONFLICT
  (login) DO NOTHING`) creates login `testuser` / password `password`,
  id `00000000-0000-0000-0000-000000000001`. It is picked up **only locally**: `spring.flyway.locations`
  lists `classpath:local/db/migration` in `application-local.properties` but not in the staging/prod
  files, and the `flyway {}` block in `build.gradle` adds the `filesystem:` equivalent only when
  `flywayMigrate` targets the default local DB (no `flyway.url` / `FLYWAY_URL` override). Applied on
  startup with the rest, or via `make migrate`.
- **Adding a `@SpringBootTest`**: the stub Bedrock `ChatModel`/`EmbeddingModel` beans live in a nested
  `@TestConfiguration StubBedrockModels` class inside `DemoChatApplicationTests`, not in a shared support
  class — a new full-context test has to bring its own or reuse that one.
- **Intent JSON rules beyond the record's shape**, all enforced by `scripts/validate-intents.mjs`:
  `intent_id` must equal the filename stem, no two *different* files may claim the same canonical
  question (case-insensitively — repeats **within** one file are deliberately allowed, see
  `scripts/validate-intents.mjs:82`), each `{placeholder}` in `answer_template` must be listed in
  `required_slots`, and unknown fields are rejected.
- **Client auth is in-memory only.** `src/app/AuthContext.tsx` holds `{userId, login, password}` and
  builds a Basic header from it; there is no login endpoint and no persistence, so a page refresh logs
  you out. There is also no router — `App.tsx` switches screens with `useState`.
- **Client SSE behind nginx**: `modules/client/nginx.conf.template` proxies `/api/` to `${BACKEND_URL}`
  (default `http://server:8080`) with `proxy_buffering off` — that flag is what keeps streaming working
  in the container image.

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
