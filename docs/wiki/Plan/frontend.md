# React Frontend: Project Structure

[← Back to README](README.md) · [Java backend](backend.md)

**Status:** chat MVP implemented — the `:client` Gradle module at `modules/client/` is now a real
Vite + React + TypeScript app (no longer an empty placeholder). It covers signup/login, starting a
chat, and sending messages with live SSE token streaming, wired against the actual backend contract
(not the target shape originally sketched below). See
[Frontend Chat MVP: auth, start-chat, SSE streaming](frontend-chat-mvp.md) for the design decisions
and known gaps behind this implementation. Still not built: routing beyond the three-screen flow,
`Dockerfile`, and the `shared/` utilities layer sketched in the original tree below.

## Tree

Actual structure as implemented (differs from the original proposal below — no `public/`,
`Dockerfile`, `shared/`, or `app/store`/`app/routes` yet; `app/` currently holds only auth state):

```
modules/client/
├── package.json
├── vite.config.ts                          # includes a dev proxy: /api → http://localhost:8080
├── index.html
└── src/
    ├── main.tsx
    ├── App.tsx                             # AuthProvider + 3-state switch: AuthPage / StartChatForm / ChatWindow
    ├── App.css
    │
    ├── app/
    │   └── AuthContext.tsx                 # in-memory {userId, login, password} + Basic auth header
    │
    └── features/
        ├── auth/
        │   ├── AuthPage.tsx                # real signup (POST /api/users) + login (manual user id)
        │   └── AuthPage.css
        │
        └── chat/
            ├── components/
            │   ├── StartChatForm.tsx       # POST /api/chats
            │   ├── ChatWindow.tsx
            │   ├── ChatWindow.css
            │   ├── MessageBubble.tsx
            │   └── StartChatForm.css
            ├── hooks/
            │   └── useChatStream.ts        # wraps chatApi.streamMessage into React state
            ├── api/
            │   └── chatApi.ts              # startChat + hand-rolled SSE parsing over fetch()
            └── types/
                └── chat.types.ts
```

The original proposal below (`ClarifyingQuestionPrompt`, `EscalationBanner`, `TypingIndicator`,
`useDialogueSession`, `shared/`, `app/store`, `app/routes`) is kept as forward-looking scope, not yet
built — the backend has no typed `escalation`/`clarify` SSE event kind for those components to key
off of (see the integration table below), and there's no multi-page routing yet since the flow is
strictly linear (auth → start chat → chat window).

```
modules/client/                             # ORIGINAL PROPOSAL — see the as-built tree above
├── package.json
├── vite.config.ts
├── Dockerfile
├── public/
│   └── index.html
└── src/
    ├── main.tsx
    ├── App.tsx
    │
    ├── features/
    │   └── chat/
    │       ├── components/
    │       │   ├── ChatWindow.tsx
    │       │   ├── MessageBubble.tsx
    │       │   ├── ClarifyingQuestionPrompt.tsx   # UI for slot filling
    │       │   ├── EscalationBanner.tsx           # "handed off to an agent"
    │       │   └── TypingIndicator.tsx            # for the SSE stream
    │       ├── hooks/
    │       │   ├── useChatStream.ts                # SSE subscription
    │       │   └── useDialogueSession.ts
    │       ├── api/
    │       │   └── chatApi.ts                      # wrapper over fetch/EventSource
    │       └── types/
    │           └── chat.types.ts
    │
    ├── shared/
    │   ├── ui/                                     # reusable components
    │   ├── config/
    │   │   └── env.ts
    │   └── utils/
    │
    └── app/
        ├── store/                                  # client-side session state
        └── routes/
```

## Backend integration points

| Feature | Backend endpoint | Frontend status |
|---|---|---|
| Sign up | `POST /api/users` | wired (`AuthPage.tsx`) |
| Log in | *(none — HTTP Basic has no login endpoint)* | credentials held in memory, user pastes their own id (see [frontend-chat-mvp.md](frontend-chat-mvp.md)) |
| Start a chat | `POST /api/chats` | wired (`StartChatForm.tsx`) |
| Send a message / receive the answer stream | `POST /api/chats/{chatId}/messages/stream` (SSE) | wired (`useChatStream.ts`, `chatApi.ts`) |
| Restore session state on page reload | *(none — no `GET /api/chats/{id}` yet)* | not possible; refreshing loses the current chat |
| "Human agent needed" / "please clarify" indicator | *(none — no typed SSE event kind)* | not built; escalated/clarifying replies render as a plain token/done stream like any other |

Earlier drafts of this page described a *target* contract (a single session-scoped `/api/chat`
endpoint with typed `escalation`/`clarify` SSE events) that was never implemented server-side; the
table above reflects the real, already-implemented backend surface instead. See
[backend.md](backend.md) for the full current API surface and
[rag-pipeline.md](rag-pipeline.md) for the SSE streaming design (`token`/`done` events,
buffer-then-chunk).

## Related documents

- [Frontend Chat MVP: auth, start-chat, SSE streaming](frontend-chat-mvp.md) — the plan implemented for this feature
- [Java backend structure](backend.md)
- [Architecture overview](overview.md)