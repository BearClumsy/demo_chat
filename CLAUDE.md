# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project status

This is a freshly-scaffolded Spring Boot project (Spring Initializr output). The only code present is the
generated application entry point and a placeholder context-load test — no controllers, services, entities,
or AI/RAG wiring have been written yet. Treat the dependency set in `build.gradle` as a statement of intended
architecture, not a description of existing code.

## Commands

Use the Gradle wrapper (`./gradlew`), not a system-installed Gradle.

- Build: `./gradlew build`
- Run the app: `./gradlew bootRun`
- Run all tests: `./gradlew test`
- Run a single test class: `./gradlew test --tests "com.example.demo_chat.DemoChatApplicationTests"`
- Run a single test method: `./gradlew test --tests "com.example.demo_chat.DemoChatApplicationTests.contextLoads"`
- Clean build output: `./gradlew clean`

Tests use JUnit 5 (`useJUnitPlatform()` is configured in `build.gradle`).

## Toolchain

- Java 26 (via Gradle toolchain in `build.gradle` — do not assume the system JDK matches; let the wrapper
  provision it).
- Spring Boot 4.0.7, with dependency versions managed via `io.spring.dependency-management`.
- Spring AI 2.0.0, imported as a BOM (`springAiVersion` in `build.gradle`).
- Root package: `com.example.demo_chat`.

## Intended architecture (from declared dependencies)

The dependency set points to a RAG-style chat application with the following shape once implemented:

- **Reactive web layer**: `spring-boot-starter-webflux` + `spring-boot-starter-webclient` — endpoints and
  outbound HTTP calls are expected to be reactive (`Mono`/`Flux`), not servlet-based.
- **LLM access**: `spring-ai-starter-model-bedrock` — chat completions go through Amazon Bedrock.
- **Vector search**: `spring-ai-starter-vector-store-qdrant` + `spring-ai-vector-store-advisor` — retrieval-
  augmented generation is backed by a Qdrant vector store, wired in via a Spring AI advisor rather than
  manual retrieval calls.
- **Document ingestion**: `spring-ai-markdown-document-reader` — source documents for the vector store are
  expected to be Markdown, chunked/loaded through Spring AI's ETL pipeline.
- **Chat memory**: `spring-ai-starter-model-chat-memory-repository-cassandra` — conversation history is
  persisted in Cassandra, accessed reactively (`spring-boot-starter-data-cassandra-reactive`).
- **Relational persistence**: `spring-boot-starter-data-jpa` + `postgresql` driver + `spring-boot-starter-
  flyway` (with `flyway-database-postgresql`) — any relational schema (e.g. app/user data distinct from
  vector or chat-memory storage) is Postgres, version-controlled via Flyway migrations. Migration files
  belong under `src/main/resources/db/migration` following Flyway's `V<version>__description.sql` naming.
- **Messaging**: `spring-boot-starter-kafka` — expect async event production/consumption alongside the
  synchronous chat API.
- **Observability**: `spring-boot-starter-actuator` — health/metrics endpoints are expected to be enabled.
- Lombok is available (`compileOnly` + annotation processor) for reducing boilerplate on entities/DTOs.

Because JPA/Postgres, Cassandra, and Qdrant are all present, expect three distinct data stores serving
different purposes (transactional data, chat memory, vector embeddings) rather than a single database —
don't default to putting new persistent state in JPA/Postgres without checking whether it's chat memory or
vector data instead.

## Configuration

`src/main/resources/application.properties` currently only sets `spring.application.name`. Connection
details for Postgres, Cassandra, Qdrant, Kafka, and Bedrock credentials will need to be added here (or in
profile-specific `application-<profile>.properties` / environment variables) as those integrations are
built out.

## Knowledge Sources

This project has two navigable knowledge sources — prefer them over raw Read/grep:

- `graphify-out/` — auto-generated code graph (god nodes, communities, cross-file relationships)
- `docs/wiki/` — Obsidian vault of manually/AI-curated project knowledge (requirements, decisions,
  notes), distinct from graphify's auto-generated graph

### graphify

- Use `graphify query "<question>"` when `graphify-out/graph.json` exists. Use
  `graphify path "<A>" "<B>"` for relationships and `graphify explain "<concept>"` for focused
  concepts. These return a scoped subgraph, usually much smaller than GRAPH_REPORT.md or raw grep
  output.
- Read `graphify-out/GRAPH_REPORT.md` only for broad architecture review or when
  query/path/explain do not surface enough context.
- After modifying code, run `graphify update .` to keep the graph current (AST-only, no API cost).

### Working with this Vault

This is an Obsidian vault. For reading/searching/writing notes, use the Obsidian CLI
(`obsidian ...`) rather than directly reading `.md` files through Read — the CLI works through the
Obsidian runtime and correctly updates links, front matter, and indexes.

Structure: `Features/<name>.md`, `Infrastructure/{Kafka,Postgres,Cassandra,Qdrant}/<resource>.md`,
`Daily/<YYYY-MM-DD>.md`, each folder with a `_template.md` to copy from. `index.md` is the MOC
entry point.

Before manually grepping files, first try:
- `graphify query "<question>"` — broad context/connections on a topic
- `obsidian search query="<term>"` — exact search by headings/tags
- `obsidian links <note>` / `obsidian backlinks <note>` — link graph of a single note
