package com.example.demo_chat.rag;

import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

/** Structured output of {@link IntentClassificationService}: the LLM's chosen intent id. */
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record IntentClassification(String intentId, double confidence) {

  /** Sentinel {@code intent_id} the classifier returns when no candidate genuinely matches. */
  public static final String OUT_OF_SCOPE = "out_of_scope";
}
