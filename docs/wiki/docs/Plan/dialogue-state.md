# Dialogue Session Model (Cassandra)

[← Back to README](README.md) · [Intent matching](intent-matching.md)

**Status:** implemented (Phase 1) as `com.example.demo_chat.rag.DialogueState` /
`DialogueStateRepository`. This doc previously described a Redis-backed design; Redis has been
dropped from the project entirely (explicit decision, 2026-07-14) — there is no Redis dependency anywhere
in `build.gradle`, and chat/session state uses Cassandra (see [overview.md](overview.md) and
[local-vs-aws.md](local-vs-aws.md)).

The implementation is deliberately smaller than the original draft below: `DialogueState` holds only the
pipeline's *working state* (status, current intent, slots) — not a duplicated `history` list, since
`ChatHistory.messages` (see [backend.md](backend.md)) already logs every turn for the chat, and
`ChatPipelineService` appends to both `ChatHistory` and `DialogueState` on every message. There's also no
TTL yet (see "not yet decided" below — still true).

## Key structure (implemented)

```
table:  dialogue_state
key:    chat_id — the same id as ChatHistory.userId (one chat, one dialogue state); this resolves the
        "session_id vs user_id" open question the original draft left open
value:  status, current_intent_id, slots (map<text,text>), last_normalized_query, updated_at
ttl:    none yet — every row persists indefinitely (see "not yet decided" below)
```

`last_normalized_query` isn't in the original draft: it's kept so that once slot filling finishes across
several turns, the final answer's `[USER MESSAGE]` prompt section (see
[prompt-engineering.md](prompt-engineering.md)) is still the user's original question, not whatever raw
text they sent to fill the last slot.

## Example value

```json
{
  "chatId": "b3f1c2a0-...",
  "status": "SLOT_FILLING",
  "currentIntentId": "refund_status",
  "slots": {},
  "lastNormalizedQuery": "Refund status for an order",
  "updatedAt": "2026-07-14T10:03:05Z"
}
```

## Statuses (`DialogueStatus`)

```
NEW ──▶ SLOT_FILLING ──▶ READY_TO_ANSWER ──▶ ANSWERED
  │                                              │
  └──────────────────▶ OUT_OF_SCOPE ◀────────────┘
```

Simplified from the original draft's `NEW → INTENT_MATCHED → SLOT_FILLING → READY_TO_ANSWER → ANSWERED`
with a separate `ESCALATED` state: the implemented enum is
`NEW, SLOT_FILLING, READY_TO_ANSWER, ANSWERED, OUT_OF_SCOPE` — `INTENT_MATCHED` was folded into the
classify-then-immediately-slot-check step (`ChatPipelineService.classifyAndRoute()`), and there's no
separate `ESCALATED` state yet (an out-of-scope reply is generated inline; a distinct escalation
workflow is future work).

## Semantic cache and rate limiting — not yet decided

The original draft of this doc put a query-result cache (`semcache:{queryHash}`) and a rate-limit counter
(`ratelimit:{userId}`) in Redis alongside dialogue state. Since Redis isn't part of this project, neither
of those has a home yet — options include Cassandra rows with a TTL, a local/in-process cache, or
something else. **Don't assume either one without revisiting this doc.** Resilience4j (mentioned in
earlier drafts for rate limiting) is also not currently a dependency in `build.gradle`. Neither is
implemented as of Phase 1.

## Why Cassandra and not in-memory

- The application is stateless → any pod can handle any request for any session during horizontal
  scaling — same reasoning already applied to `ChatHistory` (see [backend.md](backend.md)).
- Reuses the datastore already used for chat history (`ReactiveCassandraRepository`) instead of adding a
  second store just for session state.
- Point lookups by a single partition key (`chat_id`) fit Cassandra's strengths; the semantic-cache and
  rate-limit use cases above are a weaker fit, which is exactly why they're left undecided rather than
  forced onto Cassandra by default.

## Related documents

- [RAG pipeline](rag-pipeline.md)
- [Java backend structure](backend.md)