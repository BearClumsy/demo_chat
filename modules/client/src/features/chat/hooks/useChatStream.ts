import { useCallback, useState } from "react";
import { streamMessage } from "../api/chatApi";
import type { ChatMessage, DialogueStatus } from "../types/chat.types";

export function useChatStream(chatId: string, authHeader: string, currentUserId: string) {
  const [messages, setMessages] = useState<ChatMessage[]>([]);
  const [streamingReply, setStreamingReply] = useState("");
  const [status, setStatus] = useState<DialogueStatus | null>(null);
  const [isStreaming, setIsStreaming] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const send = useCallback(
    async (text: string) => {
      setError(null);
      setMessages((prev) => [
        ...prev,
        { id: crypto.randomUUID(), senderId: currentUserId, content: text, isSelf: true },
      ]);
      setStreamingReply("");
      setIsStreaming(true);

      try {
        await streamMessage({
          authHeader,
          chatId,
          message: text,
          onToken: (token) => setStreamingReply((prev) => prev + token),
          onDone: (finalStatus) => setStatus(finalStatus),
        });
      } catch (err) {
        setError(err instanceof Error ? err.message : "Failed to send message");
      } finally {
        setIsStreaming(false);
        setStreamingReply((prev) => {
          if (prev) {
            setMessages((msgs) => [
              ...msgs,
              { id: crypto.randomUUID(), senderId: "assistant", content: prev, isSelf: false },
            ]);
          }
          return "";
        });
      }
    },
    [authHeader, chatId, currentUserId],
  );

  return { messages, streamingReply, status, isStreaming, error, send };
}
