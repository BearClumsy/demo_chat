# Graph Report - .  (2026-08-31)

## Corpus Check
- Corpus is ~46,556 words - fits in a single context window. You may not need a graph.

## Summary
- 960 nodes · 1869 edges · 79 communities (55 shown, 24 thin omitted)
- Extraction: 90% EXTRACTED · 10% INFERRED · 0% AMBIGUOUS · INFERRED: 193 edges (avg confidence: 0.74)
- Token cost: 563,696 input · 0 output

## Community Hubs (Navigation)
- Chat API Controller & History Entity
- Obsidian Style-Settings Plugin (minified)
- User Registration API
- Client Build & Lint Dependencies
- React Client App Shell & Auth Context
- Pickr Colour-Picker Widget (minified)
- Terraform Env Roots & ALB/Bedrock Modules
- ECS Service & Infra Wiring Rationale
- Chat Pipeline Orchestration & SSE
- Groundedness Check & Intent JSON Validation
- User Persistence, Flyway & R2DBC Tests
- Intent Classification & Prompt Assembly
- Testcontainers Application-Context Test
- 8-Stage RAG Pipeline Design
- Minified Client Bundle Helpers A
- Knowledge Base Indexing & Retrieval
- Project Docs, CI Workflows & Daily Logs
- Minified Colour Utility Functions
- Minified Client Bundle Helpers B
- AWS Infrastructure Overview & TF Modules
- TypeScript Compiler Config
- Answer Generation & Pipeline Tests
- Chat History Storage & Dialogue State
- Client Chat UI & User Endpoints
- Style-Settings Tree Rendering (minified)
- Cassandra Repositories & Slot Filling
- Style-Settings Config Persistence (minified)
- Terraform VPC Module (three-tier subnets)
- Second Qdrant VectorStore Bean
- Minified Client Bundle Helpers C
- Intent Definition Registry
- Validation Exception Handler
- Reactive Security UserDetailsService
- Obsidian Plugin Manifest
- Semantic Cache Service Test
- WebFlux Security & Auth Config
- Backend/Frontend Plan Docs & Config Beans
- SecurityConfig Filter Chain
- ChatClient Config Bean
- DialogueStatus Enum
- Semantic Cache Service
- TextChunker Test
- Query Normalization Service
- Response Validator (guardrail)
- Knowledge Base & Deploy Workflow Docs
- Obsidian App Settings
- Obsidian Style-Settings Manifest
- Password Encoder Config (BCrypt)
- Chat Controller/Service Auth Routing
- Gradle Wrapper Script
- Spring Boot Application Entry Point
- Server Build (Flyway + Spring AI BOM)
- Daily-Report Skill & Log
- startChat Controller/Service Path
- Cassandra Table Note Template
- Catppuccin Theme Manifest
- Graphify Enforcement PreToolUse Hook
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
- Gradle Wrapper Script (gradlew)
- Kafka Topic Note Template
- Local Kafka compose service
- Obsidian Core Plugins Enabled
- Postgres Table Note Template
- Qdrant Collection Note Template

## God Nodes (most connected - your core abstractions)
1. `ChatPipelineService` - 46 edges
2. `M` - 31 edges
3. `IntentDefinition` - 27 edges
4. `DemoChatApplicationTests` - 24 edges
5. `ChatHistory` - 21 edges
6. `ChatPipelineServiceTest` - 21 edges
7. `RAG Pipeline Design` - 20 edges
8. `User (R2DBC entity)` - 19 edges
9. `ChatPipelineService` - 19 edges
10. `DialogueState` - 18 edges

## Surprising Connections (you probably didn't know these)
- `java-rules Skill` --semantically_similar_to--> `CLAUDE.md Project Guide`  [INFERRED] [semantically similar]
  .claude/skills/java-rules/SKILL.md → CLAUDE.md
- `change_shipping_address intent` --conceptually_related_to--> `IntentDefinition`  [INFERRED]
  modules/server/src/main/resources/knowledge-base/intents/change_shipping_address.json → docs/wiki/Plan/rag-pipeline.md
- `order_status intent` --conceptually_related_to--> `IntentDefinition`  [INFERRED]
  modules/server/src/main/resources/knowledge-base/intents/order_status.json → docs/wiki/Plan/rag-pipeline.md
- `refund_status intent` --conceptually_related_to--> `IntentDefinition`  [INFERRED]
  modules/server/src/main/resources/knowledge-base/intents/refund_status.json → docs/wiki/Plan/rag-pipeline.md
- `bedrock-iam module` --shares_data_with--> `container_env output <-> application-*.properties env-var contract`  [INFERRED]
  infra/terraform/modules/bedrock-iam/outputs.tf → docs/wiki/Daily/2026-08-31.md

## Import Cycles
- None detected.

## Hyperedges (group relationships)
- **GitHub Actions CI Suite** — github_workflows_backend_ci, github_workflows_frontend_ci, github_workflows_knowledge_base_lint, github_workflows_terraform_lint [EXTRACTED 0.90]
- **8-Stage RAG Turn Flow** — rag_pipeline, support_kb, semantic_cache, output_guardrail [EXTRACTED 0.90]
- **var-over-explicit-type Java Convention** — claude_skills_java_rules_skill, claude, docs_wiki_daily_2026_07_22 [EXTRACTED 0.80]
- **RAG pipeline stage flow orchestrated by ChatPipelineService** — rag_chatpipelineservice, rag_querynormalizationservice, rag_semanticcacheservice, rag_knowledgeretrievalservice, rag_intentclassificationservice, rag_scopefilter, rag_slotfillingservice, rag_promptbuilder, rag_answergenerationservice, rag_responsevalidator [EXTRACTED 1.00]
- **Storage split: Postgres (users) vs Cassandra (chat/dialogue) vs Qdrant (vectors)** — docs_wiki_infrastructure_postgres_users, docs_wiki_infrastructure_cassandra_chat_history, docs_wiki_infrastructure_cassandra_dialogue_state, docs_wiki_infrastructure_qdrant_support_kb, docs_wiki_infrastructure_qdrant_semantic_cache [EXTRACTED 1.00]
- **Terraform skeleton modules under infra/terraform** — infra_terraform_skeleton, tf_module_vpc, tf_module_alb, tf_module_ecs_service, tf_module_rds_postgres, tf_module_keyspaces, tf_module_qdrant_ec2, tf_module_msk, tf_module_bedrock_iam, tf_envs_staging_prod [EXTRACTED 1.00]
- **prod env root composes all eight Terraform modules** — infra_terraform_envs_prod_main_env_root, infra_terraform_modules_vpc_module, modules_alb_module, modules_bedrock_iam_module, infra_terraform_modules_rds_postgres_module, infra_terraform_modules_keyspaces_module, infra_terraform_modules_qdrant_ec2_module, infra_terraform_modules_msk_module, infra_terraform_modules_ecs_service_module [EXTRACTED 1.00]
- **Data-tier modules allow app subnet CIDRs instead of the ECS task SG** — infra_terraform_modules_rds_postgres_module, infra_terraform_modules_qdrant_ec2_module, infra_terraform_modules_msk_module, infra_terraform_readme_cidr_ingress_rationale [EXTRACTED 1.00]
- **container_env output wiring: module outputs must match application-*.properties** — infra_terraform_envs_prod_outputs_container_env, infra_terraform_envs_staging_outputs_container_env, infra_terraform_modules_rds_postgres_module, infra_terraform_modules_keyspaces_module, infra_terraform_modules_qdrant_ec2_module, infra_terraform_modules_msk_module, infra_terraform_readme_env_var_contract [EXTRACTED 1.00]
- **AWS data-tier modules reachable only from the application subnets** — rds_postgres_module, qdrant_ec2_module, msk_module, keyspaces_module [INFERRED 0.85]
- **ECS task definition wired to bedrock-iam execution/task roles and the container-env/secrets contract** — infra_terraform_modules_ecs_service_main_aws_ecs_task_definition, infra_terraform_modules_bedrock_iam_outputs_execution_role_arn, container_env_contract [INFERRED 0.75]
- **Data-tier security groups gated by application subnet CIDRs (SG-ref deferred to avoid module cycle)** — infra_terraform_modules_rds_postgres_main_aws_security_group_db, infra_terraform_modules_msk_main_aws_security_group_msk, infra_terraform_modules_qdrant_ec2_main_aws_security_group_qdrant [EXTRACTED 1.00]
- **Client in-memory Basic-auth flow** — modules_client_src_app_authcontext_authprovider, modules_client_src_features_auth_authpage_authpage, modules_client_src_app_authcontext_basic_header_flow, modules_client_src_features_chat_api_chatapi_startchat [EXTRACTED 0.95]
- **SSE chat streaming data flow** — modules_client_src_features_chat_components_chatwindow_chatwindow, modules_client_src_features_chat_hooks_usechatstream_usechatstream, modules_client_src_features_chat_api_chatapi_streammessage, modules_client_src_features_chat_components_messagebubble_messagebubble [EXTRACTED 0.95]
- **Three-tier VPC (public/app/data + per-AZ NAT)** — infra_terraform_modules_vpc_main_aws_subnet_public, infra_terraform_modules_vpc_main_aws_subnet_app, infra_terraform_modules_vpc_main_aws_subnet_data, infra_terraform_modules_vpc_main_aws_nat_gateway [EXTRACTED 0.95]
- **8-stage RAG pipeline orchestrated by ChatPipelineService** — modules_server_src_main_java_com_example_demo_chat_rag_chatpipelineservice_chatpipelineservice, modules_server_src_main_java_com_example_demo_chat_rag_querynormalizationservice_querynormalizationservice, modules_server_src_main_java_com_example_demo_chat_rag_knowledgeretrievalservice_knowledgeretrievalservice, modules_server_src_main_java_com_example_demo_chat_rag_intentclassificationservice_intentclassificationservice, modules_server_src_main_java_com_example_demo_chat_rag_scopefilter_scopefilter, modules_server_src_main_java_com_example_demo_chat_rag_slotfillingservice_slotfillingservice, modules_server_src_main_java_com_example_demo_chat_rag_promptbuilder_promptbuilder, modules_server_src_main_java_com_example_demo_chat_rag_answergenerationservice_answergenerationservice, modules_server_src_main_java_com_example_demo_chat_rag_responsevalidator_responsevalidator, modules_server_src_main_java_com_example_demo_chat_rag_semanticcacheservice_semanticcacheservice [INFERRED 0.95]
- **Chat message request flow (ChatController to ChatService to ChatPipelineService)** — modules_server_src_main_java_com_example_demo_chat_chat_chatcontroller_sendmessage, modules_server_src_main_java_com_example_demo_chat_chat_chatcontroller_sendmessagestream, modules_server_src_main_java_com_example_demo_chat_chat_chatservice_getchatforparticipant, modules_server_src_main_java_com_example_demo_chat_rag_chatpipelineservice_chatpipelineservice, modules_server_src_main_java_com_example_demo_chat_chat_sendmessageresponse_sendmessageresponse [INFERRED 0.95]
- **Two Qdrant VectorStore beans wiring** — modules_server_src_main_java_com_example_demo_chat_config_semanticcachevectorstoreconfig_semanticcachevectorstoreconfig, modules_server_src_main_java_com_example_demo_chat_config_semanticcachevectorstoreconfig_semanticcachevectorstore, modules_server_src_main_java_com_example_demo_chat_rag_semanticcacheservice_semanticcacheservice, modules_server_src_main_java_com_example_demo_chat_rag_knowledgeretrievalservice_knowledgeretrievalservice [INFERRED 0.75]
- **Intent classification subsystem** — modules_server_src_main_java_com_example_demo_chat_rag_intentclassificationservice_intentclassificationservice, modules_server_src_main_java_com_example_demo_chat_rag_intentclassification_intentclassification, modules_server_src_main_java_com_example_demo_chat_rag_intentdefinition_intentdefinition, modules_server_src_main_java_com_example_demo_chat_rag_intentdefinitionregistry_intentdefinitionregistry, modules_server_src_main_java_com_example_demo_chat_rag_scopefilter_scopefilter, modules_server_src_main_java_com_example_demo_chat_rag_knowledgeretrievalservice_knowledgeretrievalservice [INFERRED 0.85]
- **Spring Security reactive auth flow** — modules_server_src_main_java_com_example_demo_chat_user_securityuserdetailsservice_securityuserdetailsservice, modules_server_src_main_java_com_example_demo_chat_user_userrepository_userrepository, modules_server_src_main_java_com_example_demo_chat_user_userprincipal_userprincipal, modules_server_src_main_java_com_example_demo_chat_user_user_user [INFERRED 0.85]
- **support_kb knowledge-base indexing and retrieval** — modules_server_src_main_java_com_example_demo_chat_rag_knowledgebaseindexer_knowledgebaseindexer, modules_server_src_main_java_com_example_demo_chat_rag_intentdefinitionregistry_intentdefinitionregistry, modules_server_src_main_java_com_example_demo_chat_rag_knowledgeretrievalservice_knowledgeretrievalservice, support_kb_vector_store [INFERRED 0.85]
- **Intent JSON validation ruleset** — scripts_validate_intents, scripts_validate_intents_intent_id_equals_filename_stem, scripts_validate_intents_no_duplicate_canonical_question, scripts_validate_intents_placeholders_in_required_slots, scripts_validate_intents_unknown_fields_rejected [EXTRACTED 1.00]
- **Testcontainers-backed test set** — modules_server_src_test_java_com_example_demo_chat_demochatapplicationtests, modules_server_src_test_java_com_example_demo_chat_user_userrepositorytest, modules_server_src_main_resources_local_docker_compose_postgres, modules_server_src_main_resources_local_docker_compose_cassandra, modules_server_src_main_resources_local_docker_compose_qdrant [INFERRED 0.75]
- **Knowledge-base intent definitions** — modules_server_src_main_resources_knowledge_base_intents_change_shipping_address_change_shipping_address, modules_server_src_main_resources_knowledge_base_intents_order_status_order_status, modules_server_src_main_resources_knowledge_base_intents_refund_status_refund_status, rag_intentdefinition [INFERRED 0.75]

## Communities (79 total, 24 thin omitted)

### Community 0 - "Chat API Controller & History Entity"
Cohesion: 0.06
Nodes (44): GrantedAuthority, ChatController, Flux, Mono, PostMapping, RequestMapping, RequiredArgsConstructor, ResponseEntity (+36 more)

### Community 1 - "Obsidian Style-Settings Plugin (minified)"
Cohesion: 0.05
Nodes (22): bc(), bl(), Cl(), Cu(), de(), dn(), fn(), Ji() (+14 more)

### Community 2 - "User Registration API"
Cohesion: 0.10
Nodes (26): Duplicate email/login (DataIntegrityViolationException) maps to 409 Conflict, GetMapping, CreateUserRequest, AllArgsConstructor, Builder, Getter, NoArgsConstructor, Table (+18 more)

### Community 3 - "Client Build & Lint Dependencies"
Cohesion: 0.05
Nodes (37): eslint, @eslint/js, eslint-plugin-react-hooks, eslint-plugin-react-refresh, globals, dependencies, react, react-dom (+29 more)

### Community 4 - "React Client App Shell & Auth Context"
Cohesion: 0.11
Nodes (28): client module: empty Gradle placeholder, built with npm, App(), App root component, AppShell (screen switcher), AppShell(), AuthContext, AuthContextValue, AuthProvider (+20 more)

### Community 5 - "Pickr Colour-Picker Widget (minified)"
Cohesion: 0.12
Nodes (7): destroy(), ie(), M, Ot(), S(), V(), xe()

### Community 6 - "Terraform Env Roots & ALB/Bedrock Modules"
Cohesion: 0.13
Nodes (34): Implementation Roadmap, Phase 3b - needs an AWS account (open), Running Locally guide, Vector Store Schema (Topics + Answers), Knowledge base reindex process (KnowledgeBaseIndexer.reindex), semantic_cache Qdrant collection, support_kb Qdrant collection, Terraform prod env root (+26 more)

### Community 7 - "ECS Service & Infra Wiring Rationale"
Cohesion: 0.09
Nodes (33): Data-tier SGs ingress via app subnet CIDRs (SG-ref would cause module dependency cycle), bedrock-iam module, container_env output <-> application-*.properties env-var contract, ECS service ignores task_definition changes (deploy pipeline updates out of band), ecs-service module, Secrets/AMI vars left without defaults so a plan without real values fails fast, bedrock-iam output execution_role_arn (ECS agent image pull + task secrets), bedrock-iam output task_role_arn (container Bedrock invoke role) (+25 more)

### Community 8 - "Chat Pipeline Orchestration & SSE"
Cohesion: 0.17
Nodes (15): SendMessageResponse, buffer-then-chunk SSE streaming, ChatPipelineService, Document, Flux, Mono, ServerSentEvent, Service (+7 more)

### Community 9 - "Groundedness Check & Intent JSON Validation"
Cohesion: 0.09
Nodes (27): GroundednessCheck, JsonNaming, change_shipping_address intent, order_status intent, refund_status intent, ResponseValidatorTest, ChatClient, Test (+19 more)

### Community 10 - "User Persistence, Flyway & R2DBC Tests"
Cohesion: 0.11
Nodes (26): PasswordEncoderConfig (BCrypt), DataR2dbcTest, Daily Log 2026-08-13, Daily Log 2026-08-20, User Feature, SecurityUserDetailsService, User R2DBC entity, UserPrincipal (+18 more)

### Community 11 - "Intent Classification & Prompt Assembly"
Cohesion: 0.13
Nodes (14): AssembledPrompt, IntentClassification, JsonNaming, IntentClassificationService (stage 3), ChatClient, Mono, Service, IntentDefinition (+6 more)

### Community 12 - "Testcontainers Application-Context Test"
Cohesion: 0.13
Nodes (19): ActiveProfiles, GenericContainer, Local Cassandra compose service, Local Qdrant compose service, DemoChatApplicationTests, DemoChatApplicationTests, Bean, BeforeAll (+11 more)

### Community 13 - "8-Stage RAG Pipeline Design"
Cohesion: 0.16
Nodes (24): semantic_cache (Qdrant collection), Intent Matching and Slot Filling, Prompt Engineering and Guardrails, RAG Pipeline Design, ChatPipelineServiceTest, BeforeEach, ServerSentEvent, Output-side groundedness guardrail (+16 more)

### Community 14 - "Minified Client Bundle Helpers A"
Cohesion: 0.17
Nodes (24): At(), Bi(), c(), Cr(), dc(), Ee(), Er(), Fc() (+16 more)

### Community 15 - "Knowledge Base Indexing & Retrieval"
Cohesion: 0.13
Nodes (15): ApplicationArguments, ApplicationRunner, Component, Document, Override, VectorStore, KnowledgeBaseIndexer, Document (+7 more)

### Community 16 - "Project Docs, CI Workflows & Daily Logs"
Cohesion: 0.17
Nodes (21): Buffer-Then-Chunk SSE Streaming, CLAUDE.md Project Guide, java-rules Skill, Daily Log 2026-07-14, Daily Log 2026-07-15, Daily Log 2026-07-22, Daily Log 2026-08-12, Wiki Index (Map of Content) (+13 more)

### Community 17 - "Minified Colour Utility Functions"
Cohesion: 0.14
Nodes (18): ea(), ec(), f(), gl(), gn(), Il(), ke(), L() (+10 more)

### Community 18 - "Minified Client Bundle Helpers B"
Cohesion: 0.14
Nodes (20): bu(), du(), gu(), Ht(), hu(), Je(), Ki(), lu() (+12 more)

### Community 19 - "AWS Infrastructure Overview & TF Modules"
Cohesion: 0.18
Nodes (19): ALB (SSE-compatible long-lived connection routing), Amazon Bedrock (LLM + embeddings), ECS Fargate (Spring Boot host), Amazon MSK (Kafka, planned), Self-managed Qdrant on EC2/ECS, RDS for PostgreSQL, Bedrock Titan embedding model (amazon.titan-embed-text-v2:0, 768d), Daily Log 2026-08-31 (+11 more)

### Community 20 - "TypeScript Compiler Config"
Cohesion: 0.11
Nodes (17): compilerOptions, isolatedModules, jsx, lib, module, moduleResolution, noEmit, resolveJsonModule (+9 more)

### Community 21 - "Answer Generation & Pipeline Tests"
Cohesion: 0.24
Nodes (6): AnswerGenerationService (stage 7), ChatClient, Mono, Service, ChatPipelineServiceTest, Test

### Community 22 - "Chat History Storage & Dialogue State"
Cohesion: 0.30
Nodes (12): Amazon Keyspaces (Cassandra-compatible), Chat Feature, ChatHistory Cassandra entity, ChatHistoryRepository, ChatMessage Cassandra UDT, chat_history (Cassandra table), dialogue_state (Cassandra table), Dialogue Session Model (+4 more)

### Community 23 - "Client Chat UI & User Endpoints"
Cohesion: 0.18
Nodes (12): AuthPage (signup + login form), chatApi (hand-rolled SSE frame parser over fetch), ChatWindow / MessageBubble, StartChatForm, useChatStream hook, GET /api/users/{id} Endpoint, POST /api/users Endpoint, Moved UserController to user Package (+4 more)

### Community 24 - "Style-Settings Tree Rendering (minified)"
Cohesion: 0.24
Nodes (12): addExportButton(), addResetButton(), clearFilter(), filter(), getAllChildrenIds(), me(), removeChildren(), render() (+4 more)

### Community 25 - "Cassandra Repositories & Slot Filling"
Cohesion: 0.24
Nodes (7): ChatHistoryRepository, DialogueStateRepository, Component, SlotFillingService (stage 5), Component, TextChunker, ReactiveCassandraRepository

### Community 26 - "Style-Settings Config Persistence (minified)"
Cohesion: 0.31
Nodes (11): cleanup(), clearSection(), clearSetting(), getSetting(), initClasses(), removeClasses(), save(), setConfig() (+3 more)

### Community 27 - "Terraform VPC Module (three-tier subnets)"
Cohesion: 0.27
Nodes (11): NAT EIPs (aws_eip.nat), Internet Gateway (aws_internet_gateway.this), NAT Gateways (aws_nat_gateway.this), Private route tables (aws_route_table.private), Public route table (aws_route_table.public), App subnets (aws_subnet.app), Data subnets (aws_subnet.data), Public subnets (aws_subnet.public) (+3 more)

### Community 28 - "Second Qdrant VectorStore Bean"
Cohesion: 0.29
Nodes (9): Bean, Configuration, EmbeddingModel, VectorStore, semanticCacheVectorStore bean, SemanticCacheVectorStoreConfig, two Qdrant VectorStore beans (support_kb + semantic_cache), QdrantClient (+1 more)

### Community 29 - "Minified Client Bundle Helpers C"
Cohesion: 0.20
Nodes (10): a(), Aa(), addSettingChild(), Dl(), hl(), Ni(), Rl(), vu() (+2 more)

### Community 30 - "Intent Definition Registry"
Cohesion: 0.31
Nodes (6): intent_id must equal the intent JSON filename stem, IntentDefinitionRegistry, Component, ObjectMapper, PostConstruct, Resource

### Community 31 - "Validation Exception Handler"
Cohesion: 0.38
Nodes (6): ExceptionHandler, ResponseStatus, ValidationExceptionHandler, ProblemDetail, RestControllerAdvice, WebExchangeBindException

### Community 32 - "Reactive Security UserDetailsService"
Cohesion: 0.31
Nodes (8): Mono, Override, RequiredArgsConstructor, Service, UserDetails, SecurityUserDetailsService, Reactive R2DBC repository serves Spring Security auth with no blocking bridge, ReactiveUserDetailsService

### Community 33 - "Obsidian Plugin Manifest"
Cohesion: 0.22
Nodes (8): author, authorUrl, description, id, isDesktopOnly, minAppVersion, name, version

### Community 34 - "Semantic Cache Service Test"
Cohesion: 0.39
Nodes (4): SemanticCacheServiceTest, Test, VectorStore, SemanticCacheServiceTest

### Community 35 - "WebFlux Security & Auth Config"
Cohesion: 0.32
Nodes (8): Chat authorization hole (addParticipant bug, 2026-08-13), AuthContext (in-memory {userId, login, password}), Vite dev proxy (/api -> localhost:8080), SecurityConfig (WebFlux HTTP Basic), ChatService, ChatService.getChatForParticipant authorization gate, HTTP Basic auth (no login endpoint), backend-ci workflow

### Community 36 - "Backend/Frontend Plan Docs & Config Beans"
Cohesion: 0.39
Nodes (8): ValidationExceptionHandler (@RestControllerAdvice), ChatClientConfig, SemanticCacheVectorStoreConfig (second VectorStore bean), Java Backend Structure, Frontend Chat MVP Plan, Local vs AWS Component Mapping, Architecture Overview, Support Chat Plan README

### Community 37 - "SecurityConfig Filter Chain"
Cohesion: 0.43
Nodes (6): EnableWebFluxSecurity, Bean, Configuration, SecurityConfig, SecurityWebFilterChain, ServerHttpSecurity

### Community 38 - "ChatClient Config Bean"
Cohesion: 0.39
Nodes (6): chatClient bean, ChatClientConfig, Bean, ChatClient, ChatModel, Configuration

### Community 39 - "DialogueStatus Enum"
Cohesion: 0.25
Nodes (7): DialogueStatus, ANSWERED, ESCALATED, NEW, OUT_OF_SCOPE, READY_TO_ANSWER, SLOT_FILLING

### Community 40 - "Semantic Cache Service"
Cohesion: 0.43
Nodes (4): Mono, Service, VectorStore, SemanticCacheService

### Community 41 - "TextChunker Test"
Cohesion: 0.50
Nodes (3): TextChunkerTest, Test, TextChunkerTest

### Community 42 - "Query Normalization Service"
Cohesion: 0.48
Nodes (4): ChatClient, Mono, Service, QueryNormalizationService (stage 1)

### Community 43 - "Response Validator (guardrail)"
Cohesion: 0.43
Nodes (5): ChatClient, Mono, Service, ResponseValidator (stage 8), output-side groundedness guardrail

### Community 44 - "Knowledge Base & Deploy Workflow Docs"
Cohesion: 0.40
Nodes (6): support_kb (Qdrant collection), GitHub Actions CI/CD Plan, KnowledgeBaseIndexer, deploy-prod workflow (planned), deploy-staging workflow (planned), frontend-ci workflow

### Community 45 - "Obsidian App Settings"
Cohesion: 0.33
Nodes (5): alwaysUpdateLinks, promptDelete, readableLineLength, strictLineBreaks, vimMode

### Community 46 - "Obsidian Style-Settings Manifest"
Cohesion: 0.33
Nodes (5): author, authorUrl, minAppVersion, name, version

### Community 47 - "Password Encoder Config (BCrypt)"
Cohesion: 0.53
Nodes (4): Bean, Configuration, PasswordEncoder, PasswordEncoderConfig

### Community 48 - "Chat Controller/Service Auth Routing"
Cohesion: 0.40
Nodes (5): ChatController.addParticipant, ChatController.sendMessage, ChatController.sendMessageStream, ChatService.addParticipant, ChatService.getChatForParticipant

### Community 49 - "Gradle Wrapper Script"
Cohesion: 0.83
Nodes (3): gradlew script, die(), warn()

### Community 51 - "Server Build (Flyway + Spring AI BOM)"
Cohesion: 0.67
Nodes (3): Flyway plugin drivers on buildscript classpath, pinned to 11.14.1, server module build.gradle, Spring AI BOM 2.0.0 + Testcontainers BOM imports

## Knowledge Gaps
- **114 isolated node(s):** `promptDelete`, `alwaysUpdateLinks`, `strictLineBreaks`, `vimMode`, `readableLineLength` (+109 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **24 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `ChatPipelineService` connect `Chat Pipeline Orchestration & SSE` to `Chat API Controller & History Entity`, `Semantic Cache Service`, `Query Normalization Service`, `Intent Classification & Prompt Assembly`, `Response Validator (guardrail)`, `Testcontainers Application-Context Test`, `Knowledge Base Indexing & Retrieval`, `Chat Controller/Service Auth Routing`, `Answer Generation & Pipeline Tests`, `Cassandra Repositories & Slot Filling`, `Intent Definition Registry`?**
  _High betweenness centrality (0.112) - this node is a cross-community bridge._
- **Why does `ChatPipelineService` connect `8-Stage RAG Pipeline Design` to `Chat API Controller & History Entity`, `Backend/Frontend Plan Docs & Config Beans`, `Testcontainers Application-Context Test`, `Chat History Storage & Dialogue State`, `Client Chat UI & User Endpoints`?**
  _High betweenness centrality (0.061) - this node is a cross-community bridge._
- **Why does `ChatControllerStreamTest` connect `Chat API Controller & History Entity` to `Chat Pipeline Orchestration & SSE`, `WebFlux Security & Auth Config`, `8-Stage RAG Pipeline Design`, `Client Chat UI & User Endpoints`?**
  _High betweenness centrality (0.051) - this node is a cross-community bridge._
- **Are the 4 inferred relationships involving `DemoChatApplicationTests` (e.g. with `demo_chat.users table` and `Local Cassandra compose service`) actually correct?**
  _`DemoChatApplicationTests` has 4 INFERRED edges - model-reasoned connections that need verification._
- **What connects `promptDelete`, `alwaysUpdateLinks`, `strictLineBreaks` to the rest of the system?**
  _114 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `Chat API Controller & History Entity` be split into smaller, more focused modules?**
  _Cohesion score 0.0593607305936073 - nodes in this community are weakly interconnected._
- **Should `Obsidian Style-Settings Plugin (minified)` be split into smaller, more focused modules?**
  _Cohesion score 0.05142857142857143 - nodes in this community are weakly interconnected._