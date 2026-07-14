# Architecture Overview

[← Back to README](README.md)

## System components (current)

```
┌─────────────┐                     ┌──────────────────────────┐
│   React     │  ─── planned ────▶  │   Spring Boot (WebFlux)  │
│  (frontend) │      not built      │   demo_chat application  │
└─────────────┘                     └───────────┬───────────────┘
                                                 │
              ┌───────────────┬──────────────────┼──────────────────┬───────────────┐
              ▼               ▼                  ▼                  ▼               ▼
      ┌───────────────┐ ┌────────────┐  ┌────────────────┐ ┌───────────────┐ ┌──────────────┐
      │  Postgres      │ │ Cassandra   │  │  Qdrant         │ │  Kafka         │ │  Bedrock      │
      │  (JPA, users)  │ │ (chat hist.)│  │  (vector store, │ │  (messaging,   │ │  (LLM/embed,  │
      │  Flyway-managed│ │ reactive    │  │  dependency     │ │  dependency    │ │  dependency   │
      │                │ │             │  │  only — unused) │ │  only — unused)│ │  only — unused)│
      └───────────────┘ └────────────┘  └────────────────┘ └───────────────┘ └──────────────┘
```

There is no Redis and no Ollama in this project — chat/session persistence is Cassandra
(`spring-ai-starter-model-chat-memory-repository-cassandra` + a plain reactive Cassandra repository), and
the only configured LLM provider is Amazon Bedrock (no local-model profile exists). Qdrant, Kafka, and
Bedrock are declared dependencies with connection settings in `application.properties`, but nothing in
the codebase calls them yet — the RAG pipeline, vector retrieval, and event production/consumption are
still to be built (see [rag-pipeline.md](rag-pipeline.md)).

## Flow for a single message (current implementation)

What actually exists today, end to end:

1. **Create a user** — `POST /api/users` (open, no auth required) hashes the password with BCrypt and
   persists a `User` row in Postgres via JPA (bridged onto `Mono` with `Schedulers.boundedElastic()`,
   since there's no R2DBC driver in this project).
2. **Start a chat** — `POST /api/chats` (HTTP Basic auth required; caller must match
   `currentUserId`) validates that all `participantIds` are real users, then persists a `ChatHistory` row
   in Cassandra keyed by a newly generated `userId`, with the initial message stored as a frozen
   `ChatMessage` UDT list.
3. **Add a participant** — `POST /api/chats/{chatId}/participants` appends a user id to an existing
   `ChatHistory` row.
4. **Validation/error handling** — a single app-wide `@RestControllerAdvice`
   (`ValidationExceptionHandler`) maps bean-validation failures to 400, duplicate email/login to 409, and
   `currentUserId` mismatches to 403.

None of the RAG stages below are wired yet — they remain the target design once the pipeline is built:

5. **Context normalization**, **retrieval**, **scope filter**, **slot filling**, **answer generation**,
   **guardrail validation** — see [rag-pipeline.md](rag-pipeline.md), [intent-matching.md](intent-matching.md),
   and [prompt-engineering.md](prompt-engineering.md) for the planned design.
6. **Streaming response (SSE)** — not implemented; current endpoints return plain `Mono<ResponseEntity<...>>`,
   not `Flux<String>`.

## Design principles

- **Reactive end-to-end.** WebFlux controllers/services use `Mono`/`Flux` throughout. The one exception
  is JPA/Postgres access in `UserService`, which is blocking and explicitly bridged with
  `Schedulers.boundedElastic()` per call (documented as a deliberate trade-off in the `User` feature
  note, since there's no R2DBC driver in the project).
- **RAG as a single source of truth** (target, not yet implemented) — the same Qdrant index is meant to
  answer both "what is allowed to be discussed" and "what to answer." No retrieval code exists yet.
- **Package-by-feature.** Code is organized as `chat/`, `user/`, `common/`, `config/` — each feature
  package holds its own controller, service, entity/repository, and DTOs, rather than package-by-layer.
  See [backend.md](backend.md).
- **Auth via Spring Security (HTTP Basic).** `SecurityConfig` permits `POST /api/users` (account
  creation) and requires authentication for everything else; there's no login/token endpoint, session
  management, or config-driven environment switching (single `application.properties`, no Spring
  Profiles) yet.

## Related documents

- [RAG pipeline](rag-pipeline.md) — planned
- [Intent matching](intent-matching.md) — planned
- [Java backend structure](backend.md)
- [AWS infrastructure](infrastructure.md) — planned