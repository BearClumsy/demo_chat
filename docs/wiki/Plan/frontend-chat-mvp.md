# Frontend Chat MVP: auth, start-chat, SSE streaming

[← Back to README](README.md) · [React frontend structure](frontend.md)

**Status:** implemented, not yet verified against a live backend (see "Verification" below).

## Context

`modules/client/` had been an empty Gradle placeholder since the project's scaffolding phase (see
[roadmap.md](roadmap.md), Phase 1/2 "Simple React chat" items). This page documents the plan that
was actually implemented to turn it into a working app: a minimal but real flow — sign up or log
in, start a chat, send messages and watch the reply stream in — built directly against the current
backend contract (`ChatController`, `UserController`) rather than the aspirational shape sketched
in the original [frontend.md](frontend.md) tree.

The scope was deliberately narrower than the original proposal: no router, no `ClarifyingQuestionPrompt`/
`EscalationBanner`/`TypingIndicator` components, no `shared/` layer. Those all assumed backend
capabilities (typed SSE event kinds, multi-page session restore) that don't exist yet.

## Backend constraints that shaped the design

Read directly from `ChatController.java`, `ChatService.java`, `SecurityConfig.java`, and the
`user/` package rather than assumed from `frontend.md`'s target contract:

- **HTTP Basic auth, no login endpoint.** `SecurityConfig` uses `httpBasic()` with no session/token
  issuance — credentials are checked per-request against `login`/`password`
  (`SecurityUserDetailsService.findByUsername` looks up by `login`, not email). "Logging in"
  client-side is just holding credentials in memory and attaching `Authorization: Basic ...` to
  every request; there's nothing to call that verifies them up front.
- **No "who am I" endpoint.** `GET /api/users/{id}` needs an id you already have, and there's no
  lookup-by-login endpoint. A freshly-signed-up user gets their id from the `POST /api/users`
  response. An existing user logging back in has no way to discover their own id — so the login
  form asks for it directly, labeled as a known stand-in for a future `/api/users/me` endpoint.
- **Starting a chat needs a real participant UUID.** `POST /api/chats` requires a non-empty
  `participantIds` of real, existing user ids (`ChatService.startChat` validates against
  `UserRepository` and rejects your own id). There's no user search/list endpoint, so the "start
  chat" form asks for the other participant's UUID directly, same as the login id field.
- **No chat listing/restore endpoint.** Only start/add-participant/send/stream exist — no
  `GET /api/chats` or `GET /api/chats/{id}`. Chat state lives only in React state for the current
  browser session; a page reload loses it. This was already flagged as a gap in
  [frontend.md](frontend.md)'s original integration table.
- **SSE needs manual parsing.** `POST /api/chats/{chatId}/messages/stream` emits `event: token`
  chunks (raw reply text as `data`) and one final `event: done` (dialogue status name as `data`) —
  confirmed by reading `ChatPipelineService.streamOutcome`. Native `EventSource` can't send a POST
  body or a custom `Authorization` header, so the stream is read by hand via `fetch()` +
  `ReadableStream`, splitting on blank lines and parsing `event:`/`data:` fields.
- **No CORS config exists on the backend.** Rather than add CORS handling to `SecurityConfig` (a
  backend change, out of scope for a frontend task), `vite.config.ts` gets a dev-only proxy
  (`/api` → `http://localhost:8080`), keeping requests same-origin without touching the server.

## What was built

- `app/AuthContext.tsx` — in-memory `{userId, login, password}`, no persistence across reload
  (matches the backend's session-less design); derives the Basic auth header.
- `features/auth/AuthPage.tsx` — real signup (`POST /api/users`) and a login form with the
  manual-user-id field described above.
- `features/chat/components/StartChatForm.tsx` — `POST /api/chats` with title, participant UUID,
  and first message.
- `features/chat/api/chatApi.ts` — `startChat` plus the hand-rolled SSE frame parser for
  `streamMessage`.
- `features/chat/hooks/useChatStream.ts` — wraps `chatApi.streamMessage` into React state
  (accumulated messages, in-flight streaming reply, last dialogue status).
- `features/chat/components/ChatWindow.tsx` / `MessageBubble.tsx` — message list + composer,
  rendering the streaming reply incrementally and a status badge from the `done` event.
- `App.tsx` — no router; a simple three-state switch (`AuthPage` → `StartChatForm` → `ChatWindow`)
  wrapped in `AuthProvider`.

See the as-built tree in [frontend.md](frontend.md) for the full file layout.

## Verification

`tsc -b` and `vite build` both pass cleanly in `modules/client/`.

**Live end-to-end testing (signup → start chat → streamed reply) was not completed.** The backend
can't boot on this machine at all: `QdrantVectorStore`'s bean initialization calls Bedrock's
embedding API during Spring context startup to determine vector dimensions, and no AWS credentials
are configured anywhere locally (no `~/.aws/`, no `AWS_ACCESS_KEY_ID`). That blocks every endpoint,
not just the RAG-touching ones — this is a pre-existing environment gap, not something this feature
introduced. Local infra (colima + `docker-compose` Postgres/Cassandra/Qdrant/Kafka) was started and
confirmed healthy, then torn back down once the Bedrock credential gap was hit.

Whenever real Bedrock credentials are available: `docker-compose up -d` in
`modules/server/src/main/resources/local/`, `./gradlew :server:bootRun`, then
`cd modules/client && npm run dev` to run the flow for real.

## Related documents

- [React frontend structure (as-built tree, integration table)](frontend.md)
- [Java backend structure](backend.md)
- [RAG pipeline / SSE streaming design](rag-pipeline.md)
- [Implementation roadmap](roadmap.md)