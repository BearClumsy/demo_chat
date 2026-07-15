# Cassandra Table: dialogue_state

**Partition key:** chat_id
**Clustering key:** none
**TTL:** not set

## Columns

- `chat_id` (uuid) — partition key; same id as `chat_history.user_id` (one chat, one dialogue state).
- `status` (text) — `DialogueStatus` enum: `NEW`, `SLOT_FILLING`, `READY_TO_ANSWER`, `ANSWERED`,
  `OUT_OF_SCOPE`, `ESCALATED`.
- `current_intent_id` (text) — the intent currently being pursued, or null.
- `slots` (map\<text,text\>) — required-slot values collected so far for the current intent.
- `last_normalized_query` (text) — the normalized user query, kept across slot-filling turns so the
  final generation prompt still reflects the user's original question, not the last raw slot value.
- `updated_at` (timestamp)

## Used By

- [[Chat]] — `ChatPipelineService` reads/writes this on every `POST /api/chats/{chatId}/messages` (and
  the SSE `.../messages/stream` variant) call: loads or creates the state, runs the pipeline, then
  persists the resulting status/intent/slots before appending to `chat_history`.

## Notes

- This table replaced an earlier Redis-based design (dropped entirely — no Redis dependency anywhere in
  `build.gradle`). See `docs/wiki/Plan/dialogue-state.md` for the full history and why Cassandra
  was chosen instead (stateless app, point lookups by a single partition key, reuses the same store as
  `chat_history`).
- `ESCALATED` was added in Phase 2: an intent was matched and an answer generated, but the output-side
  groundedness guardrail (`ResponseValidator`) rejected it, so the reply sent to the user is the
  intent's `escalation_fallback` text instead. Distinct from `OUT_OF_SCOPE`, which means no intent
  matched *before* generation ever ran.
- No TTL yet — rows persist indefinitely. A dialogue never really "ends," so there's no natural
  expiry point decided yet.
- Doesn't duplicate message history — `chat_history.messages` already has that; this table is purely the
  pipeline's *working state*.
