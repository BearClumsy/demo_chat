# Qdrant Collection: semantic_cache

**Embedding model:** Bedrock Titan (`amazon.titan-embed-text-v2:0`) — same model/bean as [[support_kb]]
**Dimensions:** 768 (same embedding model as [[support_kb]])
**Distance metric:** cosine

## Payload Fields

- `answer` — the guardrail-validated generated answer text to return on a cache hit.
- `intentId` — the intent the cached answer was generated for.
- `cachedAt` — ISO-8601 timestamp of when the entry was written; not yet used for eviction (see Notes).

The embedded `text` is the **normalized user query** — this is what a future query is matched against,
not the answer itself.

## Used By

- [[Chat]] — `SemanticCacheService.lookup()`/`.store()`, called from `ChatPipelineService`:
  - `lookup()` runs right after query normalization (before retrieval/classification/generation), with
    `topK=1` and a high similarity threshold (`demo-chat.cache.similarity-threshold`, default `0.95`). A
    hit short-circuits the rest of the pipeline entirely.
  - `store()` runs only after a generated answer **passes** the output-side groundedness guardrail
    (`ResponseValidator`) — a rejected/ungrounded answer is never cached.

## Notes

- Added in Phase 2 (`docs/wiki/Plan/roadmap.md`). A second, `@Qualifier`-disambiguated `VectorStore`
  bean (`SemanticCacheVectorStoreConfig`), separate from the primary [[support_kb]] bean, but sharing the
  same underlying Qdrant client and `EmbeddingModel` — no second Qdrant connection is opened.
- Chosen over a Redis cache (no Redis in this project) or an exact-string-match Cassandra cache, so hits
  catch **semantically similar** repeat queries (paraphrases), not just literal string repeats.
- **Cache-poisoning risk**: this is exactly why writes are gated on the guardrail passing — a false
  negative there would otherwise get served to every future semantically-similar query.
- **No TTL/invalidation** yet — a knowledge-base reindex (see [[support_kb]]) does **not** invalidate
  previously-cached answers. `cachedAt` is stored so a future cleanup job can filter by max age without a
  schema change; don't assume cached answers are fresh without checking this.
- **Global, not user-scoped** — safe today because none of the four current intents bake real per-user
  data (e.g. an actual order lookup) into the generated answer; all answers are generic policy text from
  the intent's `knowledge_snippet`. If a future phase adds real per-order/user lookups, this cache must
  be scoped by slot values or excluded for those intents, or it will leak one user's data to another's
  semantically-similar query.
- Config: `demo-chat.cache.enabled`, `demo-chat.cache.similarity-threshold`,
  `demo-chat.cache.qdrant-collection` (default `semantic_cache`).
