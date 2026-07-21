package com.example.demo_chat.rag;

import java.util.List;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

/** A single allowed support topic, loaded from {@code knowledge-base/intents/*.json}. */
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record IntentDefinition(
    String intentId,
    List<String> canonicalQuestions,
    List<String> requiredSlots,
    String knowledgeSnippet,
    String systemInstruction,
    boolean allowed,
    String answerTemplate,
    String escalationFallback) {}
