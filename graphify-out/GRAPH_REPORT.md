# Graph Report - demo_chat  (2026-07-13)

## Corpus Check
- 29 files · ~4,009 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 118 nodes · 123 edges · 24 communities (17 shown, 7 thin omitted)
- Extraction: 99% EXTRACTED · 1% INFERRED · 0% AMBIGUOUS · INFERRED: 1 edges (avg confidence: 0.8)
- Token cost: 0 input · 0 output

## Community Hubs (Navigation)
- [[_COMMUNITY_Community 0|Community 0]]
- [[_COMMUNITY_Community 1|Community 1]]
- [[_COMMUNITY_Community 2|Community 2]]
- [[_COMMUNITY_Community 6|Community 6]]
- [[_COMMUNITY_Community 7|Community 7]]
- [[_COMMUNITY_Community 8|Community 8]]
- [[_COMMUNITY_Community 9|Community 9]]
- [[_COMMUNITY_Community 10|Community 10]]
- [[_COMMUNITY_Community 11|Community 11]]
- [[_COMMUNITY_Community 12|Community 12]]
- [[_COMMUNITY_Community 13|Community 13]]
- [[_COMMUNITY_Community 14|Community 14]]
- [[_COMMUNITY_Community 15|Community 15]]
- [[_COMMUNITY_Community 16|Community 16]]
- [[_COMMUNITY_Community 17|Community 17]]
- [[_COMMUNITY_Community 18|Community 18]]
- [[_COMMUNITY_Community 20|Community 20]]
- [[_COMMUNITY_Community 21|Community 21]]
- [[_COMMUNITY_Community 22|Community 22]]
- [[_COMMUNITY_Community 23|Community 23]]

## God Nodes (most connected - your core abstractions)
1. `<Feature Name>` - 8 edges
2. `Kafka Topic: <topic-name>` - 5 edges
3. `UserController` - 4 edges
4. `UserService` - 4 edges
5. `Cassandra Table: <table_name>` - 4 edges
6. `Postgres Table: <table_name>` - 4 edges
7. `Qdrant Collection: <collection_name>` - 4 edges
8. `demo_chat Wiki` - 4 edges
9. `PasswordEncoder` - 3 edges
10. `Mono` - 3 edges

## Surprising Connections (you probably didn't know these)
- None detected - all connections are within the same source files.

## Import Cycles
- None detected.

## Communities (24 total, 7 thin omitted)

### Community 0 - "Community 0"
Cohesion: 0.25
Nodes (6): Commands, Configuration, graphify, Intended architecture (from declared dependencies), Project status, Toolchain

### Community 6 - "Community 6"
Cohesion: 0.22
Nodes (8): Decisions, <Feature Name>, Infrastructure Used, Open Questions, Overview, Related Code, Requirements, Source Log

### Community 7 - "Community 7"
Cohesion: 0.25
Nodes (10): GetMapping, ResponseStatus, CreateUserRequest, ExceptionHandler, Mono, PostMapping, ResponseEntity, UserResponse (+2 more)

### Community 8 - "Community 8"
Cohesion: 0.29
Nodes (6): demo_chat Wiki, Features, Infrastructure, Linking convention, Map of Content, Structure

### Community 9 - "Community 9"
Cohesion: 0.33
Nodes (5): Consumers, Kafka Topic: <topic-name>, Notes, Producers, Schema

### Community 10 - "Community 10"
Cohesion: 0.21
Nodes (9): Bean, PasswordEncoderConfig, PasswordEncoder, CreateUserRequest, Mono, User, UserResponse, UUID (+1 more)

### Community 11 - "Community 11"
Cohesion: 0.40
Nodes (4): Cassandra Table: <table_name>, Columns, Notes, Used By

### Community 12 - "Community 12"
Cohesion: 0.40
Nodes (4): Columns, Notes, Postgres Table: <table_name>, Used By

### Community 13 - "Community 13"
Cohesion: 0.40
Nodes (4): Notes, Payload Fields, Qdrant Collection: <collection_name>, Used By

### Community 14 - "Community 14"
Cohesion: 0.50
Nodes (3): User, UserResponse, from()

### Community 17 - "Community 17"
Cohesion: 0.48
Nodes (5): ChatController, Mono, PostMapping, ResponseEntity, UUID

### Community 20 - "Community 20"
Cohesion: 0.60
Nodes (3): ChatService, Mono, UUID

### Community 23 - "Community 23"
Cohesion: 0.53
Nodes (4): ValidationExceptionHandler, ProblemDetail, ExceptionHandler, WebExchangeBindException

## Knowledge Gaps
- **40 isolated node(s):** `String`, `ChatHistory`, `ChatHistoryRepository`, `ChatMessage`, `User` (+35 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **7 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **What connects `String`, `ChatHistory`, `ChatHistoryRepository` to the rest of the system?**
  _40 weakly-connected nodes found - possible documentation gaps or missing edges._