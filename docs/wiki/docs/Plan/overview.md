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
      │  (JPA, users)  │ │ (chat hist.,│  │  (vector store, │ │  (messaging,   │ │  (LLM chat +  │
      │  Flyway-managed│ │  dialogue   │  │  support_kb     │ │  dependency    │ │  embeddings,  │
      │                │ │  state)     │  │  collection)    │ │  only — unused)│ │  wired by rag)│
      └───────────────┘ └────────────┘  └────────────────┘ └───────────────┘ └──────────────┘
```

There is no Redis and no Ollama in this project — chat/session persistence is Cassandra (a plain reactive
Cassandra repository for both `ChatHistory` and `DialogueState`; the declared
`spring-ai-starter-model-chat-memory-repository-cassandra` dependency is not wired up — see
[dialogue-state.md](dialogue-state.md) for why), and the only configured LLM provider is Amazon Bedrock
(no local-model profile exists). Qdrant and Bedrock are now called by the RAG pipeline (see
[rag-pipeline.md](rag-pipeline.md)); Kafka is still a declared dependency with connection settings in
`application.properties` that nothing in the codebase calls yet.

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
4. **Send a message** — `POST /api/chats/{chatId}/messages` (HTTP Basic auth required; caller must be a
   chat participant) runs the message through the RAG pipeline — normalize → retrieve → classify →
   scope-check → slot-fill → generate (see [rag-pipeline.md](rag-pipeline.md)) — persists the resulting
   `DialogueState`, appends the user + assistant turn to `ChatHistory`, and returns the assistant's reply
   and dialogue status (`SLOT_FILLING`, `ANSWERED`, or `OUT_OF_SCOPE`). No output-side guardrail yet —
   that's Phase 2 (see [roadmap.md](roadmap.md)).
5. **Validation/error handling** — a single app-wide `@RestControllerAdvice`
   (`ValidationExceptionHandler`) maps bean-validation failures to 400, duplicate email/login to 409, and
   `currentUserId`/participant mismatches to 403.

Still not implemented:

6. **Streaming response (SSE)** — current endpoints return plain `Mono<ResponseEntity<...>>`, not
   `Flux<String>`; the RAG pipeline's reply is generated in full before the HTTP response is written.
7. **Output-side guardrail** — `ResponseValidator` from [rag-pipeline.md](rag-pipeline.md) doesn't exist
   yet; nothing checks that a generated answer is actually grounded in the retrieved context.

## Design principles

- **Reactive end-to-end.** WebFlux controllers/services use `Mono`/`Flux` throughout. The exceptions are
  JPA/Postgres access in `UserService` and the Bedrock/Qdrant calls in `rag/*`, all of which are blocking
  and explicitly bridged with `Schedulers.boundedElastic()` per call (documented as a deliberate
  trade-off in the `User` feature note and in [backend.md](backend.md), since there's no R2DBC driver and
  the Spring AI `ChatClient`/`VectorStore` APIs used here are synchronous).
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

- [RAG pipeline](rag-pipeline.md) — implemented (Phase 1)
- [Intent matching](intent-matching.md)
- [Java backend structure](backend.md)
- [AWS infrastructure](infrastructure.md) — planned