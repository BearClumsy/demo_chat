# Implementation Roadmap

[← Back to README](README.md)

## Phase 1 — Local prototype

- [x] docker-compose: **Postgres + Cassandra + Qdrant + Kafka** (`modules/server/src/main/resources/local/docker-compose.yml`)
      — the original "Qdrant + Redis (Ollama on host)" assumption was superseded; there's no Redis or
      Ollama in this project. Cassandra's heap is capped (`MAX_HEAP_SIZE`/`HEAP_NEWSIZE`) so it fits
      alongside the other three services on a memory-constrained local Docker VM.
- [x] Backend skeleton — but as `chat/`, `user/`, `rag/`, `common/`, `config/` feature packages (see
      [backend.md](backend.md)), not the originally-planned `api`/`service`/`orchestration`/`domain`/
      `infrastructure` layered split. No `local` Spring Profile exists yet — one `application.properties`.
- [x] User management (not in the original roadmap, built first): `POST /api/users`, `GET /api/users/{id}`,
      Postgres/JPA/Flyway, BCrypt password hashing, Spring Security (HTTP Basic).
- [x] Chat skeleton: `POST /api/chats` (start a chat with participants + initial message, persisted to
      Cassandra), `POST /api/chats/{chatId}/participants`, `POST /api/chats/{chatId}/messages` (send a
      message into the RAG pipeline).
- [x] 3–5 intents in `knowledge-base/intents/*.json` for one support domain — 4 intents for order/account
      support: `refund_status`, `order_status`, `change_shipping_address`, `password_reset`. Loaded at
      startup by `IntentDefinitionRegistry` and pushed into Qdrant by `KnowledgeBaseIndexer`.
- [x] Pipeline: normalize → retrieve → scope filter → slot-fill → generate (no output guardrail yet) —
      implemented in `com.example.demo_chat.rag`, orchestrated by `ChatPipelineService`. See
      [rag-pipeline.md](rag-pipeline.md) for the stage-by-stage design and known simplifications.
- [x] Simple React chat — **superseded**: implemented directly with SSE streaming (see the Phase 2 item
      below and [frontend-chat-mvp.md](frontend-chat-mvp.md)) rather than as a separate non-streaming step
      first; the `:client` Gradle module (`modules/client/`) is now a real Vite + React + TypeScript app,
      not an empty placeholder.

**Definition of done:** an end-to-end dialogue with one intent and one slot works locally, start to
finish. **Wiring verified, live behavior not yet confirmed** — the full bean graph (Cassandra, Qdrant,
Bedrock `ChatClient`) builds and the app reaches real Bedrock/Qdrant calls (confirmed by an intentional
403 from Bedrock using placeholder AWS credentials), but no one has yet run a full dialogue against real
AWS credentials end-to-end.

## Phase 2 — Reactive + Streaming

- [x] Convert the pipeline into a fully non-blocking chain, scoped to where a real reactive driver
      exists — `chat/*` was already reactive end-to-end; `user/*` (and `ChatService.validateParticipantIds`)
      migrated from blocking JPA to R2DBC, dropping the `Schedulers.boundedElastic()` bridge entirely.
      `rag/*`'s Bedrock/Qdrant calls **remain** on `Schedulers.boundedElastic()` bridging — neither has a
      reactive-native client in this Spring AI version, and that bridging pattern is the correct way to
      wrap blocking I/O in a reactive app, not a shortcut. See [backend.md](backend.md).
- [x] SSE streaming of the answer — implemented as **buffer-then-chunk**, not live token-by-token
      generation: `ChatPipelineService.handleMessageStream` (new `POST /api/chats/{chatId}/messages/stream`
      endpoint) generates the full answer with the existing pipeline (guardrail included), then splits the
      already-validated text into word chunks via `TextChunker` and emits them as SSE `token` events
      followed by one `done` event. Chosen over true streaming (Spring AI's `ChatClient.stream()`, unused
      elsewhere in this codebase) because live streaming would mean the guardrail could only run *after*
      the client already saw the text — this preserves the guardrail's full pre-send guarantee.
- [x] React chat consuming the SSE endpoint — `modules/client/` now has a working chat MVP (signup/login,
      start a chat, `useChatStream` hook parsing the `token`/`done` SSE frames by hand since native
      `EventSource` can't POST or send an `Authorization` header). See
      [frontend-chat-mvp.md](frontend-chat-mvp.md) for the design decisions and known gaps (no chat
      listing/restore endpoint, no lookup-by-login endpoint). **Not yet verified against a live backend**
      — this machine has no AWS Bedrock credentials, so the backend can't boot to test against.
- [ ] Redis for dialogue state (instead of in-memory) — **superseded**: dialogue/chat state now lives in
      Cassandra, not Redis (see [overview.md](overview.md)).
- [x] Output-side guardrail validation — `ResponseValidator` implemented: a groundedness check
      (`GroundednessCheck` structured LLM output) runs on every generated answer before it's sent. A
      failed check routes to a new `DialogueStatus.ESCALATED` and returns the intent's
      `escalationFallback` text instead of the (possibly ungrounded) generated answer.
- [x] Semantic cache for repeated queries — `SemanticCacheService`, backed by a **second Qdrant
      collection** (`semantic_cache`, separate from `support_kb`) rather than Redis or an exact-string
      Cassandra cache, so lookups match on semantic similarity of the normalized query, not just literal
      repeats. Checked in `ChatPipelineService.startNewTurn` right after normalization, short-circuiting
      retrieval/classification/generation on a hit. Writes are gated on the output-side guardrail passing,
      so a hallucinated answer is never cached.

## Phase 3 — Staging in AWS

- [ ] Terraform: VPC, ECS, ALB, RDS (Postgres), Amazon Keyspaces/Cassandra, Qdrant on EC2
- [ ] Switch to Bedrock via the `staging` profile — Bedrock is already the only configured LLM provider;
      what's missing is the Spring Profile mechanism itself (see [local-vs-aws.md](local-vs-aws.md))
- [ ] GitHub Actions: `backend-ci`, `frontend-ci`, `deploy-staging` — see [github-actions.md](github-actions.md), planned
- [ ] Knowledge-base reindexing as a CI job (`QdrantDocumentLoader.reindex()` on merge, per
      [vector-store-schema.md](vector-store-schema.md)) — currently reindexes on every app startup instead
      (`KnowledgeBaseIndexer`, idempotent since document ids are the intent id), which is fine for a
      single local instance but not for a CI-driven staging/prod rollout.
- [ ] Validate intent matching and prompts against real Bedrock
- [ ] Load testing of the pipeline (retrieval + generation latency)

## Phase 4 — Production and scaling

- [ ] `deploy-prod.yml` with manual approval and blue/green deployment
- [ ] Resilience4j: circuit breaker/rate limiter for the LLM and Vector Store
- [ ] ECS auto-scaling by latency/RPS
- [ ] CloudWatch alarms: out-of-scope rate, retrieval/generation latency, guardrail rejection rate
- [ ] Process for the content team to update the knowledge base (PR → `knowledge-base-lint` → staging → prod)

## Phase 5 — Quality iteration

- [ ] Collect metrics: confusion matrix for intent classification on real dialogues
- [ ] Tune `similarity_threshold` and confidence thresholds based on logs
- [ ] Regression tests against a set of real user questions
- [ ] Expand the topic set (new intents) without code changes — data only

## Related documents

- [Architecture overview](overview.md)
- [Local ↔ AWS mapping](local-vs-aws.md)
- [GitHub Actions](github-actions.md) — planned
- [Frontend Chat MVP](frontend-chat-mvp.md) — implemented