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

## Backend integration points (target — current backend endpoints differ)

| Feature | Backend endpoint |
|---|---|
| Send a message / receive the answer stream | `POST /api/chat` (SSE) |
| Restore session state on page reload | `GET /api/chat/session/{sessionId}` |
| "Human agent needed" indicator | `type: "escalation"` event in the SSE stream |
| "Please clarify" indicator | `type: "clarify"` event in the SSE stream |

None of these exist yet. The backend currently exposes `POST /api/chats`, `POST /api/chats/{chatId}/participants`,
`GET /api/users/{id}`, and `POST /api/users` — plain JSON request/response, no SSE. See
[backend.md](backend.md) for the actual current API surface.

## Related documents

- [Java backend structure](backend.md)
- [Architecture overview](overview.md)