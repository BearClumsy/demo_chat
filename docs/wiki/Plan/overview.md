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
      │  (R2DBC, users)│ │ (chat hist.,│  │  (vector store: │ │  (messaging,   │ │  (LLM chat +  │
      │  Flyway-managed│ │  dialogue   │  │  support_kb +   │ │  dependency    │ │  embeddings,  │
      │  via JDBC)     │ │  state)     │  │  semantic_cache)│ │  only — unused)│ │  wired by rag)│
      └───────────────┘ └────────────┘  └────────────────┘ └───────────────┘ └──────────────┘
```

There is no Redis in this project — chat/session persistence is Cassandra (a plain reactive
Cassandra repository for both `ChatHistory` and `DialogueState`; the declared
`spring-ai-starter-model-chat-memory-repository-cassandra` dependency is not wired up — see
[dialogue-state.md](dialogue-state.md) for why). The default (and only production) LLM provider is
Amazon Bedrock; the `local,offline` Spring profile instead points chat + embeddings at a local Ollama
so the app can run with no AWS — see [local-vs-aws.md](local-vs-aws.md). Qdrant now backs two collections: `support_kb` (the knowledge base) and
`semantic_cache` (Phase 2's semantic cache, see [vector-store-schema.md](vector-store-schema.md)); both
are called by the RAG pipeline (see [rag-pipeline.md](rag-pipeline.md)). Postgres is R2DBC-backed for the
app (Phase 2) but still needs a blocking JDBC `DataSource` for Flyway migrations only. Kafka is still a
declared dependency with connection settings in `application.properties` that nothing in the codebase
calls yet.

## Flow for a single message (current implementation)

What actually exists today, end to end:

1. **Create a user** — `POST /api/users` (open, no auth required) hashes the password with BCrypt and
   persists a `User` row in Postgres via R2DBC (reactive end-to-end, no blocking bridge).
2. **Start a chat** — `POST /api/chats` (HTTP Basic auth required; caller must match
   `currentUserId`) validates that all `participantIds` are real users, then persists a `ChatHistory` row
   in Cassandra keyed by a newly generated `userId`, with the initial message stored as a frozen
   `ChatMessage` UDT list.
3. **Add a participant** — `POST /api/chats/{chatId}/participants` appends a user id to an existing
   `ChatHistory` row.
4. **Send a message** — `POST /api/chats/{chatId}/messages` (HTTP Basic auth required; caller must be a
   chat participant) runs the message through the RAG pipeline — normalize → semantic-cache check →
   retrieve → classify → scope-check → slot-fill → generate → output-side guardrail (see
   [rag-pipeline.md](rag-pipeline.md)) — persists the resulting `DialogueState`, appends the user +
   assistant turn to `ChatHistory`, and returns the assistant's reply and dialogue status
   (`SLOT_FILLING`, `ANSWERED`, `ESCALATED`, or `OUT_OF_SCOPE`).
5. **Send a message, streamed** — `POST /api/chats/{chatId}/messages/stream` runs the same pipeline
   (guardrail included) and streams the already-validated reply as SSE `token` events + one `done` event
   (buffer-then-chunk, not live token generation — see [rag-pipeline.md](rag-pipeline.md)).
6. **Validation/error handling** — a single app-wide `@RestControllerAdvice`
   (`ValidationExceptionHandler`) maps bean-validation failures to 400, duplicate email/login to 409, and
   `currentUserId`/participant mismatches to 403.

## Design principles

- **Reactive end-to-end.** WebFlux controllers/services use `Mono`/`Flux` throughout, including `user/*`
  since its Phase 2 migration to R2DBC. The remaining exception is the Bedrock/Qdrant calls in `rag/*`,
  which are blocking and explicitly bridged with `Schedulers.boundedElastic()` per call — a deliberate,
  accepted trade-off (see [backend.md](backend.md)), since neither has a reactive-native client in this
  Spring AI version.
- **RAG as a single source of truth.** The same `support_kb` Qdrant collection answers both "what is
  allowed to be discussed" (the scope filter/whitelist) and "what to answer" (the retrieved
  `knowledge_snippet`) — see [vector-store-schema.md](vector-store-schema.md). Implemented in
  `rag.KnowledgeRetrievalService`/`rag.ScopeFilter`.
- **Package-by-feature.** Code is organized as `chat/`, `user/`, `rag/`, `common/`, `config/` — each
  feature package holds its own controller, service, entity/repository, and DTOs, rather than
  package-by-layer. See [backend.md](backend.md).
- **Auth via Spring Security (HTTP Basic).** `SecurityConfig` permits `POST /api/users` (account
  creation) and requires authentication for everything else; there's no login/token endpoint, session
  management, or config-driven environment switching (single `application.properties`, no Spring
  Profiles) yet.

## Related documents

- [RAG pipeline](rag-pipeline.md) — implemented (Phase 1 + 2)
- [Intent matching](intent-matching.md)
- [Java backend structure](backend.md)
- [AWS infrastructure](infrastructure.md) — planned