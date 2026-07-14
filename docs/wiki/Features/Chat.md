# Chat

**Status:** in progress

## Overview

Lets a caller start a new chat and persists chat history in Cassandra, keyed by user. Currently a minimal
skeleton: creating a chat is wired end-to-end, but there's no send-message endpoint yet and no real
user/participant input.

## Requirements

- Expose an endpoint to start a new chat (`POST /api/chats`).
- Persist chat history keyed by user, including participant ids and messages.
- Validate incoming request parameters with centralized error handling (app-wide, not chat-specific).

## Infrastructure Used

- Cassandra: [[chat_history]]

## Decisions

- `ChatHistory`'s partition key is `user_id` (single-column primary key) — queries are expected to be by
  user, not by chat id.
- Chat messages are modeled as a Cassandra `@UserDefinedType` (`ChatMessage`: senderId, content, sentAt)
  stored as a frozen list on `ChatHistory`, rather than a separate wide-row table. Matches the current
  spec of "one entity with user id, participant ids, and messages," at the cost of the usual Cassandra
  list/tombstone anti-pattern under sustained growth.
- `ChatService.startChat()` generates a new random UUID as the record's `userId` and persists an empty
  `ChatHistory` row (no participants/messages yet) — `createNewChat` has no request body yet, so there's
  no way to pass a real caller's user id or initial participants.
- Validation error handling (`ValidationExceptionHandler`, `@RestControllerAdvice`) was added app-wide,
  not chat-specific, but is what any future chat request DTO (e.g. a "send message" body) will rely on.

## Open Questions

- Should `createNewChat` accept a request body (real user id, participant ids) instead of generating a
  random id?
- Does `ChatHistory` need to represent multiple chats per user (partition key currently allows only one
  row per user), or is one rolling history per user the intended model?
- If message volume can grow large/unbounded, should `messages` move to a separate clustered table
  instead of a frozen list?
- No message-sending endpoint exists yet — is that the next piece of this feature?

## Related Code

- `com.example.demo_chat.chat.ChatController` — `POST /api/chats` → `createNewChat`
- `com.example.demo_chat.chat.ChatService` — `startChat()`
- `com.example.demo_chat.chat.ChatHistory` — Cassandra entity
- `com.example.demo_chat.chat.ChatMessage` — Cassandra UDT
- `com.example.demo_chat.chat.ChatHistoryRepository` — `ReactiveCassandraRepository<ChatHistory, UUID>`
- `com.example.demo_chat.common.ValidationExceptionHandler` — shared validation error handling

## Source Log

- **2026-07-13** — conversation with user → created `ChatController`/`ChatService`/`ChatHistory`/
  `ChatMessage`/`ChatHistoryRepository`, wired `createNewChat` to persist a new `ChatHistory` row, added
  app-wide validation error handling.
