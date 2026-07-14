# Implementation Roadmap

[← Back to README](README.md)

## Phase 1 — Local prototype

- [x] docker-compose: **Postgres + Cassandra + Qdrant + Kafka** (`src/main/resources/local/docker-compose.yml`)
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
- [ ] Simple React chat without streaming (plain request/response) — not started, no `frontend/` exists.

**Definition of done:** an end-to-end dialogue with one intent and one slot works locally, start to
finish. **Wiring verified, live behavior not yet confirmed** — the full bean graph (Cassandra, Qdrant,
Bedrock `ChatClient`) builds and the app reaches real Bedrock/Qdrant calls (confirmed by an intentional
403 from Bedrock using placeholder AWS credentials), but no one has yet run a full dialogue against real
AWS credentials end-to-end.

## Phase 2 — Reactive + Streaming

- [ ] Convert the pipeline into a fully non-blocking chain (WebFlux) — `chat/*` is already reactive
      end-to-end; `user/*` is blocking JPA bridged via `Schedulers.boundedElastic()`, and `rag/*`'s
      Bedrock/Qdrant calls are blocking and bridged the same way (see [backend.md](backend.md)) — all
      would need a non-blocking driver/client to close this gap.
- [ ] SSE streaming of the answer in React
- [ ] Redis for dialogue state (instead of in-memory) — **superseded**: dialogue/chat state now lives in
      Cassandra, not Redis (see [overview.md](overview.md)).
- [ ] Output-side guardrail validation — `ResponseValidator` from [rag-pipeline.md](rag-pipeline.md),
      not implemented.
- [ ] Semantic cache for repeated queries

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