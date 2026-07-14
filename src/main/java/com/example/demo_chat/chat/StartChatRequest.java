package com.example.demo_chat.chat;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

/** Request body for starting a chat. */
public record StartChatRequest(
    @NotNull(message = "{chat.currentUserId.required}") UUID currentUserId,
    @NotEmpty(message = "{chat.participantIds.required}") List<@NotNull UUID> participantIds,
    @NotBlank(message = "{chat.title.required}") String title,
    @NotNull(message = "{chat.message.required}") @Valid MessageRequest message) {}
