# Cassandra Table: chat_history

**Partition key:** user_id
**Clustering key:** none
**TTL:** not set

## Columns

- `user_id` (uuid) — partition key, identifies whose chat history this row is.
- `participant_ids` (list\<uuid\>) — other users participating in the chat.
- `messages` (frozen list\<chat_message\>) — embedded message history; the `chat_message` UDT has
  `sender_id` (uuid), `content` (text), `sent_at` (timestamp).

## Used By

- [[Chat]] — `ChatService.startChat()` creates a new row on chat start; no read/update path implemented
  yet.

## Notes

- Single row per user (partition key = `user_id` only) — this models "chat history per user," not one row
  per chat conversation. If a user can have multiple chats, this schema needs a chat id in the key.
- `messages` as a frozen list on a single partition is a known Cassandra anti-pattern for unbounded growth
  (read-modify-write on every append, tombstone buildup on removal). Fine for small/bounded history;
  reconsider as a separate table clustered by `user_id` + timestamp if history grows large.
