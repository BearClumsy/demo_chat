export type StartChatParams = {
  authHeader: string;
  currentUserId: string;
  participantId: string;
  title: string;
  message: string;
};

export type StartChatResult = {
  chatId: string;
  reply: string;
  status: string;
};

export async function startChat(params: StartChatParams): Promise<StartChatResult> {
  const participantId = params.participantId.trim();
  const response = await fetch("/api/chats", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      Authorization: params.authHeader,
    },
    body: JSON.stringify({
      currentUserId: params.currentUserId,
      participantIds: participantId ? [participantId] : [],
      title: params.title,
      message: {
        userId: params.currentUserId,
        message: params.message,
        datetime: new Date().toISOString(),
      },
    }),
  });

  if (!response.ok) {
    throw new Error(await startChatError(response));
  }

  return response.json();
}

/**
 * Turns a failed POST /api/chats response into a readable message. The backend emits an
 * RFC 7807 ProblemDetail ({ detail, errors }) for bean-validation failures; the plain
 * IllegalArgumentException path has no body, so fall back to the bare status code.
 */
async function startChatError(response: Response): Promise<string> {
  try {
    const problem = await response.json();
    const parts: string[] = [];
    if (problem?.detail) {
      parts.push(String(problem.detail));
    }
    if (problem?.errors && typeof problem.errors === "object") {
      parts.push(
        Object.entries(problem.errors)
          .map(([field, msg]) => `${field}: ${msg}`)
          .join("; "),
      );
    }
    if (parts.length > 0) {
      return `Failed to start chat (${response.status}): ${parts.join(" — ")}`;
    }
  } catch {
    // body wasn't JSON — fall through to the bare status message
  }
  return `Failed to start chat (${response.status})`;
}

export type StreamMessageParams = {
  authHeader: string;
  chatId: string;
  message: string;
  onToken: (token: string) => void;
  onDone: (status: string) => void;
};

/**
 * Native EventSource can't send POST bodies or an Authorization header, so the SSE
 * stream from POST /api/chats/{chatId}/messages/stream is parsed by hand here.
 */
export async function streamMessage(params: StreamMessageParams): Promise<void> {
  const response = await fetch(`/api/chats/${params.chatId}/messages/stream`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      Accept: "text/event-stream",
      Authorization: params.authHeader,
    },
    body: JSON.stringify({ message: params.message }),
  });

  if (!response.ok || !response.body) {
    throw new Error(`Failed to stream message (${response.status})`);
  }

  const reader = response.body.getReader();
  const decoder = new TextDecoder();
  let buffer = "";

  while (true) {
    const { done, value } = await reader.read();
    if (done) break;

    buffer += decoder.decode(value, { stream: true });
    const frames = buffer.split("\n\n");
    buffer = frames.pop() ?? "";

    for (const frame of frames) {
      const event = parseSseFrame(frame);
      if (!event) continue;
      if (event.event === "token") {
        params.onToken(event.data);
      } else if (event.event === "done") {
        params.onDone(event.data);
      }
    }
  }
}

function parseSseFrame(frame: string): { event: string; data: string } | null {
  let event = "message";
  const dataLines: string[] = [];

  for (const line of frame.split("\n")) {
    if (line.startsWith("event:")) {
      event = line.slice("event:".length).trim();
    } else if (line.startsWith("data:")) {
      // Trailing spaces are meaningful here: TextChunker.chunk() appends a trailing space to
      // every word-chunk but the last so tokens can be concatenated back into the original text.
      // Only strip the single conventional leading space after the colon, per the SSE spec.
      const raw = line.slice("data:".length);
      dataLines.push(raw.startsWith(" ") ? raw.slice(1) : raw);
    }
  }

  if (dataLines.length === 0) return null;
  return { event, data: dataLines.join("\n") };
}
