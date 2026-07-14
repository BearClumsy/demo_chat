# Graph Report - .  (2026-07-14)

## Corpus Check
- Corpus is ~12,440 words - fits in a single context window. You may not need a graph.

## Summary
- 275 nodes · 402 edges · 35 communities (18 shown, 17 thin omitted)
- Extraction: 86% EXTRACTED · 14% INFERRED · 0% AMBIGUOUS · INFERRED: 57 edges (avg confidence: 0.87)
- Token cost: 167,590 input · 0 output

## Community Hubs (Navigation)
- Plan Docs: Target Architecture
- User Feature Wiki Notes
- User API Implementation
- Chat API Implementation
- Chat Feature Wiki Notes
- User Entity & Security Principal
- Chat Cassandra Entities
- Auth: UserDetails & Repository
- Data Store Docker Services
- Validation Error Handling
- Security Filter Chain Config
- Obsidian Catppuccin Theme
- Password Encoder Config
- Application Smoke Test
- Backend Package Structure Doc
- Gradle Wrapper Script
- Application Entry Point
- Obsidian App Settings
- Graphify Workflow Setup
- Bedrock LLM Access
- Root Package & Java Skill
- Document Ingestion (Markdown)
- Intended Architecture Statement
- Observability (Actuator)
- Project Status Note
- Reactive Web Layer
- Java Toolchain (Java 26)
- Docker/Colima Fix Log
- JDK 26 Toolchain Fix
- JDT-LS/LazyVim Fix
- Spotless Formatting Setup
- Kafka Topic Template
- Qdrant Collection Template

## God Nodes (most connected - your core abstractions)
1. `User` - 13 edges
2. `User Feature` - 13 edges
3. `Support Chat (RAG-based, Scoped Intent Matching) — Project Overview` - 12 edges
4. `Java Backend Stack (Spring Boot 4.0.7 + WebFlux + Spring AI 2.0.0)` - 11 edges
5. `Dialogue Session Model (Redis-based design)` - 11 edges
6. `UserPrincipal` - 10 edges
7. `GitHub Actions workflow file structure (planned)` - 10 edges
8. `ChatHistory` - 9 edges
9. `ChatService` - 9 edges
10. `UserService` - 9 edges

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

## Communities (35 total, 17 thin omitted)

### Community 0 - "Plan Docs: Target Architecture"
Cohesion: 0.08
Nodes (48): Java Backend Stack (Spring Boot 4.0.7 + WebFlux + Spring AI 2.0.0), DialogueStatus state machine (NEW→INTENT_MATCHED→SLOT_FILLING→READY_TO_ANSWER→ANSWERED / OUT_OF_SCOPE→ESCALATED), Dialogue Session Model (Redis-based design), Redis key structure (session:{id}, semcache:{queryHash}, ratelimit:{userId}), Planned backend integration points (SSE endpoints) vs actual current API surface, React Frontend planned project structure, backend-ci.yml stages, deploy-prod.yml (blue/green, manual approval) (+40 more)

### Community 1 - "User Feature Wiki Notes"
Cohesion: 0.08
Nodes (30): Knowledge Sources (graphify-out + docs/wiki), Obsidian Vault Workflow, daily-report Skill, Added V1__create_users_table.sql Migration, GET /api/users/{id} Endpoint, POST /api/users Endpoint, Postgres search_path Schema Desync Bug, 2026-07-13 Daily Session Log (+22 more)

### Community 2 - "User API Implementation"
Cohesion: 0.13
Nodes (17): GetMapping, CreateUserRequest, ExceptionHandler, Mono, PostMapping, RequestMapping, RequiredArgsConstructor, ResponseEntity (+9 more)

### Community 3 - "Chat API Implementation"
Cohesion: 0.12
Nodes (16): ReactiveCassandraRepository, ChatController, Mono, PostMapping, RequestMapping, RequiredArgsConstructor, ResponseEntity, RestController (+8 more)

### Community 4 - "Chat Feature Wiki Notes"
Cohesion: 0.15
Nodes (17): Chat Feature, ChatController, ChatHistory Entity, ChatHistory Partition Key = user_id Decision, ChatHistoryRepository, ChatMessage UDT, ChatMessage as Frozen UDT List Decision, ChatService (+9 more)

### Community 5 - "User Entity & Security Principal"
Cohesion: 0.20
Nodes (11): Entity, GrantedAuthority, AllArgsConstructor, Builder, Getter, NoArgsConstructor, Table, User (+3 more)

### Community 6 - "Chat Cassandra Entities"
Cohesion: 0.25
Nodes (12): ChatHistory, AllArgsConstructor, Builder, Getter, NoArgsConstructor, Table, ChatMessage, AllArgsConstructor (+4 more)

### Community 7 - "Auth: UserDetails & Repository"
Cohesion: 0.24
Nodes (9): JpaRepository, ReactiveUserDetailsService, Mono, Override, RequiredArgsConstructor, Service, UserDetails, SecurityUserDetailsService (+1 more)

### Community 8 - "Data Store Docker Services"
Cohesion: 0.24
Nodes (10): Chat Memory via Cassandra, Messaging via Kafka, Relational Persistence via JPA/Postgres/Flyway, Three Distinct Data Stores Rationale, Vector Search via Qdrant + Advisor, Configured Postgres/Cassandra/Qdrant/Kafka Connection Properties, Cassandra Docker Service, Kafka Docker Service (+2 more)

### Community 9 - "Validation Error Handling"
Cohesion: 0.38
Nodes (6): ProblemDetail, RestControllerAdvice, ExceptionHandler, ResponseStatus, ValidationExceptionHandler, WebExchangeBindException

### Community 10 - "Security Filter Chain Config"
Cohesion: 0.43
Nodes (6): EnableWebFluxSecurity, SecurityWebFilterChain, ServerHttpSecurity, Bean, Configuration, SecurityConfig

### Community 11 - "Obsidian Catppuccin Theme"
Cohesion: 0.33
Nodes (5): author, authorUrl, minAppVersion, name, version

### Community 12 - "Password Encoder Config"
Cohesion: 0.53
Nodes (4): Bean, Configuration, PasswordEncoder, PasswordEncoderConfig

### Community 13 - "Application Smoke Test"
Cohesion: 0.60
Nodes (3): SpringBootTest, DemoChatApplicationTests, Test

### Community 14 - "Backend Package Structure Doc"
Cohesion: 0.67
Nodes (4): chat/ package (ChatController, ChatService, ChatHistory, Cassandra), Package-by-feature over package-by-layer (deliberate choice), Reactive boundaries: chat/* fully reactive vs user/* blocking JPA bridged via Schedulers.boundedElastic(), user/ package (UserController, UserService, User JPA entity)

### Community 15 - "Gradle Wrapper Script"
Cohesion: 0.83
Nodes (3): gradlew script, die(), warn()

## Knowledge Gaps
- **52 isolated node(s):** `promptDelete`, `alwaysUpdateLinks`, `name`, `version`, `minAppVersion` (+47 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **17 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `UserRepository` connect `Auth: UserDetails & Repository` to `User API Implementation`, `Chat API Implementation`, `User Entity & Security Principal`?**
  _High betweenness centrality (0.061) - this node is a cross-community bridge._
- **Why does `ChatService` connect `Chat API Implementation` to `Auth: UserDetails & Repository`?**
  _High betweenness centrality (0.049) - this node is a cross-community bridge._
- **Why does `User` connect `User Entity & Security Principal` to `User API Implementation`, `Auth: UserDetails & Repository`?**
  _High betweenness centrality (0.041) - this node is a cross-community bridge._
- **Are the 3 inferred relationships involving `User Feature` (e.g. with `Built com.example.demo_chat.user Package (API)` and `Feature Note Template`) actually correct?**
  _`User Feature` has 3 INFERRED edges - model-reasoned connections that need verification._
- **Are the 3 inferred relationships involving `Dialogue Session Model (Redis-based design)` (e.g. with `chat/ package (ChatController, ChatService, ChatHistory, Cassandra)` and `Flow for a single message — current implementation (user create, chat start, add participant)`) actually correct?**
  _`Dialogue Session Model (Redis-based design)` has 3 INFERRED edges - model-reasoned connections that need verification._
- **What connects `promptDelete`, `alwaysUpdateLinks`, `name` to the rest of the system?**
  _52 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `Plan Docs: Target Architecture` be split into smaller, more focused modules?**
  _Cohesion score 0.07801418439716312 - nodes in this community are weakly interconnected._