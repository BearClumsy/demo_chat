package com.example.demo_chat.chat;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

/**
 * Request body for starting a chat. {@code participantIds} is optional: omit it, send {@code null},
 * or send {@code []} to start a chat with only the AI assistant.
 */
public record StartChatRequest(
    @NotNull(message = "{chat.currentUserId.required}") UUID currentUserId,
    List<@NotNull UUID> participantIds,
    @NotBlank(message = "{chat.title.required}") String title,
    @NotNull(message = "{chat.message.required}") @Valid MessageRequest message) {

  public StartChatRequest {
    if (participantIds == null) {
      participantIds = List.of();
    }
  }
}
