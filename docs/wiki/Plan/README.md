# Support Chat (RAG-based, Scoped Intent Matching)

A support chatbot backend built with Spring Boot (WebFlux) + Spring AI, where RAG is intended to become
the single source of truth for both allowed topics and answers. Phases 1 and 2 (see
[roadmap.md](roadmap.md)) are implemented: the chat/user backend (now R2DBC-backed, no more blocking
JPA), the 4-intent knowledge base, the RAG pipeline itself (normalize → retrieve → classify → slot-fill
→ generate), an output-side groundedness guardrail, a Qdrant-backed semantic cache, and SSE-streamed
replies (buffer-then-chunk). A React frontend chat MVP (signup/login, start a chat, SSE-streamed
replies) is also implemented, though not yet verified against a live backend — see
[frontend-chat-mvp.md](frontend-chat-mvp.md). Still planned: AWS deployment and CI/CD (see the
*planned* markers below).

## Documentation

### Architecture
- [Architecture overview](overview.md)
- [RAG pipeline (retrieval → scope → answer)](rag-pipeline.md) — implemented (Phase 1 + 2), including the
  output-side guardrail
- [Intent matching and slot filling](intent-matching.md) — implemented (Phase 1)
- [Prompt engineering and guardrails](prompt-engineering.md) — implemented, input- and output-side both

### Project structure
- [Java backend (Spring Boot + WebFlux + Spring AI)](backend.md)
- [React frontend](frontend.md) — chat MVP implemented; see
  [Frontend Chat MVP: auth, start-chat, SSE streaming](frontend-chat-mvp.md) for the plan behind it

### Data
- [Vector Store schema (topics/answers)](vector-store-schema.md) — implemented (Qdrant `support_kb`
  collection, reindexed on every app startup, plus a second `semantic_cache` collection for the
  semantic cache); CI-triggered reindex-on-merge is *planned*
- [Dialogue session model](dialogue-state.md) — implemented (Phase 1); Cassandra-backed, not Redis as
  originally drafted here

### Environments
- [Local ↔ AWS: component mapping](local-vs-aws.md)

### AWS
- [AWS infrastructure (network, services, Terraform)](infrastructure.md) — *planned, not yet provisioned*

### CI/CD
- [GitHub Actions: build and deploy pipelines](github-actions.md) — *planned, not yet implemented*

### Plan
- [Phased implementation roadmap](roadmap.md)
- [Frontend Chat MVP: auth, start-chat, SSE streaming](frontend-chat-mvp.md) — implemented

---

## Current repository structure

The project is a **multi-module Gradle build**: `modules/server` (this backend) and `modules/client`
(React frontend — a Vite + React + TypeScript chat MVP: signup/login, start a chat, SSE-streamed
replies; see [frontend.md](frontend.md)). There's still no separate `infra/` directory (that split
remains a plan; see [infrastructure.md](infrastructure.md)). No `.github/workflows` exist yet either
(see [github-actions.md](github-actions.md)).

```
demo_chat/                                  # multi-module Gradle build (root settings.gradle, no root build.gradle)
├── settings.gradle                         # includes :server (modules/server) and :client (modules/client)
├── docs/
│   └── wiki/                               # Obsidian vault — Features/, Infrastructure/, Daily/, Plan/ (this doc set)
├── graphify-out/                           # generated code graph (graphify)
├── modules/
│   ├── server/                             # :server — Spring Boot 4.0.7, Spring AI 2.0.0, Java 26 toolchain
│   │   ├── build.gradle                    # Groovy DSL (not Kotlin DSL)
│   │   └── src/
│   │       ├── main/
│   │       │   ├── java/com/example/demo_chat/
│   │       │   │   ├── DemoChatApplication.java
│   │       │   │   ├── chat/               # ChatController (incl. SSE /messages/stream), ChatService, ChatHistory (Cassandra), ...
│   │       │   │   ├── user/               # UserController, UserService, User (R2DBC/Postgres), UserRepository, UserPrincipal, ...
│   │       │   │   ├── rag/                # RAG pipeline: QueryNormalizationService, KnowledgeRetrievalService,
│   │       │   │   │                       #   IntentClassificationService, SlotFillingService, PromptBuilder,
│   │       │   │   │                       #   AnswerGenerationService, ResponseValidator, SemanticCacheService,
│   │       │   │   │                       #   TextChunker, ChatPipelineService, DialogueState, ...
│   │       │   │   ├── common/             # ValidationExceptionHandler — app-wide error mapping
│   │       │   │   └── config/             # SecurityConfig, PasswordEncoderConfig, ChatClientConfig,
│   │       │   │                           #   SemanticCacheVectorStoreConfig
│   │       │   └── resources/
│   │       │       ├── application.properties  # Postgres (JDBC for Flyway + R2DBC for the app), Cassandra, Bedrock, Qdrant, Kafka
│   │       │       ├── db/migration/       # Flyway (V1__create_users_table.sql)
│   │       │       ├── knowledge-base/intents/  # 4 intents (refund_status, order_status, change_shipping_address, password_reset)
│   │       │       └── local/docker-compose.yml  # postgres, cassandra, qdrant, kafka
│   │       └── test/java/com/example/demo_chat/  # unit + slice tests for R2DBC, guardrail, cache, and SSE streaming
│   └── client/                             # :client — Vite + React + TS chat MVP (auth, start-chat, SSE streaming)
│       ├── build.gradle
│       └── src/                            # see frontend.md for the full as-built tree
└── gradlew / gradlew.bat
```

Full package/class details: [backend.md](backend.md).