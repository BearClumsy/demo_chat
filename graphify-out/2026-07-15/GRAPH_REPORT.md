# Graph Report - demo_chat  (2026-07-14)

## Corpus Check
- 81 files · ~15,509 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 581 nodes · 738 edges · 85 communities (63 shown, 22 thin omitted)
- Extraction: 88% EXTRACTED · 12% INFERRED · 0% AMBIGUOUS · INFERRED: 92 edges (avg confidence: 0.85)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `db4edbf7`
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

## God Nodes (most connected - your core abstractions)
1. `User Feature` - 13 edges
2. `ChatPipelineService` - 12 edges
3. `Support Chat (RAG-based, Scoped Intent Matching) — Project Overview` - 12 edges
4. `Java Backend Stack (Spring Boot 4.0.7 + WebFlux + Spring AI 2.0.0)` - 11 edges
5. `Dialogue Session Model (Redis-based design)` - 11 edges
6. `GitHub Actions workflow file structure (planned)` - 10 edges
7. `Mono` - 9 edges
8. `CI/CD: GitHub Actions` - 9 edges
9. `Chat Feature` - 9 edges
10. `demo_chat Wiki Map of Content` - 9 edges

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

## Communities (85 total, 22 thin omitted)

### Community 0 - "Plan Docs: Target Architecture"
Cohesion: 0.08
Nodes (48): Java Backend Stack (Spring Boot 4.0.7 + WebFlux + Spring AI 2.0.0), DialogueStatus state machine (NEW→INTENT_MATCHED→SLOT_FILLING→READY_TO_ANSWER→ANSWERED / OUT_OF_SCOPE→ESCALATED), Dialogue Session Model (Redis-based design), Redis key structure (session:{id}, semcache:{queryHash}, ratelimit:{userId}), Planned backend integration points (SSE endpoints) vs actual current API surface, React Frontend planned project structure, backend-ci.yml stages, deploy-prod.yml (blue/green, manual approval) (+40 more)

### Community 1 - "User Feature Wiki Notes"
Cohesion: 0.18
Nodes (11): Built com.example.demo_chat.user Package (API), BCrypt Password Hashing Decision, CreateUserRequest, Package-by-Feature Decision, PasswordEncoderConfig, Reactive/Blocking JPA Bridge Decision, User Feature, UserRepository (+3 more)

### Community 2 - "User API Implementation"
Cohesion: 0.25
Nodes (10): GetMapping, CreateUserRequest, ExceptionHandler, Mono, PostMapping, ResponseEntity, ResponseStatus, UserResponse (+2 more)

### Community 3 - "Chat API Implementation"
Cohesion: 0.10
Nodes (22): ChatController, GrantedAuthority, ParticipantRequest, SendMessageRequest, Mono, PostMapping, ResponseEntity, SendMessageResponse (+14 more)

### Community 4 - "Chat Feature Wiki Notes"
Cohesion: 0.25
Nodes (9): Chat Feature, ChatController, ChatHistoryRepository, ChatService, startChat() Generates Random UUID Decision, ValidationExceptionHandler, App-Wide ValidationExceptionHandler Decision, Feature Note Template (+1 more)

### Community 5 - "User Entity & Security Principal"
Cohesion: 0.07
Nodes (39): AnswerGenerationService, ChatHistoryRepository, DialogueState, DialogueStateRepository, IntentClassificationService, KnowledgeRetrievalService, PipelineOutcome, PromptBuilder (+31 more)

### Community 6 - "Chat Cassandra Entities"
Cohesion: 0.13
Nodes (16): ChatService, Collection, MessageRequest, ObjectMapper, PostConstruct, IntentDefinitionRegistry, Resource, ChatHistory (+8 more)

### Community 7 - "Auth: UserDetails & Repository"
Cohesion: 0.19
Nodes (9): ReactiveUserDetailsService, Mono, Override, String, Optional, String, User, SecurityUserDetailsService (+1 more)

### Community 8 - "Data Store Docker Services"
Cohesion: 0.24
Nodes (10): Chat Memory via Cassandra, Messaging via Kafka, Relational Persistence via JPA/Postgres/Flyway, Three Distinct Data Stores Rationale, Vector Search via Qdrant + Advisor, Configured Postgres/Cassandra/Qdrant/Kafka Connection Properties, Cassandra Docker Service, Kafka Docker Service (+2 more)

### Community 9 - "Validation Error Handling"
Cohesion: 0.42
Nodes (5): ValidationExceptionHandler, ProblemDetail, ExceptionHandler, ResponseStatus, WebExchangeBindException

### Community 10 - "Security Filter Chain Config"
Cohesion: 0.53
Nodes (4): SecurityConfig, SecurityWebFilterChain, ServerHttpSecurity, Bean

### Community 11 - "Obsidian Catppuccin Theme"
Cohesion: 0.16
Nodes (10): ApplicationArguments, ApplicationRunner, DemoChatApplication, KnowledgeBaseIndexer, String, Document, IntentDefinition, IntentDefinitionRegistry (+2 more)

### Community 12 - "Password Encoder Config"
Cohesion: 0.21
Nodes (9): PasswordEncoderConfig, PasswordEncoder, Bean, CreateUserRequest, Mono, User, UserResponse, UUID (+1 more)

### Community 14 - "Backend Package Structure Doc"
Cohesion: 0.67
Nodes (4): chat/ package (ChatController, ChatService, ChatHistory, Cassandra), Package-by-feature over package-by-layer (deliberate choice), Reactive boundaries: chat/* fully reactive vs user/* blocking JPA bridged via Schedulers.boundedElastic(), user/ package (UserController, UserService, User JPA entity)

### Community 15 - "Gradle Wrapper Script"
Cohesion: 0.18
Nodes (10): Architecture, AWS, CI/CD, Current repository structure, Data, Documentation, Environments, Plan (+2 more)

### Community 16 - "Application Entry Point"
Cohesion: 0.20
Nodes (8): Commands, Configuration, graphify, Intended architecture (from declared dependencies), Knowledge Sources, Project status, Toolchain, Working with this Vault

### Community 17 - "Obsidian App Settings"
Cohesion: 0.20
Nodes (9): `backend-ci.yml` — stages, CI/CD: GitHub Actions, `deploy-prod.yml`, `deploy-staging.yml`, `frontend-ci.yml` — stages, GitHub Environments and protections, `knowledge-base-lint.yml`, Related documents (+1 more)

### Community 35 - "Community 35"
Cohesion: 0.27
Nodes (7): IntentClassificationService, ChatClient, IntentClassification, IntentDefinition, List, Mono, String

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
Nodes (6): KnowledgeRetrievalService, Document, List, Mono, String, VectorStore

### Community 40 - "Community 40"
Cohesion: 0.25
Nodes (8): Added V1__create_users_table.sql Migration, Postgres search_path Schema Desync Bug, User Entity, UUID Primary Key Decision, Postgres Table Note Template, Flyway Schema search_path Desync, Email/Login Both Unique, No Canonical Username, users Postgres Table

### Community 41 - "Community 41"
Cohesion: 0.29
Nodes (8): ChatHistory Entity, ChatHistory Partition Key = user_id Decision, ChatMessage UDT, ChatMessage as Frozen UDT List Decision, Frozen List Tombstone Anti-Pattern, chat_history Cassandra Table, Single Row Per User Partition Key Note, Cassandra Table Note Template

### Community 42 - "Community 42"
Cohesion: 0.25
Nodes (7): Dialogue Session Model (Cassandra), Example value, Key structure (target), Related documents, Semantic cache and rate limiting — not yet decided, Statuses (`DialogueStatus`), Why Cassandra and not in-memory

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
Nodes (5): ScopeFilter, IntentClassification, IntentDefinition, List, Optional

### Community 48 - "Community 48"
Cohesion: 0.25
Nodes (7): Daily, demo_chat Wiki, Features, Infrastructure, Linking convention, Map of Content, Structure

### Community 49 - "Community 49"
Cohesion: 0.29
Nodes (6): Collection indexes and parameters (Qdrant), Knowledge base update process, Metadata fields, Related documents, `support_kb` collection, Vector Store Schema (Topics + Answers)

### Community 50 - "Community 50"
Cohesion: 0.38
Nodes (4): QueryNormalizationService, ChatClient, Mono, String

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
Cohesion: 0.33
Nodes (5): Example of a single request going through the pipeline, Pipeline stages, RAG Pipeline: From User Context to Answer, Related documents, Where each stage lives in the code (see backend structure)

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
Nodes (4): Backend integration points (target — current backend endpoints differ), React Frontend: Project Structure, Related documents, Tree

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

## Knowledge Gaps
- **231 isolated node(s):** `promptDelete`, `alwaysUpdateLinks`, `name`, `version`, `minAppVersion` (+226 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **22 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `UserDetails` connect `Chat API Implementation` to `Auth: UserDetails & Repository`?**
  _High betweenness centrality (0.016) - this node is a cross-community bridge._
- **Are the 3 inferred relationships involving `User Feature` (e.g. with `Built com.example.demo_chat.user Package (API)` and `Feature Note Template`) actually correct?**
  _`User Feature` has 3 INFERRED edges - model-reasoned connections that need verification._
- **Are the 3 inferred relationships involving `Dialogue Session Model (Redis-based design)` (e.g. with `chat/ package (ChatController, ChatService, ChatHistory, Cassandra)` and `Flow for a single message — current implementation (user create, chat start, add participant)`) actually correct?**
  _`Dialogue Session Model (Redis-based design)` has 3 INFERRED edges - model-reasoned connections that need verification._
- **What connects `promptDelete`, `alwaysUpdateLinks`, `name` to the rest of the system?**
  _248 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `Plan Docs: Target Architecture` be split into smaller, more focused modules?**
  _Cohesion score 0.07801418439716312 - nodes in this community are weakly interconnected._
- **Should `Chat API Implementation` be split into smaller, more focused modules?**
  _Cohesion score 0.10227272727272728 - nodes in this community are weakly interconnected._
- **Should `User Entity & Security Principal` be split into smaller, more focused modules?**
  _Cohesion score 0.06721311475409836 - nodes in this community are weakly interconnected._