# Graph Report - demo_chat  (2026-07-21)

## Corpus Check
- 104 files · ~27,980 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 926 nodes · 1429 edges · 111 communities (90 shown, 21 thin omitted)
- Extraction: 86% EXTRACTED · 14% INFERRED · 0% AMBIGUOUS · INFERRED: 198 edges (avg confidence: 0.82)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `dc555a84`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- [[_COMMUNITY_Plan Docs Target Architecture|Plan Docs: Target Architecture]]
- [[_COMMUNITY_User Feature Wiki Notes|User Feature Wiki Notes]]
- [[_COMMUNITY_User API Implementation|User API Implementation]]
- [[_COMMUNITY_Chat API Implementation|Chat API Implementation]]
- [[_COMMUNITY_Chat Feature Wiki Notes|Chat Feature Wiki Notes]]
- [[_COMMUNITY_User Entity & Security Principal|User Entity & Security Principal]]
- [[_COMMUNITY_Chat Cassandra Entities|Chat Cassandra Entities]]
- [[_COMMUNITY_Auth UserDetails & Repository|Auth: UserDetails & Repository]]
- [[_COMMUNITY_Data Store Docker Services|Data Store Docker Services]]
- [[_COMMUNITY_Validation Error Handling|Validation Error Handling]]
- [[_COMMUNITY_Security Filter Chain Config|Security Filter Chain Config]]
- [[_COMMUNITY_Obsidian Catppuccin Theme|Obsidian Catppuccin Theme]]
- [[_COMMUNITY_Password Encoder Config|Password Encoder Config]]
- [[_COMMUNITY_Application Smoke Test|Application Smoke Test]]
- [[_COMMUNITY_Backend Package Structure Doc|Backend Package Structure Doc]]
- [[_COMMUNITY_Gradle Wrapper Script|Gradle Wrapper Script]]
- [[_COMMUNITY_Application Entry Point|Application Entry Point]]
- [[_COMMUNITY_Obsidian App Settings|Obsidian App Settings]]
- [[_COMMUNITY_Graphify Workflow Setup|Graphify Workflow Setup]]
- [[_COMMUNITY_Bedrock LLM Access|Bedrock LLM Access]]
- [[_COMMUNITY_Root Package & Java Skill|Root Package & Java Skill]]
- [[_COMMUNITY_Build Directory|Build Directory]]
- [[_COMMUNITY_Document Ingestion (Markdown)|Document Ingestion (Markdown)]]
- [[_COMMUNITY_Intended Architecture Statement|Intended Architecture Statement]]
- [[_COMMUNITY_Observability (Actuator)|Observability (Actuator)]]
- [[_COMMUNITY_Project Status Note|Project Status Note]]
- [[_COMMUNITY_Reactive Web Layer|Reactive Web Layer]]
- [[_COMMUNITY_Java Toolchain (Java 26)|Java Toolchain (Java 26)]]
- [[_COMMUNITY_DockerColima Fix Log|Docker/Colima Fix Log]]
- [[_COMMUNITY_JDK 26 Toolchain Fix|JDK 26 Toolchain Fix]]
- [[_COMMUNITY_JDT-LSLazyVim Fix|JDT-LS/LazyVim Fix]]
- [[_COMMUNITY_Spotless Formatting Setup|Spotless Formatting Setup]]
- [[_COMMUNITY_Kafka Topic Template|Kafka Topic Template]]
- [[_COMMUNITY_Qdrant Collection Template|Qdrant Collection Template]]
- [[_COMMUNITY_Community 35|Community 35]]
- [[_COMMUNITY_Community 36|Community 36]]
- [[_COMMUNITY_Community 37|Community 37]]
- [[_COMMUNITY_Community 38|Community 38]]
- [[_COMMUNITY_Community 39|Community 39]]
- [[_COMMUNITY_Community 40|Community 40]]
- [[_COMMUNITY_Community 41|Community 41]]
- [[_COMMUNITY_Community 42|Community 42]]
- [[_COMMUNITY_Community 43|Community 43]]
- [[_COMMUNITY_Community 44|Community 44]]
- [[_COMMUNITY_Community 45|Community 45]]
- [[_COMMUNITY_Community 46|Community 46]]
- [[_COMMUNITY_Community 47|Community 47]]
- [[_COMMUNITY_Community 48|Community 48]]
- [[_COMMUNITY_Community 49|Community 49]]
- [[_COMMUNITY_Community 50|Community 50]]
- [[_COMMUNITY_Community 51|Community 51]]
- [[_COMMUNITY_Community 52|Community 52]]
- [[_COMMUNITY_Community 53|Community 53]]
- [[_COMMUNITY_Community 54|Community 54]]
- [[_COMMUNITY_Community 55|Community 55]]
- [[_COMMUNITY_Community 56|Community 56]]
- [[_COMMUNITY_Community 57|Community 57]]
- [[_COMMUNITY_Community 58|Community 58]]
- [[_COMMUNITY_Community 59|Community 59]]
- [[_COMMUNITY_Community 60|Community 60]]
- [[_COMMUNITY_Community 61|Community 61]]
- [[_COMMUNITY_Community 62|Community 62]]
- [[_COMMUNITY_Community 63|Community 63]]
- [[_COMMUNITY_Community 64|Community 64]]
- [[_COMMUNITY_Community 65|Community 65]]
- [[_COMMUNITY_Community 66|Community 66]]
- [[_COMMUNITY_Community 67|Community 67]]
- [[_COMMUNITY_Community 68|Community 68]]
- [[_COMMUNITY_Community 69|Community 69]]
- [[_COMMUNITY_Community 70|Community 70]]
- [[_COMMUNITY_Community 71|Community 71]]
- [[_COMMUNITY_Community 72|Community 72]]
- [[_COMMUNITY_Community 73|Community 73]]
- [[_COMMUNITY_Community 79|Community 79]]
- [[_COMMUNITY_Community 85|Community 85]]
- [[_COMMUNITY_Community 86|Community 86]]
- [[_COMMUNITY_Community 87|Community 87]]
- [[_COMMUNITY_Community 88|Community 88]]
- [[_COMMUNITY_Community 89|Community 89]]
- [[_COMMUNITY_Community 92|Community 92]]
- [[_COMMUNITY_Community 93|Community 93]]
- [[_COMMUNITY_Community 94|Community 94]]
- [[_COMMUNITY_Community 95|Community 95]]
- [[_COMMUNITY_Community 96|Community 96]]
- [[_COMMUNITY_Community 97|Community 97]]
- [[_COMMUNITY_Community 98|Community 98]]
- [[_COMMUNITY_Community 99|Community 99]]
- [[_COMMUNITY_Community 100|Community 100]]
- [[_COMMUNITY_Community 101|Community 101]]
- [[_COMMUNITY_Community 102|Community 102]]
- [[_COMMUNITY_Community 103|Community 103]]
- [[_COMMUNITY_Community 104|Community 104]]
- [[_COMMUNITY_Community 105|Community 105]]
- [[_COMMUNITY_Community 106|Community 106]]
- [[_COMMUNITY_Community 107|Community 107]]
- [[_COMMUNITY_Community 108|Community 108]]

## God Nodes (most connected - your core abstractions)
1. `M` - 31 edges
2. `w()` - 19 edges
3. `ChatPipelineService` - 16 edges
4. `Ee()` - 14 edges
5. `kt()` - 13 edges
6. `User Feature` - 13 edges
7. `Support Chat (RAG-based, Scoped Intent Matching) — Project Overview` - 12 edges
8. `pc()` - 11 edges
9. `String` - 11 edges
10. `Java Backend Stack (Spring Boot 4.0.7 + WebFlux + Spring AI 2.0.0)` - 11 edges

## Surprising Connections (you probably didn't know these)
- `Cassandra Docker Service` --shares_data_with--> `chat_history Cassandra Table`  [INFERRED]
  src/main/resources/local/docker-compose.yml → docs/wiki/Infrastructure/Cassandra/chat_history.md
- `Postgres Docker Service` --shares_data_with--> `users Postgres Table`  [INFERRED]
  src/main/resources/local/docker-compose.yml → docs/wiki/Infrastructure/Postgres/users.md
- `Knowledge Sources (graphify-out + docs/wiki)` --references--> `demo_chat Wiki Map of Content`  [INFERRED]
  CLAUDE.md → docs/wiki/index.md
- `Obsidian Vault Workflow` --references--> `demo_chat Wiki Map of Content`  [INFERRED]
  CLAUDE.md → docs/wiki/index.md
- `NewJavaFile Skill` --conceptually_related_to--> `Root Package com.example.demo_chat`  [INFERRED]
  .claude/skills/NewJavaFile/SKILL.md → CLAUDE.md

## Import Cycles
- None detected.

## Hyperedges (group relationships)
- **Obsidian Wiki Template/Structure Pattern** — docs_wiki_features_template_feature_template, docs_wiki_infrastructure_cassandra_template_table_template, docs_wiki_infrastructure_postgres_template_table_template, docs_wiki_infrastructure_kafka_template_topic_template, docs_wiki_infrastructure_qdrant_template_collection_template, docs_wiki_index_moc [INFERRED 0.85]
- **Three Distinct Data Stores (Postgres/Cassandra/Qdrant)** — claude_md_relational_persistence, claude_md_chat_memory, claude_md_vector_search, claude_md_three_data_stores [EXTRACTED 1.00]
- **Chat Feature Related Code Group** — docs_wiki_features_chat_chatcontroller, docs_wiki_features_chat_chatservice, docs_wiki_features_chat_chathistory, docs_wiki_features_chat_chatmessage, docs_wiki_features_chat_chathistoryrepository [EXTRACTED 1.00]
- **Planned (Not Yet Implemented) Feature Set** — docs_wiki_docs_plan_rag_pipeline_stages, docs_wiki_docs_plan_intent_matching_two_stage_filter, docs_wiki_docs_plan_prompt_engineering_prompt_structure, docs_wiki_docs_plan_frontend_react_structure, docs_wiki_docs_plan_vector_store_schema_collection, docs_wiki_docs_plan_dialogue_state_redis_design, docs_wiki_docs_plan_infrastructure_aws_diagram, docs_wiki_docs_plan_github_actions_workflows [EXTRACTED 1.00]
- **Local-to-AWS Multi-Datastore Infrastructure Mapping** — docs_wiki_docs_plan_overview_system_components, docs_wiki_docs_plan_backend_stack, docs_wiki_docs_plan_local_vs_aws_mapping_table, docs_wiki_docs_plan_infrastructure_aws_services [INFERRED 0.85]
- **Answer Generation and Guardrail Pipeline** — docs_wiki_docs_plan_rag_pipeline_stages, docs_wiki_docs_plan_prompt_engineering_prompt_structure, docs_wiki_docs_plan_prompt_engineering_output_guardrails, docs_wiki_docs_plan_intent_matching_two_stage_filter [INFERRED 0.75]

## Communities (111 total, 21 thin omitted)

### Community 0 - "Plan Docs: Target Architecture"
Cohesion: 0.07
Nodes (52): chat/ package (ChatController, ChatService, ChatHistory, Cassandra), Package-by-feature over package-by-layer (deliberate choice), Reactive boundaries: chat/* fully reactive vs user/* blocking JPA bridged via Schedulers.boundedElastic(), Java Backend Stack (Spring Boot 4.0.7 + WebFlux + Spring AI 2.0.0), user/ package (UserController, UserService, User JPA entity), DialogueStatus state machine (NEW→INTENT_MATCHED→SLOT_FILLING→READY_TO_ANSWER→ANSWERED / OUT_OF_SCOPE→ESCALATED), Dialogue Session Model (Redis-based design), Redis key structure (session:{id}, semcache:{queryHash}, ratelimit:{userId}) (+44 more)

### Community 1 - "User Feature Wiki Notes"
Cohesion: 0.18
Nodes (11): Built com.example.demo_chat.user Package (API), BCrypt Password Hashing Decision, CreateUserRequest, Package-by-Feature Decision, PasswordEncoderConfig, Reactive/Blocking JPA Bridge Decision, User Feature, UserRepository (+3 more)

### Community 2 - "User API Implementation"
Cohesion: 0.25
Nodes (10): GetMapping, CreateUserRequest, ExceptionHandler, Mono, PostMapping, ResponseEntity, ResponseStatus, UserResponse (+2 more)

### Community 3 - "Chat API Implementation"
Cohesion: 0.17
Nodes (17): ChatController, Flux, Mono, PostMapping, ResponseEntity, SendMessageResponse, ServerSentEvent, String (+9 more)

### Community 4 - "Chat Feature Wiki Notes"
Cohesion: 0.25
Nodes (9): Chat Feature, ChatController, ChatHistoryRepository, ChatService, startChat() Generates Random UUID Decision, ValidationExceptionHandler, App-Wide ValidationExceptionHandler Decision, Feature Note Template (+1 more)

### Community 5 - "User Entity & Security Principal"
Cohesion: 0.41
Nodes (7): DialogueState, IntentDefinition, Mono, String, Void, PipelineOutcome, ChatPipelineService

### Community 6 - "Chat Cassandra Entities"
Cohesion: 0.06
Nodes (31): BeforeAll, Collection, DynamicPropertyRegistry, DynamicPropertySource, GrantedAuthority, IntentDefinition, Optional, Mono (+23 more)

### Community 7 - "Auth: UserDetails & Repository"
Cohesion: 0.24
Nodes (7): ChatHistoryRepository, AssembledPrompt, ServerSentEvent, String, Test, Predicate, ChatPipelineServiceTest

### Community 8 - "Data Store Docker Services"
Cohesion: 0.24
Nodes (10): Chat Memory via Cassandra, Messaging via Kafka, Relational Persistence via JPA/Postgres/Flyway, Three Distinct Data Stores Rationale, Vector Search via Qdrant + Advisor, Configured Postgres/Cassandra/Qdrant/Kafka Connection Properties, Cassandra Docker Service, Kafka Docker Service (+2 more)

### Community 9 - "Validation Error Handling"
Cohesion: 0.42
Nodes (5): ValidationExceptionHandler, ExceptionHandler, ResponseStatus, ProblemDetail, WebExchangeBindException

### Community 10 - "Security Filter Chain Config"
Cohesion: 0.53
Nodes (4): SecurityConfig, Bean, SecurityWebFilterChain, ServerHttpSecurity

### Community 11 - "Obsidian Catppuccin Theme"
Cohesion: 0.16
Nodes (10): ApplicationArguments, ApplicationRunner, DemoChatApplication, String, Document, IntentDefinition, IntentDefinitionRegistry, Override (+2 more)

### Community 12 - "Password Encoder Config"
Cohesion: 0.21
Nodes (9): PasswordEncoderConfig, Bean, CreateUserRequest, Mono, User, UserResponse, UUID, PasswordEncoder (+1 more)

### Community 14 - "Backend Package Structure Doc"
Cohesion: 0.21
Nodes (10): SemanticCacheVectorStoreConfig, EmbeddingModel, Bean, String, VectorStore, Mono, VectorStore, QdrantClient (+2 more)

### Community 15 - "Gradle Wrapper Script"
Cohesion: 0.18
Nodes (10): Architecture, AWS, CI/CD, Current repository structure, Data, Documentation, Environments, Plan (+2 more)

### Community 16 - "Application Entry Point"
Cohesion: 0.18
Nodes (9): Architecture (from declared dependencies), Commands, Configuration, graphify, Knowledge Sources, Module layout, Project status, Toolchain (+1 more)

### Community 17 - "Obsidian App Settings"
Cohesion: 0.20
Nodes (9): `backend-ci.yml` — stages, CI/CD: GitHub Actions, `deploy-prod.yml`, `deploy-staging.yml`, `frontend-ci.yml` — stages, GitHub Environments and protections, `knowledge-base-lint.yml`, Related documents (+1 more)

### Community 21 - "Build Directory"
Cohesion: 0.04
Nodes (31): addSettingChild(), bl(), Cl(), Cu(), de(), Dl(), ea(), fn() (+23 more)

### Community 35 - "Community 35"
Cohesion: 0.27
Nodes (7): ChatClient, IntentClassification, IntentDefinition, List, Mono, String, IntentClassificationService

### Community 36 - "Community 36"
Cohesion: 0.22
Nodes (8): Chat, Decisions, Infrastructure Used, Open Questions, Overview, Related Code, Requirements, Source Log

### Community 37 - "Community 37"
Cohesion: 0.22
Nodes (8): Decisions, <Feature Name>, Infrastructure Used, Open Questions, Overview, Related Code, Requirements, Source Log

### Community 38 - "Community 38"
Cohesion: 0.22
Nodes (8): Decisions, Feature: User, Infrastructure Used, Open Questions, Overview, Related Code, Requirements, Source Log

### Community 39 - "Community 39"
Cohesion: 0.33
Nodes (6): Document, List, Mono, String, VectorStore, KnowledgeRetrievalService

### Community 40 - "Community 40"
Cohesion: 0.25
Nodes (8): Added V1__create_users_table.sql Migration, Postgres search_path Schema Desync Bug, User Entity, UUID Primary Key Decision, Postgres Table Note Template, Flyway Schema search_path Desync, Email/Login Both Unique, No Canonical Username, users Postgres Table

### Community 41 - "Community 41"
Cohesion: 0.29
Nodes (8): ChatHistory Entity, ChatHistory Partition Key = user_id Decision, ChatMessage UDT, ChatMessage as Frozen UDT List Decision, Frozen List Tombstone Anti-Pattern, chat_history Cassandra Table, Single Row Per User Partition Key Note, Cassandra Table Note Template

### Community 42 - "Community 42"
Cohesion: 0.25
Nodes (7): Dialogue Session Model (Cassandra), Example value, Key structure (implemented), Related documents, Semantic cache — decided (Phase 2), Statuses (`DialogueStatus`), Why Cassandra and not in-memory

### Community 43 - "Community 43"
Cohesion: 0.25
Nodes (7): AWS Infrastructure, Core AWS services and their roles, Diagram (high level), Network layout (VPC), Related documents, Scaling for 500K+ users, Terraform structure (IaC)

### Community 44 - "Community 44"
Cohesion: 0.25
Nodes (7): Dialogue state diagram for a single intent, Example structure of one "allowed question", Example structured output from the LLM classifier, Intent Matching and Slot Filling, Related documents, Slot filling: collecting missing context, Why two levels of filtering

### Community 45 - "Community 45"
Cohesion: 0.25
Nodes (7): Example of an assembled prompt, Handling "sensitive" messages, Input-side guardrails, Output-side guardrails, Principle behind assembling the final prompt, Prompt Engineering and Guardrails, Related documents

### Community 46 - "Community 46"
Cohesion: 0.25
Nodes (7): Implementation Roadmap, Phase 1 — Local prototype, Phase 2 — Reactive + Streaming, Phase 3 — Staging in AWS, Phase 4 — Production and scaling, Phase 5 — Quality iteration, Related documents

### Community 47 - "Community 47"
Cohesion: 0.32
Nodes (5): IntentClassification, IntentDefinition, List, Optional, ScopeFilter

### Community 48 - "Community 48"
Cohesion: 0.22
Nodes (8): Daily, demo_chat Wiki, Features, Infrastructure, Linking convention, Map of Content, Plan, Structure

### Community 49 - "Community 49"
Cohesion: 0.25
Nodes (7): Collection indexes and parameters (Qdrant), Knowledge base update process, Metadata fields, Related documents, `semantic_cache` collection (Phase 2), `support_kb` collection, Vector Store Schema (Topics + Answers)

### Community 50 - "Community 50"
Cohesion: 0.38
Nodes (4): ChatClient, Mono, String, QueryNormalizationService

### Community 51 - "Community 51"
Cohesion: 0.33
Nodes (5): author, authorUrl, minAppVersion, name, version

### Community 52 - "Community 52"
Cohesion: 0.53
Nodes (4): ChatModel, ChatClientConfig, Bean, ChatClient

### Community 53 - "Community 53"
Cohesion: 0.40
Nodes (6): Knowledge Sources (graphify-out + docs/wiki), Obsidian Vault Workflow, daily-report Skill, 2026-07-13 Daily Session Log, Set Up docs/wiki as Obsidian Vault, demo_chat Wiki Map of Content

### Community 54 - "Community 54"
Cohesion: 0.33
Nodes (5): 2026-07-13, API, Data layer, Documentation, Infrastructure & tooling

### Community 55 - "Community 55"
Cohesion: 0.33
Nodes (5): Consumers, Kafka Topic: <topic-name>, Notes, Producers, Schema

### Community 56 - "Community 56"
Cohesion: 0.33
Nodes (5): Actual package tree, Java Backend: Project Structure, Rationale for the current layers, Reactive boundaries, Related documents

### Community 57 - "Community 57"
Cohesion: 0.33
Nodes (5): docker-compose (local stack, actual), Local ↔ AWS: Component Mapping, Mapping table, Related documents, Switching via Spring Profiles (planned — not implemented yet)

### Community 58 - "Community 58"
Cohesion: 0.33
Nodes (5): Architecture Overview, Design principles, Flow for a single message (current implementation), Related documents, System components (current)

### Community 59 - "Community 59"
Cohesion: 0.22
Nodes (8): Example of a single request going through the pipeline, How it works (quick reference), Known simplifications, Pipeline stages, RAG Pipeline: From User Context to Answer, Related documents, SSE streaming, Where each stage lives in the code

### Community 60 - "Community 60"
Cohesion: 0.40
Nodes (4): Cassandra Table: chat_history, Columns, Notes, Used By

### Community 61 - "Community 61"
Cohesion: 0.40
Nodes (4): Cassandra Table: <table_name>, Columns, Notes, Used By

### Community 62 - "Community 62"
Cohesion: 0.40
Nodes (5): GET /api/users/{id} Endpoint, POST /api/users Endpoint, Moved UserController to user Package, Duplicate Email/Login 409 Decision, UserController

### Community 63 - "Community 63"
Cohesion: 0.40
Nodes (4): Backend integration points (target — current backend endpoints differ in shape), React Frontend: Project Structure, Related documents, Tree

### Community 64 - "Community 64"
Cohesion: 0.40
Nodes (4): Columns, Notes, Postgres Table: <table_name>, Used By

### Community 65 - "Community 65"
Cohesion: 0.40
Nodes (4): Columns, Notes, Postgres Table: users, Used By

### Community 66 - "Community 66"
Cohesion: 0.40
Nodes (4): Notes, Payload Fields, Qdrant Collection: <collection_name>, Used By

### Community 67 - "Community 67"
Cohesion: 0.50
Nodes (3): Format, Gathering what changed, Saving

### Community 68 - "Community 68"
Cohesion: 0.33
Nodes (5): alwaysUpdateLinks, promptDelete, readableLineLength, strictLineBreaks, vimMode

### Community 79 - "Community 79"
Cohesion: 0.19
Nodes (9): Boolean, ChatClient, IntentDefinition, Mono, String, IntentDefinition, Test, ResponseValidator (+1 more)

### Community 85 - "Community 85"
Cohesion: 0.15
Nodes (12): AnswerGenerationService, DialogueStateRepository, IntentClassificationService, KnowledgeRetrievalService, IntentDefinitionRegistry, PromptBuilder, QueryNormalizationService, ResponseValidator (+4 more)

### Community 86 - "Community 86"
Cohesion: 0.20
Nodes (11): ChatControllerStreamTest, ChatService, MessageRequest, ChatHistory, List, Mono, String, UUID (+3 more)

### Community 87 - "Community 87"
Cohesion: 0.29
Nodes (5): List, String, Test, TextChunker, TextChunkerTest

### Community 88 - "Community 88"
Cohesion: 0.25
Nodes (7): Commands, Current status, demo_chat, Documentation, Getting started, Project structure, Tech stack

### Community 89 - "Community 89"
Cohesion: 0.33
Nodes (5): 2026-07-14, API — RAG pipeline, Dependencies — Bedrock Converse starter, Documentation, Infrastructure & tooling

### Community 92 - "Community 92"
Cohesion: 0.10
Nodes (10): ie(), ke(), M, oe(), Ot(), qe(), S(), Te() (+2 more)

### Community 93 - "Community 93"
Cohesion: 0.17
Nodes (32): Aa(), ac(), At(), Bi(), c(), Cr(), dc(), ec() (+24 more)

### Community 94 - "Community 94"
Cohesion: 0.14
Nodes (21): bu(), du(), gu(), Ht(), hu(), Je(), Ki(), lu() (+13 more)

### Community 95 - "Community 95"
Cohesion: 0.19
Nodes (12): addExportButton(), addResetButton(), clearFilter(), destroy(), filter(), me(), removeChildren(), render() (+4 more)

### Community 96 - "Community 96"
Cohesion: 0.30
Nodes (4): String, Void, Test, SemanticCacheServiceTest

### Community 97 - "Community 97"
Cohesion: 0.29
Nodes (7): ChatHistory, Document, Flux, List, SendMessageResponse, ServerSentEvent, UUID

### Community 98 - "Community 98"
Cohesion: 0.22
Nodes (7): UUID, AssembledPrompt, IntentDefinition, Map, String, DialogueState, PromptBuilder

### Community 99 - "Community 99"
Cohesion: 0.22
Nodes (7): String, IntentDefinition, List, Map, String, BeforeEach, SlotFillingService

### Community 100 - "Community 100"
Cohesion: 0.31
Nodes (11): cleanup(), clearSection(), clearSetting(), gl(), initClasses(), removeClasses(), save(), setConfig() (+3 more)

### Community 101 - "Community 101"
Cohesion: 0.22
Nodes (8): author, authorUrl, description, id, isDesktopOnly, minAppVersion, name, version

### Community 102 - "Community 102"
Cohesion: 0.32
Nodes (5): ChatClient, IntentDefinition, Mono, String, AnswerGenerationService

### Community 103 - "Community 103"
Cohesion: 0.43
Nodes (3): ChatServiceValidateParticipantIdsTest, BeforeEach, Test

### Community 104 - "Community 104"
Cohesion: 0.33
Nodes (5): 2026-07-15, API — RAG pipeline Phase 2, Data layer — R2DBC migration, Documentation, Infrastructure & tooling

### Community 105 - "Community 105"
Cohesion: 0.40
Nodes (4): Cassandra Table: dialogue_state, Columns, Notes, Used By

### Community 106 - "Community 106"
Cohesion: 0.40
Nodes (4): Notes, Payload Fields, Qdrant Collection: semantic_cache, Used By

### Community 107 - "Community 107"
Cohesion: 0.40
Nodes (4): Notes, Payload Fields, Qdrant Collection: support_kb, Used By

### Community 108 - "Community 108"
Cohesion: 0.67
Nodes (3): bc(), ka(), vc()

## Knowledge Gaps
- **285 isolated node(s):** `promptDelete`, `alwaysUpdateLinks`, `strictLineBreaks`, `vimMode`, `readableLineLength` (+280 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **21 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `ChatPipelineService` connect `User Entity & Security Principal` to `Community 97`, `Community 85`, `Auth: UserDetails & Repository`?**
  _High betweenness centrality (0.012) - this node is a cross-community bridge._
- **Why does `M` connect `Community 92` to `Community 93`, `Build Directory`, `Community 95`?**
  _High betweenness centrality (0.009) - this node is a cross-community bridge._
- **Why does `KnowledgeBaseIndexer` connect `Obsidian Catppuccin Theme` to `Chat Cassandra Entities`?**
  _High betweenness centrality (0.009) - this node is a cross-community bridge._
- **Are the 16 inferred relationships involving `w()` (e.g. with `At()` and `c()`) actually correct?**
  _`w()` has 16 INFERRED edges - model-reasoned connections that need verification._
- **Are the 7 inferred relationships involving `kt()` (e.g. with `dc()` and `ic()`) actually correct?**
  _`kt()` has 7 INFERRED edges - model-reasoned connections that need verification._
- **What connects `promptDelete`, `alwaysUpdateLinks`, `strictLineBreaks` to the rest of the system?**
  _302 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `Plan Docs: Target Architecture` be split into smaller, more focused modules?**
  _Cohesion score 0.07013574660633484 - nodes in this community are weakly interconnected._