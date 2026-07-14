# Vector Store Schema (Topics + Answers)

[← Back to README](README.md) · [RAG pipeline](../rag/rag-pipeline.md)

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

## Knowledge base update process

```
knowledge-base/intents/*.json  (source of truth in the repository)
        │
        ▼  (CI job on merge to main, see github-actions.md)
QdrantDocumentLoader.reindex()
        │
        ▼
Qdrant collection (staging → then prod after verification)
```

## Related documents

- [Intent matching](../rag/intent-matching.md)
- [GitHub Actions pipelines](ci-cd/github-actions.md)
