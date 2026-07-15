# Qdrant Collection: support_kb

**Embedding model:** Bedrock Titan (`amazon.titan-embed-text-v2:0`, `spring.ai.bedrock.titan.embedding.*`)
**Dimensions:** 768 (see `docs/wiki/Plan/vector-store-schema.md`)
**Distance metric:** cosine

## Payload Fields

- `topic` — the intent id (e.g. `refund_status`); doubles as the point id in Qdrant.
- `allowed` — whitelist flag; only `allowed: true` topics are eligible answers (see [[Chat]] decisions).

The embedded `text` is the intent's `knowledge_snippet` — the same text used both to decide whether a
topic is in scope and as the source content for the generated answer ("RAG as a single source of truth,"
see `docs/wiki/Plan/overview.md`).

## Used By

- [[Chat]] — `KnowledgeRetrievalService.retrieve()` (stage 2 of the RAG pipeline) runs
  `VectorStore.similaritySearch()` against this collection with `topK` = `demo-chat.rag.top-k` (default
  3) to get candidate intents for a normalized query.

## Notes

- Populated by `KnowledgeBaseIndexer` (an `ApplicationRunner`), which pushes every
  `IntentDefinition` loaded from `src/main/resources/knowledge-base/intents/*.json` into this collection
  on every app startup (`demo-chat.rag.reindex-on-startup=true`). Document ids are the intent id, so
  re-running the indexer just upserts the same points rather than duplicating them.
- Currently 4 intents: `refund_status`, `order_status`, `change_shipping_address`, `password_reset`.
- Schema is auto-created by Spring AI (`spring.ai.vectorstore.qdrant.initialize-schema=true`) — no
  manual collection setup needed locally (`docker-compose` just needs a running Qdrant instance).
- CI-triggered reindexing on knowledge-base changes (instead of on every startup) is still planned —
  see `docs/wiki/Plan/roadmap.md`, Phase 3.
- See also [[semantic_cache]] — a second, separate Qdrant collection added in Phase 2 that reuses the
  same underlying Qdrant client/embedding model as this one, but for caching generated answers, not the
  knowledge base itself.
