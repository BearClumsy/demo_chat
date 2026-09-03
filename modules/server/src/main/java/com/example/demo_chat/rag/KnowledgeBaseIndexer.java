package com.example.demo_chat.rag;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * Pushes every {@link IntentDefinition} into the Qdrant {@code support_kb} collection on startup.
 * Document ids are the intent id, so re-running this on every restart just upserts the same points
 * rather than duplicating them.
 *
 * <p>Staging/prod disable startup reindexing ({@code demo-chat.rag.reindex-on-startup=false}). The
 * Kubernetes bootstrap Job (see {@code infra/k8s/manifest-*.yaml}) instead starts the server image
 * with {@code --reindex-and-exit}: this runner seeds {@code support_kb} once and then stops the
 * application with exit code 0, so the Job completes instead of the pod staying up.
 */
@Component
public class KnowledgeBaseIndexer implements ApplicationRunner {

  private final IntentDefinitionRegistry registry;
  private final VectorStore vectorStore;
  private final ConfigurableApplicationContext applicationContext;
  private final boolean reindexOnStartup;

  public KnowledgeBaseIndexer(
      IntentDefinitionRegistry registry,
      VectorStore vectorStore,
      ConfigurableApplicationContext applicationContext,
      @Value("${demo-chat.rag.reindex-on-startup:true}") boolean reindexOnStartup) {
    this.registry = registry;
    this.vectorStore = vectorStore;
    this.applicationContext = applicationContext;
    this.reindexOnStartup = reindexOnStartup;
  }

  @Override
  public void run(ApplicationArguments args) {
    var reindexAndExit = args.containsOption("reindex-and-exit");
    if (!reindexOnStartup && !reindexAndExit) {
      return;
    }
    Mono.fromRunnable(this::reindex).subscribeOn(Schedulers.boundedElastic()).block();
    if (reindexAndExit) {
      System.exit(SpringApplication.exit(applicationContext, () -> 0));
    }
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
