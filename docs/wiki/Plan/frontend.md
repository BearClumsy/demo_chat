# React Frontend: Project Structure

[← Back to README](README.md) · [Java backend](backend.md)

**Status:** planned, not started — no `frontend/` directory, `package.json`, or React code exists in
this repo yet. Everything below is the proposed structure, kept as-is until the frontend work begins.

## Tree

```
frontend/
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

## Backend integration points (target — current backend endpoints differ in shape)

| Feature | Backend endpoint |
|---|---|
| Send a message / receive the answer stream | `POST /api/chat` (SSE) |
| Restore session state on page reload | `GET /api/chat/session/{sessionId}` |
| "Human agent needed" indicator | `type: "escalation"` event in the SSE stream |
| "Please clarify" indicator | `type: "clarify"` event in the SSE stream |

The table above is still the *target* shape (a single `/api/chat` endpoint, session-scoped, typed SSE
event kinds) — none of that exact contract exists. But as of Phase 2, **SSE streaming itself is real**,
just under a different, already-implemented shape: `POST /api/chats/{chatId}/messages/stream` runs the
full RAG pipeline (guardrail included) and streams the already-validated reply as generic `token`
events + one `done` event (see [rag-pipeline.md](rag-pipeline.md), "SSE streaming") — there's no
`escalation`/`clarify` event *type* distinction; an escalated or clarifying reply is just a short
`token`/`done` stream like any other. The backend currently exposes: `POST /api/chats`,
`POST /api/chats/{chatId}/participants`, `POST /api/chats/{chatId}/messages` (JSON),
`POST /api/chats/{chatId}/messages/stream` (SSE), `GET /api/users/{id}`, and `POST /api/users`. See
[backend.md](backend.md) for the actual current API surface.

## Related documents

- [Java backend structure](backend.md)
- [Architecture overview](overview.md)