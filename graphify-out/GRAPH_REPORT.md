# Graph Report - .  (2026-07-14)

## Corpus Check
- Corpus is ~6,071 words - fits in a single context window. You may not need a graph.

## Summary
- 141 nodes · 239 edges · 20 communities (15 shown, 5 thin omitted)
- Extraction: 87% EXTRACTED · 13% INFERRED · 0% AMBIGUOUS · INFERRED: 30 edges (avg confidence: 0.84)
- Token cost: 128,072 input · 0 output

## Community Hubs (Navigation)
- Chat Feature & Cassandra Design
- User Feature & Postgres Persistence
- User REST Controller
- User Service Layer
- User Entity & Wiki Docs
- Build Config & Password Encoder
- Chat REST Controller
- Validation Exception Handling
- Chat Message UDT
- Obsidian Theme Manifest
- Application Context Test
- Gradle Wrapper Script
- Spring Boot Application Entry
- Claude Code Tooling Config
- Obsidian Appearance Theme
- Kafka Topic Template
- Qdrant Collection Template

## God Nodes (most connected - your core abstractions)
1. `User` - 19 edges
2. `Feature: User` - 15 edges
3. `UserService (referenced, not in this chunk)` - 14 edges
4. `UserController` - 13 edges
5. `Feature: Chat` - 12 edges
6. `ChatHistory` - 11 edges
7. `CreateUserRequest` - 11 edges
8. `UserResponse (referenced, not in this chunk)` - 11 edges
9. `ChatService` - 10 edges
10. `Daily log 2026-07-13` - 10 edges

## Surprising Connections (you probably didn't know these)
- `Features/Chat.md (wiki page, referenced)` --semantically_similar_to--> `ChatService`  [INFERRED] [semantically similar]
  docs/wiki/Features/Chat.md → src/main/java/com/example/demo_chat/chat/ChatService.java
- `Features/User.md (wiki page, referenced)` --semantically_similar_to--> `User`  [INFERRED] [semantically similar]
  docs/wiki/Features/User.md → src/main/java/com/example/demo_chat/user/User.java
- `Feature: Chat` --references--> `ChatController`  [EXTRACTED]
  docs/wiki/Features/Chat.md → src/main/java/com/example/demo_chat/chat/ChatController.java
- `Feature: Chat` --references--> `ChatMessage`  [EXTRACTED]
  docs/wiki/Features/Chat.md → src/main/java/com/example/demo_chat/chat/ChatMessage.java
- `Feature: Chat` --references--> `ValidationExceptionHandler`  [EXTRACTED]
  docs/wiki/Features/Chat.md → src/main/java/com/example/demo_chat/common/ValidationExceptionHandler.java

## Import Cycles
- None detected.

## Hyperedges (group relationships)
- **Chat Feature: Controller/Service/History/Repository/Message** — src_main_java_com_example_demo_chat_chat_chatcontroller_chatcontroller, src_main_java_com_example_demo_chat_chat_chatservice_chatservice, src_main_java_com_example_demo_chat_chat_chathistory_chathistory, src_main_java_com_example_demo_chat_chat_chathistoryrepository_chathistoryrepository, src_main_java_com_example_demo_chat_chat_chatmessage_chatmessage [INFERRED 0.85]
- **User Creation & Validation Flow** — src_main_java_com_example_demo_chat_user_usercontroller_usercontroller, src_main_java_com_example_demo_chat_user_createuserrequest_createuserrequest, src_main_java_com_example_demo_chat_common_validationexceptionhandler_validationexceptionhandler, src_main_java_com_example_demo_chat_config_passwordencoderconfig_passwordencoderconfig, src_main_java_com_example_demo_chat_user_user_user [INFERRED 0.85]
- **Gradle Build System (build.gradle/settings.gradle/gradlew)** — build_project_config, settings_root_project, gradlew_wrapper_script [INFERRED 0.75]
- **User feature: code + schema tying together the User entity** — src_main_java_com_example_demo_chat_user_user_user, src_main_java_com_example_demo_chat_user_userrepository_userrepository, src_main_java_com_example_demo_chat_user_userservice_userservice, src_main_java_com_example_demo_chat_user_userresponse_userresponse, src_main_java_com_example_demo_chat_user_createuserrequest_createuserrequest, src_main_java_com_example_demo_chat_user_usercontroller_usercontroller, src_main_resources_db_migration_v1_create_users_table_users, docs_wiki_features_user_user [EXTRACTED 1.00]
- **Chat feature: code + Cassandra table implementing chat start/history** — src_main_java_com_example_demo_chat_chat_chatcontroller_chatcontroller, src_main_java_com_example_demo_chat_chat_chatservice_chatservice, src_main_java_com_example_demo_chat_chat_chathistory_chathistory, src_main_java_com_example_demo_chat_chat_chatmessage_chatmessage, src_main_java_com_example_demo_chat_chat_chathistoryrepository_chathistoryrepository, docs_wiki_features_chat_chat, docs_wiki_infrastructure_cassandra_chat_history_chat_history [EXTRACTED 1.00]
- **Obsidian wiki Map of Content entries** — docs_wiki_index_index, docs_wiki_features_chat_chat, docs_wiki_features_user_user, docs_wiki_infrastructure_cassandra_chat_history_chat_history, docs_wiki_infrastructure_postgres_users_users, docs_wiki_daily_2026_07_13_2026_07_13 [EXTRACTED 1.00]

## Communities (20 total, 5 thin omitted)

### Community 0 - "Chat Feature & Cassandra Design"
Cohesion: 0.14
Nodes (19): Feature: Chat, ChatHistory partition key = user_id, queries expected by user not chat id, ChatMessage modeled as frozen UDT list on ChatHistory vs wide-row table, Feature note template, Frozen list on single partition is a Cassandra anti-pattern for unbounded growth, Cassandra table: chat_history, Cassandra table note template, ReactiveCassandraRepository (+11 more)

### Community 1 - "User Feature & Postgres Persistence"
Cohesion: 0.18
Nodes (16): daily-report skill, NewJavaFile skill, CLAUDE.md project instructions, Daily log 2026-07-13, BCrypt via spring-security-crypto only, avoiding full Security starter, 409 Conflict via DB unique-constraint violation, not pre-check (avoids race), Feature: User, UUID primary key over auto-increment to avoid leaking guessable ids (+8 more)

### Community 2 - "User REST Controller"
Cohesion: 0.26
Nodes (10): GetMapping, ResponseStatus, ExceptionHandler, Mono, PostMapping, RequestMapping, RequiredArgsConstructor, ResponseEntity (+2 more)

### Community 3 - "User Service Layer"
Cohesion: 0.25
Nodes (7): CreateUserRequest, UserResponse (referenced, not in this chunk), Mono, PasswordEncoder, RequiredArgsConstructor, Service, UserService (referenced, not in this chunk)

### Community 4 - "User Entity & Wiki Docs"
Cohesion: 0.23
Nodes (12): Daily/2026-07-13.md (wiki daily note, referenced), Features/Chat.md (wiki page, referenced), Features/User.md (wiki page, referenced), Obsidian Core Plugins Enabled, Obsidian Workspace Layout State, Entity, AllArgsConstructor, Builder (+4 more)

### Community 5 - "Build Config & Password Encoder"
Cohesion: 0.31
Nodes (7): Bean, build.gradle Dependency & Plugin Configuration, Configuration, Gradle Wrapper Script (gradlew), settings.gradle Root Project Declaration, PasswordEncoder, PasswordEncoderConfig

### Community 6 - "Chat REST Controller"
Cohesion: 0.39
Nodes (7): ChatController, Mono, PostMapping, RequestMapping, RequiredArgsConstructor, ResponseEntity, RestController

### Community 7 - "Validation Exception Handling"
Cohesion: 0.48
Nodes (5): ProblemDetail, RestControllerAdvice, ExceptionHandler, ValidationExceptionHandler, WebExchangeBindException

### Community 8 - "Chat Message UDT"
Cohesion: 0.52
Nodes (6): ChatMessage, AllArgsConstructor, Builder, Getter, NoArgsConstructor, UserDefinedType

### Community 9 - "Obsidian Theme Manifest"
Cohesion: 0.33
Nodes (5): author, authorUrl, minAppVersion, name, version

### Community 10 - "Application Context Test"
Cohesion: 0.60
Nodes (3): SpringBootTest, DemoChatApplicationTests, Test

### Community 11 - "Gradle Wrapper Script"
Cohesion: 0.83
Nodes (3): gradlew script, die(), warn()

## Knowledge Gaps
- **13 isolated node(s):** `name`, `version`, `minAppVersion`, `author`, `authorUrl` (+8 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **5 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `Feature: Chat` connect `Chat Feature & Cassandra Design` to `Chat Message UDT`, `User Feature & Postgres Persistence`, `Chat REST Controller`, `Validation Exception Handling`?**
  _High betweenness centrality (0.181) - this node is a cross-community bridge._
- **Why does `User` connect `User Entity & Wiki Docs` to `User Feature & Postgres Persistence`, `User REST Controller`, `User Service Layer`, `Build Config & Password Encoder`?**
  _High betweenness centrality (0.143) - this node is a cross-community bridge._
- **Why does `build.gradle Dependency & Plugin Configuration` connect `Build Config & Password Encoder` to `Chat Feature & Cassandra Design`, `User Service Layer`, `User Entity & Wiki Docs`, `Chat REST Controller`?**
  _High betweenness centrality (0.123) - this node is a cross-community bridge._
- **Are the 4 inferred relationships involving `User` (e.g. with `build.gradle Dependency & Plugin Configuration` and `Features/User.md (wiki page, referenced)`) actually correct?**
  _`User` has 4 INFERRED edges - model-reasoned connections that need verification._
- **What connects `name`, `version`, `minAppVersion` to the rest of the system?**
  _13 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `Chat Feature & Cassandra Design` be split into smaller, more focused modules?**
  _Cohesion score 0.1422924901185771 - nodes in this community are weakly interconnected._