export type StartChatParams = {
  authHeader: string;
  currentUserId: string;
  participantId: string;
  title: string;
  message: string;
};

export async function startChat(params: StartChatParams): Promise<string> {
  const response = await fetch("/api/chats", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      Authorization: params.authHeader,
    },
    body: JSON.stringify({
      currentUserId: params.currentUserId,
      participantIds: [params.participantId],
      title: params.title,
      message: {
        userId: params.currentUserId,
        message: params.message,
        datetime: new Date().toISOString(),
      },
    }),
  });

  if (!response.ok) {
    throw new Error(`Failed to start chat (${response.status})`);
  }

  return response.json();
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
      dataLines.push(line.slice("data:".length).trim());
    }
  }

  if (dataLines.length === 0) return null;
  return { event, data: dataLines.join("\n") };
}
