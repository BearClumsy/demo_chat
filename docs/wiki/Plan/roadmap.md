# Implementation Roadmap

[← Back to README](README.md)

## Phase 1 — Local prototype

- [x] docker-compose: **Postgres + Cassandra + Qdrant + Kafka** (`modules/server/src/main/resources/local/docker-compose.yml`)
      — the original "Qdrant + Redis (Ollama on host)" assumption was superseded: there's no Redis in
      this project, and Ollama is no longer a host prerequisite — an `ollama` service (plus a one-shot
      model-pull) gated behind the `offline` Compose profile (`make up-offline`) now backs the
      `local,offline` Spring profile's no-AWS run; a plain `make up` is unchanged. Cassandra's heap is
      capped (`MAX_HEAP_SIZE`/`HEAP_NEWSIZE`) so it fits alongside the other three services on a
      memory-constrained local Docker VM.
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
      listing/restore endpoint, no lookup-by-login endpoint). Verifiable end-to-end without AWS since
      the `local,offline` profile added (`make up-offline` + `make run-offline`); still no CI coverage.
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

Split in two: **3a** is everything that can be built and verified without an AWS account (done), **3b**
is everything that needs one (open).

### Phase 3a — profiles, containers, CI (done)

- [x] Spring Profiles — `application.properties` now holds only environment-independent settings
      (`spring.profiles.default=local`, Bedrock model ids, collection names, `demo-chat.*` tuning), with
      `application-local.properties` (the docker-compose stack) and `application-staging.properties` /
      `application-prod.properties` binding every host, port, and credential to an environment variable.
      Secrets have no defaults, so a missing one fails startup instead of falling back to a dev value.
      Staging/prod also cap actuator exposure to `health,info,metrics` and set
      `reindex-on-startup=false`. See [local-vs-aws.md](local-vs-aws.md).
- [x] No-AWS local run — `application-offline.properties`, activated as `local,offline`
      (`make run-offline`), points `spring.ai.model.{chat,embedding}` at Ollama instead of Bedrock so
      the app boots with no AWS credentials (chat = `llama3.1`, embeddings = `nomic-embed-text`, 768-dim;
      groundedness guardrail disabled since small local models are unreliable at the JSON verdict).
      Ollama runs either as a native install or as the `offline`-profile compose service started by
      `make up-offline`; the containerised path needs a Docker VM of >= ~12 GiB (Colima's default
      2 GiB OOM-kills `llama3.1` on the first chat turn).
- [x] The test suite runs anywhere — `DemoChatApplicationTests` was a bare `@SpringBootTest` that only
      passed against a running docker-compose stack plus AWS credentials. It now starts Postgres,
      Cassandra, and Qdrant as Testcontainers and stubs only the Bedrock `ChatModel`/`EmbeddingModel`
      (via `spring.ai.model.chat=none` / `spring.ai.model.embedding=none` plus a `@TestConfiguration`),
      so `./gradlew :server:build` is green on a clean machine with Docker and nothing else. Without
      this, CI would have been meaningless.
- [x] Containerization — `modules/server/Dockerfile` (multi-stage, layered jar, non-root, healthcheck
      on `/actuator/health`) and `modules/client/Dockerfile` (Vite build → nginx with SPA fallback and
      an `/api` proxy that disables buffering so SSE still streams). `/actuator/health` is now
      `permitAll` in `SecurityConfig` so container and ALB probes work; every other actuator endpoint
      stays authenticated.
- [x] GitHub Actions: `backend-ci` (spotlessCheck + build + tests + server image build), `frontend-ci`
      (ESLint — newly added to the client — + `tsc`/vite build + client image build), and
      `knowledge-base-lint` (`scripts/validate-intents.mjs`). No image push and no deploy workflow yet;
      there is no ECR to push to. See [github-actions.md](github-actions.md).

### Phase 3b — needs an AWS account (open)

**Compute pivot (2026-09-03):** the deploy target is now **self-managed Kubernetes (kubeadm) on
EC2** behind the NGINX Ingress Controller, not ECS Fargate. See [kubernetes.md](kubernetes.md). The
ECS modules (`ecs-service`, `alb`, `bedrock-iam`) are retained lint-clean but no longer
instantiated.

- [~] Terraform: VPC, RDS (Postgres), Amazon Keyspaces/Cassandra, Qdrant on EC2, MSK — plus the
      Kubernetes layer: `modules/{k8s-cluster, alb-k8s, ecr, github-oidc}`, `envs/{staging,prod}`
      rewired. **Code skeleton done, not applied**, CI-linted by `terraform-lint`. Still needs an
      account: `terraform apply`, the S3/DynamoDB state backend, real `node_ami_id` / cert / secret
      ARNs / `github_org` / `admin_cidr`, the actual `kubeadm init` + `make k8s-addons`, and
      porting the full `chat_history`/`dialogue_state` Keyspaces schemas. See
      [infrastructure.md](infrastructure.md).
- [~] `infra/k8s/` — consolidated `manifest-{staging,prod}.yaml` (Deployments, probes, Ingress
      objects with SSE annotations, HPA/PDB/NetworkPolicy, the KB-bootstrap Job) + `addons/`
      (pinned Calico / metrics-server / ingress-nginx / NTH). CI-linted by the new `manifests-lint`
      workflow (`kubeconform` + `kubectl --dry-run` + `shellcheck` + `actionlint`).
- [~] `deploy-staging.yml` + `deploy-prod.yml` + `ecr` module + GitHub OIDC role — written as
      skeletons (OIDC → ECR push → render → S3 → SSM Run Command → `kubectl apply` on a
      control-plane node; no inbound kube-apiserver). They reference GitHub Environment `vars.*`
      that only exist once Terraform is applied.
- [x] Knowledge-base reindexing off the startup path — `KnowledgeBaseIndexer` gained a
      `--reindex-and-exit` one-shot mode; the `demo-chat-kb-bootstrap` Job runs it to seed Qdrant
      `support_kb`. (A CI-triggered reindex on KB changes is still a possible future refinement.)
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