import { useState } from "react";
import "./App.css";
import { AuthProvider, useAuth } from "./app/AuthContext";
import AuthPage from "./features/auth/AuthPage";
import StartChatForm from "./features/chat/components/StartChatForm";
import ChatWindow from "./features/chat/components/ChatWindow";

function AppShell() {
  const { credentials } = useAuth();
  const [chatId, setChatId] = useState<string | null>(null);

  if (!credentials) {
    return <AuthPage />;
  }

  if (!chatId) {
    return (
      <div className="app-centered">
        <StartChatForm onStarted={setChatId} />
      </div>
    );
  }

  return (
    <div className="app-centered">
      <ChatWindow chatId={chatId} />
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
