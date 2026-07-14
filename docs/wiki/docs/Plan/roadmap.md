# Implementation Roadmap

[← Back to README](README.md)

## Phase 1 — Local prototype

- [x] docker-compose: **Postgres + Cassandra + Qdrant + Kafka** (`src/main/resources/local/docker-compose.yml`)
      — the original "Qdrant + Redis (Ollama on host)" assumption was superseded; there's no Redis or
      Ollama in this project.
- [x] Backend skeleton — but as `chat/`, `user/`, `common/`, `config/` feature packages (see
      [backend.md](backend.md)), not the originally-planned `api`/`service`/`orchestration`/`domain`/
      `infrastructure` layered split. No `local` Spring Profile exists yet — one `application.properties`.
- [x] User management (not in the original roadmap, built first): `POST /api/users`, `GET /api/users/{id}`,
      Postgres/JPA/Flyway, BCrypt password hashing, Spring Security (HTTP Basic).
- [x] Chat skeleton: `POST /api/chats` (start a chat with participants + initial message, persisted to
      Cassandra), `POST /api/chats/{chatId}/participants`. No send-message endpoint yet.
- [ ] 3–5 intents in `knowledge-base/intents/*.json` for one support domain — not started.
- [ ] Pipeline: normalize → retrieve → scope filter → slot-fill → generate (no output guardrail yet) —
      not started; Bedrock/Qdrant are configured dependencies but nothing calls them yet.
- [ ] Simple React chat without streaming (plain request/response) — not started, no `frontend/` exists.

**Definition of done:** an end-to-end dialogue with one intent and one slot works locally, start to
finish. **Not yet met** — current state is chat/user CRUD scaffolding without any RAG behavior.

## Phase 2 — Reactive + Streaming

- [ ] Convert the pipeline into a fully non-blocking chain (WebFlux) — `chat/*` is already reactive
      end-to-end; `user/*` is blocking JPA bridged via `Schedulers.boundedElastic()` (see
      [backend.md](backend.md)) and would need this if it stays in the reactive request path.
- [ ] SSE streaming of the answer in React
- [ ] Redis for dialogue state (instead of in-memory) — **superseded**: dialogue/chat state now lives in
      Cassandra, not Redis (see [overview.md](overview.md)).
- [ ] Output-side guardrail validation
- [ ] Semantic cache for repeated queries

## Phase 3 — Staging in AWS

- [ ] Terraform: VPC, ECS, ALB, RDS (Postgres), Amazon Keyspaces/Cassandra, Qdrant on EC2
- [ ] Switch to Bedrock via the `staging` profile — Bedrock is already the only configured LLM provider;
      what's missing is the Spring Profile mechanism itself (see [local-vs-aws.md](local-vs-aws.md))
- [ ] GitHub Actions: `backend-ci`, `frontend-ci`, `deploy-staging` — see [github-actions.md](github-actions.md), planned
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