# Graph Report - demo_chat  (2026-07-14)

## Corpus Check
- 46 files · ~6,925 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 249 nodes · 285 edges · 43 communities (29 shown, 14 thin omitted)
- Extraction: 94% EXTRACTED · 6% INFERRED · 0% AMBIGUOUS · INFERRED: 16 edges (avg confidence: 0.82)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `4af61717`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- [[_COMMUNITY_Chat History Wiki Notes|Chat History Wiki Notes]]
- [[_COMMUNITY_Chat Domain Model & DTOs|Chat Domain Model & DTOs]]
- [[_COMMUNITY_Password Encoding Config|Password Encoding Config]]
- [[_COMMUNITY_UserPrincipal (Security Principal)|UserPrincipal (Security Principal)]]
- [[_COMMUNITY_User REST Endpoints|User REST Endpoints]]
- [[_COMMUNITY_Reactive User Details Service|Reactive User Details Service]]
- [[_COMMUNITY_Project Instructions (CLAUDE.md)|Project Instructions (CLAUDE.md)]]
- [[_COMMUNITY_Chat Feature Wiki Page|Chat Feature Wiki Page]]
- [[_COMMUNITY_Feature Note Template|Feature Note Template]]
- [[_COMMUNITY_WebFlux Security Config|WebFlux Security Config]]
- [[_COMMUNITY_Wiki Index  MOC|Wiki Index / MOC]]
- [[_COMMUNITY_Obsidian Theme Manifest|Obsidian Theme Manifest]]
- [[_COMMUNITY_Validation Exception Handling|Validation Exception Handling]]
- [[_COMMUNITY_Daily Log 2026-07-13|Daily Log 2026-07-13]]
- [[_COMMUNITY_Kafka Topic Note Template|Kafka Topic Note Template]]
- [[_COMMUNITY_Cassandra Table Note Template|Cassandra Table Note Template]]
- [[_COMMUNITY_Obsidian Workspace State|Obsidian Workspace State]]
- [[_COMMUNITY_Postgres Table Note Template|Postgres Table Note Template]]
- [[_COMMUNITY_Qdrant Collection Note Template|Qdrant Collection Note Template]]
- [[_COMMUNITY_Daily Report Skill|Daily Report Skill]]
- [[_COMMUNITY_Application Entry Point|Application Entry Point]]
- [[_COMMUNITY_Application Context Test|Application Context Test]]
- [[_COMMUNITY_ChatHistory Entity|ChatHistory Entity]]
- [[_COMMUNITY_ChatHistoryRepository|ChatHistoryRepository]]
- [[_COMMUNITY_ChatMessage Model|ChatMessage Model]]
- [[_COMMUNITY_Data Store Rationale (Cassandra)|Data Store Rationale (Cassandra)]]
- [[_COMMUNITY_Graphify Enforcement Hook & Permissions|Graphify Enforcement Hook & Permissions]]
- [[_COMMUNITY_Obsidian Catppuccin Theme|Obsidian Catppuccin Theme]]
- [[_COMMUNITY_Gradle Wrapper & Root Project|Gradle Wrapper & Root Project]]
- [[_COMMUNITY_CreateUserRequest DTO|CreateUserRequest DTO]]
- [[_COMMUNITY_User Entity|User Entity]]
- [[_COMMUNITY_Reactive Web Layer Concept|Reactive Web Layer Concept]]
- [[_COMMUNITY_New Java File Skill|New Java File Skill]]
- [[_COMMUNITY_Kafka Topic Template (variant)|Kafka Topic Template (variant)]]
- [[_COMMUNITY_Qdrant Collection Template (variant)|Qdrant Collection Template (variant)]]

## God Nodes (most connected - your core abstractions)
1. `Feature: User` - 16 edges
2. `Postgres Table: users` - 10 edges
3. `Cassandra Table: chat_history` - 9 edges
4. `Chat` - 8 edges
5. `<Feature Name>` - 8 edges
6. `UserPrincipal` - 7 edges
7. `Daily log 2026-07-13` - 7 edges
8. `Feature: Chat` - 6 edges
9. `demo_chat Wiki index (Map of Content)` - 6 edges
10. `ChatController` - 5 edges

## Surprising Connections (you probably didn't know these)
- `Feature: User` --references--> `users table (Flyway V1 migration)`  [EXTRACTED]
  docs/wiki/Features/User.md → src/main/resources/db/migration/V1__create_users_table.sql
- `Postgres Table: users` --references--> `users table (Flyway V1 migration)`  [EXTRACTED]
  docs/wiki/Infrastructure/Postgres/users.md → src/main/resources/db/migration/V1__create_users_table.sql
- `Daily log 2026-07-13` --references--> `users table (Flyway V1 migration)`  [EXTRACTED]
  docs/wiki/Daily/2026-07-13.md → src/main/resources/db/migration/V1__create_users_table.sql
- `daily-report skill` --conceptually_related_to--> `Daily log 2026-07-13`  [INFERRED]
  .claude/skills/daily-report/SKILL.md → docs/wiki/Daily/2026-07-13.md
- `local docker-compose (Postgres/Cassandra/Qdrant/Kafka)` --shares_data_with--> `Cassandra Table: chat_history`  [INFERRED]
  src/main/resources/local/docker-compose.yml → docs/wiki/Infrastructure/Cassandra/chat_history.md

## Import Cycles
- None detected.

## Hyperedges (group relationships)
- **User Creation & Validation Flow** — src_main_java_com_example_demo_chat_user_usercontroller_usercontroller, src_main_java_com_example_demo_chat_user_createuserrequest_createuserrequest, src_main_java_com_example_demo_chat_common_validationexceptionhandler_validationexceptionhandler, src_main_java_com_example_demo_chat_config_passwordencoderconfig_passwordencoderconfig, src_main_java_com_example_demo_chat_user_user_user [INFERRED 0.85]
- **User feature: code + schema tying together the User entity** — src_main_java_com_example_demo_chat_user_user_user, src_main_java_com_example_demo_chat_user_userrepository_userrepository, src_main_java_com_example_demo_chat_user_userservice_userservice, src_main_java_com_example_demo_chat_user_userresponse_userresponse, src_main_java_com_example_demo_chat_user_createuserrequest_createuserrequest, src_main_java_com_example_demo_chat_user_usercontroller_usercontroller, src_main_resources_db_migration_v1_create_users_table_users, docs_wiki_features_user_user [EXTRACTED 1.00]
- **Chat feature: code + Cassandra table implementing chat start/history** — src_main_java_com_example_demo_chat_chat_chatcontroller_chatcontroller, src_main_java_com_example_demo_chat_chat_chatservice_chatservice, src_main_java_com_example_demo_chat_chat_chathistory_chathistory, src_main_java_com_example_demo_chat_chat_chatmessage_chatmessage, src_main_java_com_example_demo_chat_chat_chathistoryrepository_chathistoryrepository, docs_wiki_features_chat_chat, docs_wiki_infrastructure_cassandra_chat_history_chat_history [EXTRACTED 1.00]
- **Obsidian wiki Map of Content entries** — docs_wiki_index_index, docs_wiki_features_chat_chat, docs_wiki_features_user_user, docs_wiki_infrastructure_cassandra_chat_history_chat_history, docs_wiki_infrastructure_postgres_users_users, docs_wiki_daily_2026_07_13_2026_07_13 [EXTRACTED 1.00]

## Communities (43 total, 14 thin omitted)

### Community 0 - "Chat History Wiki Notes"
Cohesion: 0.08
Nodes (32): Cassandra Table: chat_history, Columns, Notes, Used By, daily-report skill, Daily log 2026-07-13, Feature: Chat, ChatHistory partition key = user_id, queries expected by user not chat id (+24 more)

### Community 1 - "Chat Domain Model & DTOs"
Cohesion: 0.23
Nodes (11): ChatController, ParticipantRequest, ExceptionHandler, Mono, PostMapping, ResponseEntity, ResponseStatus, UUID (+3 more)

### Community 2 - "Password Encoding Config"
Cohesion: 0.21
Nodes (9): PasswordEncoderConfig, PasswordEncoder, Bean, CreateUserRequest, Mono, User, UserResponse, UUID (+1 more)

### Community 3 - "UserPrincipal (Security Principal)"
Cohesion: 0.16
Nodes (11): GrantedAuthority, UserDetails, List, Override, String, User, UUID, User (+3 more)

### Community 4 - "User REST Endpoints"
Cohesion: 0.25
Nodes (10): GetMapping, CreateUserRequest, ExceptionHandler, Mono, PostMapping, ResponseEntity, ResponseStatus, UserResponse (+2 more)

### Community 5 - "Reactive User Details Service"
Cohesion: 0.19
Nodes (9): Optional, ReactiveUserDetailsService, Mono, Override, String, String, User, SecurityUserDetailsService (+1 more)

### Community 6 - "Project Instructions (CLAUDE.md)"
Cohesion: 0.33
Nodes (8): Commands, Configuration, graphify, Intended architecture (from declared dependencies), Knowledge Sources, Project status, Toolchain, Working with this Vault

### Community 7 - "Chat Feature Wiki Page"
Cohesion: 0.22
Nodes (8): Chat, Decisions, Infrastructure Used, Open Questions, Overview, Related Code, Requirements, Source Log

### Community 8 - "Feature Note Template"
Cohesion: 0.22
Nodes (8): Decisions, <Feature Name>, Infrastructure Used, Open Questions, Overview, Related Code, Requirements, Source Log

### Community 9 - "WebFlux Security Config"
Cohesion: 0.53
Nodes (4): SecurityConfig, SecurityWebFilterChain, ServerHttpSecurity, Bean

### Community 10 - "Wiki Index / MOC"
Cohesion: 0.25
Nodes (7): Daily, demo_chat Wiki, Features, Infrastructure, Linking convention, Map of Content, Structure

### Community 11 - "Obsidian Theme Manifest"
Cohesion: 0.33
Nodes (5): author, authorUrl, minAppVersion, name, version

### Community 12 - "Validation Exception Handling"
Cohesion: 0.53
Nodes (4): ValidationExceptionHandler, ProblemDetail, ExceptionHandler, WebExchangeBindException

### Community 13 - "Daily Log 2026-07-13"
Cohesion: 0.33
Nodes (5): 2026-07-13, API, Data layer, Documentation, Infrastructure & tooling

### Community 14 - "Kafka Topic Note Template"
Cohesion: 0.33
Nodes (5): Consumers, Kafka Topic: <topic-name>, Notes, Producers, Schema

### Community 15 - "Cassandra Table Note Template"
Cohesion: 0.40
Nodes (4): Cassandra Table: <table_name>, Columns, Notes, Used By

### Community 16 - "Obsidian Workspace State"
Cohesion: 0.40
Nodes (5): Daily/2026-07-13.md (wiki daily note, referenced), Features/Chat.md (wiki page, referenced), Features/User.md (wiki page, referenced), Obsidian Core Plugins Enabled, Obsidian Workspace Layout State

### Community 17 - "Postgres Table Note Template"
Cohesion: 0.40
Nodes (4): Columns, Notes, Postgres Table: <table_name>, Used By

### Community 18 - "Qdrant Collection Note Template"
Cohesion: 0.40
Nodes (4): Notes, Payload Fields, Qdrant Collection: <collection_name>, Used By

### Community 19 - "Daily Report Skill"
Cohesion: 0.50
Nodes (3): Format, Gathering what changed, Saving

### Community 29 - "CreateUserRequest DTO"
Cohesion: 0.30
Nodes (8): ChatService, ChatHistory, MessageRequest, List, Mono, String, UUID, Void

## Knowledge Gaps
- **93 isolated node(s):** `name`, `version`, `minAppVersion`, `author`, `authorUrl` (+88 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **14 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `UserDetails` connect `UserPrincipal (Security Principal)` to `Reactive User Details Service`?**
  _High betweenness centrality (0.019) - this node is a cross-community bridge._
- **Are the 2 inferred relationships involving `Postgres Table: users` (e.g. with `Postgres table note template` and `local docker-compose (Postgres/Cassandra/Qdrant/Kafka)`) actually correct?**
  _`Postgres Table: users` has 2 INFERRED edges - model-reasoned connections that need verification._
- **Are the 2 inferred relationships involving `Cassandra Table: chat_history` (e.g. with `Cassandra table note template` and `local docker-compose (Postgres/Cassandra/Qdrant/Kafka)`) actually correct?**
  _`Cassandra Table: chat_history` has 2 INFERRED edges - model-reasoned connections that need verification._
- **What connects `name`, `version`, `minAppVersion` to the rest of the system?**
  _100 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `Chat History Wiki Notes` be split into smaller, more focused modules?**
  _Cohesion score 0.0773109243697479 - nodes in this community are weakly interconnected._