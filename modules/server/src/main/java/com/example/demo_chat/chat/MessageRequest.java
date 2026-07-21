package com.example.demo_chat.chat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.UUID;

/** Request body for a chat's initial message. */
public record MessageRequest(
    @NotNull(message = "{chat.message.userId.required}") UUID userId,
    @NotBlank(message = "{chat.message.content.required}") String message,
    @NotNull(message = "{chat.message.datetime.required}") Instant datetime) {}
