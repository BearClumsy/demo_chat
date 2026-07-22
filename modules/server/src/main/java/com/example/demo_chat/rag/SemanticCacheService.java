package com.example.demo_chat.rag;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * Caches previously-generated, guardrail-validated answers in a dedicated Qdrant collection, keyed
 * by semantic similarity of the normalized query rather than an exact string match.
 */
@Service
public class SemanticCacheService {

  private final VectorStore cacheVectorStore;
  private final double similarityThreshold;
  private final boolean enabled;

  public SemanticCacheService(
      @Qualifier("semanticCacheVectorStore") VectorStore cacheVectorStore,
      @Value("${demo-chat.cache.similarity-threshold:0.95}") double similarityThreshold,
      @Value("${demo-chat.cache.enabled:true}") boolean enabled) {
    this.cacheVectorStore = cacheVectorStore;
    this.similarityThreshold = similarityThreshold;
    this.enabled = enabled;
  }

  /**
   * @param normalizedQuery the normalized user query
   * @return the cached answer for a near-duplicate of {@code normalizedQuery}, or empty if none is
   *     cached above the similarity threshold (or the cache is disabled)
   */
  public Mono<String> lookup(String normalizedQuery) {
    if (!enabled) {
      return Mono.empty();
    }
    return Mono.fromCallable(
            () ->
                cacheVectorStore.similaritySearch(
                    SearchRequest.builder()
                        .query(normalizedQuery)
                        .topK(1)
                        .similarityThreshold(similarityThreshold)
                        .build()))
        .subscribeOn(Schedulers.boundedElastic())
        .flatMap(
            documents ->
                documents.isEmpty()
                    ? Mono.empty()
                    : Mono.justOrEmpty((String) documents.get(0).getMetadata().get("answer")));
  }

  /**
   * Stores a validated answer for future lookups.
   *
   * @param normalizedQuery the normalized user query (embedded/matched against future lookups)
   * @param intentId the intent the answer was generated for
   * @param answer the guardrail-validated answer text
   */
  public Mono<Void> store(String normalizedQuery, String intentId, String answer) {
    if (!enabled) {
      return Mono.empty();
    }
    var document =
        Document.builder()
            .id(UUID.randomUUID().toString())
            .text(normalizedQuery)
            .metadata("answer", answer)
            .metadata("intentId", intentId)
            .metadata("cachedAt", Instant.now().toString())
            .build();
    return Mono.fromRunnable(() -> cacheVectorStore.add(List.of(document)))
        .subscribeOn(Schedulers.boundedElastic())
        .then();
  }
}
