# Graph Report - demo_chat  (2026-09-05)

## Corpus Check
- 198 files · ~61,870 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 1023 nodes · 1723 edges · 111 communities (68 shown, 38 thin omitted)
- Extraction: 93% EXTRACTED · 7% INFERRED · 0% AMBIGUOUS · INFERRED: 118 edges (avg confidence: 0.81)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `b09b0708`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- ChatPipelineService
- DemoChatApplicationTests.java
- Terraform AWS IaC (Phase 3, lint-only)
- org.junit.jupiter.api.Test
- App.tsx
- devDependencies
- compilerOptions
- User
- Plan/README.md (architecture/roadmap sub-index)
- UserPrincipal
- infra/k8s/addons README
- Kubernetes deploy layer (kubeadm on EC2) (wiki doc)
- DialogueStatus
- roadmap.md
- Offline profile: commit `11374d4`, then containerise Ollama and boot it for real
- manifest-prod.yaml
- infra/k8s README
- CLAUDE.md
- docs/wiki/index.md (vault MOC)
- gradlew
- Architecture Overview
- install.sh
- DemoChatApplication
- Running Locally
- CI/CD: GitHub Actions (wiki doc)
- CI/CD: GitHub Actions
- demo_chat
- AWS Infrastructure (wiki doc)
- Documentation
- Chat
- validate-intents.mjs
- Architecture Overview (wiki doc)
- <Feature Name>
- Feature: User
- Kubernetes deploy layer (kubeadm on EC2)
- RAG Pipeline: From User Context to Answer
- Implementation Roadmap
- Map of Content
- Dialogue Session Model (Cassandra)
- AWS Infrastructure
- Intent Matching and Slot Filling
- Prompt Engineering and Guardrails
- infra/terraform
- 2026-08-13
- Frontend Chat MVP: auth, start-chat, SSE streaming
- 2026-07-13
- 2026-07-14
- offline Spring Profile (Ollama, no AWS)
- Vector Store Schema (Topics + Answers)
- 2026-07-15
- 2026-09-04
- infra/k8s
- alb Module (retained reference, ECS/Fargate ALB)
- verify-chat.sh
- Root Makefile (thin alias layer)
- React Chat MVP (modules/client)
- R__seed_local_test_user.sql (local-only Flyway seed)
- daily-report Skill
- docker-compose: cassandra service
- GitHub Actions CI (backend-ci, frontend-ci, knowledge-base-lint)
- java-rules Skill
- Spring Profiles (local/staging/prod)
- Resolved Ambiguous Bedrock EmbeddingModel Bean
- Configured Postgres/Cassandra/Qdrant/Kafka Connection Properties
- Reinstalled Docker/Colima, Fixed credsStore Mismatch
- Added V1__create_users_table.sql Migration
- Set Up graphify for Codebase
- Fixed JDK 26 Toolchain Auto-Provisioning
- Fixed jdtls in LazyVim
- Postgres search_path Schema Desync Bug
- Added Google Java Format via Spotless
- Built com.example.demo_chat.user Package (API)
- Set Up docs/wiki as Obsidian Vault
- Feature Note Template
- Cassandra Table Note Template
- Kafka Topic Note Template
- Postgres Table Note Template
- Qdrant Collection Note Template
- terraform-lint Workflow
- ecs-service Module (Retained Reference)
- Kafka Topic: <topic-name>
- Client index.html Entry Point
- Java Backend: Project Structure
- Local ↔ AWS: Component Mapping
- docker-compose: kafka service
- docker-compose: ollama service (offline profile)
- docker-compose: postgres service
- docker-compose: qdrant service
- infra/terraform README
- Cassandra Table: chat_history
- Cassandra Table: dialogue_state
- Cassandra Table: <table_name>
- 2026-08-31
- React Frontend: Project Structure
- Postgres Table: <table_name>
- Postgres Table: users
- Qdrant Collection: semantic_cache
- Qdrant Collection: support_kb
- Qdrant Collection: <collection_name>
- 2026-08-12
- daily-report/SKILL.md
- 2026-07-22
- 2026-08-20
- alb/README.md
- bedrock-iam/README.md
- ecs-service/README.md

## God Nodes (most connected - your core abstractions)
1. `ChatPipelineService` - 47 edges
2. `docs/wiki/index.md (vault MOC)` - 28 edges
3. `Plan/README.md (architecture/roadmap sub-index)` - 24 edges
4. `AWS Infrastructure (wiki doc)` - 23 edges
5. `IntentDefinition` - 22 edges
6. `ChatPipelineServiceTest` - 22 edges
7. `Kubernetes deploy layer (kubeadm on EC2) (wiki doc)` - 19 edges
8. `DialogueState` - 18 edges
9. `ChatService` - 17 edges
10. `User` - 17 edges

## Surprising Connections (you probably didn't know these)
- `Flyway Dual-Configuration Gotcha (Boot vs Gradle plugin)` --semantically_similar_to--> `Flyway Doesn't Auto-Migrate on bootRun (unresolved bug)`  [INFERRED] [semantically similar]
  CLAUDE.md → docs/wiki/Daily/2026-09-03.md
- `DialogueStatus.READY_TO_ANSWER Never Assigned (rationale/gotcha)` --semantically_similar_to--> `Slot-Filling NPE: Empty Cassandra Map Reads Back Null`  [INFERRED] [semantically similar]
  CLAUDE.md → docs/wiki/Daily/2026-09-03.md
- `Known gaps / TODO list` --conceptually_related_to--> `Amazon Keyspaces (Cassandra-compatible)`  [INFERRED]
  infra/terraform/README.md → docs/wiki/Plan/infrastructure.md
- `Deploy flow (what the workflow does)` --references--> `deploy-prod.yml (skeleton)`  [INFERRED]
  infra/k8s/README.md → docs/wiki/Plan/github-actions.md
- `Deploy flow (what the workflow does)` --references--> `deploy-staging.yml (skeleton)`  [INFERRED]
  infra/k8s/README.md → docs/wiki/Plan/github-actions.md

## Import Cycles
- None detected.

## Hyperedges (group relationships)
- **GitHub Actions CI Suite** — github_workflows_backend_ci, github_workflows_frontend_ci, github_workflows_knowledge_base_lint, github_workflows_terraform_lint [EXTRACTED 0.90]
- **Kubernetes Deploy Pipeline (Terraform through SSM Apply)** — claude_k8scluster_module, claude_albk8s_module, claude_ecr_module, claude_githuboidc_module, github_workflows_deploy_staging, github_workflows_deploy_prod, github_workflows_manifests_lint [EXTRACTED 0.95]
- **var-over-explicit-type Java Convention** — claude_skills_java_rules_skill, claude, docs_wiki_daily_2026_07_22 [EXTRACTED 0.80]
- **Cluster add-on stack installed by install.sh** — infra_k8s_addons_readme_calico, infra_k8s_addons_readme_metrics_server, infra_k8s_addons_readme_ingress_nginx, infra_k8s_addons_readme_nth [EXTRACTED 1.00]
- **Deliberately duplicated per-environment config pattern** — infra_k8s_manifest_prod, infra_k8s_manifest_staging, docs_wiki_plan_kubernetes [EXTRACTED 1.00]
- **Retained Lint-Clean Reference Modules (Not Instantiated by Any Env)** — infra_terraform_modules_alb_readme_alb, infra_terraform_modules_bedrock_iam_readme_bedrock_iam, infra_terraform_modules_ecs_service_readme_ecs_service [EXTRACTED 1.00]
- **local docker-compose services backing the local Spring profile** — modules_server_src_main_resources_local_docker_compose_postgres, modules_server_src_main_resources_local_docker_compose_cassandra, modules_server_src_main_resources_local_docker_compose_cassandra_init, modules_server_src_main_resources_local_docker_compose_qdrant, modules_server_src_main_resources_local_docker_compose_kafka, modules_server_src_main_resources_local_docker_compose_ollama [EXTRACTED 1.00]

## Communities (111 total, 38 thin omitted)

### Community 0 - "ChatPipelineService"
Cohesion: 0.07
Nodes (30): ChatHistoryRepository, SendMessageResponse, AnswerGenerationService, AssembledPrompt, ChatPipelineService, PipelineOutcome, DialogueState, DialogueStateRepository (+22 more)

### Community 1 - "DemoChatApplicationTests.java"
Cohesion: 0.05
Nodes (37): io.qdrant.client.QdrantClient, jakarta.annotation.PostConstruct, ChatClientConfig, PasswordEncoderConfig, SecurityConfig, SemanticCacheVectorStoreConfig, IntentDefinitionRegistry, Override (+29 more)

### Community 2 - "Terraform AWS IaC (Phase 3, lint-only)"
Cohesion: 0.16
Nodes (20): Terraform module: alb (retained, lint-clean, unused), Terraform module: alb-k8s, Terraform module: bedrock-iam (retained, lint-clean, unused), Terraform module: ecr, Terraform module: ecs-service (retained, lint-clean, unused), Terraform module: github-oidc, graphify-out/ (auto-generated code graph), infra/k8s manifests (manifest-staging.yaml / manifest-prod.yaml) (+12 more)

### Community 3 - "org.junit.jupiter.api.Test"
Cohesion: 0.06
Nodes (18): ChatService, MessageRequest, StartChatResponse, GroundednessCheck, PropertyNamingStrategies.SnakeCaseStrategy, UserRepository, ChatControllerStartChatTest, ChatServiceStartChatTest (+10 more)

### Community 4 - "App.tsx"
Cohesion: 0.11
Nodes (25): RFC-7807, App(), AppShell(), AuthContext, AuthContextValue, AuthProvider(), Credentials, useAuth() (+17 more)

### Community 5 - "devDependencies"
Cohesion: 0.05
Nodes (37): eslint, @eslint/js, eslint-plugin-react-hooks, eslint-plugin-react-refresh, globals, dependencies, react, react-dom (+29 more)

### Community 6 - "compilerOptions"
Cohesion: 0.11
Nodes (17): compilerOptions, isolatedModules, jsx, lib, module, moduleResolution, noEmit, resolveJsonModule (+9 more)

### Community 7 - "User"
Cohesion: 0.35
Nodes (10): lombok.AllArgsConstructor, lombok.Builder, lombok.Getter, lombok.NoArgsConstructor, ChatHistory, ChatMessage, User, org.springframework.data.cassandra.core.mapping.Table (+2 more)

### Community 8 - "Plan/README.md (architecture/roadmap sub-index)"
Cohesion: 0.20
Nodes (10): Kubernetes Deploy Target (kubeadm on EC2, not ECS), Plan/README.md (architecture/roadmap sub-index), Plan/dialogue-state.md, Plan/frontend-chat-mvp.md, Plan/github-actions.md, Plan/infrastructure.md, Plan/kubernetes.md, Plan/local-vs-aws.md (+2 more)

### Community 9 - "UserPrincipal"
Cohesion: 0.06
Nodes (30): lombok.RequiredArgsConstructor, ChatController, ParticipantRequest, SendMessageRequest, StartChatRequest, ValidationExceptionHandler, CreateUserRequest, Override (+22 more)

### Community 10 - "infra/k8s/addons README"
Cohesion: 0.22
Nodes (9): Calico CNI (VXLAN mode), cluster-autoscaler (optional), NGINX Ingress Controller (DaemonSet), infra/k8s/addons README, Calico add-on, cluster-autoscaler add-on (optional), ingress-nginx add-on (DaemonSet), metrics-server add-on (+1 more)

### Community 11 - "Kubernetes deploy layer (kubeadm on EC2) (wiki doc)"
Cohesion: 0.28
Nodes (9): Compute pivot to kubeadm on EC2 (2026-09-03), ECS Fargate (superseded compute target), Kubernetes deploy layer (kubeadm on EC2) (wiki doc), modules/alb-k8s (Terraform), modules/k8s-cluster (Terraform), Cluster bootstrap seed election (SSM lock), Roadmap note: compute pivot to Kubernetes, Terraform modules list (+1 more)

### Community 12 - "DialogueStatus"
Cohesion: 0.25
Nodes (7): DialogueStatus, ANSWERED, ESCALATED, NEW, OUT_OF_SCOPE, READY_TO_ANSWER, SLOT_FILLING

### Community 13 - "roadmap.md"
Cohesion: 0.17
Nodes (15): Semantic cache as read-caching for scale, Local <-> AWS: Component Mapping (wiki doc), Local docker-compose stack, local,offline profile (Ollama, no AWS), Flow for a single message, System components diagram, Phase 1 - Local prototype, Phase 2 - Reactive + Streaming (+7 more)

### Community 14 - "Offline profile: commit `11374d4`, then containerise Ollama and boot it for real"
Cohesion: 0.10
Nodes (19): 2026-09-03, App-side changes (small, enabling), Bug found on the first real end-to-end boot: non-UUID Qdrant point id, Chat OOMs on the first turn — Colima VM too small for llama3.1, Chat: `participantIds` now optional — assistant-only chats (not this session), CI, Docs drift fixed, Local dev: seeded test user (+11 more)

### Community 15 - "manifest-prod.yaml"
Cohesion: 0.13
Nodes (16): HPA scaling (CPU 70%), ConfigMap demo-chat-config (prod), Deployment demo-chat-client (prod, 2 replicas), Deployment demo-chat-server (prod, 3 replicas), HorizontalPodAutoscaler (prod, server 3->10 / client 2->4), Ingress demo-chat-web/api (prod), Job demo-chat-kb-bootstrap (prod), Namespace demo-chat (prod, PodSecurity restricted) (+8 more)

### Community 16 - "infra/k8s README"
Cohesion: 0.40
Nodes (5): manifests-lint workflow, infra/k8s manifests (staging/prod), infra/k8s README, make k8s-lint / k8s-render-staging / k8s-apply-staging, Manifest layout / object table

### Community 17 - "CLAUDE.md"
Cohesion: 0.11
Nodes (17): API surface, Architecture (from declared dependencies), Client, Commands, Configuration, Gotchas, graphify, Infrastructure (Kubernetes) (+9 more)

### Community 18 - "docs/wiki/index.md (vault MOC)"
Cohesion: 0.06
Nodes (57): Bedrock Titan embedding model (amazon.titan-embed-text-v2:0, 768d), Buffer-Then-Chunk SSE Streaming, Chat authorization hole (addParticipant bug, 2026-08-13), DialogueStatus.READY_TO_ANSWER Never Assigned (rationale/gotcha), docs/wiki/ (Obsidian vault knowledge source), SSE Streaming Endpoint (buffer-then-chunk), AuthPage (signup + login form), GET /api/users/{id} Endpoint (+49 more)

### Community 19 - "gradlew"
Cohesion: 0.83
Nodes (3): gradlew script, die(), warn()

### Community 20 - "Architecture Overview"
Cohesion: 0.33
Nodes (5): Architecture Overview, Design principles, Flow for a single message (current implementation), Related documents, System components (current)

### Community 21 - "install.sh"
Cohesion: 0.83
Nodes (3): kubectl_apply(), install.sh script, wait_rollout()

### Community 23 - "Running Locally"
Cohesion: 0.15
Nodes (12): 1. Start the dependencies, 2. Create the Cassandra keyspace (one-time), 3. Export AWS credentials, 4. Run the backend, 5. Run the frontend, Exercising the API directly, Prerequisites, Related documents (+4 more)

### Community 25 - "CI/CD: GitHub Actions (wiki doc)"
Cohesion: 0.29
Nodes (12): CI/CD: GitHub Actions (wiki doc), backend-ci.yml workflow, deploy-prod.yml (skeleton), deploy-staging.yml (skeleton), frontend-ci.yml workflow, GitHub Environments & protections, knowledge-base-lint.yml workflow, CI -> cluster via SSM Run Command (+4 more)

### Community 26 - "CI/CD: GitHub Actions"
Cohesion: 0.18
Nodes (10): `backend-ci.yml` — stages (implemented), CI/CD: GitHub Actions, `deploy-prod.yml`, `deploy-staging.yml`, `frontend-ci.yml` — stages (implemented), GitHub Environments and protections, `knowledge-base-lint.yml` (implemented), Related documents (+2 more)

### Community 27 - "demo_chat"
Cohesion: 0.11
Nodes (19): Schedulers.boundedElastic() Bridging (rationale), Flyway Dual-Configuration Gotcha (Boot vs Gradle plugin), KB Bootstrap Job (--reindex-and-exit), Postgres/JPA to R2DBC Migration (rationale), 8-Stage RAG Pipeline, Qdrant semantic_cache Collection, Semantic-Cache Lookup (non-numbered short-circuit stage), Qdrant support_kb Collection (+11 more)

### Community 28 - "AWS Infrastructure (wiki doc)"
Cohesion: 0.22
Nodes (13): AWS Infrastructure (wiki doc), ALB (load balancing), Amazon Bedrock (managed LLM), CloudFront CDN, CloudWatch (logs/metrics/alarms), ECR (Docker image registry), Amazon Keyspaces (Cassandra-compatible), Qdrant on EC2/ECS (self-managed vector store) (+5 more)

### Community 29 - "Documentation"
Cohesion: 0.18
Nodes (10): Architecture, AWS, CI/CD, Current repository structure, Data, Documentation, Environments, Plan (+2 more)

### Community 30 - "Chat"
Cohesion: 0.22
Nodes (8): Chat, Decisions, Infrastructure Used, Open Questions, Overview, Related Code, Requirements, Source Log

### Community 32 - "validate-intents.mjs"
Cohesion: 0.20
Nodes (10): ARRAY_FIELDS, BOOLEAN_FIELDS, dir, errors, fail(), KNOWN_FIELDS, seenIntentIds, seenQuestions (+2 more)

### Community 33 - "Architecture Overview (wiki doc)"
Cohesion: 0.23
Nodes (16): AuthContext (in-memory {userId, login, password}), chatApi (hand-rolled SSE frame parser over fetch), ChatWindow / MessageBubble, StartChatForm, useChatStream hook, Vite dev proxy (/api -> localhost:8080), Daily Log 2026-07-14, Java Backend Structure (+8 more)

### Community 34 - "<Feature Name>"
Cohesion: 0.22
Nodes (8): Decisions, <Feature Name>, Infrastructure Used, Open Questions, Overview, Related Code, Requirements, Source Log

### Community 35 - "Feature: User"
Cohesion: 0.22
Nodes (8): Decisions, Feature: User, Infrastructure Used, Open Questions, Overview, Related Code, Requirements, Source Log

### Community 38 - "Kubernetes deploy layer (kubeadm on EC2)"
Cohesion: 0.22
Nodes (8): CI → cluster (SSM Run Command, no inbound exposure), Cluster bootstrap (`modules/k8s-cluster`), Ingress / ALB (`modules/alb-k8s`), Kubernetes deploy layer (kubeadm on EC2), Manifests (`infra/k8s/manifest-{staging,prod}.yaml`), Scaling, Shape, Still deferred (needs an AWS account)

### Community 39 - "RAG Pipeline: From User Context to Answer"
Cohesion: 0.22
Nodes (8): Example of a single request going through the pipeline, How it works (quick reference), Known simplifications, Pipeline stages, RAG Pipeline: From User Context to Answer, Related documents, SSE streaming, Where each stage lives in the code

### Community 40 - "Implementation Roadmap"
Cohesion: 0.22
Nodes (9): Implementation Roadmap, Phase 1 — Local prototype, Phase 2 — Reactive + Streaming, Phase 3 — Staging in AWS, Phase 3a — profiles, containers, CI (done), Phase 3b — needs an AWS account (open), Phase 4 — Production and scaling, Phase 5 — Quality iteration (+1 more)

### Community 42 - "Map of Content"
Cohesion: 0.22
Nodes (8): Daily, demo_chat Wiki, Features, Infrastructure, Linking convention, Map of Content, Plan, Structure

### Community 43 - "Dialogue Session Model (Cassandra)"
Cohesion: 0.25
Nodes (7): Dialogue Session Model (Cassandra), Example value, Key structure (implemented), Related documents, Semantic cache — decided (Phase 2), Statuses (`DialogueStatus`), Why Cassandra and not in-memory

### Community 44 - "AWS Infrastructure"
Cohesion: 0.25
Nodes (7): AWS Infrastructure, Core AWS services and their roles, Diagram (high level), Network layout (VPC), Related documents, Scaling for 500K+ users, Terraform structure (IaC)

### Community 46 - "Intent Matching and Slot Filling"
Cohesion: 0.18
Nodes (10): Output-side groundedness guardrail, Dialogue state diagram for a single intent, Example structure of one "allowed question", Example structured output from the LLM classifier, Intent Matching and Slot Filling, Related documents, Slot filling: collecting missing context, Why two levels of filtering (+2 more)

### Community 47 - "Prompt Engineering and Guardrails"
Cohesion: 0.22
Nodes (8): Example of an assembled prompt, Handling "sensitive" messages, Input-side guardrails, Output-side guardrails, Principle behind assembling the final prompt, Prompt Engineering and Guardrails, Related documents, SYSTEM/CONTEXT/USER STATE/USER MESSAGE prompt structure

### Community 48 - "infra/terraform"
Cohesion: 0.25
Nodes (7): Deploy path: Kubernetes on EC2, infra/terraform, Known gaps / TODO, Linting locally, Modules, Status: lint-only, The env-var contract

### Community 49 - "2026-08-13"
Cohesion: 0.29
Nodes (6): 2026-08-13, Chat authorization hole, Docs & graph, Flyway: standalone migration tasks, Local tooling & tests, Open / uncommitted

### Community 50 - "Frontend Chat MVP: auth, start-chat, SSE streaming"
Cohesion: 0.29
Nodes (6): Backend constraints that shaped the design, Context, Frontend Chat MVP: auth, start-chat, SSE streaming, Related documents, Verification, What was built

### Community 54 - "2026-07-13"
Cohesion: 0.33
Nodes (5): 2026-07-13, API, Data layer, Documentation, Infrastructure & tooling

### Community 55 - "2026-07-14"
Cohesion: 0.33
Nodes (5): 2026-07-14, API — RAG pipeline, Dependencies — Bedrock Converse starter, Documentation, Infrastructure & tooling

### Community 58 - "offline Spring Profile (Ollama, no AWS)"
Cohesion: 0.40
Nodes (5): offline Spring Profile (Ollama, no AWS), make verify-chat / scripts/verify-chat.sh, Colima VM Undersized for llama3.1 (OOM fix), Ollama Containerized in local/docker-compose.yml, make colima-offline + make verify-chat tooling

### Community 59 - "Vector Store Schema (Topics + Answers)"
Cohesion: 0.22
Nodes (10): Knowledge base reindex process (KnowledgeBaseIndexer.reindex), semantic_cache Qdrant collection, support_kb Qdrant collection, Collection indexes and parameters (Qdrant), Knowledge base update process, Metadata fields, Related documents, `semantic_cache` collection (Phase 2) (+2 more)

### Community 60 - "2026-07-15"
Cohesion: 0.33
Nodes (5): 2026-07-15, API — RAG pipeline Phase 2, Data layer — R2DBC migration, Documentation, Infrastructure & tooling

### Community 61 - "2026-09-04"
Cohesion: 0.33
Nodes (5): 2026-09-04, Auth: default the login/signup toggle to login, Chat: three visibility bugs found on first real UI use, all fixed, Tooling: `make stop` to kill a foregrounded local run, Working tree at end of day

### Community 62 - "infra/k8s"
Cohesion: 0.33
Nodes (5): infra/k8s, Layout, Local check, Status: lint-only, What's in a manifest

### Community 63 - "alb Module (retained reference, ECS/Fargate ALB)"
Cohesion: 1.00
Nodes (3): alb Module (retained reference, ECS/Fargate ALB), bedrock-iam Module (retained reference, ECS task/execution roles), ecs-service Module (retained reference)

### Community 92 - "Kafka Topic: <topic-name>"
Cohesion: 0.33
Nodes (5): Consumers, Kafka Topic: <topic-name>, Notes, Producers, Schema

### Community 95 - "Java Backend: Project Structure"
Cohesion: 0.33
Nodes (5): Actual package tree, Java Backend: Project Structure, Rationale for the current layers, Reactive boundaries, Related documents

### Community 96 - "Local ↔ AWS: Component Mapping"
Cohesion: 0.33
Nodes (5): docker-compose (local stack, actual), Local ↔ AWS: Component Mapping, Mapping table, Related documents, Switching via Spring Profiles (implemented)

### Community 101 - "infra/terraform README"
Cohesion: 0.25
Nodes (7): Cluster add-ons, Node AMI prerequisites, Running it, terraform-lint.yml workflow, Terraform structure (IaC layout), infra/terraform README, Known gaps / TODO list

### Community 102 - "Cassandra Table: chat_history"
Cohesion: 0.40
Nodes (4): Cassandra Table: chat_history, Columns, Notes, Used By

### Community 103 - "Cassandra Table: dialogue_state"
Cohesion: 0.40
Nodes (4): Cassandra Table: dialogue_state, Columns, Notes, Used By

### Community 104 - "Cassandra Table: <table_name>"
Cohesion: 0.40
Nodes (4): Cassandra Table: <table_name>, Columns, Notes, Used By

### Community 105 - "2026-08-31"
Cohesion: 0.40
Nodes (4): 2026-08-31, Code review of the skeleton — follow-ups before an apply, Phase 3b (no-AWS slice): Terraform skeleton + lint CI, Tooling: graphify graph rebuilt for the new infra

### Community 106 - "React Frontend: Project Structure"
Cohesion: 0.40
Nodes (4): Backend integration points, React Frontend: Project Structure, Related documents, Tree

### Community 107 - "Postgres Table: <table_name>"
Cohesion: 0.40
Nodes (4): Columns, Notes, Postgres Table: <table_name>, Used By

### Community 108 - "Postgres Table: users"
Cohesion: 0.40
Nodes (4): Columns, Notes, Postgres Table: users, Used By

### Community 109 - "Qdrant Collection: semantic_cache"
Cohesion: 0.40
Nodes (4): Notes, Payload Fields, Qdrant Collection: semantic_cache, Used By

### Community 110 - "Qdrant Collection: support_kb"
Cohesion: 0.40
Nodes (4): Notes, Payload Fields, Qdrant Collection: support_kb, Used By

### Community 111 - "Qdrant Collection: <collection_name>"
Cohesion: 0.40
Nodes (4): Notes, Payload Fields, Qdrant Collection: <collection_name>, Used By

### Community 113 - "2026-08-12"
Cohesion: 0.50
Nodes (3): 2026-08-12, Root Makefile, Wiki & tooling notes

### Community 114 - "daily-report/SKILL.md"
Cohesion: 0.50
Nodes (3): Format, Gathering what changed, Saving

## Knowledge Gaps
- **370 isolated node(s):** `name`, `private`, `version`, `type`, `dev` (+365 more)
  These have ≤1 connection - possible missing edges or undocumented components. (Counts symbols only; 455 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **38 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `docs/wiki/index.md (vault MOC)` connect `docs/wiki/index.md (vault MOC)` to `Architecture Overview (wiki doc)`, `Terraform AWS IaC (Phase 3, lint-only)`, `Plan/README.md (architecture/roadmap sub-index)`, `roadmap.md`, `demo_chat`?**
  _High betweenness centrality (0.025) - this node is a cross-community bridge._
- **Why does `ChatPipelineService` connect `ChatPipelineService` to `UserPrincipal`, `org.junit.jupiter.api.Test`, `DemoChatApplicationTests.java`?**
  _High betweenness centrality (0.025) - this node is a cross-community bridge._
- **Why does `Plan/README.md (architecture/roadmap sub-index)` connect `Plan/README.md (architecture/roadmap sub-index)` to `Architecture Overview (wiki doc)`, `Kubernetes deploy layer (kubeadm on EC2) (wiki doc)`, `roadmap.md`, `docs/wiki/index.md (vault MOC)`, `CI/CD: GitHub Actions (wiki doc)`, `demo_chat`, `AWS Infrastructure (wiki doc)`?**
  _High betweenness centrality (0.022) - this node is a cross-community bridge._
- **What connects `name`, `private`, `version` to the rest of the system?**
  _370 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `ChatPipelineService` be split into smaller, more focused modules?**
  _Cohesion score 0.07350975674953221 - nodes in this community are weakly interconnected._
- **Should `DemoChatApplicationTests.java` be split into smaller, more focused modules?**
  _Cohesion score 0.053208137715179966 - nodes in this community are weakly interconnected._
- **Should `org.junit.jupiter.api.Test` be split into smaller, more focused modules?**
  _Cohesion score 0.0633879781420765 - nodes in this community are weakly interconnected._