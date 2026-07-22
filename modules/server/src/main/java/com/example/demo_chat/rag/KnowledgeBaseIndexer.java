package com.example.demo_chat.rag;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * Pushes every {@link IntentDefinition} into the Qdrant {@code support_kb} collection on startup.
 * Document ids are the intent id, so re-running this on every restart just upserts the same points
 * rather than duplicating them.
 */
@Component
public class KnowledgeBaseIndexer implements ApplicationRunner {

  private final IntentDefinitionRegistry registry;
  private final VectorStore vectorStore;
  private final boolean reindexOnStartup;

  public KnowledgeBaseIndexer(
      IntentDefinitionRegistry registry,
      VectorStore vectorStore,
      @Value("${demo-chat.rag.reindex-on-startup:true}") boolean reindexOnStartup) {
    this.registry = registry;
    this.vectorStore = vectorStore;
    this.reindexOnStartup = reindexOnStartup;
  }

  @Override
  public void run(ApplicationArguments args) {
    if (!reindexOnStartup) {
      return;
    }
    Mono.fromRunnable(this::reindex).subscribeOn(Schedulers.boundedElastic()).block();
  }

  private void reindex() {
    var documents = registry.findAll().stream().map(this::toDocument).toList();
    vectorStore.add(documents);
  }

  private Document toDocument(IntentDefinition intent) {
    return Document.builder()
        .id(intent.intentId())
        .text(intent.knowledgeSnippet())
        .metadata("topic", intent.intentId())
        .metadata("allowed", intent.allowed())
        .build();
  }
}
