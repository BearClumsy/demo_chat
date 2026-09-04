import { useCallback, useRef, useState } from "react";
import { streamMessage } from "../api/chatApi";
import type { ChatMessage, DialogueStatus } from "../types/chat.types";

export function useChatStream(
  chatId: string,
  authHeader: string,
  currentUserId: string,
  initialMessages: ChatMessage[] = [],
  initialStatus: DialogueStatus | null = null,
) {
  const [messages, setMessages] = useState<ChatMessage[]>(initialMessages);
  const [streamingReply, setStreamingReply] = useState("");
  const [status, setStatus] = useState<DialogueStatus | null>(initialStatus);
  const [isStreaming, setIsStreaming] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const replyRef = useRef("");

  const send = useCallback(
    async (text: string) => {
      setError(null);
      setMessages((prev) => [
        ...prev,
        { id: crypto.randomUUID(), senderId: currentUserId, content: text, isSelf: true },
      ]);
      replyRef.current = "";
      setStreamingReply("");
      setIsStreaming(true);

      try {
        await streamMessage({
          authHeader,
          chatId,
          message: text,
          onToken: (token) => {
            replyRef.current += token;
            setStreamingReply(replyRef.current);
          },
          onDone: (finalStatus) => setStatus(finalStatus),
        });
      } catch (err) {
        setError(err instanceof Error ? err.message : "Failed to send message");
      } finally {
        setIsStreaming(false);
        const finalReply = replyRef.current;
        replyRef.current = "";
        setStreamingReply("");
        if (finalReply) {
          setMessages((msgs) => [
            ...msgs,
            { id: crypto.randomUUID(), senderId: "assistant", content: finalReply, isSelf: false },
          ]);
        }
      }
    },
    [authHeader, chatId, currentUserId],
  );

  return { messages, streamingReply, status, isStreaming, error, send };
}
