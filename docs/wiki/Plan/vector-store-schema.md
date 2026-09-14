# Vector Store Schema (Topics + Answers)

[← Back to README](README.md) · [RAG pipeline](rag-pipeline.md)

Backed by pgvector (`org.springframework.ai:spring-ai-starter-vector-store-pgvector`) — two tables
in the same Postgres instance as `users`, not a separate Qdrant service. See
[postgres-vector-migration.md](postgres-vector-migration.md) for the migration itself and
[[support_kb]] / [[semantic_cache]] in the Obsidian wiki's `Infrastructure/Postgres/` for the
as-built table shape.

## `support_kb` table

Each record is simultaneously an "allowed topic" (for the scope filter)
and a "source of the answer" (for generation).

```json
{
  "id": "refund_status_001",
  "vector": [0.021, -0.114, "... 1024 dims"],
  "text": "Refunds are processed within 3-5 business days from the moment the request is confirmed.",
  "metadata": {
    "topic": "refund_status",
    "canonical_questions": [
      "Where is my refund?",
      "When will the refund money arrive?",
      "Refund status"
    ],
    "required_context": ["order_id"],
    "answer_template": "For order {order_id}: the refund is processed within 3-5 days from confirmation.",
    "allowed": true,
    "escalation_fallback": "If the status is unclear, please contact a human agent.",
    "version": "2026-06-01",
    "locale": "en"
  }
}
```

## Metadata fields

| Field | Purpose |
|---|---|
| `topic` | Topic/intent identifier, used in the scope prompt |
| `canonical_questions` | Reference phrasings for the fast embedding filter |
| `required_context` | List of slots that must be collected before answering |
| `answer_template` | Template for personalization after slot filling |
| `allowed` | Explicit flag — is this topic allowed (whitelist principle) |
| `escalation_fallback` | Text used when there isn't enough information |
| `version` / `locale` | Versioning of the knowledge base content |

## Table indexes and parameters (pgvector)

| Parameter | Value |
|---|---|
| Distance | Cosine (`vector_cosine_ops`) |
| Vector size | 1024 (Bedrock Titan v2; depends on the embedding model — see the offline-profile gotcha in `CLAUDE.md`) |
| Index | HNSW, Postgres/pgvector defaults (no explicit `m`/`ef_construction` tuning) |
| Metadata | a single `json` column (`topic`, `allowed`), not indexed — filtering happens in application code (`ScopeFilter`), not pushed down to Postgres |

## `semantic_cache` table (Phase 2)

A second, separate pgvector table used by `SemanticCacheService` to cache previously-generated,
guardrail-validated answers — keyed by semantic similarity of the normalized query, not an exact string
match. Configured via `demo-chat.cache.*` properties; created by a dedicated `VectorStore` bean
(`SemanticCacheVectorStoreConfig`) alongside the primary `support_kb` `VectorStore` bean, both sharing the
same underlying `JdbcTemplate`/`EmbeddingModel` beans — no second database connection is opened.

```json
{
  "id": "b7e3...",
  "vector": ["... embedding of the normalized query"],
  "text": "where is my refund",
  "metadata": {
    "answer": "For order 48213: the refund is processed within 3-5 days from confirmation.",
    "intentId": "refund_status",
    "cachedAt": "2026-07-15T10:03:05Z"
  }
}
```

Looked up with `topK=1` and a high similarity threshold (`demo-chat.cache.similarity-threshold`, default
`0.95`) right after query normalization; a hit skips retrieval, classification, generation, and the
guardrail entirely (a cache hit was already guardrail-validated when it was written). Written only after
a stage-[8] guardrail pass, so a rejected/ungrounded answer is never cached. No TTL/invalidation exists
yet — a `cachedAt` field is stored so a future cleanup job can filter by max age without a schema change.

## Knowledge base update process

```
knowledge-base/intents/*.json  (source of truth in the repository)
        │
        ▼  (today: on every app startup — KnowledgeBaseIndexer, an ApplicationRunner;
        │   planned: CI job on merge to main instead, see github-actions.md)
KnowledgeBaseIndexer.reindex()  (upserts by intent id, idempotent)
        │
        ▼
pgvector table (support_kb)
```

## Related documents

- [Intent matching](intent-matching.md)
- [GitHub Actions pipelines](github-actions.md) — planned
