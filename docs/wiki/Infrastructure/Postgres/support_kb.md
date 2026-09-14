# Postgres Table: support_kb

**Migration:** `V2__create_vector_store_tables.sql` (pgvector, replacing the earlier Qdrant `support_kb`
collection — see `docs/wiki/Plan/postgres-vector-migration.md`)

**Embedding model:** Bedrock Titan (`amazon.titan-embed-text-v2:0`, `spring.ai.bedrock.titan.embedding.*`)
**Dimensions:** 1024 (column is `vector(1024)`, fixed at migration time — see Notes)
**Distance metric:** cosine (`vector_cosine_ops`, HNSW index)

## Columns

- `id` — `uuid`, PK, `DEFAULT uuid_generate_v4()` (Spring AI's own default; the app always supplies a
  deterministic id explicitly — see Notes)
- `content` — `text`, the embedded text (the intent's `knowledge_snippet`)
- `metadata` — `json`:
  - `topic` — the intent id (e.g. `refund_status`)
  - `allowed` — whitelist flag; only `allowed: true` topics are eligible answers (see [[Chat]] decisions)
- `embedding` — `vector(1024)`

The embedded `content` is the intent's `knowledge_snippet` — the same text used both to decide whether a
topic is in scope and as the source content for the generated answer ("RAG as a single source of truth,"
see `docs/wiki/Plan/overview.md`).

## Used By

- [[Chat]] — `KnowledgeRetrievalService.retrieve()` (stage 2 of the RAG pipeline) runs
  `VectorStore.similaritySearch()` against this table with `topK` = `demo-chat.rag.top-k` (default
  3) to get candidate intents for a normalized query.

## Notes

- Populated by `KnowledgeBaseIndexer` (an `ApplicationRunner`), which pushes every
  `IntentDefinition` loaded from `modules/server/src/main/resources/knowledge-base/intents/*.json` into
  this table on every app startup (`demo-chat.rag.reindex-on-startup=true`). Document ids are a
  name-based UUID derived from the intent id, so re-running the indexer just upserts (`ON CONFLICT (id)
  DO UPDATE`) the same rows rather than duplicating them.
- Currently 4 intents: `refund_status`, `order_status`, `change_shipping_address`, `password_reset`.
- Schema (extension, table, HNSW index) is Flyway-managed (`V2__create_vector_store_tables.sql`) in
  staging/prod; `spring.ai.vectorstore.pgvector.initialize-schema=true` in local/test/offline is a
  redundant safety net that no-ops once Flyway has already created the table.
- The embedding column width is fixed at `vector(1024)` — unlike Qdrant, which could recreate a
  collection at whatever dimension the active embedding model reported, switching to a different-sized
  embedding model here needs a new migration (`ALTER COLUMN ... TYPE vector(n)`) or a fresh table, not
  just a config change. The offline profile's Ollama `nomic-embed-text` (768-dim) already can't share
  this column with Bedrock Titan v2 (1024-dim) — see the `CLAUDE.md` gotcha; `make nuke && make up` (or
  `make up-offline`) resets the local Postgres volume when switching between the two.
- CI-triggered reindexing on knowledge-base changes (instead of on every startup) is still planned —
  see `docs/wiki/Plan/roadmap.md`, Phase 3.
- See also [[semantic_cache]] — a second, separate pgvector table added in Phase 2 that reuses the
  same underlying `JdbcTemplate`/`EmbeddingModel` beans as this one, but for caching generated answers,
  not the knowledge base itself.
