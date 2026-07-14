package com.example.demo_chat.chat;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

/** Request body for adding a participant to a chat. */
public record ParticipantRequest(
    @NotNull(message = "{chat.participant.userId.required}") UUID userId) {}
