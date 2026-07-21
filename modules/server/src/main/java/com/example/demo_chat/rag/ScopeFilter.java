package com.example.demo_chat.rag;

import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/** Stage 4 of the RAG pipeline: decides whether a classified intent is in-scope. */
@Component
public class ScopeFilter {

  private final double similarityThreshold;

  public ScopeFilter(
      @Value("${demo-chat.rag.similarity-threshold:0.75}") double similarityThreshold) {
    this.similarityThreshold = similarityThreshold;
  }

  /**
   * @param classification the LLM's intent classification
   * @param candidates the candidate intents the classifier was allowed to choose from
   * @return the matched, allowed {@link IntentDefinition}, or empty if out of scope
   */
  public Optional<IntentDefinition> resolve(
      IntentClassification classification, List<IntentDefinition> candidates) {
    if (IntentClassification.OUT_OF_SCOPE.equals(classification.intentId())
        || classification.confidence() < similarityThreshold) {
      return Optional.empty();
    }
    return candidates.stream()
        .filter(candidate -> candidate.intentId().equals(classification.intentId()))
        .filter(IntentDefinition::allowed)
        .findFirst();
  }
}
