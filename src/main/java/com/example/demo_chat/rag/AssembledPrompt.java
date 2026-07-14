package com.example.demo_chat.rag;

/** The SYSTEM and USER halves of the four-part prompt assembled by {@link PromptBuilder}. */
public record AssembledPrompt(String system, String user) {}
