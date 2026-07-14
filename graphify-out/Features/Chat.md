# Chat

**Status:** in progress

## Overview

Lets a caller start a new chat and persists chat history in Cassandra, keyed by user. Currently a minimal
skeleton: creating a chat is wired end-to-end, but there's no send-message endpoint yet and no real
user/participant input.

Planned next piece: an AI agent response path — the caller sends a message, the app retrieves relevant
context from the vector store, calls the LLM with conversation history, and streams the reply back as it
is generated, persisting the exchange to chat history.

## Requirements

- Expose an endpoint to start a new chat (`POST /api/chats`).
- Persist chat history keyed by user, including participant ids and messages.
- Validate incoming request parameters with centralized error handling (app-wide, not chat-specific).
- Expose a send-message endpoint that streams the AI agent's reply back to the caller as it is generated,
  rather than waiting for the full completion.
- Ground replies in project knowledge: retrieve relevant chunks from the vector store before calling the
  LLM (RAG), rather than relying on the model's parametric knowledge alone.
- Carry prior turns of the same chat into each new LLM call so replies are contextually coherent.

## Infrastructure Used

- Cassandra: [[chat_history]]
- Qdrant: collection not yet created — planned as the retrieval source for RAG (see Decisions)

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
- Planned architecture for AI agent responses: Spring AI's `ChatClient`, called reactively and returning
  `Flux<String>` (or SSE) from the send-message endpoint, so tokens stream to the caller as the model
  generates them, instead of a synchronous request/response.
- Retrieval and memory are planned to be handled by Spring AI advisors rather than hand-rolled retrieval
  calls: a Qdrant `VectorStore` advisor injects relevant document chunks into the prompt, and a Cassandra-
  backed `ChatMemory` advisor supplies prior turns of the conversation — both attached to the same
  `ChatClient`, so the controller/service layer doesn't manually stitch context together.
- LLM calls go through Amazon Bedrock (`spring-ai-starter-model-bedrock`), matching the model access
  layer already declared in `build.gradle`.

## Open Questions

- Should `createNewChat` accept a request body (real user id, participant ids) instead of generating a
  random id?
- Does `ChatHistory` need to represent multiple chats per user (partition key currently allows only one
  row per user), or is one rolling history per user the intended model?
- If message volume can grow large/unbounded, should `messages` move to a separate clustered table
  instead of a frozen list?
- Does the Cassandra `ChatMemory` advisor's storage model line up with the existing hand-rolled
  `ChatHistory` table, or will chat memory end up duplicated across two different Cassandra schemas?
- What source documents populate the Qdrant collection, and how are they chunked/re-ingested when they
  change (`spring-ai-markdown-document-reader` implies Markdown, but the actual doc set isn't decided)?
- SSE vs. a raw streaming body for the send-message endpoint — does the frontend need SSE semantics
  (event types, reconnect), or is a plain streamed response enough?

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
- **2026-07-14** — conversation with user → captured the planned architecture for AI agent responses:
  streaming `ChatClient` over WebFlux, Qdrant vector-store advisor for RAG, Cassandra `ChatMemory` advisor
  for conversation history, Bedrock as the model backend. No code written yet — plan only.
