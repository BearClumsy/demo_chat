# RAG Pipeline: From User Context to Answer

[← Back to README](README.md) · [Architecture overview](overview.md)

## Pipeline stages

```
raw_input
   │
   ▼
[1] Normalization      → clean query (LLM)
   │
   ▼
[2] Embedding           → query vector
   │
   ▼
[3] Retrieval           → top-N candidates from the Vector Store
   │
   ▼
[4] Scope threshold     → in-scope / out-of-scope
   │
   ├── out-of-scope ──▶ escalate to a human agent
   │
   ▼
[5] Slot filling        → check required_context
   │
   ├── something missing ──▶ clarifying question
   │
   ▼
[6] Prompt assembly     → system prompt + retrieved content + slots
   │
   ▼
[7] Generation          → LLM generates the answer
   │
   ▼
[8] Guardrail check     → is the answer actually grounded in the context?
   │
   ▼
response → to the user (SSE stream)
```

## Example of a single request going through the pipeline

**Input:** `"I want my money back, when is it finally coming"`

**[1] Normalization:**
```
"Refund status for an order"
```

**[2]–[3] Retrieval (top-1 from Qdrant):**
```json
{
  "id": "refund_status_001",
  "score": 0.86,
  "metadata": {
    "topic": "refund_status",
    "required_context": ["order_id"],
    "allowed": true
  }
}
```

**[4] Scope threshold:** `0.86 > 0.75` → in-scope, continue.

**[5] Slot filling:** `order_id` is missing from the session state →
the bot asks for the order number without generating a final answer.

**[6]–[7] After receiving order_id:** the prompt is assembled from the
`knowledge_snippet` + `order_id`, and the LLM generates a personalized answer.

**[8] Guardrail:** a separate lightweight pass (or an NLI classifier)
verifies that the answer doesn't contain facts unsupported by the
retrieved content.

## Where each stage lives in the code (see backend structure)

| Stage | Layer in Spring Boot |
|---|---|
| Normalization | `service.normalization.QueryNormalizationService` |
| Retrieval | `service.retrieval.KnowledgeRetrievalService` |
| Scope threshold | `service.retrieval.ScopeFilter` |
| Slot filling | `service.dialogue.SlotFillingService` |
| Prompt assembly | `service.prompt.PromptBuilder` |
| Generation | `service.generation.AnswerGenerationService` |
| Guardrail | `service.guardrail.ResponseValidator` |

Full package structure is in [backend.md](../project-structure/backend.md).

## Related documents

- [Intent matching and slot filling](intent-matching.md)
- [Prompt engineering](prompt-engineering.md)
- [Vector Store schema](../data/vector-store-schema.md)
