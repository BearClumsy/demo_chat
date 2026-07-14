# Dialogue Session Model (Cassandra)

[← Back to README](README.md) · [Intent matching](intent-matching.md)

**Status:** planned, not yet implemented — no `DialogueState`/`DialogueStateService` code exists yet (see
[rag-pipeline.md](rag-pipeline.md)). This doc previously described a Redis-backed design; Redis has been
dropped from the project entirely (explicit decision, 2026-07-14) — there is no Redis dependency anywhere
in `build.gradle`, and chat/session state uses Cassandra (see [overview.md](overview.md) and
[local-vs-aws.md](local-vs-aws.md)). Below is the same session model redesigned around Cassandra, mirroring
the existing `ChatHistory` entity.

## Key structure (target)

```
table:  dialogue_state
key:    session_id (or user_id — same open question as ChatHistory's partition key, see backend.md)
value:  status, current_intent_id, slots (map<text,text>), history (frozen list<UDT>), created_at, updated_at
ttl:    per-row TTL set on write/update (Cassandra `USING TTL`), refreshed on each update to approximate
        a sliding window — Cassandra has no native sliding-expiry primitive like Redis's EXPIRE
```

## Example value

```json
{
  "sessionId": "b3f1c2a0-...",
  "userId": "anonymous-or-auth-id",
  "status": "SLOT_FILLING",
  "currentIntentId": "refund_status",
  "slots": {
    "order_id": null
  },
  "history": [
    { "role": "user", "text": "I want my money back" },
    { "role": "assistant", "text": "Could you provide your order number, please?" }
  ],
  "createdAt": "2026-07-14T10:03:00Z",
  "updatedAt": "2026-07-14T10:03:05Z"
}
```

## Statuses (`DialogueStatus`)

```
NEW → INTENT_MATCHED → SLOT_FILLING → READY_TO_ANSWER → ANSWERED
                    ↘ OUT_OF_SCOPE → ESCALATED
```

## Semantic cache and rate limiting — not yet decided

The original draft of this doc put a query-result cache (`semcache:{queryHash}`) and a rate-limit counter
(`ratelimit:{userId}`) in Redis alongside dialogue state. Since Redis isn't part of this project, neither
of those has a home yet — options include Cassandra rows with a TTL, a local/in-process cache, or
something else. **Don't assume either one without revisiting this doc.** Resilience4j (mentioned in
earlier drafts for rate limiting) is also not currently a dependency in `build.gradle`.

## Why Cassandra and not in-memory

- The application is stateless → any pod can handle any request for any session during horizontal
  scaling — same reasoning already applied to `ChatHistory` (see [backend.md](backend.md)).
- Reuses the datastore already used for chat history (`ReactiveCassandraRepository`) instead of adding a
  second store just for session state.
- Point lookups by a single partition key (`session_id`) fit Cassandra's strengths; the semantic-cache and
  rate-limit use cases above are a weaker fit, which is exactly why they're left undecided rather than
  forced onto Cassandra by default.

## Related documents

- [RAG pipeline](rag-pipeline.md)
- [Java backend structure](backend.md)