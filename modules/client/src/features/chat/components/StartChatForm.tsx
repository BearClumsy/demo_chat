import { useState, type FormEvent } from "react";
import { useAuth } from "../../../app/AuthContext";
import { startChat, type StartChatResult } from "../api/chatApi";
import "./StartChatForm.css";

export default function StartChatForm({
  onStarted,
}: {
  onStarted: (result: StartChatResult, firstMessage: string) => void;
}) {
  const { authHeader, credentials } = useAuth();
  const [title, setTitle] = useState("");
  const [participantId, setParticipantId] = useState("");
  const [message, setMessage] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setError(null);
    setIsSubmitting(true);
    try {
      const result = await startChat({
        authHeader: authHeader!,
        currentUserId: credentials!.userId,
        participantId,
        title,
        message,
      });
      onStarted(result, message);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to start chat");
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <form className="start-chat-card" onSubmit={handleSubmit}>
      <h1>Start a chat</h1>

      <label className="start-chat-field">
        <span>Title</span>
        <input value={title} onChange={(event) => setTitle(event.target.value)} required />
      </label>

      <label className="start-chat-field">
        <span>Participant user ID (optional)</span>
        <input
          value={participantId}
          onChange={(event) => setParticipantId(event.target.value)}
          placeholder="Leave blank to chat only with the assistant"
        />
      </label>

      <label className="start-chat-field">
        <span>First message</span>
        <textarea
          value={message}
          onChange={(event) => setMessage(event.target.value)}
          required
          rows={3}
        />
      </label>

      <button type="submit" disabled={isSubmitting}>
        {isSubmitting ? "Starting…" : "Start chat"}
      </button>

      {error && <p className="start-chat-error">{error}</p>}
    </form>
  );
}
