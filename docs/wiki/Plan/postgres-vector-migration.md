# Replace Qdrant with PostgreSQL (pgvector)

[← Back to README](README.md) · [RAG pipeline](rag-pipeline.md) · [Vector Store schema](vector-store-schema.md)

*Status: planned, not yet implemented (2026-09-14).*

## Context

The app currently uses Qdrant as its vector store, in two shapes: an autoconfigured primary
`VectorStore` (collection `support_kb`, backing RAG retrieval) and a manually-declared second
`VectorStore` bean (collection `semantic_cache`, backing the cache short-circuit), both sharing one
Qdrant client/embedding model. Qdrant exists as a local Docker Compose container, a never-applied
Terraform EC2+EBS module, and full (also never-applied) k8s ConfigMap/Secret/NetworkPolicy wiring.

The goal is to drop the extra moving part (a separate Qdrant service/process/infra footprint in
every environment) and host both vector collections as tables in the Postgres instance the app
already runs, using Spring AI's PGVector store. Investigation confirmed this is a clean swap:
neither consumer does Qdrant-specific filtering (`SearchRequest` is bare query+topK[+threshold]
everywhere), so `KnowledgeRetrievalService` and `SemanticCacheService` need no code changes beyond
bean wiring. Three open design questions were resolved with the user before finalizing this plan:
delete the Qdrant Terraform module outright (rather than keep it as a lint-clean reference), keep
two separate Postgres tables/beans (mirrors today's two-collection split, avoids adding
filter-expression code), and treat the offline profile's differing embedding dimension as the same
pre-existing `make nuke`-to-reset gotcha it already is today, just against a Postgres table instead
of a Qdrant volume.

Nothing is live in AWS (Terraform/k8s are lint-only), so this is a clean-slate change with no data
migration to worry about — only local dev volumes need a reset.

## Implementation steps

### 0. Verify the exact PgVectorStore DDL first

Before writing the Flyway migration, pull the actual schema Spring AI's `PgVectorStore`
auto-creates (via `initializeSchema=true`) from the resolved `spring-ai-pgvector-store` jar
(version pinned by the `spring-ai-bom:2.0.0` already in `modules/server/build.gradle`) — column
names/types for id/content/metadata/embedding, and the exact index-creation SQL. Copying this
precisely avoids the new `V2__` migration diverging from what Spring AI itself would create (which
would break query compatibility or double-create objects).

### 1. Dependencies — `modules/server/build.gradle`

- Remove `implementation 'org.springframework.ai:spring-ai-starter-vector-store-qdrant'`.
- Add `implementation 'org.springframework.ai:spring-ai-starter-vector-store-pgvector'`.
- No driver change needed — `runtimeOnly 'org.postgresql:postgresql'` already covers pgvector (it's
  a Postgres extension, not a different wire protocol), and `spring-boot-starter-jdbc` (already a
  dependency, used today for Flyway) is sufficient for Spring Boot to autoconfigure the
  `DataSource`/`JdbcTemplate` beans `PgVectorStore` needs — no new bean required.

### 2. Flyway migration — new file `modules/server/src/main/resources/db/migration/V2__create_vector_store_tables.sql`

(Never edit `V1__create_users_table.sql`.) Using the DDL confirmed in step 0, create:
- `CREATE EXTENSION IF NOT EXISTS vector;`
- `demo_chat.support_kb` table (`id uuid PRIMARY KEY`, content/metadata/embedding columns matching
  Spring AI's schema, `embedding vector(1024)` — pinned to Bedrock Titan v2's dimension, matching
  `StubBedrockModels.EMBEDDING_DIMENSIONS` in tests and the `CLAUDE.md` gotcha about the
  offline/Titan dimension mismatch) plus an HNSW cosine index.
- `demo_chat.semantic_cache` table, same shape, plus its HNSW cosine index.
- Run this same migration in **every** profile (including local/test/offline) for one code path;
  Spring AI's `initialize-schema=true` in local/test/offline just no-ops against the
  already-created tables (`IF NOT EXISTS` semantics), matching today's belt-and-suspenders pattern.
- Per the confirmed decision: no separate offline-only table/schema. If the offline profile embeds
  at 768 dimensions against a `vector(1024)` column, inserts fail the same way the Qdrant dimension
  mismatch already does today — resolved by `make nuke && make up-offline`, not a new regression.

### 3. Java — `modules/server/src/main/java/com/example/demo_chat/config/SemanticCacheVectorStoreConfig.java`

Rewrite to build a `PgVectorStore` instead of a `QdrantVectorStore`:

```java
@Configuration
public class SemanticCacheVectorStoreConfig {

  @Bean
  @Qualifier("semanticCacheVectorStore")
  public VectorStore semanticCacheVectorStore(
      JdbcTemplate jdbcTemplate,
      EmbeddingModel embeddingModel,
      @Value("${demo-chat.cache.pgvector-table:semantic_cache}") String tableName,
      @Value("${spring.ai.vectorstore.pgvector.schema-name:demo_chat}") String schemaName,
      @Value("${spring.ai.vectorstore.pgvector.initialize-schema:true}") boolean initializeSchema) {
    return PgVectorStore.builder(jdbcTemplate, embeddingModel)
        .schemaName(schemaName)
        .vectorTableName(tableName)
        .initializeSchema(initializeSchema)
        .build();
  }
}
```

Rename the property `demo-chat.cache.qdrant-collection` → `demo-chat.cache.pgvector-table`
everywhere it appears (property files, this class, docs).

The primary/unqualified `VectorStore` (→ `support_kb`) comes from the pgvector starter's
autoconfiguration, driven entirely by `spring.ai.vectorstore.pgvector.*` properties — no manual
bean, exactly mirroring today's Qdrant setup.

**`rag/KnowledgeBaseIndexer.java`**: no behavioral change — `PgVectorStore.add()` does an
upsert-by-id (`INSERT ... ON CONFLICT (id) DO UPDATE`) against a `uuid` primary key, and the
existing `UUID.nameUUIDFromBytes(intentId.getBytes())` deterministic id already produces a valid
UUID, so idempotent reindexing is preserved. Only update the javadoc/comments that currently cite
the Qdrant-specific reason for the UUID scheme ("Qdrant point ids must be an unsigned integer or a
UUID") to reference pgvector's UUID primary key instead.

**`rag/KnowledgeRetrievalService.java`**, **`rag/SemanticCacheService.java`**: no code changes —
both depend only on the generic `VectorStore`/`SearchRequest` API.

### 4. Properties — all five profile files + test

- **`application.properties`**: remove `spring.ai.vectorstore.qdrant.collection-name=support_kb`;
  add `spring.ai.vectorstore.pgvector.table-name=support_kb`,
  `spring.ai.vectorstore.pgvector.schema-name=demo_chat`; rename
  `demo-chat.cache.qdrant-collection` → `demo-chat.cache.pgvector-table`.
- **`application-local.properties`**: delete the four `spring.ai.vectorstore.qdrant.*` lines; add
  `spring.ai.vectorstore.pgvector.initialize-schema=true`. The existing `spring.datasource.*` block
  (already present for Flyway) now also backs pgvector — no new datasource config.
- **`application-staging.properties`** / **`application-prod.properties`**: remove the `# Qdrant
  (self-managed...)` block (all `QDRANT_*`-bound keys); add
  `spring.ai.vectorstore.pgvector.schema-name=${POSTGRES_SCHEMA:demo_chat}`,
  `spring.ai.vectorstore.pgvector.dimensions=1024`,
  `spring.ai.vectorstore.pgvector.initialize-schema=false` (Flyway owns schema creation; pinning
  `dimensions` explicitly avoids any live Bedrock call at boot, same intent as today's
  `QDRANT_INITIALIZE_SCHEMA=false`). Keep the two files duplicated key-for-key per existing
  convention.
- **`application-offline.properties`**: replace
  `spring.ai.vectorstore.qdrant.initialize-schema=true` with
  `spring.ai.vectorstore.pgvector.initialize-schema=true`.
- **`application-test.properties`**: remove the two `spring.ai.vectorstore.qdrant.*` lines; add
  `spring.ai.vectorstore.pgvector.initialize-schema=true`,
  `spring.ai.vectorstore.pgvector.schema-name=demo_chat`.

### 5. Tests — `modules/server/src/test/java/com/example/demo_chat/DemoChatApplicationTests.java`

- Delete the Qdrant `@Container` field and its two `@DynamicPropertySource` registrations
  (`spring.ai.vectorstore.qdrant.host/port`).
- Change the Postgres Testcontainer image from `postgres:16-alpine` to `pgvector/pgvector:pg16` (the
  plain image doesn't ship the `vector` extension) — this is the **same** container already used
  for Flyway/R2DBC, so no second container is needed (a net simplification vs. today's two
  containers).
- No new `@DynamicPropertySource` entries needed for pgvector — schema/table/dimension are static
  profile properties; only the JDBC URL (already registered) is container-derived.
- Update comments/javadoc referencing "Qdrant collection" → "pgvector table".
- `SemanticCacheServiceTest.java`: no changes (pure Mockito `VectorStore` mock, no real backend).

### 6. Local dev — `modules/server/src/main/resources/local/docker-compose.yml`

- Delete the `qdrant` service block and the `qdrant-data` named volume.
- Change the `postgres` service image from `postgres:16-alpine` to `pgvector/pgvector:pg16`.
- Update the sizing comment that lists "Postgres/Cassandra/Qdrant/Kafka" to drop "Qdrant".
- After merging, run `make nuke && make up` locally to reset volumes (new Postgres image + schema).

### 7. Infra (lint-only, never applied)

- **Delete** `infra/terraform/modules/qdrant-ec2/` outright (confirmed with user).
- `infra/terraform/envs/staging/main.tf` and `envs/prod/main.tf`: remove the `module "qdrant" {
  ... }` block and the four `QDRANT_*` keys from `plaintext_env`; remove the `qdrant_ami_id`
  variable from both envs' `variables.tf` if it becomes unused (keeps `tflint`'s
  `terraform_unused_declarations` check clean).
- `infra/k8s/manifest-staging.yaml` / `manifest-prod.yaml`:
  - ConfigMap `demo-chat-config`: remove `QDRANT_HOST/PORT/USE_TLS/INITIALIZE_SCHEMA`.
  - Secret `demo-chat-secrets`: remove `QDRANT_API_KEY`.
  - NetworkPolicy `allow-egress`: remove the port-6334 egress rule and update its comment.
  - `demo-chat-kb-bootstrap` Job: remove the `QDRANT_INITIALIZE_SCHEMA` env override (keep
    `DEMO_CHAT_RAG_REINDEX_ON_STARTUP=true` — the job's purpose, seeding `support_kb` once, is
    unchanged); update its description comment to drop the "collections need creating" framing
    since Flyway now creates the table ahead of the job.
  - Drop any other stray "Qdrant" mentions in comments (e.g. the liveness-probe outage comment).
- `infra/terraform/modules/rds-postgres/`: no code change — `engine_version` already defaults to a
  pgvector-capable Postgres version (15+); add a one-line comment noting the vector tables are
  created by the same Flyway migration as `users`, no separate provisioning step.
- Run `make tf-lint` and `make k8s-lint` after these edits.

### 8. Docs

- Move `docs/wiki/Infrastructure/Qdrant/support_kb.md` and `semantic_cache.md` to
  `docs/wiki/Infrastructure/Postgres/` (that directory already exists alongside `users.md`).
  Rewrite "Qdrant collection" → "Postgres table (pgvector)" framing; fix the dimension figure to
  1024 (the existing "768" in these docs is a stale/pre-existing error — Titan v2 is 1024-dim per
  `StubBedrockModels` and the `CLAUDE.md` gotcha); preserve `semantic_cache.md`'s no-TTL/cache-
  poisoning risk note verbatim (unrelated to this migration). Delete the old
  `Infrastructure/Qdrant/` directory once moved.
- `docs/wiki/Plan/rag-pipeline.md` and `docs/wiki/Plan/vector-store-schema.md`: replace Qdrant
  collection references with pgvector table references; the note about Bedrock/vector-store calls
  being wrapped in `Mono.fromCallable(...).subscribeOn(Schedulers.boundedElastic())` stays —
  PgVectorStore is equally JDBC/blocking, same bridging applies, just reword "Qdrant" → "pgvector".
- `CLAUDE.md` — per its own "Keeping this file current" section, update every Qdrant mention:
  - Common tasks: "Start local infra (Postgres, Cassandra, Qdrant)" → drop "Qdrant".
  - Server package map (`config/`): "second Qdrant `VectorStore` bean" → "second pgvector
    `VectorStore` bean (table `semantic_cache`)".
  - Storage split: "Qdrant holds two vector collections" → "Postgres (via pgvector) holds two
    vector tables (`support_kb`, `semantic_cache`) alongside `users`"; update the "decide whether
    it's chat memory (Cassandra) or vector data (Qdrant)" guidance to say "(pgvector)".
  - Conventions › Java: "Bedrock and Qdrant calls bridge through boundedElastic()" → "Bedrock and
    pgvector (JDBC) calls…"; add a clause noting pgvector's JDBC access is an intentionally
    separate connection style from the `users` R2DBC pool, both against the same Postgres instance.
  - Tests section: drop "Qdrant" from the Testcontainers list (one fewer container now).
  - Configuration section: "Qdrant collection names" → "pgvector table names"; drop
    `QDRANT_API_KEY` from the secrets list; reword the schema-initialization sentence to reference
    `spring.ai.vectorstore.pgvector.initialize-schema`.
  - Infrastructure section: "seed Qdrant `support_kb`" → "seed the `support_kb` pgvector table".
  - Gotchas: reword the dimension-mismatch gotcha from "same Qdrant volume" to "same Postgres
    volume/table" (the `make nuke` remedy is unchanged).

### 9. Order of landing

Since nothing is live in AWS, this is a single clean-slate swap, not a staged rollout:
1. build.gradle + Flyway migration + Java bean/property changes together (the app can't start in a
   half-migrated state).
2. `docker-compose.yml` update, then `make nuke && make up` locally.
3. Test changes in the same change set (tests won't compile/run against a removed Qdrant container
   otherwise).
4. Infra (Terraform/k8s) changes — same or a follow-up commit, no ordering dependency.
5. Docs last, once file paths/property names are final.

## Verification

- `./gradlew :server:spotlessApply` then `:server:spotlessCheck`.
- `./gradlew :server:test` (needs Docker — Testcontainers now use `pgvector/pgvector:pg16` +
  Cassandra); confirms `DemoChatApplicationTests` passes against the new bean graph.
- `./gradlew :server:flywayInfo` / `:server:flywayMigrate` locally to confirm `V2__` applies
  cleanly on top of `V1__`.
- `make nuke && make up`, then `./gradlew :server:bootRun` (local profile) — confirm
  `KnowledgeBaseIndexer` populates `support_kb` on startup; run `make verify-chat` for an
  end-to-end smoke test (health → create chat → send message → non-empty reply).
- Idempotency check: trigger reindexing twice (e.g. two app restarts with
  `reindex-on-startup=true`) and confirm `SELECT count(*) FROM demo_chat.support_kb` matches the
  intent count both times — validates the deterministic-UUID upsert behavior.
- `make tf-lint` and `make k8s-lint` after the infra edits.
- Diff `application-staging.properties` against `application-prod.properties` to confirm they still
  match key-for-key aside from intentional pool-size/log-level deltas.

## Open follow-ups if this plan is later revisited

- The exact Flyway DDL (column names/types for `content`/`metadata`/`embedding`, HNSW index
  parameters) still needs to be pulled from the resolved `spring-ai-pgvector-store` jar per step 0
  — this plan documents the *shape*, not verbatim SQL.
- No `ef_construction`/`m` HNSW tuning is proposed (Postgres/pgvector defaults) — fine at the
  current 4-intent knowledge base scale; revisit if the knowledge base grows significantly.
