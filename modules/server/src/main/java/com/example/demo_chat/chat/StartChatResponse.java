package com.example.demo_chat.chat;

import java.util.UUID;

/** Response body for starting a chat: its id, the assistant's reply, and the resulting status. */
public record StartChatResponse(UUID chatId, String reply, String status) {}
