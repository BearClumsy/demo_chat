# RAG Pipeline: From User Context to Answer

[← Back to README](README.md) · [Architecture overview](overview.md)

**Status:** implemented (Phase 1 + 2), triggered by `POST /api/chats/{chatId}/messages` (buffered JSON
reply) or `POST /api/chats/{chatId}/messages/stream` (the same, guardrail-validated reply chunked over
SSE — see "SSE streaming" below). Stage [8] Guardrail is now implemented (`ResponseValidator`), as is a
semantic cache (`SemanticCacheService`, checked right after stage [1], not pictured as a numbered stage
below since it short-circuits the rest of the pipeline on a hit).

## How it works (quick reference)

For a new turn, `ChatPipelineService` runs through these stages, each handled by its own service:

1. **Normalize** (`QueryNormalizationService`) — rewrites the raw message into a clean query via a
   Bedrock LLM call.
2. **Semantic-cache check** (`SemanticCacheService.lookup`) — embeds the normalized query and matches it
   (`topK=1`, similarity ≥ `demo-chat.cache.similarity-threshold`) against the Qdrant `semantic_cache`
   collection. A hit returns the cached answer immediately and skips everything below.
3. **Retrieve** (`KnowledgeRetrievalService`) — on a cache miss, `VectorStore.similaritySearch` pulls the
   top-K candidate intents from the Qdrant `support_kb` collection.
4. **Classify** (`IntentClassificationService`) — an LLM call picks a single `intent_id` from those
   candidates (or `out_of_scope`), with a confidence score.
5. **Scope-check** (`ScopeFilter`) — rejects the classification if confidence is below
   `demo-chat.rag.similarity-threshold` or the intent isn't one of the retrieved candidates → routes to
   an out-of-scope reply if so.
6. **Slot-fill** (`SlotFillingService`) — checks whether the intent's `required_slots` are already
   collected in `DialogueState`; if not, asks a clarifying question instead of answering yet.
7. **Generate** (`PromptBuilder` + `AnswerGenerationService`) — assembles a SYSTEM/CONTEXT/USER
   STATE/USER MESSAGE prompt from the intent's `knowledge_snippet` and slot values, then calls the LLM
   for the final answer.
8. **Guardrail** (`ResponseValidator`) — a second LLM call checks the answer is actually grounded in
   that `knowledge_snippet`. A failed check swaps the reply for the intent's `escalation_fallback` text
   and sets status `ESCALATED` instead of `ANSWERED` — and it's never cached.

A guardrail-passed answer is written back into `semantic_cache` (step 2) for future near-duplicate
queries. Every turn's status/intent/slots persist to `DialogueState` (Cassandra), and both the user's
message and the bot's reply get appended to `ChatHistory`.

Two things worth knowing about *how* this executes, not just what it does:
- Bedrock and Qdrant calls are blocking under the hood, so every call site wraps them in
  `Mono.fromCallable(...).subscribeOn(Schedulers.boundedElastic())` rather than being truly
  non-blocking — a deliberate scope choice (see [backend.md](backend.md)), not an oversight.
- The SSE endpoint (`/messages/stream`) runs this exact same pipeline; it does not stream tokens live
  from the LLM. It waits for the full guardrail-validated answer, then chunks that already-safe text out
  over SSE — specifically so streaming can't bypass the guardrail (see "SSE streaming" below).

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
   ├── not grounded ──▶ intent's escalation_fallback text, status ESCALATED (not cached)
   │
   ▼
response → to the user — either one JSON response (`/messages`) or an SSE stream of the same,
           already-validated text chunked into `token` events + one `done` event (`/messages/stream`,
           buffer-then-chunk; see "SSE streaming" below)
```

A semantic-cache check (`SemanticCacheService.lookup`) sits right after stage [1]: if a near-duplicate of
the normalized query was previously answered and passed the guardrail, its cached answer is returned
immediately, skipping stages [2]–[8] entirely. A cache write (`SemanticCacheService.store`) only happens
after a stage-[8] guardrail pass, so a rejected answer is never cached.

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

**[8] Guardrail:** `ResponseValidator` asks the same `ChatClient` for a structured `GroundednessCheck`
(`grounded`, `reasoning`) verifying the answer doesn't contain facts unsupported by the intent's
`knowledgeSnippet`. If ungrounded, the reply becomes the intent's `escalationFallback` text and the
dialogue status becomes `ESCALATED` instead of `ANSWERED` — and the answer is never written to the
semantic cache.

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
| [8] Guardrail | `ResponseValidator` + `GroundednessCheck` (LLM structured output: `grounded`, `reasoning`) |

Supporting components, not pipeline stages themselves:

- `IntentDefinition` / `IntentDefinitionRegistry` — loads `knowledge-base/intents/*.json` at startup into
  an in-memory map; single source of truth for both indexing and post-classification lookup.
- `KnowledgeBaseIndexer` — `ApplicationRunner` that pushes every `IntentDefinition` into the Qdrant
  `support_kb` collection on startup (see [vector-store-schema.md](vector-store-schema.md)).
- `DialogueState` / `DialogueStatus` / `DialogueStateRepository` — the pipeline's per-chat working state
  (status, current intent, slots); see [dialogue-state.md](dialogue-state.md) for the schema and how it
  differs from the original draft. `DialogueStatus` gained an `ESCALATED` value in Phase 2 for the
  guardrail-rejected case (distinct from `OUT_OF_SCOPE`, which is pre-generation).
- `SemanticCacheService` — checked right after stage [1] (`ChatPipelineService.startNewTurn`); backed by
  a second Qdrant collection (`semantic_cache`, see [vector-store-schema.md](vector-store-schema.md)),
  not Redis or Cassandra, so lookups match on semantic similarity rather than an exact string.
- `TextChunker` — splits the already stage-[8]-validated answer into word chunks for the SSE streaming
  endpoint; a pure function, not itself a pipeline stage.

## SSE streaming

`ChatPipelineService.handleMessageStream` (backing `POST /api/chats/{chatId}/messages/stream`) runs
stages [1]–[8] exactly as above — unchanged — and only differs in how the final reply is delivered: once
the (guardrail-validated, or cache-hit) answer text is fully known, `TextChunker` splits it into words and
`ChatController` streams them as SSE `token` events (with a small configurable delay,
`demo-chat.streaming.chunk-delay-millis`) followed by one `done` event carrying the dialogue status. This
is a **buffer-then-chunk** simulation of streaming, not live token-by-token generation from the LLM
(Spring AI's `ChatClient.stream()` is unused here) — chosen specifically so the stage-[8] guardrail keeps
its full pre-send guarantee: with true live streaming, the client would see text before a groundedness
check could run on it. Clarifying-question and out-of-scope replies go through the same SSE endpoint too,
just as a very short token stream.

Full package structure is in [backend.md](backend.md).

## Known simplifications

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