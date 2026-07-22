package com.example.demo_chat.rag;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * Stage 3 of the RAG pipeline: given the top-K candidate intents from retrieval, asks the LLM to
 * pick exactly one {@code intent_id} (or {@link IntentClassification#OUT_OF_SCOPE}). The model is
 * only offered the retrieved candidates, so it can't invent an id outside the whitelist.
 */
@Service
public class IntentClassificationService {

  private static final String SYSTEM_PROMPT =
      """
      You are an intent classifier for a customer support bot. Given the user's message and a \
      list of candidate topics, pick the single best matching topic id from the candidates. If \
      none of the candidates genuinely match, respond with intent_id "%s" instead. Include your \
      confidence in the match as a number between 0 and 1."""
          .formatted(IntentClassification.OUT_OF_SCOPE);

  private final ChatClient chatClient;

  public IntentClassificationService(ChatClient chatClient) {
    this.chatClient = chatClient;
  }

  /**
   * @param normalizedQuery the normalized user query
   * @param candidates the top-K intents retrieved for this query
   * @return the classified intent id and the model's confidence in it
   */
  public Mono<IntentClassification> classify(
      String normalizedQuery, List<IntentDefinition> candidates) {
    var candidateList =
        candidates.stream()
            .map(
                candidate ->
                    "- %s: %s"
                        .formatted(
                            candidate.intentId(),
                            String.join(" | ", candidate.canonicalQuestions())))
            .collect(Collectors.joining("\n"));
    var userPrompt =
        """
        User message: %s

        Candidate topics:
        %s"""
            .formatted(normalizedQuery, candidateList);
    return Mono.fromCallable(
            () ->
                chatClient
                    .prompt()
                    .system(SYSTEM_PROMPT)
                    .user(userPrompt)
                    .call()
                    .entity(IntentClassification.class))
        .subscribeOn(Schedulers.boundedElastic());
  }
}
