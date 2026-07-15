# Vector Store Schema (Topics + Answers)

[← Back to README](README.md) · [RAG pipeline](rag-pipeline.md)

## `support_kb` collection

Each record is simultaneously an "allowed topic" (for the scope filter)
and a "source of the answer" (for generation).

```json
{
  "id": "refund_status_001",
  "vector": [0.021, -0.114, "... 768 dims"],
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

## Collection indexes and parameters (Qdrant)

| Parameter | Value |
|---|---|
| Distance | Cosine |
| Vector size | 768 (depends on the embedding model) |
| Payload index | `topic`, `allowed`, `locale` — for filtering during search |
| HNSW `ef_construct` | tuned to the size of the knowledge base (typically 100–200) |

## `semantic_cache` collection (Phase 2)

A second, separate Qdrant collection used by `SemanticCacheService` to cache previously-generated,
guardrail-validated answers — keyed by semantic similarity of the normalized query, not an exact string
match. Configured via `demo-chat.cache.*` properties; created by a dedicated `VectorStore` bean
(`SemanticCacheVectorStoreConfig`) alongside the primary `support_kb` `VectorStore` bean, both sharing the
same underlying Qdrant client/embedding model.

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
Qdrant collection (support_kb)
```

## Related documents

- [Intent matching](intent-matching.md)
- [GitHub Actions pipelines](github-actions.md) — planned
