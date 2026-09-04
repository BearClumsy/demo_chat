# Graph Report - /Users/dmitriyplatonov/Projects/demo_chat  (2026-09-04)

## Corpus Check
- 219 files · ~65,860 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 1010 nodes · 1890 edges · 101 communities (66 shown, 35 thin omitted)
- Extraction: 91% EXTRACTED · 9% INFERRED · 0% AMBIGUOUS · INFERRED: 161 edges (avg confidence: 0.73)
- Token cost: 0 input · 0 output

## Community Hubs (Navigation)
- Obsidian Style Settings Plugin
- Knowledge Base Indexer & Intents
- Terraform Modules & Project Rationale
- Client Package Manifest & ESLint Deps
- Color Picker Widget Internals
- User Entity & REST API
- React App Shell & Auth Context
- Chat Pipeline Orchestration
- Chat Controller Endpoints
- Spring Security User Details & R2DBC Tests
- Plugin Runtime Helpers
- Kubernetes Compute Pivot
- Color Model Utilities
- Architecture Overview & Roadmap
- Plugin DOM Utilities
- Kubernetes Manifests & Scaling
- Chat Participant Authorization
- Intent Classification Records
- Client Features & Config Beans
- TypeScript Compiler Config
- Chat Feature Bugs & Fixes
- 8-Stage RAG Pipeline
- Repositories & Prompt Components
- Chat Pipeline Tests
- User Auth & Flyway Migrations
- CI/CD GitHub Actions
- Start Chat Request Flow
- Qdrant Vector Store & Embeddings
- AWS Infrastructure Mapping
- Settings Tree Rendering
- Settings Persistence & CSS Variables
- Groundedness Validation Tests
- Intent JSON Validator Script
- Dialogue State & Slot Filling
- Plugin Settings Helpers
- Validation Exception Handling
- Slot Filling Service & Tests
- Plugin Manifest Metadata
- Semantic Cache Vector Store Bean
- Semantic Cache Service Tests
- WebFlux Security Config
- Start Chat Controller Tests
- Dialogue Status Enum
- Knowledge Retrieval Service
- Semantic Cache Service
- Text Chunker Tests
- Chat History Cassandra Entity
- Chat Message Cassandra UDT
- Chat Client Configuration
- Answer Generation Service
- Query Normalization Service
- SSE Stream Controller Tests
- CI Workflows & Build Aliases
- Obsidian App Settings
- Obsidian Plugin Manifest
- Password Encoder Config
- Response Validator Service
- Streaming & Guardrail Daily Logs
- Offline Ollama Profile
- Vector Store Schema
- Gradle Wrapper Script
- Kubernetes Add-on Installer
- Spring Boot Entry Point
- Retained ECS Reference Modules
- Chat Smoke Test Script
- Root Makefile Aliases
- React Chat MVP
- Local Test User Seed
- Daily Report Skill
- Cassandra Compose Services
- GitHub Actions CI Overview
- Java Rules Skill
- Spring Profiles
- Bedrock Embedding Bean Fix
- Datastore Connection Properties
- Docker/Colima Reinstall
- Users Table Migration
- Graphify Setup
- JDK 26 Toolchain Fix
- jdtls LazyVim Fix
- Postgres search_path Bug
- Spotless Java Format
- User Package Build-Out
- Obsidian Vault Setup
- Daily Log 2026-07-22
- Feature Note Template
- Cassandra Note Template
- Kafka Note Template
- Postgres Note Template
- Qdrant Note Template
- Terraform Lint Workflow
- ECS Service Reference Module
- Client HTML Entry Point
- Kafka Compose Service
- Ollama Compose Service
- Postgres Compose Service
- Qdrant Compose Service

## God Nodes (most connected - your core abstractions)
1. `ChatPipelineService` - 46 edges
2. `M` - 31 edges
3. `ChatPipelineServiceTest` - 22 edges
4. `IntentDefinition` - 21 edges
5. `AWS Infrastructure (wiki doc)` - 20 edges
6. `ChatPipelineService` - 20 edges
7. `DialogueState` - 19 edges
8. `ChatHistory` - 18 edges
9. `Kubernetes deploy layer (kubeadm on EC2) (wiki doc)` - 18 edges
10. `RAG Pipeline Design` - 18 edges

## Surprising Connections (you probably didn't know these)
- `DialogueStatus.READY_TO_ANSWER Never Assigned (rationale/gotcha)` --semantically_similar_to--> `Slot-Filling NPE: Empty Cassandra Map Reads Back Null`  [INFERRED] [semantically similar]
  CLAUDE.md → docs/wiki/Daily/2026-09-03.md
- `Flyway Dual-Configuration Gotcha (Boot vs Gradle plugin)` --semantically_similar_to--> `Flyway Doesn't Auto-Migrate on bootRun (unresolved bug)`  [INFERRED] [semantically similar]
  CLAUDE.md → docs/wiki/Daily/2026-09-03.md
- `Known gaps / TODO list` --conceptually_related_to--> `Amazon Keyspaces (Cassandra-compatible)`  [INFERRED]
  infra/terraform/README.md → docs/wiki/Plan/infrastructure.md
- `daily-report Skill` --conceptually_related_to--> `2026-07-13 Daily Session Log`  [INFERRED]
  .claude/skills/daily-report/SKILL.md → docs/wiki/Daily/2026-07-13.md
- `deploy-prod.yml (skeleton deploy workflow)` --implements--> `SSM Run Command Deploy Mechanism (rationale: CI never opens conn to kube-apiserver)`  [EXTRACTED]
  .github/workflows/deploy-prod.yml → CLAUDE.md

## Import Cycles
- None detected.

## Hyperedges (group relationships)
- **GitHub Actions CI Suite** — github_workflows_backend_ci, github_workflows_frontend_ci, github_workflows_knowledge_base_lint, github_workflows_terraform_lint [EXTRACTED 0.90]
- **8-Stage RAG Pipeline Flow** — rag_chatpipelineservice, rag_querynormalizationservice, rag_knowledgeretrievalservice, rag_intentclassificationservice, rag_scopefilter, rag_slotfillingservice, rag_promptbuilder, rag_answergenerationservice, rag_responsevalidator, rag_semanticcacheservice [EXTRACTED 1.00]
- **Kubernetes Deploy Pipeline (Terraform through SSM Apply)** — claude_k8scluster_module, claude_albk8s_module, claude_ecr_module, claude_githuboidc_module, github_workflows_deploy_staging, github_workflows_deploy_prod, github_workflows_manifests_lint [EXTRACTED 0.95]
- **var-over-explicit-type Java Convention** — claude_skills_java_rules_skill, claude, docs_wiki_daily_2026_07_22 [EXTRACTED 0.80]
- **2026-09-04 Chat Visibility Bug-Fix Session** — docs_wiki_daily_2026_09_04_chat_visibility_bugs, docs_wiki_features_chat_chatservice, docs_wiki_features_chat_chatcontroller, rag_chatpipelineservice, rag_textchunker [EXTRACTED 0.90]
- **CI-to-Kubernetes deploy pipeline via SSM** — docs_wiki_plan_github_actions_deploy_staging, docs_wiki_plan_github_actions_deploy_prod, docs_wiki_plan_kubernetes_ssm_deploy, prod_main_module_k8s_cluster [INFERRED 0.85]
- **RAG pipeline stage flow orchestrated by ChatPipelineService** — rag_chatpipelineservice, rag_querynormalizationservice, rag_semanticcacheservice, rag_knowledgeretrievalservice, rag_intentclassificationservice, rag_scopefilter, rag_slotfillingservice, rag_promptbuilder, rag_answergenerationservice, rag_responsevalidator [EXTRACTED 1.00]
- **Cluster add-on stack installed by install.sh** — infra_k8s_addons_readme_calico, infra_k8s_addons_readme_metrics_server, infra_k8s_addons_readme_ingress_nginx, infra_k8s_addons_readme_nth [EXTRACTED 1.00]
- **Deliberately duplicated per-environment config pattern** — infra_k8s_manifest_prod, infra_k8s_manifest_staging, docs_wiki_plan_kubernetes [EXTRACTED 1.00]
- **Retained Lint-Clean Reference Modules (Not Instantiated by Any Env)** — infra_terraform_modules_alb_readme_alb, infra_terraform_modules_bedrock_iam_readme_bedrock_iam, infra_terraform_modules_ecs_service_readme_ecs_service [EXTRACTED 1.00]
- **Retained ECS Fargate Reference (Not Instantiated)** — infra_terraform_modules_ecs_service_readme_ecs_service_module, alb_module, bedrock_iam_module [EXTRACTED 1.00]
- **local docker-compose services backing the local Spring profile** — modules_server_src_main_resources_local_docker_compose_postgres, modules_server_src_main_resources_local_docker_compose_cassandra, modules_server_src_main_resources_local_docker_compose_cassandra_init, modules_server_src_main_resources_local_docker_compose_qdrant, modules_server_src_main_resources_local_docker_compose_kafka, modules_server_src_main_resources_local_docker_compose_ollama [EXTRACTED 1.00]

## Communities (101 total, 35 thin omitted)

### Community 0 - "Obsidian Style Settings Plugin"
Cohesion: 0.05
Nodes (22): bc(), bl(), Cl(), Cu(), de(), dn(), fn(), Ji() (+14 more)

### Community 1 - "Knowledge Base Indexer & Intents"
Cohesion: 0.07
Nodes (29): ActiveProfiles, ApplicationArguments, ApplicationRunner, ConfigurableApplicationContext, GenericContainer, IntentDefinitionRegistry, Component, Component (+21 more)

### Community 2 - "Terraform Modules & Project Rationale"
Cohesion: 0.06
Nodes (44): Terraform module: alb (retained, lint-clean, unused), Terraform module: alb-k8s, Terraform module: bedrock-iam (retained, lint-clean, unused), Schedulers.boundedElastic() Bridging (rationale), docs/wiki/ (Obsidian vault knowledge source), Terraform module: ecr, Terraform module: ecs-service (retained, lint-clean, unused), Flyway Dual-Configuration Gotcha (Boot vs Gradle plugin) (+36 more)

### Community 3 - "Client Package Manifest & ESLint Deps"
Cohesion: 0.05
Nodes (37): eslint, @eslint/js, eslint-plugin-react-hooks, eslint-plugin-react-refresh, globals, dependencies, react, react-dom (+29 more)

### Community 4 - "Color Picker Widget Internals"
Cohesion: 0.12
Nodes (7): destroy(), ie(), M, Ot(), S(), V(), xe()

### Community 5 - "User Entity & REST API"
Cohesion: 0.11
Nodes (23): GetMapping, CreateUserRequest, AllArgsConstructor, Builder, Getter, NoArgsConstructor, Table, User (+15 more)

### Community 6 - "React App Shell & Auth Context"
Cohesion: 0.12
Nodes (22): RFC-7807, AppShell(), AuthContext, AuthContextValue, AuthProvider(), Credentials, useAuth(), AuthPage() (+14 more)

### Community 7 - "Chat Pipeline Orchestration"
Cohesion: 0.18
Nodes (13): ChatPipelineService, Document, Flux, Mono, ServerSentEvent, Service, PipelineOutcome, DialogueState (+5 more)

### Community 8 - "Chat Controller Endpoints"
Cohesion: 0.15
Nodes (15): GrantedAuthority, ChatController, Flux, Mono, PostMapping, RequestMapping, RequiredArgsConstructor, ResponseEntity (+7 more)

### Community 9 - "Spring Security User Details & R2DBC Tests"
Cohesion: 0.13
Nodes (16): DataR2dbcTest, Mono, Override, RequiredArgsConstructor, Service, UserDetails, SecurityUserDetailsService, Mono (+8 more)

### Community 10 - "Plugin Runtime Helpers"
Cohesion: 0.17
Nodes (24): At(), Bi(), c(), Cr(), dc(), Ee(), Er(), Fc() (+16 more)

### Community 11 - "Kubernetes Compute Pivot"
Cohesion: 0.11
Nodes (23): terraform-lint.yml workflow, Compute pivot to kubeadm on EC2 (2026-09-03), ECS Fargate (superseded compute target), Terraform structure (IaC layout), Kubernetes deploy layer (kubeadm on EC2) (wiki doc), modules/alb-k8s (Terraform), Calico CNI (VXLAN mode), cluster-autoscaler (optional) (+15 more)

### Community 12 - "Color Model Utilities"
Cohesion: 0.14
Nodes (18): ea(), ec(), f(), gl(), gn(), Il(), ke(), L() (+10 more)

### Community 13 - "Architecture Overview & Roadmap"
Cohesion: 0.13
Nodes (21): Semantic cache as read-caching for scale, Local <-> AWS: Component Mapping (wiki doc), Local docker-compose stack, local,offline profile (Ollama, no AWS), Switching via Spring Profiles, Architecture Overview (wiki doc), Design principles, Flow for a single message (+13 more)

### Community 14 - "Plugin DOM Utilities"
Cohesion: 0.14
Nodes (20): bu(), du(), gu(), Ht(), hu(), Je(), Ki(), lu() (+12 more)

### Community 15 - "Kubernetes Manifests & Scaling"
Cohesion: 0.12
Nodes (18): HPA scaling (CPU 70%), infra/k8s manifests (staging/prod), ConfigMap demo-chat-config (prod), Deployment demo-chat-client (prod, 2 replicas), Deployment demo-chat-server (prod, 3 replicas), HorizontalPodAutoscaler (prod, server 3->10 / client 2->4), Ingress demo-chat-web/api (prod), Job demo-chat-kb-bootstrap (prod) (+10 more)

### Community 16 - "Chat Participant Authorization"
Cohesion: 0.22
Nodes (9): ChatService, Mono, RequiredArgsConstructor, Service, UserRepository, ChatServiceValidateParticipantIdsTest, BeforeEach, Test (+1 more)

### Community 17 - "Intent Classification Records"
Cohesion: 0.18
Nodes (9): AssembledPrompt, IntentClassification, JsonNaming, IntentClassificationService, ChatClient, Mono, Service, IntentDefinition (+1 more)

### Community 18 - "Client Features & Config Beans"
Cohesion: 0.12
Nodes (18): AuthContext (in-memory {userId, login, password}), AuthPage (signup + login form), chatApi (hand-rolled SSE frame parser over fetch), ChatWindow / MessageBubble, StartChatForm, useChatStream hook, Vite dev proxy (/api -> localhost:8080), ValidationExceptionHandler (@RestControllerAdvice) (+10 more)

### Community 19 - "TypeScript Compiler Config"
Cohesion: 0.11
Nodes (17): compilerOptions, isolatedModules, jsx, lib, module, moduleResolution, noEmit, resolveJsonModule (+9 more)

### Community 20 - "Chat Feature Bugs & Fixes"
Cohesion: 0.18
Nodes (17): Chat authorization hole (addParticipant bug, 2026-08-13), DialogueStatus.READY_TO_ANSWER Never Assigned (rationale/gotcha), SSE Streaming Endpoint (buffer-then-chunk), SecurityConfig (WebFlux HTTP Basic), Jackson 3 java.time.Instant Deserialization Bug, participantIds Made Optional (assistant-only chats), Slot-Filling NPE: Empty Cassandra Map Reads Back Null, Three Chat Visibility Bugs Fixed (no reply, duplicate bubble, garbled text) (+9 more)

### Community 21 - "8-Stage RAG Pipeline"
Cohesion: 0.28
Nodes (16): 8-Stage RAG Pipeline, Semantic-Cache Lookup (non-numbered short-circuit stage), RAG Pipeline Design, AnswerGenerationService (stage 7), AssembledPrompt record, ChatPipelineService, GroundednessCheck record, IntentClassification record (+8 more)

### Community 22 - "Repositories & Prompt Components"
Cohesion: 0.17
Nodes (9): ChatHistoryRepository, DialogueStateRepository, Component, PromptBuilder, Component, ScopeFilter, Component, TextChunker (+1 more)

### Community 23 - "Chat Pipeline Tests"
Cohesion: 0.28
Nodes (5): SendMessageResponse, ChatPipelineServiceTest, BeforeEach, ServerSentEvent, Test

### Community 24 - "User Auth & Flyway Migrations"
Cohesion: 0.25
Nodes (14): PasswordEncoderConfig (BCrypt), Daily Log 2026-08-13, Daily Log 2026-08-20, User Feature, SecurityUserDetailsService, User R2DBC entity, UserPrincipal, UserRepository (R2dbcRepository) (+6 more)

### Community 25 - "CI/CD GitHub Actions"
Cohesion: 0.25
Nodes (14): CI/CD: GitHub Actions (wiki doc), backend-ci.yml workflow, deploy-prod.yml (skeleton), deploy-staging.yml (skeleton), frontend-ci.yml workflow, GitHub Environments & protections, knowledge-base-lint.yml workflow, manifests-lint workflow (+6 more)

### Community 26 - "Start Chat Request Flow"
Cohesion: 0.25
Nodes (5): MessageRequest, StartChatRequest, ChatServiceStartChatTest, BeforeEach, Test

### Community 27 - "Qdrant Vector Store & Embeddings"
Cohesion: 0.26
Nodes (13): Bedrock Titan embedding model (amazon.titan-embed-text-v2:0, 768d), KB Bootstrap Job (--reindex-and-exit), Qdrant semantic_cache Collection, Qdrant support_kb Collection, Non-UUID Qdrant Point Id Bug (KnowledgeBaseIndexer), semantic_cache (Qdrant collection), support_kb (Qdrant collection), Plan/vector-store-schema.md (+5 more)

### Community 28 - "AWS Infrastructure Mapping"
Cohesion: 0.22
Nodes (13): AWS Infrastructure (wiki doc), ALB (load balancing), Amazon Bedrock (managed LLM), CloudFront CDN, CloudWatch (logs/metrics/alarms), ECR (Docker image registry), Amazon Keyspaces (Cassandra-compatible), Qdrant on EC2/ECS (self-managed vector store) (+5 more)

### Community 29 - "Settings Tree Rendering"
Cohesion: 0.24
Nodes (12): addExportButton(), addResetButton(), clearFilter(), filter(), getAllChildrenIds(), me(), removeChildren(), render() (+4 more)

### Community 30 - "Settings Persistence & CSS Variables"
Cohesion: 0.31
Nodes (11): cleanup(), clearSection(), clearSetting(), getSetting(), initClasses(), removeClasses(), save(), setConfig() (+3 more)

### Community 31 - "Groundedness Validation Tests"
Cohesion: 0.35
Nodes (5): GroundednessCheck, JsonNaming, ChatClient, Test, ResponseValidatorTest

### Community 32 - "Intent JSON Validator Script"
Cohesion: 0.22
Nodes (10): ARRAY_FIELDS, BOOLEAN_FIELDS, dir, errors, fail(), KNOWN_FIELDS, seenIntentIds, seenQuestions (+2 more)

### Community 33 - "Dialogue State & Slot Filling"
Cohesion: 0.29
Nodes (10): dialogue_state (Cassandra table), Dialogue Session Model, Intent Matching and Slot Filling, Prompt Engineering and Guardrails, Output-side groundedness guardrail, SYSTEM/CONTEXT/USER STATE/USER MESSAGE prompt structure, DialogueState entity, DialogueStatus enum (+2 more)

### Community 34 - "Plugin Settings Helpers"
Cohesion: 0.20
Nodes (10): a(), Aa(), addSettingChild(), Dl(), hl(), Ni(), Rl(), vu() (+2 more)

### Community 35 - "Validation Exception Handling"
Cohesion: 0.38
Nodes (6): ExceptionHandler, ResponseStatus, ValidationExceptionHandler, ProblemDetail, RestControllerAdvice, WebExchangeBindException

### Community 36 - "Slot Filling Service & Tests"
Cohesion: 0.36
Nodes (4): Component, SlotFillingService, Test, SlotFillingServiceTest

### Community 37 - "Plugin Manifest Metadata"
Cohesion: 0.22
Nodes (8): author, authorUrl, description, id, isDesktopOnly, minAppVersion, name, version

### Community 38 - "Semantic Cache Vector Store Bean"
Cohesion: 0.39
Nodes (7): Bean, Configuration, EmbeddingModel, VectorStore, SemanticCacheVectorStoreConfig, QdrantClient, Qualifier

### Community 39 - "Semantic Cache Service Tests"
Cohesion: 0.39
Nodes (3): Test, VectorStore, SemanticCacheServiceTest

### Community 40 - "WebFlux Security Config"
Cohesion: 0.43
Nodes (6): EnableWebFluxSecurity, Bean, Configuration, SecurityConfig, SecurityWebFilterChain, ServerHttpSecurity

### Community 41 - "Start Chat Controller Tests"
Cohesion: 0.36
Nodes (4): StartChatResponse, ChatControllerStartChatTest, Test, WebTestClient

### Community 42 - "Dialogue Status Enum"
Cohesion: 0.25
Nodes (7): DialogueStatus, ANSWERED, ESCALATED, NEW, OUT_OF_SCOPE, READY_TO_ANSWER, SLOT_FILLING

### Community 43 - "Knowledge Retrieval Service"
Cohesion: 0.43
Nodes (5): Document, Mono, Service, VectorStore, KnowledgeRetrievalService

### Community 44 - "Semantic Cache Service"
Cohesion: 0.43
Nodes (4): Mono, Service, VectorStore, SemanticCacheService

### Community 46 - "Chat History Cassandra Entity"
Cohesion: 0.52
Nodes (6): ChatHistory, AllArgsConstructor, Builder, Getter, NoArgsConstructor, Table

### Community 47 - "Chat Message Cassandra UDT"
Cohesion: 0.52
Nodes (6): ChatMessage, AllArgsConstructor, Builder, Getter, NoArgsConstructor, UserDefinedType

### Community 48 - "Chat Client Configuration"
Cohesion: 0.48
Nodes (5): ChatClientConfig, Bean, ChatClient, ChatModel, Configuration

### Community 49 - "Answer Generation Service"
Cohesion: 0.48
Nodes (4): AnswerGenerationService, ChatClient, Mono, Service

### Community 50 - "Query Normalization Service"
Cohesion: 0.48
Nodes (4): ChatClient, Mono, Service, QueryNormalizationService

### Community 51 - "SSE Stream Controller Tests"
Cohesion: 0.52
Nodes (3): ChatControllerStreamTest, Test, WebTestClient

### Community 52 - "CI Workflows & Build Aliases"
Cohesion: 0.47
Nodes (5): Daily Log 2026-08-12, backend-ci Workflow, frontend-ci Workflow, knowledge-base-lint Workflow, Thin-Alias Root Makefile

### Community 53 - "Obsidian App Settings"
Cohesion: 0.33
Nodes (5): alwaysUpdateLinks, promptDelete, readableLineLength, strictLineBreaks, vimMode

### Community 54 - "Obsidian Plugin Manifest"
Cohesion: 0.33
Nodes (5): author, authorUrl, minAppVersion, name, version

### Community 55 - "Password Encoder Config"
Cohesion: 0.53
Nodes (4): Bean, Configuration, PasswordEncoder, PasswordEncoderConfig

### Community 56 - "Response Validator Service"
Cohesion: 0.53
Nodes (4): ChatClient, Mono, Service, ResponseValidator

### Community 57 - "Streaming & Guardrail Daily Logs"
Cohesion: 0.60
Nodes (5): Buffer-Then-Chunk SSE Streaming, Daily Log 2026-07-14, Daily Log 2026-07-15, Output-Side Groundedness Guardrail, JPA-to-R2DBC Migration

### Community 58 - "Offline Ollama Profile"
Cohesion: 0.40
Nodes (5): offline Spring Profile (Ollama, no AWS), make verify-chat / scripts/verify-chat.sh, Colima VM Undersized for llama3.1 (OOM fix), Ollama Containerized in local/docker-compose.yml, make colima-offline + make verify-chat tooling

### Community 59 - "Vector Store Schema"
Cohesion: 0.83
Nodes (4): Vector Store Schema (Topics + Answers), Knowledge base reindex process (KnowledgeBaseIndexer.reindex), semantic_cache Qdrant collection, support_kb Qdrant collection

### Community 60 - "Gradle Wrapper Script"
Cohesion: 0.83
Nodes (3): gradlew script, die(), warn()

### Community 61 - "Kubernetes Add-on Installer"
Cohesion: 0.83
Nodes (3): kubectl_apply(), install.sh script, wait_rollout()

### Community 63 - "Retained ECS Reference Modules"
Cohesion: 1.00
Nodes (3): alb Module (retained reference, ECS/Fargate ALB), bedrock-iam Module (retained reference, ECS task/execution roles), ecs-service Module (retained reference)

## Knowledge Gaps
- **157 isolated node(s):** `promptDelete`, `alwaysUpdateLinks`, `strictLineBreaks`, `vimMode`, `readableLineLength` (+152 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **35 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `ChatService` connect `Chat Feature Bugs & Fixes` to `User Auth & Flyway Migrations`, `8-Stage RAG Pipeline`, `Chat History Cassandra Entity`?**
  _High betweenness centrality (0.129) - this node is a cross-community bridge._
- **Why does `ChatHistory` connect `Chat History Cassandra Entity` to `Chat Pipeline Orchestration`, `Chat Message Cassandra UDT`, `Chat Participant Authorization`, `Intent Classification Records`, `SSE Stream Controller Tests`, `Repositories & Prompt Components`, `Start Chat Request Flow`?**
  _High betweenness centrality (0.088) - this node is a cross-community bridge._
- **Why does `docs/wiki/index.md (vault MOC)` connect `Terraform Modules & Project Rationale` to `Chat Feature Bugs & Fixes`, `Architecture Overview & Roadmap`?**
  _High betweenness centrality (0.077) - this node is a cross-community bridge._
- **What connects `promptDelete`, `alwaysUpdateLinks`, `strictLineBreaks` to the rest of the system?**
  _157 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `Obsidian Style Settings Plugin` be split into smaller, more focused modules?**
  _Cohesion score 0.05142857142857143 - nodes in this community are weakly interconnected._
- **Should `Knowledge Base Indexer & Intents` be split into smaller, more focused modules?**
  _Cohesion score 0.07400555041628122 - nodes in this community are weakly interconnected._
- **Should `Terraform Modules & Project Rationale` be split into smaller, more focused modules?**
  _Cohesion score 0.05813953488372093 - nodes in this community are weakly interconnected._