export type ChatMessage = {
  id: string;
  senderId: string;
  content: string;
  isSelf: boolean;
};

export type DialogueStatus = string;

export type StartChatFormValues = {
  title: string;
  participantId: string;
  message: string;
};
