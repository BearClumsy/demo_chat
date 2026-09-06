# Graph Report - demo_chat  (2026-09-06)

## Corpus Check
- 76 files · ~61,870 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 1437 nodes · 2671 edges · 166 communities (97 shown, 45 thin omitted)
- Extraction: 96% EXTRACTED · 4% INFERRED · 0% AMBIGUOUS · INFERRED: 112 edges (avg confidence: 0.81)
- Token cost: 114,629 input · 0 output

## Community Hubs (Navigation)
- Bedrock/Qdrant Chat Client Config
- Terraform Prod Environment
- Terraform Staging Environment
- React Client App Shell
- Client ESLint Dependencies
- ECS Service Terraform Module
- K8s Cluster Compute (ASG)
- VPC Terraform Module
- Text Chunking & Response Validation
- Chat Service Message Handling
- RDS Postgres Terraform Module
- ALB Listener & Target Group (Ingress)
- GitHub OIDC Deploy IAM
- Chat Participant Management
- Chat Pipeline & KB Indexer
- Qdrant EC2 Terraform Module
- App ALB Listener & Target Group
- SSE Streaming Design Notes
- Daily Log: Qdrant Point ID Bug
- K8s Prod Manifest (HPA/ConfigMap)
- K8s Cluster Security Groups
- Chat Controller Endpoints
- ECS Execution & Bedrock IAM
- Kubernetes Deploy Target Decision
- Semantic Cache & Local/AWS Mapping
- K8s Cluster KMS & S3 State
- Client TypeScript Config
- Chat History & Send-Message DTOs
- ECR Terraform Module
- MSK (Kafka) Terraform Module
- Prompt Builder & Scope Filter
- Start-Chat & User Principal DTOs
- CLAUDE.md Architecture & Gotchas
- K8s Node IAM Instance Profiles
- K8s Cluster SSM Deploy/Rollback
- Answer Generation Service
- Groundedness & Intent Classification
- Frontend Auth & Chat UI Notes
- CI Workflow Docs
- Lombok Annotations
- Chat/Slot-Filling Bug Notes
- User Feature & Security Details Notes
- Phase 3b & K8s Pivot Notes
- Running Locally Guide
- Bedrock Titan & Qdrant Collections
- AWS Infrastructure Overview Notes
- Validation Exception Handler
- Create/Get User Endpoints
- API Load Balancer (Network)
- Intent Definition Registry
- Plan Docs Index
- Intent JSON Validation Script
- K8s Add-ons Overview
- Module Layout & RAG Pipeline Notes
- Chat Feature Note
- Feature Note Template (filled)
- User Feature Note
- Cassandra Keyspaces Terraform Module
- Kubernetes Architecture Doc
- Prompt Engineering Doc
- RAG Pipeline Doc
- Wiki Index (MOC)
- K8s Deploy README
- Dialogue Status Enum
- Dialogue State Doc
- AWS Infrastructure Doc
- Project README
- Terraform README
- Daily Log: Auth Hole Fix
- Send-Message Request/Test
- Frontend Chat MVP Doc
- CLAUDE.md Client & Infra Sections
- Daily Log 2026-07-13
- Daily Log: RAG Pipeline API
- Daily Log: R2DBC Migration
- Daily Log: Chat Visibility Bugs
- Kafka Topic Note Template (filled)
- Security User Details Service
- Local vs AWS Mapping Doc
- Architecture Overview Doc
- Cassandra chat_history Table Doc
- Cassandra dialogue_state Table Doc
- Cassandra Table Note Template (filled)
- Daily Log: Terraform Skeleton Review
- React Frontend Structure Doc
- Postgres Table Note Template (filled)
- Postgres users Table Doc
- Qdrant semantic_cache Collection Doc
- Qdrant support_kb Collection Doc
- Qdrant Collection Note Template (filled)
- Daily Log: Root Makefile
- Daily-Report Skill Doc
- Daily Log: CI Workflows & Makefile
- Backend CI Workflow
- Gradle Wrapper Script
- K8s Add-ons Install Script
- Spring Boot Application Entrypoint
- CLAUDE.md API & Module Layout
- Daily Log: Code Style & Tooling
- Daily Log: Wiki Maintenance
- Retained-Reference Terraform Modules
- Verify-Chat Smoke Test Script
- ALB Module README (retained)
- Bedrock-IAM Module README (retained)
- Daily-Report Skill Log Entry
- Daily Log: Colima OOM Fix
- ECS-Service Module README (retained)
- Users Table Migration
- Docker-Compose Cassandra Services
- Java-Rules Skill
- Daily Log: Bedrock Bean Fix
- Daily Log: DB Connection Config
- Daily Log: Docker Credential Fix
- Daily Log: Users Migration Added
- Daily Log: graphify Setup
- Daily Log: JDK Toolchain Fix
- Daily Log: jdtls/LazyVim Fix
- Daily Log: Postgres search_path Bug
- Daily Log: Spotless/GJF Added
- Daily Log: User Package Built
- Daily Log: Obsidian Vault Setup
- Daily Log: Terraform Code Review
- Daily Log: graphify Rebuild (infra)
- Daily Log: terraform-lint Introduced
- Daily Log: Flyway Auto-Migrate Bug
- Daily Log: Kubeadm Bootstrap Rationale
- Daily Log: Ollama Containerized
- Daily Log: Qdrant Point ID Bug (Indexer)
- Daily Log: Seeded Test User Decision
- Daily Log: AuthPage Default Mode
- Daily Log: make stop Target
- Feature Note Template (blank)
- Cassandra Table Template (blank)
- Kafka Topic Template (blank)
- Postgres Table Template (blank)
- Qdrant Collection Template (blank)
- Terraform-Lint CI Workflow
- Client HTML Entry Point
- Docker-Compose Kafka Service
- Docker-Compose Ollama Service
- Docker-Compose Postgres Service
- Docker-Compose Qdrant Service

## God Nodes (most connected - your core abstractions)
1. `ChatPipelineService` - 49 edges
2. `Java Backend: Project Structure` - 37 edges
3. `docs/wiki/index.md (vault MOC)` - 27 edges
4. `CI/CD: GitHub Actions` - 26 edges
5. `Running Locally` - 24 edges
6. `var.name` - 24 edges
7. `AWS Infrastructure (wiki doc)` - 23 edges
8. `IntentDefinition` - 22 edges
9. `ChatPipelineServiceTest` - 22 edges
10. `Kubernetes deploy layer (kubeadm on EC2) (wiki doc)` - 20 edges

## Surprising Connections (you probably didn't know these)
- `Semantic Cache Short-Circuit` --conceptually_related_to--> `SemanticCacheService`  [EXTRACTED]
  CLAUDE.md → modules/server/src/main/java/com/example/demo_chat/rag/SemanticCacheService.java
- `Project README` --semantically_similar_to--> `Running Locally`  [INFERRED] [semantically similar]
  README.md → docs/wiki/Plan/running-locally.md
- `Java Backend: Project Structure` --references--> `AnswerGenerationService`  [EXTRACTED]
  docs/wiki/Plan/backend.md → modules/server/src/main/java/com/example/demo_chat/rag/AnswerGenerationService.java
- `Java Backend: Project Structure` --references--> `ChatPipelineService`  [EXTRACTED]
  docs/wiki/Plan/backend.md → modules/server/src/main/java/com/example/demo_chat/rag/ChatPipelineService.java
- `Java Backend: Project Structure` --references--> `DialogueState`  [EXTRACTED]
  docs/wiki/Plan/backend.md → modules/server/src/main/java/com/example/demo_chat/rag/DialogueState.java

## Import Cycles
- None detected.

## Hyperedges (group relationships)
- **var-over-explicit-type Java Convention** — claude_skills_java_rules_skill, claude, docs_wiki_daily_2026_07_22 [EXTRACTED 0.80]
- **Deliberately duplicated per-environment config pattern** — infra_k8s_manifest_prod, infra_k8s_manifest_staging, docs_wiki_plan_kubernetes [EXTRACTED 1.00]
- **Cluster add-on stack installed by install.sh** — infra_k8s_addons_readme_calico, infra_k8s_addons_readme_metrics_server, infra_k8s_addons_readme_ingress_nginx, infra_k8s_addons_readme_nth [EXTRACTED 1.00]
- **local docker-compose services backing the local Spring profile** — modules_server_src_main_resources_local_docker_compose_postgres, modules_server_src_main_resources_local_docker_compose_cassandra, modules_server_src_main_resources_local_docker_compose_cassandra_init, modules_server_src_main_resources_local_docker_compose_qdrant, modules_server_src_main_resources_local_docker_compose_kafka, modules_server_src_main_resources_local_docker_compose_ollama [EXTRACTED 1.00]
- **Retained Lint-Clean Reference Modules (Not Instantiated by Any Env)** — infra_terraform_modules_alb_readme_alb, infra_terraform_modules_bedrock_iam_readme_bedrock_iam, infra_terraform_modules_ecs_service_readme_ecs_service [EXTRACTED 1.00]
- **8-Stage RAG Pipeline Orchestration** — modules_server_src_main_java_com_example_demo_chat_rag_chatpipelineservice_chatpipelineservice, modules_server_src_main_java_com_example_demo_chat_rag_querynormalizationservice_querynormalizationservice, modules_server_src_main_java_com_example_demo_chat_rag_knowledgeretrievalservice_knowledgeretrievalservice, modules_server_src_main_java_com_example_demo_chat_rag_intentclassificationservice_intentclassificationservice, modules_server_src_main_java_com_example_demo_chat_rag_scopefilter_scopefilter, modules_server_src_main_java_com_example_demo_chat_rag_slotfillingservice_slotfillingservice, modules_server_src_main_java_com_example_demo_chat_rag_promptbuilder_promptbuilder, modules_server_src_main_java_com_example_demo_chat_rag_answergenerationservice_answergenerationservice, modules_server_src_main_java_com_example_demo_chat_rag_responsevalidator_responsevalidator [EXTRACTED 1.00]
- **Independent Path-Filtered CI Lint/Build Lanes** — github_workflows_backend_ci_workflow, github_workflows_frontend_ci_workflow, github_workflows_knowledge_base_lint_workflow, github_workflows_terraform_lint_workflow, github_workflows_manifests_lint_workflow [INFERRED 0.85]
- **AWS Kubernetes Deploy Pipeline (OIDC + SSM)** — github_workflows_deploy_staging_workflow, github_workflows_deploy_prod_workflow, concept_kubernetes_deploy_target [INFERRED 0.80]

## Communities (166 total, 45 thin omitted)

### Community 0 - "Bedrock/Qdrant Chat Client Config"
Cohesion: 0.08
Nodes (27): io.qdrant.client.QdrantClient, ChatClientConfig, PasswordEncoderConfig, SecurityConfig, SemanticCacheVectorStoreConfig, DemoChatApplicationTests, StubBedrockModels, UserRepositoryTest (+19 more)

### Community 1 - "Terraform Prod Environment"
Cohesion: 0.10
Nodes (43): local.github_deploy_subjects, local.name, local.plaintext_env, local.tags, module.alb_k8s, module.ecr, module.github_oidc, module.k8s_cluster (+35 more)

### Community 2 - "Terraform Staging Environment"
Cohesion: 0.10
Nodes (43): local.github_deploy_subjects, local.name, local.plaintext_env, local.tags, module.alb_k8s, module.ecr, module.github_oidc, module.k8s_cluster (+35 more)

### Community 3 - "React Client App Shell"
Cohesion: 0.11
Nodes (25): RFC-7807, App(), AppShell(), AuthContext, AuthContextValue, AuthProvider(), Credentials, useAuth() (+17 more)

### Community 4 - "Client ESLint Dependencies"
Cohesion: 0.05
Nodes (37): eslint, @eslint/js, eslint-plugin-react-hooks, eslint-plugin-react-refresh, globals, dependencies, react, react-dom (+29 more)

### Community 5 - "ECS Service Terraform Module"
Cohesion: 0.13
Nodes (30): aws_cloudwatch_log_group.app, aws_ecs_cluster.this, aws_ecs_service.app, aws_ecs_task_definition.app, aws_security_group.task, local.container_environment, local.container_secrets, output.cluster_arn (+22 more)

### Community 6 - "K8s Cluster Compute (ASG)"
Cohesion: 0.13
Nodes (27): aws_autoscaling_group.control_plane, aws_autoscaling_group.worker, aws_launch_template.control_plane, aws_launch_template.worker, aws_vpc_security_group_ingress_rule.worker_ingress_nodeports, local.common_user_data_vars, local.single_control_plane, output.control_plane_asg_name (+19 more)

### Community 7 - "VPC Terraform Module"
Cohesion: 0.22
Nodes (25): aws_eip.nat, aws_internet_gateway.this, aws_nat_gateway.this, aws_route_table_association.app, aws_route_table_association.data, aws_route_table_association.public, aws_route_table.private, aws_route_table.public (+17 more)

### Community 8 - "Text Chunking & Response Validation"
Cohesion: 0.13
Nodes (6): TextChunker, ResponseValidatorTest, SemanticCacheServiceTest, SlotFillingServiceTest, TextChunkerTest, org.junit.jupiter.api.Test

### Community 9 - "Chat Service Message Handling"
Cohesion: 0.21
Nodes (6): ChatPipelineService, PipelineOutcome, DialogueState, org.junit.jupiter.api.BeforeEach, org.springframework.security.access.AccessDeniedException, reactor.core.publisher.Mono

### Community 10 - "RDS Postgres Terraform Module"
Cohesion: 0.16
Nodes (22): aws_db_instance.this, aws_db_subnet_group.this, aws_security_group.db, output.db_host, output.db_name, output.db_port, output.security_group_id, var.allocated_storage (+14 more)

### Community 11 - "ALB Listener & Target Group (Ingress)"
Cohesion: 0.17
Nodes (21): aws_lb_listener.http_redirect, aws_lb_listener.https, aws_lb_target_group.ingress, aws_lb.this, aws_route53_record.this, aws_security_group.alb, output.alb_dns_name, output.alb_security_group_id (+13 more)

### Community 12 - "GitHub OIDC Deploy IAM"
Cohesion: 0.18
Nodes (19): aws_iam_openid_connect_provider.github, aws_iam_role.deploy, aws_iam_role_policy.deploy, data.aws_iam_policy_document.assume, data.aws_iam_policy_document.deploy, local.oidc_provider_arn, output.deploy_role_arn, output.oidc_provider_arn (+11 more)

### Community 13 - "Chat Participant Management"
Cohesion: 0.16
Nodes (7): ChatService, MessageRequest, StartChatRequest, UserRepository, ChatServiceStartChatTest, ChatServiceValidateParticipantIdsTest, org.springframework.data.r2dbc.repository.R2dbcRepository

### Community 14 - "Chat Pipeline & KB Indexer"
Cohesion: 0.15
Nodes (9): Override, KnowledgeBaseIndexer, KnowledgeRetrievalService, SemanticCacheService, org.springframework.ai.document.Document, org.springframework.ai.vectorstore.VectorStore, org.springframework.boot.ApplicationArguments, org.springframework.boot.ApplicationRunner (+1 more)

### Community 15 - "Qdrant EC2 Terraform Module"
Cohesion: 0.19
Nodes (18): aws_ebs_volume.qdrant_data, aws_instance.qdrant, aws_security_group.qdrant, aws_volume_attachment.qdrant_data, output.instance_id, output.qdrant_host, output.qdrant_private_ip, output.security_group_id (+10 more)

### Community 16 - "App ALB Listener & Target Group"
Cohesion: 0.20
Nodes (17): aws_lb_listener.http_redirect, aws_lb_listener.https, aws_lb_target_group.app, aws_lb.this, aws_security_group.alb, output.alb_arn, output.alb_dns_name, output.alb_security_group_id (+9 more)

### Community 17 - "SSE Streaming Design Notes"
Cohesion: 0.14
Nodes (19): Buffer-Then-Chunk SSE Streaming, Daily Log 2026-07-14, Daily Log 2026-07-15, Daily Log 2026-07-22, dialogue_state (Cassandra table), Dialogue Session Model, RAG Pipeline Design, Output-Side Groundedness Guardrail (+11 more)

### Community 18 - "Daily Log: Qdrant Point ID Bug"
Cohesion: 0.10
Nodes (19): 2026-09-03, App-side changes (small, enabling), Bug found on the first real end-to-end boot: non-UUID Qdrant point id, Chat OOMs on the first turn — Colima VM too small for llama3.1, Chat: `participantIds` now optional — assistant-only chats (not this session), CI, Docs drift fixed, Local dev: seeded test user (+11 more)

### Community 19 - "K8s Prod Manifest (HPA/ConfigMap)"
Cohesion: 0.12
Nodes (18): HPA scaling (CPU 70%), infra/k8s manifests (staging/prod), ConfigMap demo-chat-config (prod), Deployment demo-chat-client (prod, 2 replicas), Deployment demo-chat-server (prod, 3 replicas), HorizontalPodAutoscaler (prod, server 3->10 / client 2->4), Ingress demo-chat-web/api (prod), Job demo-chat-kb-bootstrap (prod) (+10 more)

### Community 20 - "K8s Cluster Security Groups"
Cohesion: 0.22
Nodes (19): aws_security_group.control_plane, aws_security_group.worker, aws_vpc_security_group_egress_rule.cp_all, aws_vpc_security_group_egress_rule.worker_all, aws_vpc_security_group_ingress_rule.cp_api_from_admin, aws_vpc_security_group_ingress_rule.cp_api_from_vpc, aws_vpc_security_group_ingress_rule.cp_controlplane_components_self, aws_vpc_security_group_ingress_rule.cp_etcd_self (+11 more)

### Community 21 - "Chat Controller Endpoints"
Cohesion: 0.24
Nodes (10): lombok.RequiredArgsConstructor, ChatController, ParticipantRequest, UserController, org.springframework.http.codec.ServerSentEvent, org.springframework.http.ResponseEntity, org.springframework.web.bind.annotation.PostMapping, org.springframework.web.bind.annotation.RequestMapping (+2 more)

### Community 22 - "ECS Execution & Bedrock IAM"
Cohesion: 0.21
Nodes (16): aws_iam_role.execution, aws_iam_role_policy_attachment.execution_managed, aws_iam_role_policy.bedrock, aws_iam_role_policy.execution_secrets, aws_iam_role.task, data.aws_iam_policy_document.bedrock_invoke, data.aws_iam_policy_document.secrets_read, data.aws_iam_policy_document.task_assume (+8 more)

### Community 23 - "Kubernetes Deploy Target Decision"
Cohesion: 0.13
Nodes (19): Kubernetes Deploy Target (kubeadm on EC2), Compute pivot to kubeadm on EC2 (2026-09-03), ECS Fargate (superseded compute target), Kubernetes deploy layer (kubeadm on EC2) (wiki doc), modules/alb-k8s (Terraform), Calico CNI (VXLAN mode), cluster-autoscaler (optional), NGINX Ingress Controller (DaemonSet) (+11 more)

### Community 24 - "Semantic Cache & Local/AWS Mapping"
Cohesion: 0.17
Nodes (18): Semantic cache as read-caching for scale, Local <-> AWS: Component Mapping (wiki doc), Local docker-compose stack, local,offline profile (Ollama, no AWS), Switching via Spring Profiles, Architecture Overview (wiki doc), Flow for a single message, RAG as a single source of truth (+10 more)

### Community 25 - "K8s Cluster KMS & S3 State"
Cohesion: 0.18
Nodes (18): aws_kms_alias.cluster, aws_kms_key.cluster, aws_s3_bucket_lifecycle_configuration.this, aws_s3_bucket_policy.this, aws_s3_bucket_public_access_block.this, aws_s3_bucket_server_side_encryption_configuration.this, aws_s3_bucket.this, aws_s3_bucket_versioning.this (+10 more)

### Community 26 - "Client TypeScript Config"
Cohesion: 0.11
Nodes (17): compilerOptions, isolatedModules, jsx, lib, module, moduleResolution, noEmit, resolveJsonModule (+9 more)

### Community 27 - "Chat History & Send-Message DTOs"
Cohesion: 0.20
Nodes (6): ChatHistoryRepository, SendMessageResponse, AssembledPrompt, DialogueStateRepository, ChatPipelineServiceTest, org.springframework.data.cassandra.repository.ReactiveCassandraRepository

### Community 28 - "ECR Terraform Module"
Cohesion: 0.20
Nodes (14): aws_ecr_lifecycle_policy.this, aws_ecr_repository.this, local.repository_names, output.client_repository_url, output.registry_id, output.repository_arns, output.server_repository_url, var.client_repository_name (+6 more)

### Community 29 - "MSK (Kafka) Terraform Module"
Cohesion: 0.21
Nodes (14): aws_msk_cluster.this, aws_security_group.msk, output.bootstrap_brokers_plaintext, output.bootstrap_brokers_tls, output.security_group_id, var.allowed_cidr_blocks, var.broker_instance_type, var.ebs_volume_size_gb (+6 more)

### Community 30 - "Prompt Builder & Scope Filter"
Cohesion: 0.17
Nodes (10): Package-by-Feature Layout, PromptBuilder, ScopeFilter, SlotFillingService, org.springframework.stereotype.Component, Actual package tree, Java Backend: Project Structure, Rationale for the current layers (+2 more)

### Community 31 - "Start-Chat & User Principal DTOs"
Cohesion: 0.18
Nodes (6): StartChatResponse, Override, UserPrincipal, ChatControllerStartChatTest, org.springframework.security.core.GrantedAuthority, org.springframework.test.web.reactive.server.WebTestClient

### Community 32 - "CLAUDE.md Architecture & Gotchas"
Cohesion: 0.13
Nodes (14): Architecture (from declared dependencies), Configuration, Gotchas, graphify, Knowledge Sources, Project status, Toolchain, Working with this Vault (+6 more)

### Community 33 - "K8s Node IAM Instance Profiles"
Cohesion: 0.28
Nodes (14): aws_iam_instance_profile.control_plane, aws_iam_instance_profile.worker, aws_iam_role.control_plane, aws_iam_role_policy_attachment.control_plane_ssm, aws_iam_role_policy_attachment.worker_ssm, aws_iam_role_policy.control_plane, aws_iam_role_policy.worker, aws_iam_role.worker (+6 more)

### Community 34 - "K8s Cluster SSM Deploy/Rollback"
Cohesion: 0.16
Nodes (14): aws_ssm_document.deploy, aws_ssm_document.rollback, output.control_plane_role_arn, output.control_plane_security_group_id, output.deploy_bucket, output.deploy_bucket_arn, output.ssm_deploy_document_arn, output.ssm_deploy_document_name (+6 more)

### Community 35 - "Answer Generation Service"
Cohesion: 0.30
Nodes (6): AnswerGenerationService, IntentClassificationService, QueryNormalizationService, ResponseValidator, org.springframework.ai.chat.client.ChatClient, org.springframework.stereotype.Service

### Community 36 - "Groundedness & Intent Classification"
Cohesion: 0.23
Nodes (7): GroundednessCheck, PropertyNamingStrategies.SnakeCaseStrategy, IntentClassification, PropertyNamingStrategies.SnakeCaseStrategy, IntentDefinition, PropertyNamingStrategies.SnakeCaseStrategy, tools.jackson.databind.annotation.JsonNaming

### Community 37 - "Frontend Auth & Chat UI Notes"
Cohesion: 0.16
Nodes (14): AuthContext (in-memory {userId, login, password}), AuthPage (signup + login form), chatApi (hand-rolled SSE frame parser over fetch), ChatWindow / MessageBubble, StartChatForm, useChatStream hook, Vite dev proxy (/api -> localhost:8080), GET /api/users/{id} Endpoint (+6 more)

### Community 38 - "CI Workflow Docs"
Cohesion: 0.14
Nodes (13): deploy-prod Workflow (skeleton), frontend-ci Workflow, knowledge-base-lint Workflow, `backend-ci.yml` — stages (implemented), CI/CD: GitHub Actions, `deploy-prod.yml`, `deploy-staging.yml`, `frontend-ci.yml` — stages (implemented) (+5 more)

### Community 39 - "Lombok Annotations"
Cohesion: 0.43
Nodes (10): lombok.AllArgsConstructor, lombok.Builder, lombok.Getter, lombok.NoArgsConstructor, ChatHistory, ChatMessage, User, org.springframework.data.cassandra.core.mapping.Table (+2 more)

### Community 40 - "Chat/Slot-Filling Bug Notes"
Cohesion: 0.23
Nodes (13): Chat authorization hole (addParticipant bug, 2026-08-13), Jackson 3 java.time.Instant Deserialization Bug, participantIds Made Optional (assistant-only chats), Slot-Filling NPE: Empty Cassandra Map Reads Back Null, Three Chat Visibility Bugs Fixed (no reply, duplicate bubble, garbled text), Chat Feature, ChatController, ChatHistory Cassandra entity (+5 more)

### Community 41 - "User Feature & Security Details Notes"
Cohesion: 0.29
Nodes (13): Daily Log 2026-08-13, Daily Log 2026-08-20, User Feature, SecurityUserDetailsService, User R2DBC entity, UserPrincipal, UserRepository (R2dbcRepository), UserService (+5 more)

### Community 42 - "Phase 3b & K8s Pivot Notes"
Cohesion: 0.21
Nodes (13): Phase 3b Terraform Skeleton (2026-08-31), Pivot to Kubernetes (kubeadm on EC2) from ECS Fargate, docs/wiki/index.md (vault MOC), deploy-prod.yml (skeleton deploy workflow), deploy-staging.yml (skeleton deploy workflow), manifests-lint.yml (kubeconform/shellcheck/actionlint CI), Features/Chat.md, Infrastructure note: chat_history (Cassandra) (+5 more)

### Community 43 - "Running Locally Guide"
Cohesion: 0.15
Nodes (12): 1. Start the dependencies, 2. Create the Cassandra keyspace (one-time), 3. Export AWS credentials, 4. Run the backend, 5. Run the frontend, Exercising the API directly, Prerequisites, Related documents (+4 more)

### Community 44 - "Bedrock Titan & Qdrant Collections"
Cohesion: 0.21
Nodes (11): Bedrock Titan embedding model (amazon.titan-embed-text-v2:0, 768d), semantic_cache (Qdrant collection), support_kb (Qdrant collection), Knowledge base reindex process (KnowledgeBaseIndexer.reindex), Collection indexes and parameters (Qdrant), Knowledge base update process, Metadata fields, Related documents (+3 more)

### Community 45 - "AWS Infrastructure Overview Notes"
Cohesion: 0.24
Nodes (12): AWS Infrastructure (wiki doc), ALB (load balancing), Amazon Bedrock (managed LLM), CloudFront CDN, CloudWatch (logs/metrics/alarms), ECR (Docker image registry), Amazon Keyspaces (Cassandra-compatible), Qdrant on EC2/ECS (self-managed vector store) (+4 more)

### Community 46 - "Validation Exception Handler"
Cohesion: 0.30
Nodes (7): ValidationExceptionHandler, org.springframework.dao.DataIntegrityViolationException, org.springframework.http.ProblemDetail, org.springframework.web.bind.annotation.ExceptionHandler, org.springframework.web.bind.annotation.ResponseStatus, org.springframework.web.bind.annotation.RestControllerAdvice, org.springframework.web.bind.support.WebExchangeBindException

### Community 47 - "Create/Get User Endpoints"
Cohesion: 0.26
Nodes (4): CreateUserRequest, UserResponse, UserService, org.springframework.web.bind.annotation.GetMapping

### Community 48 - "API Load Balancer (Network)"
Cohesion: 0.29
Nodes (10): aws_lb.api, aws_lb_listener.api, aws_lb_target_group.api, aws_security_group.vpce, aws_vpc_endpoint.ssm, output.api_endpoint, output.api_server_url, var.app_subnet_cidrs (+2 more)

### Community 49 - "Intent Definition Registry"
Cohesion: 0.25
Nodes (4): jakarta.annotation.PostConstruct, IntentDefinitionRegistry, org.springframework.core.io.Resource, tools.jackson.databind.ObjectMapper

### Community 50 - "Plan Docs Index"
Cohesion: 0.18
Nodes (10): Architecture, AWS, CI/CD, Current repository structure, Data, Documentation, Environments, Plan (+2 more)

### Community 51 - "Intent JSON Validation Script"
Cohesion: 0.20
Nodes (10): ARRAY_FIELDS, BOOLEAN_FIELDS, dir, errors, fail(), KNOWN_FIELDS, seenIntentIds, seenQuestions (+2 more)

### Community 52 - "K8s Add-ons Overview"
Cohesion: 0.22
Nodes (9): Cluster add-ons, Node AMI prerequisites, Running it, Terraform structure (IaC layout), Phase 3b - needs an AWS account (open), infra/k8s README, make k8s-lint / k8s-render-staging / k8s-apply-staging, infra/terraform README (+1 more)

### Community 53 - "Module Layout & RAG Pipeline Notes"
Cohesion: 0.24
Nodes (10): Multi-Module Gradle Layout (server + client), 8-Stage RAG Pipeline, Seeded Local Test User (testuser), Intent Matching and Slot Filling, Prompt Engineering and Guardrails, Plan Index (Support Chat), Phased Implementation Roadmap, Vector Store Schema (topics/answers) (+2 more)

### Community 54 - "Chat Feature Note"
Cohesion: 0.22
Nodes (8): Chat, Decisions, Infrastructure Used, Open Questions, Overview, Related Code, Requirements, Source Log

### Community 55 - "Feature Note Template (filled)"
Cohesion: 0.22
Nodes (8): Decisions, <Feature Name>, Infrastructure Used, Open Questions, Overview, Related Code, Requirements, Source Log

### Community 56 - "User Feature Note"
Cohesion: 0.22
Nodes (8): Decisions, Feature: User, Infrastructure Used, Open Questions, Overview, Related Code, Requirements, Source Log

### Community 57 - "Cassandra Keyspaces Terraform Module"
Cohesion: 0.33
Nodes (6): aws_keyspaces_keyspace.this, aws_keyspaces_table.open_chats_by_bucket, output.keyspace_arn, output.keyspace_name, var.keyspace_name, var.tags

### Community 58 - "Kubernetes Architecture Doc"
Cohesion: 0.22
Nodes (8): CI → cluster (SSM Run Command, no inbound exposure), Cluster bootstrap (`modules/k8s-cluster`), Ingress / ALB (`modules/alb-k8s`), Kubernetes deploy layer (kubeadm on EC2), Manifests (`infra/k8s/manifest-{staging,prod}.yaml`), Scaling, Shape, Still deferred (needs an AWS account)

### Community 59 - "Prompt Engineering Doc"
Cohesion: 0.22
Nodes (8): Example of an assembled prompt, Handling "sensitive" messages, Input-side guardrails, Output-side guardrails, Principle behind assembling the final prompt, Prompt Engineering and Guardrails, Related documents, SYSTEM/CONTEXT/USER STATE/USER MESSAGE prompt structure

### Community 60 - "RAG Pipeline Doc"
Cohesion: 0.22
Nodes (8): Example of a single request going through the pipeline, How it works (quick reference), Known simplifications, Pipeline stages, RAG Pipeline: From User Context to Answer, Related documents, SSE streaming, Where each stage lives in the code

### Community 61 - "Wiki Index (MOC)"
Cohesion: 0.22
Nodes (8): Daily, demo_chat Wiki, Features, Infrastructure, Linking convention, Map of Content, Plan, Structure

### Community 62 - "K8s Deploy README"
Cohesion: 0.25
Nodes (7): CI -> cluster via SSM Run Command, Deploy flow (what the workflow does), infra/k8s, Layout, Local check, Status: lint-only, What's in a manifest

### Community 63 - "Dialogue Status Enum"
Cohesion: 0.25
Nodes (7): DialogueStatus, ANSWERED, ESCALATED, NEW, OUT_OF_SCOPE, READY_TO_ANSWER, SLOT_FILLING

### Community 64 - "Dialogue State Doc"
Cohesion: 0.25
Nodes (7): Dialogue Session Model (Cassandra), Example value, Key structure (implemented), Related documents, Semantic cache — decided (Phase 2), Statuses (`DialogueStatus`), Why Cassandra and not in-memory

### Community 65 - "AWS Infrastructure Doc"
Cohesion: 0.25
Nodes (7): AWS Infrastructure, Core AWS services and their roles, Diagram (high level), Network layout (VPC), Related documents, Scaling for 500K+ users, Terraform structure (IaC)

### Community 66 - "Project README"
Cohesion: 0.25
Nodes (7): Commands, Current status, demo_chat, Documentation, Getting started, Project structure, Tech stack

### Community 67 - "Terraform README"
Cohesion: 0.25
Nodes (7): Deploy path: Kubernetes on EC2, infra/terraform, Known gaps / TODO, Linting locally, Modules, Status: lint-only, The env-var contract

### Community 68 - "Daily Log: Auth Hole Fix"
Cohesion: 0.29
Nodes (6): 2026-08-13, Chat authorization hole, Docs & graph, Flyway: standalone migration tasks, Local tooling & tests, Open / uncommitted

### Community 70 - "Frontend Chat MVP Doc"
Cohesion: 0.29
Nodes (6): Backend constraints that shaped the design, Context, Frontend Chat MVP: auth, start-chat, SSE streaming, Related documents, Verification, What was built

### Community 71 - "CLAUDE.md Client & Infra Sections"
Cohesion: 0.33
Nodes (6): Client, Commands, Infrastructure (Kubernetes), Infrastructure (Terraform), Knowledge base, Server

### Community 72 - "Daily Log 2026-07-13"
Cohesion: 0.33
Nodes (5): 2026-07-13, API, Data layer, Documentation, Infrastructure & tooling

### Community 73 - "Daily Log: RAG Pipeline API"
Cohesion: 0.33
Nodes (5): 2026-07-14, API — RAG pipeline, Dependencies — Bedrock Converse starter, Documentation, Infrastructure & tooling

### Community 74 - "Daily Log: R2DBC Migration"
Cohesion: 0.33
Nodes (5): 2026-07-15, API — RAG pipeline Phase 2, Data layer — R2DBC migration, Documentation, Infrastructure & tooling

### Community 75 - "Daily Log: Chat Visibility Bugs"
Cohesion: 0.33
Nodes (5): 2026-09-04, Auth: default the login/signup toggle to login, Chat: three visibility bugs found on first real UI use, all fixed, Tooling: `make stop` to kill a foregrounded local run, Working tree at end of day

### Community 76 - "Kafka Topic Note Template (filled)"
Cohesion: 0.33
Nodes (5): Consumers, Kafka Topic: <topic-name>, Notes, Producers, Schema

### Community 77 - "Security User Details Service"
Cohesion: 0.47
Nodes (4): Override, SecurityUserDetailsService, org.springframework.security.core.userdetails.ReactiveUserDetailsService, org.springframework.security.core.userdetails.UserDetails

### Community 78 - "Local vs AWS Mapping Doc"
Cohesion: 0.33
Nodes (5): docker-compose (local stack, actual), Local ↔ AWS: Component Mapping, Mapping table, Related documents, Switching via Spring Profiles (implemented)

### Community 79 - "Architecture Overview Doc"
Cohesion: 0.33
Nodes (5): Architecture Overview, Design principles, Flow for a single message (current implementation), Related documents, System components (current)

### Community 80 - "Cassandra chat_history Table Doc"
Cohesion: 0.40
Nodes (4): Cassandra Table: chat_history, Columns, Notes, Used By

### Community 81 - "Cassandra dialogue_state Table Doc"
Cohesion: 0.40
Nodes (4): Cassandra Table: dialogue_state, Columns, Notes, Used By

### Community 82 - "Cassandra Table Note Template (filled)"
Cohesion: 0.40
Nodes (4): Cassandra Table: <table_name>, Columns, Notes, Used By

### Community 83 - "Daily Log: Terraform Skeleton Review"
Cohesion: 0.40
Nodes (4): 2026-08-31, Code review of the skeleton — follow-ups before an apply, Phase 3b (no-AWS slice): Terraform skeleton + lint CI, Tooling: graphify graph rebuilt for the new infra

### Community 84 - "React Frontend Structure Doc"
Cohesion: 0.40
Nodes (4): Backend integration points, React Frontend: Project Structure, Related documents, Tree

### Community 85 - "Postgres Table Note Template (filled)"
Cohesion: 0.40
Nodes (4): Columns, Notes, Postgres Table: <table_name>, Used By

### Community 86 - "Postgres users Table Doc"
Cohesion: 0.40
Nodes (4): Columns, Notes, Postgres Table: users, Used By

### Community 87 - "Qdrant semantic_cache Collection Doc"
Cohesion: 0.40
Nodes (4): Notes, Payload Fields, Qdrant Collection: semantic_cache, Used By

### Community 88 - "Qdrant support_kb Collection Doc"
Cohesion: 0.40
Nodes (4): Notes, Payload Fields, Qdrant Collection: support_kb, Used By

### Community 89 - "Qdrant Collection Note Template (filled)"
Cohesion: 0.40
Nodes (4): Notes, Payload Fields, Qdrant Collection: <collection_name>, Used By

### Community 90 - "Daily Log: Root Makefile"
Cohesion: 0.50
Nodes (3): 2026-08-12, Root Makefile, Wiki & tooling notes

### Community 91 - "Daily-Report Skill Doc"
Cohesion: 0.50
Nodes (3): Format, Gathering what changed, Saving

### Community 92 - "Daily Log: CI Workflows & Makefile"
Cohesion: 0.50
Nodes (4): Daily Log 2026-08-12, frontend-ci Workflow, knowledge-base-lint Workflow, Thin-Alias Root Makefile

### Community 93 - "Backend CI Workflow"
Cohesion: 0.50
Nodes (4): backend-ci build Job, Check formatting and run tests Step, Build the server image Step, backend-ci Workflow

### Community 94 - "Gradle Wrapper Script"
Cohesion: 0.83
Nodes (3): gradlew script, die(), warn()

### Community 95 - "K8s Add-ons Install Script"
Cohesion: 0.83
Nodes (3): kubectl_apply(), install.sh script, wait_rollout()

### Community 97 - "CLAUDE.md API & Module Layout"
Cohesion: 0.67
Nodes (3): API surface, Module layout, Server package map

### Community 100 - "Retained-Reference Terraform Modules"
Cohesion: 1.00
Nodes (3): alb Module (retained reference, ECS/Fargate ALB), bedrock-iam Module (retained reference, ECS task/execution roles), ecs-service Module (retained reference)

## Ambiguous Edges - Review These
- `CI/CD: GitHub Actions` → `Kubernetes Deploy Target (kubeadm on EC2)`  [AMBIGUOUS]
  docs/wiki/Plan/github-actions.md · relation: conceptually_related_to

## Knowledge Gaps
- **355 isolated node(s):** `AuthContextValue`, `Credentials`, `Mode`, `StartChatParams`, `StreamMessageParams` (+350 more)
  These have ≤1 connection - possible missing edges or undocumented components. (Counts symbols only; 461 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **45 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **What is the exact relationship between `CI/CD: GitHub Actions` and `Kubernetes Deploy Target (kubeadm on EC2)`?**
  _Edge tagged AMBIGUOUS (relation: conceptually_related_to) - confidence is low._
- **Why does `Java Backend: Project Structure` connect `Prompt Builder & Scope Filter` to `CLAUDE.md Architecture & Gotchas`, `Bedrock/Qdrant Chat Client Config`, `Answer Generation Service`, `Frontend Auth & Chat UI Notes`, `Lombok Annotations`, `Text Chunking & Response Validation`, `Chat Service Message Handling`, `Chat Participant Management`, `Chat Pipeline & KB Indexer`, `Create/Get User Endpoints`, `SSE Streaming Design Notes`, `Module Layout & RAG Pipeline Notes`, `Chat Controller Endpoints`, `Semantic Cache & Local/AWS Mapping`, `Dialogue Status Enum`?**
  _High betweenness centrality (0.062) - this node is a cross-community bridge._
- **Why does `ChatPipelineService` connect `Chat Service Message Handling` to `CLAUDE.md Architecture & Gotchas`, `Bedrock/Qdrant Chat Client Config`, `Answer Generation Service`, `Groundedness & Intent Classification`, `Send-Message Request/Test`, `Text Chunking & Response Validation`, `Chat Participant Management`, `Chat Pipeline & KB Indexer`, `Intent Definition Registry`, `Chat Controller Endpoints`, `Chat History & Send-Message DTOs`, `Prompt Builder & Scope Filter`, `Start-Chat & User Principal DTOs`?**
  _High betweenness centrality (0.032) - this node is a cross-community bridge._
- **Why does `Plan Index (Support Chat)` connect `Module Layout & RAG Pipeline Notes` to `Project README`, `Frontend Auth & Chat UI Notes`, `CI Workflow Docs`, `Phase 3b & K8s Pivot Notes`, `Running Locally Guide`, `AWS Infrastructure Overview Notes`, `SSE Streaming Design Notes`, `Kubernetes Deploy Target Decision`, `Semantic Cache & Local/AWS Mapping`, `Prompt Builder & Scope Filter`?**
  _High betweenness centrality (0.022) - this node is a cross-community bridge._
- **What connects `AuthContextValue`, `Credentials`, `Mode` to the rest of the system?**
  _355 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `Bedrock/Qdrant Chat Client Config` be split into smaller, more focused modules?**
  _Cohesion score 0.08244680851063829 - nodes in this community are weakly interconnected._
- **Should `Terraform Prod Environment` be split into smaller, more focused modules?**
  _Cohesion score 0.1026827012025902 - nodes in this community are weakly interconnected._