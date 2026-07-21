import type { ChatMessage } from "../types/chat.types";

export default function MessageBubble({ message }: { message: ChatMessage }) {
  return (
    <div className={`message-row ${message.isSelf ? "message-row--self" : ""}`}>
      <div className={`message-bubble ${message.isSelf ? "message-bubble--self" : ""}`}>
        {message.content}
      </div>
    </div>
  );
}
