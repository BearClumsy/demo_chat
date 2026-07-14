# Support Chat (RAG-based, Scoped Intent Matching)

A support chatbot backend built with Spring Boot (WebFlux) + Spring AI, where RAG is intended to become
the single source of truth for both allowed topics and answers. Today the repo is a single Gradle module
with chat/user scaffolding and the Spring AI/Bedrock/Qdrant dependencies wired in `build.gradle` — the
RAG pipeline itself, the React frontend, AWS deployment, and CI/CD are not built yet (see the *planned*
markers below).

## Documentation

### Architecture
- [Architecture overview](overview.md)
- [RAG pipeline (retrieval → scope → answer)](rag-pipeline.md) — *planned, not yet implemented*
- [Intent matching and slot filling](intent-matching.md) — *planned, not yet implemented*
- [Prompt engineering and guardrails](prompt-engineering.md) — *planned, not yet implemented*

### Project structure
- [Java backend (Spring Boot + WebFlux + Spring AI)](backend.md)
- [React frontend](frontend.md) — *planned, not started*

### Data
- [Vector Store schema (topics/answers)](vector-store-schema.md) — *planned, not yet implemented*
- [Dialogue session model](dialogue-state.md) — *planned, not yet implemented*

### Environments
- [Local ↔ AWS: component mapping](local-vs-aws.md)

### AWS
- [AWS infrastructure (network, services, Terraform)](infrastructure.md) — *planned, not yet provisioned*

### CI/CD
- [GitHub Actions: build and deploy pipelines](github-actions.md) — *planned, not yet implemented*

### Plan
- [Phased implementation roadmap](roadmap.md)

---

## Current repository structure

The project is **one Gradle module at the repo root** — there's no separate `frontend/`, `backend/`, or
`infra/` directory (that split is still just a plan; see [frontend.md](frontend.md) and
[infrastructure.md](infrastructure.md)). No `.github/workflows` exist yet either
(see [github-actions.md](github-actions.md)).

```
demo_chat/                                # single Gradle module — Spring Boot 4.0.7, Spring AI 2.0.0, Java 26 toolchain
├── build.gradle                          # Groovy DSL (not Kotlin DSL)
├── settings.gradle
├── docs/
│   └── wiki/                             # Obsidian vault — Features/, Infrastructure/, Daily/, Plan/ (this doc set)
├── graphify-out/                         # generated code graph (graphify)
├── src/
│   ├── main/
│   │   ├── java/com/example/demo_chat/
│   │   │   ├── DemoChatApplication.java
│   │   │   ├── chat/                     # ChatController, ChatService, ChatHistory (Cassandra), ChatMessage, ChatHistoryRepository, ...
│   │   │   ├── user/                     # UserController, UserService, User (JPA/Postgres), UserRepository, UserPrincipal, ...
│   │   │   ├── common/                   # ValidationExceptionHandler — app-wide error mapping
│   │   │   └── config/                   # SecurityConfig, PasswordEncoderConfig
│   │   └── resources/
│   │       ├── application.properties    # Postgres, Cassandra, Bedrock, Qdrant, Kafka connection settings
│   │       ├── db/migration/             # Flyway (V1__create_users_table.sql)
│   │       └── local/docker-compose.yml  # postgres, cassandra, qdrant, kafka
│   └── test/java/com/example/demo_chat/
└── gradlew / gradlew.bat
```

Full package/class details: [backend.md](backend.md).