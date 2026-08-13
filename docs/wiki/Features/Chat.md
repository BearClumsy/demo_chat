# Chat

**Status:** in progress — start/participants/messages implemented end-to-end; no read-back endpoints yet

## Overview

Lets an authenticated user start a chat with validated participants, add further participants, and send
messages that are answered by the RAG pipeline (see [[rag-pipeline]]). Chat history — participants,
title, and the full message list — is persisted in Cassandra. Replies come back either as a single JSON
response or as an SSE stream. Every chat-scoped endpoint is gated on the caller being a participant.

## Requirements

- Start a new chat (`POST /api/chats`) with a title, participant ids, and an initial message.
- Add a participant to an existing chat (`POST /api/chats/{chatId}/participants`).
- Send a message into a chat and get the assistant's reply, either whole
  (`POST /api/chats/{chatId}/messages`) or streamed (`.../messages/stream`).
- Persist chat history keyed by chat, including participant ids, title, and messages.
- Restrict every chat-scoped operation to that chat's participants.
- Validate incoming request parameters with centralized error handling (app-wide, not chat-specific).

## Infrastructure Used

- Cassandra: [[chat_history]]
- Cassandra: [[dialogue_state]] — written by the pipeline on every turn, not by `ChatService` directly

## Decisions

- `ChatHistory`'s partition key is **misnamed**: `@PrimaryKey("user_id")` actually holds the *chat* id,
  so there is one row per chat, not per user. Renaming it needs hand-written CQL — Cassandra has no
  migration tool here (`spring.cassandra.schema-action` is `none` in staging/prod).
- Chat messages are modeled as a Cassandra `@UserDefinedType` (`ChatMessage`: senderId, content, sentAt)
  stored as a frozen list on `ChatHistory`, rather than a separate wide-row table — at the cost of the
  usual Cassandra list/tombstone anti-pattern under sustained growth.
- `ChatService.startChat()` generates a random UUID for the chat and stores the caller plus the requested
  participants. It rejects a `participantIds` list containing the caller ("Cannot start a chat with
  yourself") and verifies every id resolves to a real user before writing.
- **Authorization lives in the service, not in config.** `SecurityConfig` only requires authentication;
  there is no method security. Instead `ChatController.startChat` rejects a `currentUserId` that doesn't
  match the authenticated principal (403), and every `/{chatId}/**` operation routes through
  `ChatService.getChatForParticipant()`, which yields 403 for a non-participant and 404 for an unknown
  chat id.
- `addParticipant()` takes the **caller's** id as its own parameter rather than trusting the request
  body, and runs the participant check before validating the id being added — so an outsider can neither
  add themselves to a chat nor probe which user ids exist.
- The message endpoints delegate the turn to `ChatPipelineService`, which persists the dialogue state and
  then appends *two* `ChatMessage` entries per turn: the user's message and the assistant's reply, the
  latter under a sentinel sender id of all zeros (`ASSISTANT_SENDER_ID`).
- The SSE variant buffers the complete reply and then chunks it for transport — it is **not** live token
  generation. It emits `token` events followed by one `done` event carrying the `DialogueStatus` name.
- Validation error handling (`ValidationExceptionHandler`, `@RestControllerAdvice`) was added app-wide,
  not chat-specific, and is what the chat request DTOs rely on.

## Open Questions

- There is no read endpoint — no "list my chats" and no "fetch history for chat X". The client keeps the
  conversation only in memory, so a page refresh loses it. This is the most visible gap.
- No remove-participant endpoint, and no notion of a chat owner/admin — any participant can add anyone.
- Should `ChatHistory`'s `user_id` column be renamed to `chat_id`? It needs manual DDL plus a data
  migration, so it has been left alone so far.
- If message volume can grow large/unbounded, should `messages` move to a separate clustered table
  instead of a frozen list?

## Related Code

- `com.example.demo_chat.chat.ChatController` — `POST /api/chats`, `/{chatId}/participants`,
  `/{chatId}/messages`, `/{chatId}/messages/stream`
- `com.example.demo_chat.chat.ChatService` — `startChat()`, `addParticipant()`,
  `getChatForParticipant()`, `validateParticipantIds()`
- `com.example.demo_chat.chat.ChatHistory` — Cassandra entity
- `com.example.demo_chat.chat.ChatMessage` — Cassandra UDT
- `com.example.demo_chat.chat.ChatHistoryRepository` — `ReactiveCassandraRepository<ChatHistory, UUID>`
- `com.example.demo_chat.chat` DTOs — `StartChatRequest`, `MessageRequest`, `ParticipantRequest`,
  `SendMessageRequest`, `SendMessageResponse`
- `com.example.demo_chat.rag.ChatPipelineService` — owns the message turn and appends to `chat_history`
- `com.example.demo_chat.config.SecurityConfig` — authentication only; no method security
- `com.example.demo_chat.common.ValidationExceptionHandler` — shared validation error handling
- `modules/server/src/test/java/com/example/demo_chat/chat/` — `ChatServiceValidateParticipantIdsTest`,
  `ChatControllerStreamTest`

## Source Log

- **2026-07-13** — conversation with user → created `ChatController`/`ChatService`/`ChatHistory`/
  `ChatMessage`/`ChatHistoryRepository`, wired `createNewChat` to persist a new `ChatHistory` row, added
  app-wide validation error handling.
- **2026-08-13** — code review of the CLAUDE.md drift-correction commit → found `addParticipant` had no
  caller authorization (any authenticated user could add themselves to any chat they knew the id of) and
  fixed it by routing through `getChatForParticipant()`. Rewrote this note, which had gone stale at the
  2026-07-13 skeleton and still claimed there was no send-message endpoint and no way to pass a real
  caller id or participants.
