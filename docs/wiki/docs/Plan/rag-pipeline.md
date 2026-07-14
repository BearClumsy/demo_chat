# RAG Pipeline: From User Context to Answer

[← Back to README](README.md) · [Architecture overview](overview.md)

**Status:** implemented (Phase 1), triggered by `POST /api/chats/{chatId}/messages`. Stage [8] Guardrail
below is **not implemented** — it's Phase 2 (see [roadmap.md](roadmap.md)); everything else is.

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
[8] Guardrail check     → is the answer actually grounded in the context?  (not implemented — Phase 2)
   │
   ▼
response → to the user (SSE stream — also not implemented; the reply is returned in one JSON response body)
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
retrieved content. Not implemented — see Status above.

## Where each stage lives in the code

Implemented flat in `com.example.demo_chat.rag` (package-by-feature, not the layered
`service.<layer>.ClassName` naming this table originally used — see
[backend.md](backend.md)'s Rationale section for why):

| Stage | Class |
|---|---|
| Orchestration | `ChatPipelineService` — ties every stage below together, persists `DialogueState`, appends to `ChatHistory` |
| [1] Normalization | `QueryNormalizationService` |
| [2]-[3] Retrieval | `KnowledgeRetrievalService` (`VectorStore.similaritySearch`, top-K via `demo-chat.rag.top-k`) |
| — Intent classification | `IntentClassificationService` + `IntentClassification` (LLM structured output: `intentId`, `confidence`) — the two-stage matching from [intent-matching.md](intent-matching.md) |
| [4] Scope threshold | `ScopeFilter` (`demo-chat.rag.similarity-threshold`, plus whitelist enforcement: the classified id must be one of the retrieved candidates) |
| [5] Slot filling | `SlotFillingService` |
| [6] Prompt assembly | `PromptBuilder` + `AssembledPrompt` |
| [7] Generation | `AnswerGenerationService` (also generates topic-constrained clarifying questions) |
| [8] Guardrail | *not implemented* — Phase 2 |

Supporting components, not pipeline stages themselves:

- `IntentDefinition` / `IntentDefinitionRegistry` — loads `knowledge-base/intents/*.json` at startup into
  an in-memory map; single source of truth for both indexing and post-classification lookup.
- `KnowledgeBaseIndexer` — `ApplicationRunner` that pushes every `IntentDefinition` into the Qdrant
  `support_kb` collection on startup (see [vector-store-schema.md](vector-store-schema.md)).
- `DialogueState` / `DialogueStatus` / `DialogueStateRepository` — the pipeline's per-chat working state
  (status, current intent, slots); see [dialogue-state.md](dialogue-state.md) for the schema and how it
  differs from the original draft.

Full package structure is in [backend.md](backend.md).

## Known Phase-1 simplifications

- No output guardrail (stage [8]) and no SSE streaming — both Phase 2.
- Slot filling doesn't re-run the LLM to extract a slot value: while `DialogueState.status ==
  SLOT_FILLING`, the next raw user message is taken verbatim as the value of the one outstanding slot.
- `DialogueState` doesn't duplicate message history — `ChatHistory.messages` already has it (see
  [dialogue-state.md](dialogue-state.md)).
- Knowledge-base reindexing runs on every app startup rather than as a CI job (see
  [roadmap.md](roadmap.md), Phase 3).

## Related documents

- [Intent matching and slot filling](intent-matching.md)
- [Prompt engineering](prompt-engineering.md)
- [Vector Store schema](vector-store-schema.md)
- [Dialogue session model](dialogue-state.md)