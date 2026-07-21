import { useState, type FormEvent } from "react";
import { useAuth } from "../../../app/AuthContext";
import { useChatStream } from "../hooks/useChatStream";
import MessageBubble from "./MessageBubble";
import "./ChatWindow.css";

export default function ChatWindow({ chatId }: { chatId: string }) {
  const { authHeader, credentials } = useAuth();
  const { messages, streamingReply, status, isStreaming, error, send } = useChatStream(
    chatId,
    authHeader!,
    credentials!.userId,
  );
  const [draft, setDraft] = useState("");

  function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!draft.trim() || isStreaming) return;
    send(draft.trim());
    setDraft("");
  }

  return (
    <div className="chat-window">
      <header className="chat-window__header">
        <span>Chat {chatId}</span>
        {status && <span className="chat-window__status">{status}</span>}
      </header>

      <div className="chat-window__messages">
        {messages.map((message) => (
          <MessageBubble key={message.id} message={message} />
        ))}
        {streamingReply && (
          <MessageBubble
            message={{ id: "streaming", senderId: "assistant", content: streamingReply, isSelf: false }}
          />
        )}
      </div>

      {error && <p className="chat-window__error">{error}</p>}

      <form className="chat-window__composer" onSubmit={handleSubmit}>
        <input
          type="text"
          value={draft}
          onChange={(event) => setDraft(event.target.value)}
          placeholder="Type a message"
          disabled={isStreaming}
        />
        <button type="submit" disabled={isStreaming || !draft.trim()}>
          {isStreaming ? "Sending…" : "Send"}
        </button>
      </form>
    </div>
  );
}
