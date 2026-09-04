import { useState } from "react";
import "./App.css";
import { AuthProvider, useAuth } from "./app/AuthContext";
import AuthPage from "./features/auth/AuthPage";
import StartChatForm from "./features/chat/components/StartChatForm";
import ChatWindow from "./features/chat/components/ChatWindow";
import type { StartChatResult } from "./features/chat/api/chatApi";
import type { ChatMessage } from "./features/chat/types/chat.types";

function AppShell() {
  const { credentials } = useAuth();
  const [chat, setChat] = useState<{
    chatId: string;
    initialMessages: ChatMessage[];
    initialStatus: string;
  } | null>(null);

  if (!credentials) {
    return <AuthPage />;
  }

  if (!chat) {
    return (
      <div className="app-centered">
        <StartChatForm
          onStarted={(result: StartChatResult, firstMessage: string) => {
            setChat({
              chatId: result.chatId,
              initialMessages: [
                {
                  id: crypto.randomUUID(),
                  senderId: credentials.userId,
                  content: firstMessage,
                  isSelf: true,
                },
                {
                  id: crypto.randomUUID(),
                  senderId: "assistant",
                  content: result.reply,
                  isSelf: false,
                },
              ],
              initialStatus: result.status,
            });
          }}
        />
      </div>
    );
  }

  return (
    <div className="app-centered">
      <ChatWindow
        chatId={chat.chatId}
        initialMessages={chat.initialMessages}
        initialStatus={chat.initialStatus}
      />
    </div>
  );
}

export default function App() {
  return (
    <AuthProvider>
      <AppShell />
    </AuthProvider>
  );
}
