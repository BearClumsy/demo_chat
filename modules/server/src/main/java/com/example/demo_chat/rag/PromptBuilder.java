package com.example.demo_chat.rag;

import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

/**
 * Stage 6 of the RAG pipeline: assembles the final prompt from strictly separated parts (SYSTEM /
 * CONTEXT / USER STATE / USER MESSAGE) so the model can't infer facts that aren't present in the
 * retrieved content.
 */
@Component
public class PromptBuilder {

  private static final String SYSTEM_TEMPLATE =
      """
      You are a support assistant. Answer ONLY based on the text in the CONTEXT block. Do not \
      invent facts. Do not go beyond the "%s" topic. If there isn't enough information, say that \
      clarification from a human agent is needed.

      %s""";

  private static final String USER_TEMPLATE =
      """
      [CONTEXT]
      %s

      [USER STATE]
      %s

      [USER MESSAGE]
      %s""";

  /**
   * @param intent the confirmed, in-scope intent
   * @param slots the slots collected for this dialogue
   * @param normalizedQuery the original normalized user query
   * @return the assembled system/user prompt
   */
  public AssembledPrompt build(
      IntentDefinition intent, Map<String, String> slots, String normalizedQuery) {
    String system = SYSTEM_TEMPLATE.formatted(intent.intentId(), intent.systemInstruction());
    String userState =
        slots.isEmpty()
            ? "(none)"
            : slots.entrySet().stream()
                .map(entry -> entry.getKey() + ": " + entry.getValue())
                .collect(Collectors.joining("\n"));
    String user = USER_TEMPLATE.formatted(intent.knowledgeSnippet(), userState, normalizedQuery);
    return new AssembledPrompt(system, user);
  }
}
