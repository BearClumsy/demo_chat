package com.example.demo_chat.chat;

import jakarta.validation.constraints.NotBlank;

/** Request body for sending a message into an existing chat's RAG pipeline. */
public record SendMessageRequest(
    @NotBlank(message = "{chat.message.content.required}") String message) {}
